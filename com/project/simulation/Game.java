package com.project.simulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.project.city.City;
import com.project.utils.Reactive;
import com.project.utils.SerializedBlob;

public class Game {
    public Reactive<Simulator> simulator = new Reactive<>(null);

    public String[] getSaves() {
        return null;
    }

    public Game() {

    }

    public List<String> listSaves() {
        String[] files = new File("./saves").list((d, f) -> f.endsWith(".xml"));
        return files == null ? List.of() : List.of(files).stream().map(f -> f.substring(0, f.length() - 4)).toList();
    }

    public void loadSave(String gameName) throws IOException {
        String file = Files.readString(Path.of("./saves", gameName + ".xml"));
        simulator.set(new Simulator(new City(SerializedBlob.parse(file))));
    }

    public void createSave() throws IOException {
        City city = simulator.get().city;
        if (!Files.exists(Path.of("./saves"))) {
            Files.createDirectory(Path.of("./saves"));
        }
        Files.writeString(Path.of("./saves", city.name + ".xml"), city.toBlob().toXml());
    }

    public void startNewSimulation(int mapSizeX, int mapSizeY, GameDifficulty difficulty, String cityName) {
        if (this.simulator.get() != null)
            return;
        City city = new City(mapSizeX, mapSizeY, difficulty.startingMoney, cityName);
        this.simulator.set(new Simulator(city));
    }
}
