package buildings.publicbuilding.service.police;

public class SmallPoliceStation extends PoliceStation {
    public boolean hasExtraPoliceCarsGarage;

    public boolean getHasExtraPoliceCarsGarage() { return hasExtraPoliceCarsGarage; }
    public void buildExtraPoliceCarsGarage() {}
    public void removeExtraPoliceCarsGarage() {}

    public int getPrice() { return 0; }
    public void setCrimeRate(int crimeRateReduction) {}
    public int getMaintanenceCostPerDay() { return 0; }
    public int getRange() { return 0; }
    public int getCrimeReduction(int x, int y) { return 0; }
}
