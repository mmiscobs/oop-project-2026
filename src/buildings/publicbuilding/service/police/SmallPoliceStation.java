package buildings.publicbuilding.service.police;

import java.util.Map;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;

public class SmallPoliceStation extends PoliceStation {
    static {
        Buildable.registry.put(SmallPoliceStation.class, SmallPoliceStation::new);
    }

    public Upgrade[] getUpgrades() {
        return new Upgrade[] { this.policeCarsGarage };
    }

    private PoliceCarsGarage policeCarsGarage = new PoliceCarsGarage();

    class PoliceCarsGarage extends PublicBuilding.Upgrade {
        public int getPrice() {
            return 3000;
        }
    }

    public int getPrice() {
        return 10000;
    }

    public void setCrimeRate(int crimeRateReduction) {
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
