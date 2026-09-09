package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Soil;
import com.fieldstory.farm.model.WaterResult;

public interface WateringService {
    WaterResult water(Soil soil);
}
