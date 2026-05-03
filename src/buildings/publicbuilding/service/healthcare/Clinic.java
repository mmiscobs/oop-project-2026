package buildings.publicbuilding.service.healthcare;

public class Clinic extends HealthcareBuilding {
    public boolean hasAmbulanceGarage;

    public boolean getHasAmbulanceGarage() { return hasAmbulanceGarage; }
    public void buildAmbulanceGarage() {}
    public void removeAmbulanceGarage() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getRange() { return 0; }
    public int getHealthIncrease(int x, int y) { return 0; }
}
