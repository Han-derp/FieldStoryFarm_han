package com.fieldstory.farm.integration;

import com.fieldstory.farm.manager.GameManager;
import com.fieldstory.farm.model.*;
import com.fieldstory.farm.service.SaveService;
import com.fieldstory.farm.service.impl.JsonSaveService;
import com.fieldstory.farm.service.impl.TestGameClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P0官方核心经营闭环集成测试。
 *
 * 与单元测试不同，本测试同时使用真实的：
 * GameManager + Economy/Land/Planting/Watering/Growth/Harvest Service
 * + Model + JsonSaveService，仅用TestGameClock替代真实时间。
 */
class P0AcceptanceIntegrationTest {

    @TempDir
    Path tempDir;

    private GameManager newTestGame(Path savePath) {
        SaveService saveService = new JsonSaveService(savePath);
        return new GameManager(saveService, TestGameClock::new);
    }

    @Test
    void fullP0AcceptanceFlowShouldPersistAndRestore() {
        Path savePath = tempDir.resolve("p0-acceptance.json");
        GameManager game = newTestGame(savePath);

        // ① 新建游戏；② 验证金币=500。
        game.newGame();
        assertEquals(500, game.getPlayer().getGold());
        assertEquals(GameManager.P0_START_WORLD_TIME, game.getClock().now());
        assertEquals(144, game.getFarm().getTiles().size());
        assertEquals(64, game.getFarm().countTiles(FarmPlot.FARM_PLOT));
        assertEquals(64, game.getFarm().allSoils().size());

        Soil wheatSoil = game.getFarm().getSoil(0, 0);
        Soil cornSoil = game.getFarm().getSoil(0, 1);
        Soil carrotSoil = game.getFarm().getSoil(0, 2);

        // ③ 开垦3块地：3 * 5金币。
        assertTrue(game.getLandService().reclaim(wheatSoil));
        assertTrue(game.getLandService().reclaim(cornSoil));
        assertTrue(game.getLandService().reclaim(carrotSoil));
        assertEquals(485, game.getPlayer().getGold());
        assertEquals(SoilState.TILLED, wheatSoil.getState());
        assertEquals(SoilState.TILLED, cornSoil.getState());
        assertEquals(SoilState.TILLED, carrotSoil.getState());

        // ④ 分别购买小麦、玉米、胡萝卜各1颗。
        assertEquals(PurchaseResult.SUCCESS,
                game.getEconomyService().buySeed(CropType.WHEAT, 1));
        assertEquals(PurchaseResult.SUCCESS,
                game.getEconomyService().buySeed(CropType.CORN, 1));
        assertEquals(PurchaseResult.SUCCESS,
                game.getEconomyService().buySeed(CropType.CARROT, 1));
        assertEquals(440, game.getPlayer().getGold());
        assertEquals(1, game.getEconomyService().getSeedCount(CropType.WHEAT));
        assertEquals(1, game.getEconomyService().getSeedCount(CropType.CORN));
        assertEquals(1, game.getEconomyService().getSeedCount(CropType.CARROT));

        // ⑤ 三块地分别播种；播种只消耗库存，不再次扣金币。
        assertTrue(game.getPlantingService().plant(wheatSoil, CropType.WHEAT));
        assertTrue(game.getPlantingService().plant(cornSoil, CropType.CORN));
        assertTrue(game.getPlantingService().plant(carrotSoil, CropType.CARROT));
        assertEquals(440, game.getPlayer().getGold());
        assertEquals(0, game.getEconomyService().getSeedCount(CropType.WHEAT));
        assertEquals(0, game.getEconomyService().getSeedCount(CropType.CORN));
        assertEquals(0, game.getEconomyService().getSeedCount(CropType.CARROT));

        // ⑥ 等待三种作物都进入SPROUT。
        // 胡萝卜进入20%需要19.2游戏小时，因此推进20小时即可统一验证。
        game.advanceWorld(Duration.ofHours(20));
        assertEquals(GrowthStage.SPROUT, wheatSoil.getCrop().getGrowthStage());
        assertEquals(GrowthStage.SPROUT, cornSoil.getCrop().getGrowthStage());
        assertEquals(GrowthStage.SPROUT, carrotSoil.getCrop().getGrowthStage());

        // ⑦ 主动浇水。
        assertEquals(WaterResult.SUCCESS, game.getWateringService().water(wheatSoil));
        assertEquals(WaterResult.SUCCESS, game.getWateringService().water(cornSoil));
        assertEquals(WaterResult.SUCCESS, game.getWateringService().water(carrotSoil));
        assertEquals(1, wheatSoil.getCrop().getManualWaterCount());
        assertEquals(1, cornSoil.getCrop().getManualWaterCount());
        assertEquals(1, carrotSoil.getCrop().getManualWaterCount());

        // ⑧ 观察成长速度变化：浇水后OperationRate=1.05。
        double carrotBefore = carrotSoil.getCrop().getGrowthProgress();
        game.advanceWorld(Duration.ofHours(24));
        double carrotAfter = carrotSoil.getCrop().getGrowthProgress();
        assertEquals(26.25, carrotAfter - carrotBefore, 0.0001,
                "胡萝卜浇水后24游戏小时应增长26.25%（25% * 1.05）");

        // ⑨ 三种作物全部成熟。
        // 前面共推进44小时，再推进49小时，累计93小时；一轮浇水足以让胡萝卜成熟。
        game.advanceWorld(Duration.ofHours(49));
        assertMature(wheatSoil);
        assertMature(cornSoil);
        assertMature(carrotSoil);

        // ⑩ 全部收获；⑪ 金币按基础售价正确增加。
        assertEquals(50, game.getHarvestService().harvest(wheatSoil));
        assertEquals(70, game.getHarvestService().harvest(cornSoil));
        assertEquals(60, game.getHarvestService().harvest(carrotSoil));
        assertEquals(620, game.getPlayer().getGold());
        assertHarvestedToTilled(wheatSoil);
        assertHarvestedToTilled(cornSoil);
        assertHarvestedToTilled(carrotSoil);

        // ⑫ 再购买至少1颗种子；⑬ 在收获后的TILLED土地重新播种。
        assertEquals(PurchaseResult.SUCCESS,
                game.getEconomyService().buySeed(CropType.WHEAT, 1));
        assertEquals(610, game.getPlayer().getGold());
        assertTrue(game.getPlantingService().plant(wheatSoil, CropType.WHEAT));
        assertEquals(0, game.getEconomyService().getSeedCount(CropType.WHEAT));
        assertEquals(SoilState.PLANTED, wheatSoil.getState());
        assertNotNull(wheatSoil.getCrop());
        assertEquals(CropType.WHEAT, wheatSoil.getCrop().getCropType());

        // ⑭ 退出游戏：保存当前快照。
        LocalDateTime savedWorldTime = game.getClock().now();
        double savedProgress = wheatSoil.getCrop().getGrowthProgress();
        game.save();
        assertTrue(java.nio.file.Files.isRegularFile(savePath));

        // ⑮ 重新启动：创建全新的GameManager模拟进程重启。
        GameManager restarted = newTestGame(savePath);
        restarted.start();

        // ⑯ 金币、种子、土地、作物、世界时间全部恢复。
        assertEquals(610, restarted.getPlayer().getGold());
        assertEquals(0, restarted.getEconomyService().getSeedCount(CropType.WHEAT));
        assertEquals(0, restarted.getEconomyService().getSeedCount(CropType.CORN));
        assertEquals(0, restarted.getEconomyService().getSeedCount(CropType.CARROT));
        assertEquals(savedWorldTime, restarted.getClock().now());
        assertEquals(144, restarted.getFarm().getTiles().size());
        assertEquals(64, restarted.getFarm().countTiles(FarmPlot.FARM_PLOT));
        assertNull(restarted.getFarm().getTile(0, 0).getSoil());

        Soil restoredWheat = restarted.getFarm().getSoil(0, 0);
        Soil restoredCorn = restarted.getFarm().getSoil(0, 1);
        Soil restoredCarrot = restarted.getFarm().getSoil(0, 2);

        assertEquals(SoilState.PLANTED, restoredWheat.getState());
        assertNotNull(restoredWheat.getCrop());
        assertEquals(CropType.WHEAT, restoredWheat.getCrop().getCropType());
        assertEquals(savedProgress, restoredWheat.getCrop().getGrowthProgress(), 0.0001);

        assertHarvestedToTilled(restoredCorn);
        assertHarvestedToTilled(restoredCarrot);

        // P0无离线推进：load本身不能改变保存时的世界时间/成长进度。
        assertEquals(savedWorldTime, restarted.getClock().now());
        assertEquals(savedProgress, restoredWheat.getCrop().getGrowthProgress(), 0.0001);
    }

