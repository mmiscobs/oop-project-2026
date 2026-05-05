package buildings.publicbuilding.service.healthcare;

import buildings.Buildable;

public class Hospital extends HealthcareBuilding {
    static {
        Buildable.registry.put(Hospital.class, Hospital::new);
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
        return 1000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 0;
    }

    public int getRange() {
        return 0;
    }

    public int getHealthIncrease(int x, int y) {
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
