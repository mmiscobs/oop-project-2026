package buildings.publicbuilding.transportation;

import java.util.Map;

import buildings.Buildable;

public class Road extends PublicTransportation {
    static {
        Buildable.registry.put(Road.class, Road::new);
    }
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
        return (int) (30 * (double) Math.min(getCongestion(), 100) / 100);
    }

    public int getWidth() {
        return 1;
    }

    public int getLength() {
        return 1;
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("has trees", hasPlantedTrees ? "yes" : "no");
        return details;
    }
}
