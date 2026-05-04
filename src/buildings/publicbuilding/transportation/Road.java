package buildings.publicbuilding.transportation;

public class Road extends PublicTransportation {
    public boolean hasPlantedTrees;

    public boolean getHasPlantedTrees() {
        return hasPlantedTrees;
    }

    public void buildPlantedTrees() {
        hasPlantedTrees = true;
    }

    public void removePlantedTrees() {
        hasPlantedTrees = false;
    }

    public int getPrice() {
        return 100;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 10 + (hasPlantedTrees ? 2 : 0);
    }

    public int getCapacity() {
        return 20;
    }

    public int computeNoiseLevel() {
        return 30;
    }

    public int getWidth() {
        return 1;
    }

    public int getLength() {
        return 1;
    }
}
