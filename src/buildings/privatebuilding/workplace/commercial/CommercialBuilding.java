package buildings.privatebuilding.workplace.commercial;

import java.util.Map;

import buildings.privatebuilding.workplace.WorkplaceBuilding;

public abstract class CommercialBuilding extends WorkplaceBuilding {
    private int visitors;

    public abstract int getVisitorsCapacity();

    public int getVisitors() {
        return visitors;
    }

    public void addVisitor() {
    }

    public void removeVisitor() {
    }

    public static int calculateDemand() {
        return 0;
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("visitors", Integer.toString(getVisitors()));
        details.put("visitors capacity", Integer.toString(getVisitorsCapacity()));
        return details;
    }
}
