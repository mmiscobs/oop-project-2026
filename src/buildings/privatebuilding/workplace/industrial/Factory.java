package buildings.privatebuilding.workplace.industrial;

import buildings.Buildable;

public class Factory extends IndustrialBuilding {
    static {
        Buildable.registry.put(Factory.class, Factory::new);
        Buildable.blobRegistry.put(Factory.class, Factory::fromBlob);
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
