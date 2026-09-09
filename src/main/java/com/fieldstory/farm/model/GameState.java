package com.fieldstory.farm.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 游戏状态聚合根（E 存档模块持有）。
 *
 * <p>P0 存档（验收规范 §40-§41）以 {@link GameState} 为保存/加载单元，必须覆盖：
 * Player.gold、seedInventory（在 {@link Player} 内）、GameClock.currentWorldTime、
 * 所有 FarmPlot 状态（经 {@link Farm#getSoils()}）以及每株 Crop（挂在 Soil 上）。
 *
 * <p>本类只保存“现在是什么状态”，不含任何游戏计算（统一 Model 原则，验收规范 §四）。
 */
public class GameState {

    /** 玩家：金币与种子库存（B 模块 Player 模型） */
    private Player player;

    /** 农场：拥有全部 Soil（中心 8×8），属于 A 模块 Farm 模型 */
    private Farm farm;

    /** 退出/存档时刻的世界时间，对应 GameClock.currentWorldTime（D 模块时间） */
    private LocalDateTime currentWorldTime;

    public GameState() {
        this(null, null, null);
    }

    public GameState(Player player, Farm farm, LocalDateTime currentWorldTime) {
        this.player = player;
        this.farm = farm;
        this.currentWorldTime = currentWorldTime;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    /** 当前世界时间；新建存档尚未推进时间时可为 null（完整集成后由 D 模块 GameClock 提供）。 */
    public LocalDateTime getCurrentWorldTime() {
        return currentWorldTime;
    }

    public void setCurrentWorldTime(LocalDateTime currentWorldTime) {
        this.currentWorldTime = currentWorldTime;
    }

    /** 玩家维度是否等价（用于存档往返校验；土地/作物在测试中按格逐一断言）。 */
    public boolean samePlayerState(GameState other) {
        if (other == null || other.player == null) {
            return player == null;
        }
        if (player == null) {
            return false;
        }
        return Objects.equals(player.getName(), other.player.getName())
                && player.getGold() == other.player.getGold()
                && Objects.equals(player.getSeedInventory(), other.player.getSeedInventory());
    }

    /** 时间维度是否等价。 */
    public boolean sameWorldTime(GameState other) {
        return other != null && Objects.equals(currentWorldTime, other.currentWorldTime);
    }
}
