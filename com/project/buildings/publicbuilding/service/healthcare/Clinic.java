package com.project.buildings.publicbuilding.service.healthcare;

import java.util.Map;

import com.project.buildings.Buildable;
import com.project.buildings.publicbuilding.PublicBuilding;

import com.project.city.City;
import com.project.utils.SerializedBlob;

public class Clinic extends HealthcareBuilding {
    public Clinic() {
        super();
    }

    protected Clinic(SerializedBlob blob, City city) {
        super(blob, city);
        this.ambulanceGarage = new AmbulanceGarage(blob);
    }

    public SerializedBlob toBlob(SerializedBlob.Factory Factory) {
        return super.toBlob(Factory).extendMap(Map.of("ambulanceGarage", ambulanceGarage.toBlob(Factory)));
    }

    static {
        Buildable.registry.put(Clinic.class, Clinic::new);
        Buildable.blobRegistry.put(Clinic.class, Clinic::new);
    }

    public int getPrice() {
        return 11000;
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.ambulanceGarage };
    }

    private AmbulanceGarage ambulanceGarage = new AmbulanceGarage();

    class AmbulanceGarage extends PublicBuilding.Upgrade {
        AmbulanceGarage() {
        }

        AmbulanceGarage(SerializedBlob blob) {
            super(blob);
        }

        public int getPrice() {
            return 3000;
        }
    }

    public int getMaintanenceCostPerDay() {
        return 700;
    }

    public int getRange() {
        return 6;
    }

    public int getHealthIncrease(int x, int y) {
        return 0;
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

        details.put("has ambulances garage", ambulanceGarage.getIsBuilt() ? "yes" : "no");
        return details;
    }
}
