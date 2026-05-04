package buildings.privatebuilding.residential;

public class Condominium extends ResidentialBuilding {
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
