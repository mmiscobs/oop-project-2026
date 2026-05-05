package buildings.privatebuilding.residential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import buildings.Buildable;
import buildings.privatebuilding.PrivateBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.Citizen;
import city.City;

public abstract class ResidentialBuilding extends PrivateBuilding {
    public abstract int getCapacity();

    public static int calculateDemand(City city) {
        int totalVacancies = 0;
        int totalJobPositions = 0;
        int totalPopulation = city.homelessPeople.size();
        for (Buildable building : city.builtBuildings()) {
            if (building instanceof ResidentialBuilding r) {
                totalVacancies += r.getCapacity();
                totalPopulation += r.residents.size();
            }
            if (building instanceof WorkplaceBuilding r) {
                totalJobPositions += r.getWorkersCapacity();
            }
        }
        int freeVacancies = Math.max(0, totalVacancies - totalPopulation);
        int freeJobPositions = Math.max(0, totalJobPositions - totalPopulation);
        if (totalPopulation == 0)
            return 25;
        double easeOfFindingJob = (double) freeJobPositions / totalPopulation;
        if (easeOfFindingJob == 0)
            return 0;
        double easeOfFindingHousing = (double) freeVacancies / totalPopulation;
        if (easeOfFindingHousing == 0)
            return 100;
        double housingDemand = 1 / easeOfFindingHousing;
        return (int) Math.clamp(easeOfFindingJob * housingDemand * 100, 5, 100);
    }

    private ArrayList<Citizen> residents = new ArrayList<>();

    public List<Citizen> getResidents() {
        return residents;
    }

    public void destroy() {
        super.destroy();
        for (Citizen resident : residents) {
            resident.home = null;
        }
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
