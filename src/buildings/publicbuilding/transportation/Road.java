package buildings.publicbuilding.transportation;

import java.util.Map;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;
import city.City;
import utils.SerializedBlob;

public class Road extends PublicTransportation {
    public Road() {
        super();
    }

    protected Road(SerializedBlob blob, City city) {
        super(blob, city);
        this.plantedTrees = new PlantedTrees(blob.map().get("plantedTrees"));
    }

    static {
        Buildable.registry.put(Road.class, Road::new);
        Buildable.blobRegistry.put(Road.class, Road::fromBlob);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.plantedTrees };
    }

    private PlantedTrees plantedTrees = new PlantedTrees();

    class PlantedTrees extends PublicBuilding.Upgrade {
        PlantedTrees() {
        }

        PlantedTrees(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 3000;
        }
    }

    public int getPrice() {
        return 100;
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
