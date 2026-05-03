package buildings.publicbuilding.transportation;

public class Highway extends PublicTransportation {
    public boolean hasNoiseBarriers;

    public boolean getHasNoiseBarriers() { return hasNoiseBarriers; }
    public void buildNoiseBarriers() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getCapacity() { return 0; }
    public int computeNoiseLevel() { return 0; }
}
