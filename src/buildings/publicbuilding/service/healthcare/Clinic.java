package buildings.publicbuilding.service.healthcare;

import buildings.Buildable;

public class Clinic extends HealthcareBuilding {
    static {
        Buildable.registry.put(Clinic.class, Clinic::new);
    }
    public boolean hasAmbulanceGarage;

    public boolean getHasAmbulanceGarage() {
        return hasAmbulanceGarage;
    }

    public void buildAmbulanceGarage() {
    }

    public void removeAmbulanceGarage() {
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

    public int getHealthIncrease(int x, int y) {
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
