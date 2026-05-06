package com.project.buildings.privatebuilding.residential;

import com.project.buildings.Buildable;
import com.project.city.City;
import com.project.utils.SerializedBlob;

public class Condominium extends ResidentialBuilding {
    static {
        Buildable.registry.put(Condominium.class, Condominium::new);
        Buildable.blobRegistry.put(Condominium.class, Condominium::new);
    }

    public Condominium() {
        super();
    }

    public Condominium(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public int getPrice() {
        return 20000;
    }

    public int getCapacity() {
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
