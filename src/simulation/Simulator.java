package simulation;

import city.City;
import city.Citizen;

public class Simulator {
    public City city;
    public GameSpeed gameSpeed;
    public CityDatum cityDatum;

    public Simulator(City city) {
        this.city = city;
        this.gameSpeed = GameSpeed.Stopped;
        this.cityDatum = new CityDatum();
    }

    public Citizen getRandomCitizen() {
        return null;
    }

    public void setGameSpeed(GameSpeed speed) {
    }

    public void runSimulationTick() {
    }
}
