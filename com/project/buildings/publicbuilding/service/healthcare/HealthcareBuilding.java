package com.project.buildings.publicbuilding.service.healthcare;

import com.project.buildings.publicbuilding.service.PublicServiceBuilding;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public abstract class HealthcareBuilding extends PublicServiceBuilding {
    public HealthcareBuilding() {
        super();
    }

    protected HealthcareBuilding(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public abstract int getHealthIncrease(int x, int y);

    @Override
    public Class<? extends PublicServiceBuilding> getPublicServiceTypeClass() {
        return HealthcareBuilding.class;
    }
}
