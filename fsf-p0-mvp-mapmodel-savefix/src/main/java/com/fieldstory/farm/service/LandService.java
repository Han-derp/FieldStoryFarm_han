package com.fieldstory.farm.service;

import com.fieldstory.farm.model.Soil;

public interface LandService {
    boolean reclaim(Soil soil);
    void removeCropAndSetTilled(Soil soil);
}
