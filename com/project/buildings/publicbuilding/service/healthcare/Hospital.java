package com.project.buildings.publicbuilding.service.healthcare;

import java.util.Map;

import com.project.buildings.Buildable;
import com.project.buildings.publicbuilding.PublicBuilding;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public class Hospital extends HealthcareBuilding {
    public Hospital() {
        super();
    }

    protected Hospital(SerializedBlob blob, City city) {
        super(blob, city);
        this.helipad = new Helipad(blob.map().get("helipad"));
    }

    public SerializedBlob toBlob() {
        return super.toBlob().extendMap(Map.of("helipad", helipad.toBlob()));
    }

    static {
        Buildable.registry.put(Hospital.class, Hospital::new);
        Buildable.blobRegistry.put(Hospital.class, Hospital::new);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.helipad };
    }

    private Helipad helipad = new Helipad();

    class Helipad extends PublicBuilding.Upgrade {
        Helipad() {
        }

        Helipad(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 3000;
        }
    }

    public int getPrice() {
        return 15000;
    }

    public int getMaintanenceCostPerDay() {
        return 2000;
    }

    public int getRange() {
        return 8 + (helipad.getIsBuilt() ? 3 : 0);
    }

    public int getHealthIncrease(int x, int y) {
        return 0;
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("has helipad", helipad.getIsBuilt() ? "yes" : "no");
        return details;
    }
}
