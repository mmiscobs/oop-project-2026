package buildings.privatebuilding.workplace.commercial;

import java.util.Map;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.Citizen;
import city.City;

public abstract class CommercialBuilding extends WorkplaceBuilding {
    public abstract int getVisitorsCapacity();

    public void addVisitor(Citizen citizen) {
        if (visitors.size() < getVisitorsCapacity())
            visitors.add(citizen);
    }

    public static int calculateDemand(City city) {
        int totalVisitorPlaces = 0;
        int totalJobPositions = 0;
        int totalPopulation = city.homelessPeople.size();
        for (Buildable building : city.builtBuildings()) {
            if (building instanceof ResidentialBuilding r) {
                totalPopulation += r.getResidents().size();
            }
            if (building instanceof CommercialBuilding c) {
                totalVisitorPlaces += c.getVisitorsCapacity();
            }
            if (building instanceof WorkplaceBuilding r) {
                totalJobPositions += r.getWorkersCapacity();
            }
        }
        if (totalVisitorPlaces == 0 || totalJobPositions == 0)
            return 100;
        double easeOfFindingConsumer = (double) totalPopulation / totalVisitorPlaces;
        double easeOfFindingWorker = (double) totalPopulation / totalJobPositions;
        return (int) Math.clamp(easeOfFindingConsumer * easeOfFindingWorker * 100, 5, 100);
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
