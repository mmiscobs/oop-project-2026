package com.project.buildings.publicbuilding.service.police;

import com.project.buildings.publicbuilding.service.PublicServiceBuilding;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public abstract class PoliceStation extends PublicServiceBuilding {
    public PoliceStation() {
        super();
    }

    protected PoliceStation(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public abstract int getCrimeReduction(int x, int y);

    @Override
    public Class<? extends PublicServiceBuilding> getPublicServiceTypeClass() {
        return PoliceStation.class;
    }
}
