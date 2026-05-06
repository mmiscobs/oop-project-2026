package buildings.privatebuilding.workplace.office;

import buildings.Buildable;

import city.City;
import utils.SerializedBlob;

public class Skyrise extends OfficeBuilding {
    public Skyrise() {
        super();
    }

    protected Skyrise(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(Skyrise.class, Skyrise::new);
        Buildable.blobRegistry.put(Skyrise.class, Skyrise::new);
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
