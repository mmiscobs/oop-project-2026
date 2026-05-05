package buildings.privatebuilding.residential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import buildings.privatebuilding.PrivateBuilding;
import city.Citizen;

public abstract class ResidentialBuilding extends PrivateBuilding {
    public abstract int getCapacity();

    public static int calculateDemand() {
        return 0;
    }

    private ArrayList<Citizen> residents = new ArrayList<>();

    public List<Citizen> getResidents() {
        return residents;
    }

    public List<Citizen> evictResidents() {
        List<Citizen> evictedResidents = List.copyOf(residents);
        residents.clear();
        return evictedResidents;
    }

    public void addResident(Citizen citizen) {
        if (residents.size() < getCapacity())
            residents.add(citizen);
    }

    private final static int RESIDENT_TAX = 3;

    public int getResidentTax() {
        return RESIDENT_TAX * getResidents().size();
    }

    public int calculateProfitPerTick() {
        return getResidentTax();
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("residents", Integer.toString(getResidents().size()));
        details.put("capacity", Integer.toString(getCapacity()));
        return details;
    }
}
