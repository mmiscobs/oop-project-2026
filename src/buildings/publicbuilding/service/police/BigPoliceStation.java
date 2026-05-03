package buildings.publicbuilding.service.police;

public class BigPoliceStation extends PoliceStation {
    public boolean hasHelipad;

    public boolean getHasHelipad() { return hasHelipad; }
    public void buildHelipad() {}
    public void removeHelipad() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getRange() { return 0; }
    public int getCrimeReduction(int x, int y) { return 0; }
}
