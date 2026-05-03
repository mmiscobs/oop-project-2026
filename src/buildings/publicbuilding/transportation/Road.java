package buildings.publicbuilding.transportation;

public class Road extends PublicTransportation {
    public boolean hasPlantedTrees;

    public boolean getHasPlantedTrees() { return hasPlantedTrees; }
    public void buildPlantedTrees() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getCapacity() { return 0; }
    public int computeNoiseLevel() { return 0; }
}
