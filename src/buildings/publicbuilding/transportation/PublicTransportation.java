package buildings.publicbuilding.transportation;

import java.util.Map;

import buildings.publicbuilding.PublicBuilding;

public abstract class PublicTransportation extends PublicBuilding {
    private int congestion;

    public abstract int getCapacity();

    public int getCongestion() {
        return congestion;
    }

    public int getPresentCitizensAmount() {
        return 0;
    }

    public void increaseCongestion() {
    }

    public void decreaseCongestion() {
    }

    public abstract int computeNoiseLevel();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("congestion", Integer.toString(getCongestion()));
        details.put("present citizens", Integer.toString(getPresentCitizensAmount()));
        return details;
    }
}
