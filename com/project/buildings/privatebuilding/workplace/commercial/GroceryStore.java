package com.project.buildings.privatebuilding.workplace.commercial;

import com.project.buildings.Buildable;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public class GroceryStore extends CommercialBuilding {
    public GroceryStore() {
        super();
    }

    protected GroceryStore(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(GroceryStore.class, GroceryStore::new);
        Buildable.blobRegistry.put(GroceryStore.class, GroceryStore::new);
    }

    public int getPrice() {
        return 1000;
    }

    public int getWorkersCapacity() {
        return 3;
    }

    public int getVisitorsCapacity() {
        return 10;
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
