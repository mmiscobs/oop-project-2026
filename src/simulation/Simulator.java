package simulation;

import city.City;
import utils.Reactive;
import utils.Reactive.Observable;

import javax.swing.Timer;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;

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

    private Reactive<Integer> citizensAmount = new Reactive<>(0);
    public Observable<Integer> citizensAmountView = citizensAmount.readOnly();
    private Reactive<Integer> homelessCitizensAmount = new Reactive<>(0);
    public Observable<Integer> homelessCitizensAmountView = homelessCitizensAmount.readOnly();

    public void runSimulationTick() {
        city.payBuildingsUpkeepPerTick();
        city.accomodateHomelessPeople();
        city.accomodateNewImmigrants();
        city.evictHomelessPeople();

        if (onTick != null)
            onTick.accept(currentTick);

        updateCitizensAmount();
    }

    private void updateCitizensAmount() {
        int recalculatedCitizensAmount = city.homelessPeople.size();
        for (Buildable building : city.grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                recalculatedCitizensAmount += residentialBuilding.getResidents().size();
            }
        }
        citizensAmount.set(recalculatedCitizensAmount);
        homelessCitizensAmount.set(city.homelessPeople.size());
    }
}
