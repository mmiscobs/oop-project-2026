package buildings.privatebuilding.workplace.commercial;

import java.util.Map;

import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.Citizen;

public abstract class CommercialBuilding extends WorkplaceBuilding {
    public abstract int getVisitorsCapacity();

    public void addVisitor(Citizen citizen) {
        if (visitors.size() < getVisitorsCapacity())
            visitors.add(citizen);
    }

    public static int calculateDemand() {
        return 0;
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
