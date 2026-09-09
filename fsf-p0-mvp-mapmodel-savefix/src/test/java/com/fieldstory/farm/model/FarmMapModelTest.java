package com.fieldstory.farm.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FarmMapModelTest {

    @Test
    void p0FarmShouldBeFull12x12MapWith64SoilsOnlyInCenter() {
        Farm farm = Farm.createP0Farm();

        assertEquals(144, farm.getTiles().size());
        assertEquals(64, farm.countTiles(FarmPlot.FARM_PLOT));
        assertEquals(80, farm.countTiles(FarmPlot.DECORATION_AREA));
        assertEquals(64, farm.allSoils().size());

        for (MapTile tile : farm.getTiles()) {
            if (Farm.isFarmMapCoordinate(tile.getRow(), tile.getColumn())) {
                assertEquals(FarmPlot.FARM_PLOT, tile.getPlotType());
                assertNotNull(tile.getSoil());
            } else {
                assertNotEquals(FarmPlot.FARM_PLOT, tile.getPlotType());
                assertNull(tile.getSoil(), "外围功能格不得持有Soil");
            }
        }
    }

    @Test
    void localFarmCoordinateShouldMapToCenterMapTile() {
        Farm farm = Farm.createP0Farm();

        Soil first = farm.getSoil(0, 0);
        MapTile firstTile = farm.getTile(2, 2);
        assertSame(first, firstTile.getSoil());
        assertEquals(0, first.getRow());
        assertEquals(0, first.getColumn());

        Soil last = farm.getSoil(7, 7);
        MapTile lastTile = farm.getTile(9, 9);
        assertSame(last, lastTile.getSoil());
        assertEquals(7, last.getRow());
        assertEquals(7, last.getColumn());
    }

    @Test
    void outerTileShouldNeverExposeSoilInP0() {
        Farm farm = Farm.createP0Farm();
        assertNull(farm.getTile(0, 0).getSoil());
        assertNull(farm.getTile(11, 11).getSoil());
    }
}
