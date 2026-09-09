package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.SoilState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoilStateTest {

    @Test
    void emptySoilCostsFiveGoldAndBecomesTilled() {
        Player player = new Player();
        BasicEconomyService economy = new BasicEconomyService(player);
        BasicLandService land = new BasicLandService(economy);
        Soil soil = new Soil(1, 0, 0);

        assertTrue(land.reclaim(soil));
        assertEquals(SoilState.TILLED, soil.getState());
        assertEquals(495, player.getGold());
    }

    @Test
    void insufficientGoldLeavesSoilUnchanged() {
        Player player = new Player();
        player.setGold(4);
        BasicEconomyService economy = new BasicEconomyService(player);
        BasicLandService land = new BasicLandService(economy);
        Soil soil = new Soil(1, 0, 0);

        assertFalse(land.reclaim(soil));
        assertEquals(SoilState.EMPTY, soil.getState());
        assertEquals(4, player.getGold());
    }
}
