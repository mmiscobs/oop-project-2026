package buildings.privatebuilding.workplace.commercial;

import java.util.Map;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.Citizen;
import city.City;

import utils.SerializedBlob;

public abstract class CommercialBuilding extends WorkplaceBuilding {
    public CommercialBuilding() {
        super();
    }

    protected CommercialBuilding(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public abstract int getVisitorsCapacity();

    public void addVisitor(Citizen citizen) {
        if (visitors.size() < getVisitorsCapacity())
            visitors.add(citizen);
    }

    public static int calculateDemand(City city) {
        double retailShortage = calculateRetailShortage(city);
        double laborShortage = calculateLaborShortage(city);
        if (retailShortage == 0 || laborShortage == 0)
            return 100;
        double easeOfFindingConsumer = (double) 1 / retailShortage;
        double easeOfFindingWorker = (double) 1 / laborShortage;
        return (int) Math.clamp(easeOfFindingConsumer * easeOfFindingWorker * 100, 5, 100);
    }

    public static double calculateRetailShortage(City city) {
        int totalVisitorPlaces = 0;
        int totalPopulation = city.homelessPeople.size();
        for (Buildable building : city.builtBuildings()) {
            if (building instanceof ResidentialBuilding r) {
                totalPopulation += r.getResidents().size();
            }
            if (building instanceof CommercialBuilding c) {
                totalVisitorPlaces += c.getVisitorsCapacity();
            }
        }
        if (totalVisitorPlaces == 0)
            return 1;
        double easeOfFindingConsumer = (double) totalPopulation / totalVisitorPlaces;
        return 1 / easeOfFindingConsumer;
    }

    private final static int SALES_TAX = 3;

    public int calculateSalesTax() {
        return SALES_TAX * getVisitors().size();
    }

    public int calculateProfitPerTick() {
        return super.calculateProfitPerTick() + calculateSalesTax();
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("visitors", Integer.toString(getVisitors().size()));
        details.put("visitors capacity", Integer.toString(getVisitorsCapacity()));
        return details;
    }
}
