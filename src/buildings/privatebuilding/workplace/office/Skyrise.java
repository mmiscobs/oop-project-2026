package buildings.privatebuilding.workplace.office;

import buildings.Buildable;

public class Skyrise extends OfficeBuilding {
    static {
        Buildable.registry.put(Skyrise.class, Skyrise::new);
        Buildable.blobRegistry.put(Skyrise.class, Skyrise::fromBlob);
    }

    public int getPrice() {
        return 10000;
    }

    public int getWorkersCapacity() {
        return 100;
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
