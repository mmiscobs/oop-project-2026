package buildings.publicbuilding.service.healthcare;

public class Hospital extends HealthcareBuilding {
    public boolean hasHelipad;

    public boolean getHasHelipad() { return hasHelipad; }
    public void buildHelipad() {}
    public void removeHelipad() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getRange() { return 0; }
    public int getHealthIncrease(int x, int y) { return 0; }
}
