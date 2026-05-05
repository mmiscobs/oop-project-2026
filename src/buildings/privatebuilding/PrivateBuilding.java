package buildings.privatebuilding;

import java.util.Map;

import buildings.Buildable;

public abstract class PrivateBuilding extends Buildable {
    protected boolean isBuilt;

    public abstract boolean getIsBuilt();

    public abstract int calculateProfitPerTick();

    public int getPresentCitizensAmount() {
        return 0;
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("is built", isBuilt ? "yes" : "no");
        details.put("current tax revenue", Integer.toString(calculateProfitPerTick()));
        details.put("present citizens", Integer.toString(getPresentCitizensAmount()));
        return details;
    }
}
