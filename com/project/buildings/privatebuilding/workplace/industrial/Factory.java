package com.project.buildings.privatebuilding.workplace.industrial;

import com.project.buildings.Buildable;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public class Factory extends IndustrialBuilding {
    public Factory() {
        super();
    }

    protected Factory(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(Factory.class, Factory::new);
        Buildable.blobRegistry.put(Factory.class, Factory::new);
    }

    public int getPrice() {
        return 1000;
    }

    public int getWorkersCapacity() {
        return 15;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getLength() {
        return 1;
    }
}
