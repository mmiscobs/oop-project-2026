package buildings.publicbuilding;

import java.util.Map;

import buildings.Buildable;

public abstract class PublicBuilding extends Buildable {
    public abstract int getMaintanenceCostPerDay();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("maintanence", Integer.toString(getMaintanenceCostPerDay()));
        return details;
    }
}
