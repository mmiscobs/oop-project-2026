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
        return 1000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 50;
    }

    public int getRange() {
        return 0;
    }

    public int getCrimeReduction(int x, int y) {
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

        details.put("has extra police cars garage", hasExtraPoliceCarsGarage ? "yes" : "no");
        return details;
    }
}
