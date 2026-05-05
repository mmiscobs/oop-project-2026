package buildings.privatebuilding.residential;

import buildings.Buildable;

public class Condominium extends ResidentialBuilding {
    static {
        Buildable.registry.put(Condominium.class, Condominium::new);
    }

    public int getPrice() {
        return 1000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public boolean getIsBuilt() {
        return false;
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
