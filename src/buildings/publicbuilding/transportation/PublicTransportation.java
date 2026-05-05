package buildings.publicbuilding.transportation;

import java.util.Map;

import buildings.publicbuilding.PublicBuilding;

public abstract class PublicTransportation extends PublicBuilding {
    public abstract int getCapacity();

    public int getCongestion() {
        return (int) ((double) getVisitors().size() / getCapacity() * 100);
    }

    public abstract int computeNoiseLevel();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("congestion", Integer.toString(getCongestion()));
        return details;
    }
}
