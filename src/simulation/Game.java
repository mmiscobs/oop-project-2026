package simulation;

import city.City;

public class Game {
    public Simulator simulator;
    private String loadedGame;

    public String[] getSaves() {
        return null;
    }

    public Game() {

    }

    public void loadSave(String saveName) {
    }

    public void createSave(String saveName) {
    }

    public void startNewSimulation(int mapSizeX, int mapSizeY, GameDifficulty difficulty) {
        City city = new City(mapSizeX, mapSizeY, difficulty.startingMoney);
        this.simulator = new Simulator(city);
    }
}
