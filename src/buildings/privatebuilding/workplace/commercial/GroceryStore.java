package buildings.privatebuilding.workplace.commercial;

import buildings.Buildable;

public class GroceryStore extends CommercialBuilding {
    static {
        Buildable.registry.put(GroceryStore.class, GroceryStore::new);
    }

    public int getPrice() {
        return 1000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public boolean getIsBuilt() {
        return false;
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
