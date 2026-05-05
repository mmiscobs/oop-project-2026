package buildings.publicbuilding.service.police;

import java.util.Map;

import buildings.Buildable;

public class SmallPoliceStation extends PoliceStation {
    static {
        Buildable.registry.put(SmallPoliceStation.class, SmallPoliceStation::new);
    }
    public boolean hasExtraPoliceCarsGarage;

    public boolean getHasExtraPoliceCarsGarage() {
        return hasExtraPoliceCarsGarage;
    }

    public void buildExtraPoliceCarsGarage() {
    }

    public void removeExtraPoliceCarsGarage() {
    }

    public int getPrice() {
        return 10000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 500 + (hasExtraPoliceCarsGarage ? 100 : 0);
    }

    public int getRange() {
        return 5 + (hasExtraPoliceCarsGarage ? 1 : 0);
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

        details.put("has extra police cars garage", hasExtraPoliceCarsGarage ? "yes" : "no");
        return details;
    }
}
