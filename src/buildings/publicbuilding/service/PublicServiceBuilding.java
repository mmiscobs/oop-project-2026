package buildings.publicbuilding.service;

import java.util.Map;

import buildings.publicbuilding.PublicBuilding;

public abstract class PublicServiceBuilding extends PublicBuilding {
    public abstract int getRange();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("range", Integer.toString(getRange()));
        return details;
    }
}
