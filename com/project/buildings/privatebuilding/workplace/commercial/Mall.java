package com.project.buildings.privatebuilding.workplace.commercial;

import com.project.buildings.Buildable;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public class Mall extends CommercialBuilding {
    public Mall() {
        super();
    }

    protected Mall(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(Mall.class, Mall::new);
        Buildable.blobRegistry.put(Mall.class, Mall::new);
    }

    public int getPrice() {
        return 15000;
    }

    public int getWorkersCapacity() {
        return 30;
    }

    public int getVisitorsCapacity() {
        return 50;
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getLength() {
        return 2;
    }
}
