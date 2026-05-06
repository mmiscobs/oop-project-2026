package buildings.publicbuilding.service.police;

import java.util.Map;

import buildings.Buildable;

public class BigPoliceStation extends PoliceStation {
    static {
        Buildable.registry.put(BigPoliceStation.class, BigPoliceStation::new);
    }
    public boolean hasHelipad;

    public boolean getHasHelipad() {
        return hasHelipad;
    }

    public void buildHelipad() {
    }

    public void removeHelipad() {
    }

    public int getPrice() {
        return 25000;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 1000;
    }

    public int getRange() {
        return 8 + (hasHelipad ? 2 : 0);
    }

    public int getCrimeReduction(int x, int y) {
        return 3;
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

        details.put("has helipad", hasHelipad ? "yes" : "no");
        return details;
    }
}