    @Test
    void failedOperationsShouldNotCorruptIntegratedState() {
        GameManager game = newTestGame(tempDir.resolve("p0-failure.json"));
        game.newGame();

        Soil soil = game.getFarm().getSoil(0, 0);

        // EMPTY不能直接播种，而且失败不能消耗金币/种子。
        assertEquals(PurchaseResult.SUCCESS,
                game.getEconomyService().buySeed(CropType.CORN, 1));
        int goldAfterPurchase = game.getPlayer().getGold();
        assertFalse(game.getPlantingService().plant(soil, CropType.CORN));
        assertEquals(goldAfterPurchase, game.getPlayer().getGold());
        assertEquals(1, game.getEconomyService().getSeedCount(CropType.CORN));
        assertEquals(SoilState.EMPTY, soil.getState());

        // 开垦一次成功，重复开垦失败且不能再次扣钱。
        assertTrue(game.getLandService().reclaim(soil));
        int goldAfterReclaim = game.getPlayer().getGold();
        assertFalse(game.getLandService().reclaim(soil));
        assertEquals(goldAfterReclaim, game.getPlayer().getGold());

        assertTrue(game.getPlantingService().plant(soil, CropType.CORN));

        // SEED阶段不可浇水，状态不得变化。
        assertEquals(WaterResult.STAGE_NOT_ALLOWED, game.getWateringService().water(soil));
        assertEquals(0, soil.getCrop().getManualWaterCount());

        // 未成熟不能收获，不能增加金币，也不能移除Crop。
        int goldBeforeEarlyHarvest = game.getPlayer().getGold();
        Crop sameCrop = soil.getCrop();
        assertEquals(0, game.getHarvestService().harvest(soil));
        assertEquals(goldBeforeEarlyHarvest, game.getPlayer().getGold());
        assertSame(sameCrop, soil.getCrop());
        assertEquals(SoilState.PLANTED, soil.getState());

        // 进入SPROUT后首次浇水成功，同游戏日第二次必须失败。
        game.advanceWorld(Duration.ofHours(15));
        assertEquals(GrowthStage.SPROUT, soil.getCrop().getGrowthStage());
        assertEquals(WaterResult.SUCCESS, game.getWateringService().water(soil));
        assertEquals(WaterResult.ALREADY_WATERED_TODAY, game.getWateringService().water(soil));
        assertEquals(1, soil.getCrop().getManualWaterCount());
    }

    private static void assertMature(Soil soil) {
        assertNotNull(soil.getCrop());
        assertEquals(GrowthStage.MATURE, soil.getCrop().getGrowthStage());
        assertEquals(100.0, soil.getCrop().getGrowthProgress(), 0.0001);
    }

    private static void assertHarvestedToTilled(Soil soil) {
        assertEquals(SoilState.TILLED, soil.getState());
        assertNull(soil.getCrop());
    }
}
