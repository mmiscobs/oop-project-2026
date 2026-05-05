package buildings.publicbuilding.transportation;

import java.util.Map;

import buildings.Buildable;

public class Street extends PublicTransportation {
    static {
        Buildable.registry.put(Street.class, Street::new);
    }
    public boolean hasSpeedLimiters;

    public boolean getHasSpeedLimiters() {
        return hasSpeedLimiters;
    }

    public void buildSpeedLimiters() {
        this.hasSpeedLimiters = true;
    }

    public void removeSpeedLimiters() {
        this.hasSpeedLimiters = false;
    }

    public int getPrice() {
        return 10;
    }

    public void setCrimeRate(int crimeRateReduction) {
    }

    public int getMaintanenceCostPerDay() {
        return 1 + (this.hasSpeedLimiters ? 1 : 0);
    }

    public int getCapacity() {
        return this.hasSpeedLimiters ? 3 : 5;
    }

    public int computeNoiseLevel() {
        return (int) ((this.hasSpeedLimiters ? 5 : 10) * (double) Math.min(getCongestion(), 100) / 100);
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

        details.put("has trees", hasSpeedLimiters ? "yes" : "no");
        return details;
    }
}
