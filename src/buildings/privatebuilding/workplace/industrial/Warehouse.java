package buildings.privatebuilding.workplace.industrial;

import buildings.Buildable;

import city.City;
import utils.SerializedBlob;

public class Warehouse extends IndustrialBuilding {
    public Warehouse() {
        super();
    }

    protected Warehouse(SerializedBlob blob, City city) {
        super(blob, city);
    }

    static {
        Buildable.registry.put(Warehouse.class, Warehouse::new);
        Buildable.blobRegistry.put(Warehouse.class, Warehouse::new);
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
