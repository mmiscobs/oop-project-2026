package city;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import buildings.Buildable;
import buildings.Buildable.BuildableRef;
import buildings.privatebuilding.PrivateBuilding;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import buildings.publicbuilding.PublicBuilding;
import loans.Loan;
import utils.Point;
import utils.Reactive;
import utils.SerializedBlob;
import utils.Reactive.Observable;
import utils.Reactive.ReactiveArrayList;

public class City {
    public CityGrid grid;
    private ReactiveArrayList<Loan> loans = new ReactiveArrayList<>();
    public Observable<List<Loan>> loansView = loans.readOnly();

    private Reactive<Double> money = new Reactive<>(0.0);
    public Reactive.Observable<Double> moneyView = money.readOnly();

    public final String name;

    public City(int sizeX, int sizeY, double startingMoney, String name) {
        this.grid = new CityGrid(sizeX, sizeY);
        this.money.set(startingMoney);
        this.name = name;
    }

    public City(SerializedBlob blob) {
        this.name = blob.map().get("name").string();
        this.money.set(blob.map().get("money").doubleValue());
        this.grid = new CityGrid(blob.map().get("grid"), this);
        this.loans.addAll(blob.map().get("loans").array().stream().map(Loan::fromBlob).toList());
        this.homelessPeople
                .addAll(blob.map().get("homelessPeople").array().stream().map(b -> Citizen.fromBlob(b, this)).toList());
    }

    public SerializedBlob toBlob() {
        return SerializedBlob.fromMap(Map.of("name", SerializedBlob.string(name),
                "money", SerializedBlob.doubleValue(money.get()),
                "grid", grid.toBlob(),
                "loans", SerializedBlob.array(loans.stream().map(l -> l.toBlob()).toList()),
                "homelessPeople", SerializedBlob.array(homelessPeople.stream().map(h -> h.toBlob()).toList())));
    }

    public boolean build(PublicBuilding.Upgrade upgrade) {
        if (upgrade.getPrice() > money.get())
            return false;
        money.set(money.get() - upgrade.getPrice());
        return true;
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
        this.money.set(this.money.get() + loan.getLoanSize());
    }

    public void payOutLoan(Loan loan) {
        if (loan.paymentLeftView.get() < money.get()) {
            money.set(money.get() - loan.paymentLeftView.get());
            loan.payOutLoan(loan.paymentLeftView.get());
            if (loan.paidOutLoan())
                loans.remove(loan);
        }
    }

    public double serviceLoans() {
        double totalPaid = 0;
        Iterator<Loan> loanIter = this.loans.iterator();
        while (loanIter.hasNext()) {
            Loan loan = loanIter.next();
            double paid = Math.min(loan.getPerTickInterest(), money.get());
            loan.payPerTickInterest((int) paid);
            money.set(money.get() - paid);
            totalPaid += paid;
            if (loan.paidOutLoan())
                loanIter.remove();
        }
        return totalPaid;
    }

    public double getTotalDebt() {
        return this.loans.stream().map(l -> l.getPaymentLeft()).reduce(0, Integer::sum);
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
        for (Buildable building : builtBuildings()) {
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

    public List<PrivateBuilding> builtBuildings() {
        ArrayList<PrivateBuilding> built = new ArrayList<>();

        for (Buildable building : grid.buildings.values())
            if (building instanceof PrivateBuilding p && p.getIsBuilt())
                built.add(p);
        return built;
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
            if (homeless.location.get() != null) {
                homeless.location.get().removeVisitor(homeless);
            }
            if (homeless.work.get() != null) {
                homeless.work.get().removeHiredWorker(homeless);
            }
            delta--;
        }
    }

    private List<Citizen> unemployedPeople() {
        ArrayList<Citizen> unemployed = new ArrayList<>();

        for (Buildable building : grid.buildings.values()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                for (Citizen citizen : residentialBuilding.getResidents()) {
                    if (citizen.work == null) {
                        unemployed.add(citizen);
                    }
                }
            }
        }
        for (Citizen homeless : homelessPeople) {
            if (homeless.work == null)
                unemployed.add(homeless);
        }
        return unemployed;
    }

    private List<WorkplaceBuilding> vacantWorkplaces() {
        ArrayList<WorkplaceBuilding> vacant = new ArrayList<>();
        for (Buildable building : builtBuildings()) {
            if (building instanceof WorkplaceBuilding workplace) {
                if (workplace.hasOpenJobPositions()) {
                    vacant.add(workplace);
                }
            }
        }
        return vacant;
    }

    public void employPeople() {
        Iterator<Citizen> unemployedIterator = unemployedPeople().iterator();
        if (!unemployedIterator.hasNext())
            return;
        for (WorkplaceBuilding vacantWorkplace : vacantWorkplaces()) {
            while (vacantWorkplace.hasOpenJobPositions() && unemployedIterator.hasNext()) {
                Citizen unemployed = unemployedIterator.next();
                vacantWorkplace.addHiredWorker(unemployed);
                unemployed.work = new BuildableRef<>(vacantWorkplace, this);
            }
        }
    }

    private int vacantApartments() {
        int vacant = 0;
        for (Buildable building : builtBuildings()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                int delta = residentialBuilding.getCapacity() - residentialBuilding.getResidents().size();
                vacant += delta;
            }
        }
        return vacant;
    }

    private int totalPop() {
        int pop = 0;
        for (Buildable building : builtBuildings()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                pop += residentialBuilding.getResidents().size();
            }
        }
        return pop;
    }

    public void accomodateNewImmigrants() {
        double laborShortage = WorkplaceBuilding.calculateLaborShortage(this);
        int willingToMoveInto = laborShortage < 1 ? totalPop() == 0 ? 1 : 0
                : (int) (laborShortage * vacantApartments());
        for (Buildable building : builtBuildings()) {
            if (building instanceof ResidentialBuilding) {
                ResidentialBuilding residentialBuilding = (ResidentialBuilding) building;
                int delta = residentialBuilding.getCapacity() - residentialBuilding.getResidents().size();
                int initialDelta = delta;
                while (delta > (int) (initialDelta * 0.9) && willingToMoveInto > 0) {
                    residentialBuilding.addResident(new Citizen(this, residentialBuilding, null));
                    willingToMoveInto--;
                    delta--;
                }
            }
        }
    }
}
