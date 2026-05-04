package buildings;

import city.City;

public abstract class Buildable {
    protected City city;

    private int crimeRate;

    abstract public int getWidth();

    abstract public int getLength();

    public abstract int getPrice();

    public int getCrimeRate() {
        return crimeRate;
    }

    public abstract void setCrimeRate(int crimeRateReduction);
}
