package buildings.publicbuilding.transportation;

public class Street extends PublicTransportation {
    public boolean hasSpeedLimiters;

    public boolean getHasSpeedLimiters() { return hasSpeedLimiters; }
    public void buildSpeedLimiters() {}
    public void removeSpeedLimiters() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getCapacity() { return 0; }
    public int computeNoiseLevel() { return 0; }
}
