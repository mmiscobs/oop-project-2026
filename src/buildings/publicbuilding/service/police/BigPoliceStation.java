package buildings.publicbuilding.service.police;

import buildings.Buildable;

public class BigPoliceStation extends PoliceStation {
    static {
        Buildable.registry.put(BigPoliceStation.class, BigPoliceStation::new);
    }
    public boolean hasHelipad;

    public boolean getHasHelipad() {
        return hasHelipad;
    }

    public void buildHelipad() {
    }

    public void removeHelipad() {
    }

    public int getPrice() {
        return 0;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 0;
    }

    public int getRange() {
        return 0;
    }

    public int getCrimeReduction(int x, int y) {
        return 0;
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
