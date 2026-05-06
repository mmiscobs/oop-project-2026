package buildings.publicbuilding.transportation;

import java.util.Map;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;

public class Road extends PublicTransportation {
    static {
        Buildable.registry.put(Road.class, Road::new);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.plantedTrees };
    }

    private PlantedTrees plantedTrees = new PlantedTrees();

    class PlantedTrees extends PublicBuilding.Upgrade {
        public int getPrice() {
            return 3000;
        }
    }

    public int getPrice() {
        return 100;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 10 + (plantedTrees.getIsBuilt() ? 2 : 0);
    }

    public int getCapacity() {
        return 20;
    }

    public int computeNoiseLevel() {
        return (int) (60 * (double) Math.min(getCongestion(), 100) / 100);
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

        details.put("has trees", plantedTrees.getIsBuilt() ? "yes" : "no");
        return details;
    }
}
