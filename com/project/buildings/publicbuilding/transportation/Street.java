package com.project.buildings.publicbuilding.transportation;

import java.util.Map;

import com.project.buildings.Buildable;
import com.project.buildings.publicbuilding.PublicBuilding;
import com.project.city.City;
import com.project.utils.SerializedBlob;

public class Street extends PublicTransportation {
    public Street() {
        super();
    }

    protected Street(SerializedBlob blob, City city) {
        super(blob, city);
        this.plantedTrees = new PlantedTrees(blob.map().get("plantedTrees"));
        this.speedLimiters = new SpeedLimiters(blob.map().get("speedLimiters"));
    }

    public SerializedBlob toBlob() {
        return super.toBlob()
                .extendMap(Map.of(
                        "plantedTrees", plantedTrees.toBlob(),
                        "speedLimiters", speedLimiters.toBlob()));
    }

    static {
        Buildable.registry.put(Street.class, Street::new);
        Buildable.blobRegistry.put(Street.class, Street::new);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { speedLimiters, plantedTrees };
    }

    private SpeedLimiters speedLimiters = new SpeedLimiters();

    class SpeedLimiters extends PublicBuilding.Upgrade {
        SpeedLimiters() {
            super();
        }

        SpeedLimiters(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 5;
        }
    }

    private PlantedTrees plantedTrees = new PlantedTrees();

    class PlantedTrees extends PublicBuilding.Upgrade {
        PlantedTrees() {
            super();
        }

        PlantedTrees(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 10;
        }
    }

    public int getPrice() {
        return 10;
    }

    public int getMaintanenceCostPerDay() {
        return 1 + (speedLimiters.getIsBuilt() ? 1 : 0) + (plantedTrees.getIsBuilt() ? 2 : 0);
    }

    public int getCapacity() {
        return speedLimiters.getIsBuilt() ? 3 : 5;
    }

    public int computeNoiseLevel() {
        return (int) (((35 - (speedLimiters.getIsBuilt() ? -15 : 0) - (plantedTrees.getIsBuilt() ? -10 : 0)))
                * (double) Math.min(getCongestion(), 100) / 100);
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

        details.put("has trees", speedLimiters.getIsBuilt() ? "yes" : "no");
        return details;
    }
}
