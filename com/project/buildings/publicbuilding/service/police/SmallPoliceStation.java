package com.project.buildings.publicbuilding.service.police;

import java.util.Map;

import com.project.buildings.Buildable;
import com.project.buildings.publicbuilding.PublicBuilding;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public class SmallPoliceStation extends PoliceStation {
    public SmallPoliceStation() {
        super();
    }

    protected SmallPoliceStation(SerializedBlob blob, City city) {
        super(blob, city);
        this.policeCarsGarage = new PoliceCarsGarage(blob.map().get("policeCarsGarage"));
    }

    public SerializedBlob toBlob(SerializedBlob.Factory Factory) {
        return super.toBlob(Factory).extendMap(Map.of("policeCarsGarage", policeCarsGarage.toBlob(Factory)));
    }

    static {
        Buildable.registry.put(SmallPoliceStation.class, SmallPoliceStation::new);
        Buildable.blobRegistry.put(SmallPoliceStation.class, SmallPoliceStation::new);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.policeCarsGarage };
    }

    private PoliceCarsGarage policeCarsGarage = new PoliceCarsGarage();

    class PoliceCarsGarage extends PublicBuilding.Upgrade {
        PoliceCarsGarage() {
        }

        PoliceCarsGarage(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 3000;
        }
    }

    public int getPrice() {
        return 10000;
    }

    public int getMaintanenceCostPerDay() {
        return 500 + (policeCarsGarage.getIsBuilt() ? 100 : 0);
    }

    public int getRange() {
        return 5 + (policeCarsGarage.getIsBuilt() ? 1 : 0);
    }

    public int getCrimeReduction(int x, int y) {
        return 4;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("has extra police cars garage", policeCarsGarage.getIsBuilt() ? "yes" : "no");
        return details;
    }
}
