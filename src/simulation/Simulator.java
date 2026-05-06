package simulation;

import city.City;
import utils.Reactive;
import utils.Reactive.Observable;

import javax.swing.Timer;

import buildings.Buildable;
import buildings.privatebuilding.PrivateBuilding;
import buildings.privatebuilding.residential.ResidentialBuilding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import city.Citizen;

public class Simulator {
    public City city;
    public GameSpeed gameSpeed;
    private Reactive<Integer> currentTick = new Reactive<>(0);
    public Observable<Integer> currentTickView = currentTick.readOnly();

    public Simulator(City city) {
        this.city = city;
        this.gameSpeed = GameSpeed.Stopped;
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
                runSimulationTick();
                currentTick.set(currentTick.get() + 1);
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
            return lastBusinessTax.get() + lastPurchaseTax.get() + lastResidentTax.get() - lastBuildingsUpkeep.get()
                    - lastLoansService.get().intValue();
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
                    lastLoansService.subscribe(l -> onUpdate.accept(0)),
            };
            return () -> {
                for (Runnable runnable : cleanups) {
                    runnable.run();
                }
            };
        }
    };

    private Reactive<Double> lastLoansService = new Reactive<>(0.0);
    public Observable<Double> lastLoansServiceView = lastLoansService.readOnly();
    private Reactive<Double> totalDebt = new Reactive<>(0.0);
    public Observable<Double> totalDebtView = totalDebt.readOnly();

    public void runSimulationTick() {
        lastBuildingsUpkeep.set(city.payBuildingsUpkeepPerTick());
        city.accomodateHomelessPeople();
        city.accomodateNewImmigrants();
        city.evictHomelessPeople();
        city.employPeople();
        lastLoansService.set(city.serviceLoans());
        totalDebt.set(city.getTotalDebt());
        buildPrivateBuildings();
        lastResidentTax.set(city.collectTaxesFromResidents());
        lastBusinessTax.set(city.collectTaxesFromBusinesses());
        lastPurchaseTax.set(city.collectTaxesFromPurchases());
        runAllBuildings();
        runAllCitizens();

        updateCitizensAmount();
    }

    private List<Citizen> allCitizens() {
        ArrayList<Citizen> citizens = new ArrayList<>();
        for (Buildable building : city.grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                citizens.addAll(residentialBuilding.getResidents());
            }
        }
        citizens.addAll(city.homelessPeople);
        return citizens;
    }

    private void runAllCitizens() {
        for (Citizen citizen : allCitizens()) {
            citizen.runSimulationTick(currentTick.get());
        }
    }

    private void runAllBuildings() {
        for (Buildable building : city.grid.buildings.values()) {
            building.runSimulationTick(city);
        }
    }

    private void buildPrivateBuildings() {
        for (Buildable building : city.grid.buildings.values()) {
            if (building instanceof PrivateBuilding privateBuilding && !privateBuilding.getIsBuilt()) {
                double changeOfBuildingInTick = PrivateBuilding.calculateDemand(privateBuilding, city) * 0.8;
                if (flipCoinWithChance(changeOfBuildingInTick)) {
                    privateBuilding.build();
                }
            }
        }
    }

    private static boolean flipCoinWithChance(double chance) {
        return new Random().nextFloat(0, 100) < chance;
    }

    public int averageCitizenSatisfaction() {
        if (allCitizens().size() == 0)
            return 100;
        return allCitizens().stream().map(c -> c.getSatisfaction()).reduce(0, Integer::sum) / allCitizens().size();
    }

    public int averageCrimeRate() {
        if (city.grid.buildings.size() == 0)
            return 0;
        return city.grid.buildings.values().stream().map(c -> c.getCrimeRate()).reduce(0, Integer::sum)
                / city.grid.buildings.size();
    }

    public int averageCitizenHealth() {
        if (allCitizens().size() == 0)
            return 100;
        return allCitizens().stream().map(c -> c.getSatisfaction()).reduce(0, Integer::sum) / allCitizens().size();
    }

    private void updateCitizensAmount() {
        citizensAmount.set(allCitizens().size());
        homelessCitizensAmount.set(city.homelessPeople.size());
    }
}
