package city;

import java.util.ArrayList;
import java.util.Iterator;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import buildings.publicbuilding.PublicBuilding;
import loans.Loan;
import utils.Point;
import utils.Reactive;

public class City {
    public CityGrid grid;
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

    public ArrayList<Citizen> homelessPeople = new ArrayList<>();

    public void demolish(Point place) {
        Buildable existingBuilding = grid.getBuildingAt(place);
        if (existingBuilding == null || existingBuilding.getPrice() * DEMOLISHMENT_COEF > money.get())
            return;
        if (existingBuilding instanceof ResidentialBuilding) {
            ResidentialBuilding residentialBuilding = (ResidentialBuilding) existingBuilding;
            homelessPeople.addAll(residentialBuilding.evictResidents());
        }
        existingBuilding.destroy();
        money.set(money.get() - (int) (existingBuilding.getPrice() * DEMOLISHMENT_COEF));
        grid.removeBuildingAt(place);
    }

    public void takeOutLoan(Loan loan) {
        this.loans.add(loan);
    }

    public static final int TICKS_IN_DAY = 10;

    public int payBuildingsUpkeepPerTick() {
        double buildingsUpkeep = 0;
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof PublicBuilding) {
                PublicBuilding publicBuilding = (PublicBuilding) building;
                buildingsUpkeep += ((double) publicBuilding.getMaintanenceCostPerDay()) / TICKS_IN_DAY;
            }
        }
        money.set(money.get() - (int) buildingsUpkeep);
        return (int) buildingsUpkeep;
    }

    public void accomodateHomelessPeople() {
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                int delta = residentialBuilding.getCapacity() - residentialBuilding.getResidents().size();
                int initialDelta = delta;
                Iterator<Citizen> homelessPeopleIterator = homelessPeople.iterator();
                while (homelessPeopleIterator.hasNext() && delta > (int) (initialDelta * 0.9)) {
                    Citizen homeless = homelessPeopleIterator.next();
                    residentialBuilding.addResident(homeless);
                    homelessPeopleIterator.remove();
                    delta--;
                }
            }
        }
    }

    public int collectTaxesFromResidents() {
        int residentTax = 0;
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                residentTax += residentialBuilding.getResidentTax();
            }
        }
        money.set(money.get() + residentTax);
        return residentTax;
    }

    public int collectTaxesFromPurchases() {
        int purchaseTax = 0;
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof CommercialBuilding) {
                CommercialBuilding commercialBuilding = (CommercialBuilding) building;
                purchaseTax += commercialBuilding.calculateSalesTax();
            }
        }
        money.set(money.get() + purchaseTax);
        return purchaseTax;
    }

    public int collectTaxesFromBusinesses() {
        int businessTax = 0;
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof WorkplaceBuilding) {
                WorkplaceBuilding workplaceBuilding = (WorkplaceBuilding) building;
                businessTax += workplaceBuilding.getBusinessTax();
            }
        }
        money.set(money.get() + businessTax);
        return businessTax;
    }

    public void evictHomelessPeople() {
        int delta = homelessPeople.size();
        int initialDelta = delta;
        Iterator<Citizen> homelessPeopleIterator = homelessPeople.iterator();
        while (homelessPeopleIterator.hasNext() && delta > (int) (initialDelta * 0.9)) {
            Citizen homeless = homelessPeopleIterator.next();
            homelessPeopleIterator.remove();
            delta--;
        }
    }

    public void employPeople() {

    }

    public void accomodateNewImmigrants() {
        for (Buildable building : grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                int delta = residentialBuilding.getCapacity() - residentialBuilding.getResidents().size();
                int initialDelta = delta;
                while (delta > (int) (initialDelta * 0.9)) {
                    residentialBuilding.addResident(new Citizen(this, residentialBuilding, null));
                    delta--;
                }
            }
        }
    }
}
