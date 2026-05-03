package buildings;

import city.City;

public abstract class Buildable {
    protected City city;
    protected int x;
    protected int y;

    private int crimeRate;

    public int getX() { return x; }
    public int getY() { return y; }
    public abstract int getPrice();

    public int getCrimeRate() { return crimeRate; }
    public abstract void setCrimeRate(int crimeRateReduction);
}
