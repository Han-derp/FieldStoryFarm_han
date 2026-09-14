package com.fieldstory.farm.service.impl;

import com.fieldstory.farm.model.Decoration;
import com.fieldstory.farm.model.DecorationType;
import com.fieldstory.farm.model.GameState;
import com.fieldstory.farm.model.Player;
import com.fieldstory.farm.model.economy.DecorationPlacementResult;
import com.fieldstory.farm.model.impl.BasicFarm;
import com.fieldstory.farm.service.DecorationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecorationServiceTest {

    @Test
    void purchaseStateSurvivesServiceReconstruction() {
        GameState state = new GameState(new Player("T", 500), 1);
        DecorationService first = new BasicDecorationService(new BasicFarm(), state);
        first.addPurchasedDecoration(DecorationType.SUNFLOWER, 1);

        DecorationService restored = new BasicDecorationService(new BasicFarm(), state);
        assertEquals(1, restored.getOwnedCount(DecorationType.SUNFLOWER));
        assertFalse(restored.getOwnedDecorations().get(0).isPlaced());
    }

    @Test
    void canOnlyPlaceOnDecorationArea() {
        GameState state = new GameState(new Player("T", 500), 1);
        DecorationService service = new BasicDecorationService(new BasicFarm(), state);
        Decoration decoration = service.addPurchasedDecoration(DecorationType.SUNFLOWER, 1).get(0);

        assertEquals(DecorationPlacementResult.NOT_DECORATION_AREA,
                service.place(decoration, 2, 2));
        assertEquals(DecorationPlacementResult.SUCCESS,
                service.place(decoration, 1, 1));
        assertTrue(decoration.isPlaced());
    }

    @Test
    void failedMoveKeepsOriginalPosition() {
        GameState state = new GameState(new Player("T", 500), 1);
        DecorationService service = new BasicDecorationService(new BasicFarm(), state);
        Decoration decoration = service.addPurchasedDecoration(DecorationType.SUNFLOWER, 1).get(0);
        assertEquals(DecorationPlacementResult.SUCCESS, service.place(decoration, 1, 1));

        assertEquals(DecorationPlacementResult.NOT_DECORATION_AREA,
                service.move(decoration, 2, 2));
        assertEquals(1, decoration.getRow());
        assertEquals(1, decoration.getColumn());
    }
}
