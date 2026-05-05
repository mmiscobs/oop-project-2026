package simulation;

import city.City;

import javax.swing.Timer;

import buildings.Buildable;

import java.util.function.Consumer;

import city.Citizen;

public class Simulator {
    public City city;
    public GameSpeed gameSpeed;
    public CityDatum cityDatum;
    public int currentTick;
    public Consumer<Integer> onTick;

    public Simulator(City city) {
        this.city = city;
        this.gameSpeed = GameSpeed.Stopped;
        this.cityDatum = new CityDatum();
    }

    public void startSimulation() {
        class Ticker {
            int val = 0;
        }
        Ticker ticker = new Ticker();
        int timerInterval = 200;
        Timer timer = new Timer(timerInterval, e -> {
            ticker.val += timerInterval;
            if (ticker.val % this.gameSpeed.msBetweenTicks == 0) {
                currentTick++;
                runSimulationTick();
            }
        });
        timer.start();
    }

    public Citizen getRandomCitizen() {
        return null;
    }

    public void setGameSpeed(GameSpeed speed) {
    }

    public void runSimulationTick() {
        city.payBuildingsUpkeepPerTick();

        if (onTick != null)
            onTick.accept(currentTick);
    }
}
