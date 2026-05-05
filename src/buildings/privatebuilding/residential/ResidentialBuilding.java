package buildings.privatebuilding.residential;

import java.util.Map;

import buildings.privatebuilding.PrivateBuilding;

public abstract class ResidentialBuilding extends PrivateBuilding {
    public abstract int getCapacity();

    public static int calculateDemand() {
        return 0;
    }

    public int getResidents() {
        return 0;
    }

    public void addResident() {
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("residents", Integer.toString(getResidents()));
        details.put("capacity", Integer.toString(getCapacity()));
        return details;
    }
}
