package buildings.publicbuilding.transportation;

import buildings.publicbuilding.PublicBuilding;

public abstract class PublicTransportation extends PublicBuilding {
    private int congestion;

    public abstract int getCapacity();
    public int getCongestion() { return congestion; }
    public void increaseCongestion() {}
    public void decreaseCongestion() {}
    public abstract int computeNoiseLevel();
}
