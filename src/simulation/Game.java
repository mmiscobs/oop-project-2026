package simulation;

import city.City;
import utils.Reactive;

public class Game {
    public Reactive<Simulator> simulator = new Reactive<>(null);

    public String[] getSaves() {
        return null;
    }

    public Game() {

    }

    public void loadSave(String gameName) {
    }

    public void createSave() {
    }

    public void startNewSimulation(int mapSizeX, int mapSizeY, GameDifficulty difficulty, String cityName) {
        if (this.simulator.get() != null)
            return;
        City city = new City(mapSizeX, mapSizeY, difficulty.startingMoney, cityName);
        this.simulator.set(new Simulator(city));
    }
}
