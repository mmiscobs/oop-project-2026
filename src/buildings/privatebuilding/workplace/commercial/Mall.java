package buildings.privatebuilding.workplace.commercial;

import buildings.Buildable;

public class Mall extends CommercialBuilding {
    static {
        Buildable.registry.put(Mall.class, Mall::new);
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
