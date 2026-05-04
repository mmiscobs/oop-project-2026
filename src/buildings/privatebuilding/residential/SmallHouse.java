package buildings.privatebuilding.residential;

import buildings.Buildable;

public class SmallHouse extends ResidentialBuilding {
    static {
        Buildable.registry.put(SmallHouse.class, SmallHouse::new);
    }

    public int getPrice() {
        return 0;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public boolean getIsBuilt() {
        return false;
    }

    public int calculateProfitPerTick() {
        return 0;
    }

    public int getCapacity() {
        return 5;
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
