package buildings.publicbuilding.transportation;

public class Street extends PublicTransportation {
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
        return this.hasSpeedLimiters ? 5 : 10;
    }

    public int getWidth() {
        return 1;
    }

    public int getLength() {
        return 1;
    }
}
