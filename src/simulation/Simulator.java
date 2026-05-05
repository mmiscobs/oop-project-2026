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
    private Reactive<Integer> lastResidentTax = new Reactive<>(0);
    public Observable<Integer> lastResidentTaxView = lastResidentTax.readOnly();
    private Reactive<Integer> lastBusinessTax = new Reactive<>(0);
    public Observable<Integer> lastBusinessTaxView = lastBusinessTax.readOnly();
    private Reactive<Integer> lastPurchaseTax = new Reactive<>(0);
    public Observable<Integer> lastPurchaseTaxView = lastPurchaseTax.readOnly();
    private Reactive<Integer> lastBuildingsUpkeep = new Reactive<>(0);
    public Observable<Integer> lastBuildingsUpkeepView = lastBuildingsUpkeep.readOnly();
    public Observable<Integer> netIncomeView = new Reactive.Observable<Integer>() {
        public Integer get() {
            return lastBusinessTax.get() + lastPurchaseTax.get() + lastResidentTax.get() - lastBuildingsUpkeep.get();
        }

        public Runnable subscribe(Consumer<Integer> listener) {
            Consumer<Integer> onUpdate = r -> {
                listener.accept(get());
            };
            Runnable[] cleanups = new Runnable[] {
                    lastBuildingsUpkeepView.subscribe(onUpdate),
                    lastPurchaseTax.subscribe(onUpdate),
                    lastResidentTax.subscribe(onUpdate),
                    lastBusinessTax.subscribe(onUpdate),
            };
            return () -> {
                for (Runnable runnable : cleanups) {
                    runnable.run();
                }
            };
        }
    };

    public void runSimulationTick() {
        lastBuildingsUpkeep.set(city.payBuildingsUpkeepPerTick());
        city.accomodateHomelessPeople();
        city.accomodateNewImmigrants();
        city.evictHomelessPeople();
        lastResidentTax.set(city.collectTaxesFromResidents());
        lastBusinessTax.set(city.collectTaxesFromBusinesses());
        lastPurchaseTax.set(city.collectTaxesFromPurchases());
        runAllCitizens();

        if (onTick != null)
            onTick.accept(currentTick);

        updateCitizensAmount();
    }

    private void runAllCitizens() {
        for (Buildable building : city.grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                for (Citizen citizen : residentialBuilding.getResidents()) {
                    citizen.runSimulationTick(currentTick);
                }
            }
        }
        for (Citizen homeless : city.homelessPeople) {
            homeless.runSimulationTick(currentTick);
        }
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
