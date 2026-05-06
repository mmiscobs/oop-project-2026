package buildings.privatebuilding.workplace.commercial;

import buildings.Buildable;

public class GroceryStore extends CommercialBuilding {
    static {
        Buildable.registry.put(GroceryStore.class, GroceryStore::new);
        Buildable.blobRegistry.put(GroceryStore.class, GroceryStore::fromBlob);
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
