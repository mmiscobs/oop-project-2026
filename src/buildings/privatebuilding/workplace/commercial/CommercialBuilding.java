package buildings.privatebuilding.workplace.commercial;

import buildings.privatebuilding.workplace.WorkplaceBuilding;

public abstract class CommercialBuilding extends WorkplaceBuilding {
    private int visitors;

    public abstract int getVisitorsCapacity();
    public int getVisitors() { return visitors; }
    public void addVisitor() {}
    public void removeVisitor() {}
    public static int calculateDemand() { return 0; }
}
