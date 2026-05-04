package simulation;

import city.City;

import javax.swing.Timer;
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
        Timer timer = new Timer(500, e -> {
            ticker.val++;
            int advancedTicks = 0;
            switch (gameSpeed) {
                case GameSpeed.Stopped:
                    advancedTicks = 0;
                    break;
                case GameSpeed.Fast:
                    advancedTicks = 1;
                    break;
                case GameSpeed.Normal:
                    advancedTicks = 2;
                    break;
                case GameSpeed.Slow:
                    advancedTicks = 3;
                    break;
            }
            if (advancedTicks != 0 && ticker.val % advancedTicks == 0) {
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
        if (onTick != null)
            onTick.accept(currentTick);
    }
}
