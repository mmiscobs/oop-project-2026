package buildings.publicbuilding.service.healthcare;

import java.util.Map;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;

public class Clinic extends HealthcareBuilding {
    static {
        Buildable.registry.put(Clinic.class, Clinic::new);
    }

    public int getPrice() {
        return 11000;
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.ambulanceGarage };
    }

    private AmbulanceGarage ambulanceGarage = new AmbulanceGarage();

    class AmbulanceGarage extends PublicBuilding.Upgrade {
        public int getPrice() {
            return 3000;
        }
    }

    public void setCrimeRate(int crimeRateReduction) {
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
