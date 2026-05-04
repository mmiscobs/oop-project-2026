package buildings.privatebuilding.workplace.office;

import buildings.Buildable;

public class Skyrise extends OfficeBuilding {

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

    public int getWorkersCapacity() {
        return 0;
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
