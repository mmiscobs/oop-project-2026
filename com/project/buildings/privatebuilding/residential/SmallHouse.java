package com.project.buildings.privatebuilding.residential;

import com.project.buildings.Buildable;
import com.project.city.City;
import com.project.utils.SerializedBlob;

public class SmallHouse extends ResidentialBuilding {
    public SmallHouse() {
        super();
    }

    public SmallHouse(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(SmallHouse.class, SmallHouse::new);
        Buildable.blobRegistry.put(SmallHouse.class, SmallHouse::new);
    }

    public int getPrice() {
        return 1000;
    }

    public int getCapacity() {
        return 5;
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
