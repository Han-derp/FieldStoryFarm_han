package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.PurchaseResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicEconomyServiceTest {

    @Test
    void newPlayerStartsWithFiveHundredGold() {
        Player player = new Player();
        BasicEconomyService service = new BasicEconomyService(player);
        assertEquals(500, service.getGold());
    }

    @Test
    void buyingCornDeductsGoldAndAddsInventory() {
        Player player = new Player();
        BasicEconomyService service = new BasicEconomyService(player);

        assertEquals(PurchaseResult.SUCCESS, service.buySeed(CropType.CORN, 2));
        assertEquals(470, service.getGold());
        assertEquals(2, service.getSeedCount(CropType.CORN));
    }

    @Test
    void failedPurchaseDoesNotMutateState() {
        Player player = new Player();
        player.setGold(5);
        BasicEconomyService service = new BasicEconomyService(player);

        assertEquals(PurchaseResult.INSUFFICIENT_GOLD, service.buySeed(CropType.CARROT, 1));
        assertEquals(5, service.getGold());
        assertEquals(0, service.getSeedCount(CropType.CARROT));
        assertFalse(service.hasSeed(CropType.CARROT, 1));
        assertTrue(service.canAfford(5));
    }
}
