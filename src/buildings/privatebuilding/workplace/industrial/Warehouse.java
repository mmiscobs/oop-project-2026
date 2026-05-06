package buildings.privatebuilding.workplace.industrial;

import buildings.Buildable;

public class Warehouse extends IndustrialBuilding {
    static {
        Buildable.registry.put(Warehouse.class, Warehouse::new);
        Buildable.blobRegistry.put(Warehouse.class, Warehouse::fromBlob);
    }

    public int getPrice() {
        return 1500;
    }

    public int getWorkersCapacity() {
        return 20;
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
