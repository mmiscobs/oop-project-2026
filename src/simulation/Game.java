package simulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import city.City;
import utils.Reactive;
import utils.SerializedBlob;

public class Game {
    public Reactive<Simulator> simulator = new Reactive<>(null);

    public String[] getSaves() {
        return null;
    }

    public Game() {

    }

    public String[] listSaves() {
        String[] files = new File("./saves").list();
        return files == null ? new String[0] : files;
    }

    public void loadSave(String gameName) throws IOException {
        String file = Files.readString(Path.of("./saves", gameName));
        simulator.set(new Simulator(new City(SerializedBlob.parse(file))));
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
