package com.fieldstory.farm.service;

import com.fieldstory.farm.model.CropType;
import com.fieldstory.farm.model.Soil;

public interface PlantingService {
    boolean plant(Soil soil, CropType type);
}
