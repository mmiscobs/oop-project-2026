package buildings.privatebuilding.workplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import buildings.Buildable;
import buildings.privatebuilding.PrivateBuilding;
import buildings.privatebuilding.residential.ResidentialBuilding;
import city.Citizen;
import city.City;

public abstract class WorkplaceBuilding extends PrivateBuilding {
    private ArrayList<Citizen> hiredWorkers = new ArrayList<>();

    public List<Citizen> getHiredWorkers() {
        return List.copyOf(hiredWorkers);
    }

    public void addHiredWorker(Citizen worker) {
        if (hasOpenJobPositions())
            hiredWorkers.add(worker);
    }

    public void removeHiredWorker(Citizen worker) {
        hiredWorkers.remove(worker);
    }

    public void destroy() {
        super.destroy();
        for (Citizen worker : hiredWorkers) {
            worker.work = null;
        }
    }

    private final static int BUSINESS_TAX = 3;

    public int getBusinessTax() {
        return BUSINESS_TAX * getHiredWorkers().size();
    }

    @Override
    public int calculateProfitPerTick() {
        return getBusinessTax();
    }

    public abstract int getWorkersCapacity();

    public boolean hasOpenJobPositions() {
        return getWorkersCapacity() > hiredWorkers.size();
    }

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("workers hired", Integer.toString(hiredWorkers.size()));
        details.put("workers capacity", Integer.toString(getWorkersCapacity()));
        return details;
    }

    public static double calculateLaborShortage(City city) {
        int totalJobPositions = 0;
        int totalPopulation = city.homelessPeople.size();
        for (Buildable building : city.builtBuildings()) {
            if (building instanceof ResidentialBuilding r) {
                totalPopulation += r.getResidents().size();
            }
            if (building instanceof WorkplaceBuilding r) {
                totalJobPositions += r.getWorkersCapacity();
            }
        }
        if (totalJobPositions == 0)
            return 1;
        double easeOfFindingWorker = (double) totalPopulation / totalJobPositions;
        if (easeOfFindingWorker == 0)
            return 1;
        return 1 / easeOfFindingWorker;
    }
}
