package city;

import java.util.ArrayList;
import java.util.List;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;
import loans.Loan;
import utils.Point;
import utils.Reactive;

public class City {
    public CityGrid grid;
    private ArrayList<Citizen> citizens;
    private ArrayList<Loan> loans;

    private Reactive<Double> money = new Reactive<>(0.0);
    public Reactive.Observable<Double> moneyView = money.readOnly();

    public City(int sizeX, int sizeY, double startingMoney) {
        this.grid = new CityGrid(this, sizeX, sizeY);
        this.money.set(startingMoney);
    }

    public void build(Buildable building, Point place) {
        if (building.getPrice() > money.get())
            return;
        money.set(money.get() - building.getPrice());
        grid.placeBuildingAt(place, building);
    }

    public final static double DEMOLISHMENT_COEF = 0.1;

    public void demolish(Point place) {
        Buildable existingBuilding = grid.getBuildingAt(place);
        if (existingBuilding == null || existingBuilding.getPrice() * DEMOLISHMENT_COEF > money.get())
            return;
        money.set(money.get() - (int) (existingBuilding.getPrice() * DEMOLISHMENT_COEF));
        grid.removeBuildingAt(place);
    }

    public List<Citizen> getCitizens() {
        return List.copyOf(citizens);
    }

    public void takeOutLoan(Loan loan) {
        this.loans.add(loan);
    }

    public static final int TICKS_IN_DAY = 100;

    public void payBuildingsUpkeepPerTick() {
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof PublicBuilding) {
                PublicBuilding publicBuilding = (PublicBuilding) building;
                money.set(money.get() - ((double) publicBuilding.getMaintanenceCostPerDay()) / TICKS_IN_DAY);
            }
        }
    }
}
