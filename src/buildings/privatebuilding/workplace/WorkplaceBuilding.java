package buildings.privatebuilding.workplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import buildings.privatebuilding.PrivateBuilding;
import city.Citizen;

public abstract class WorkplaceBuilding extends PrivateBuilding {
    private int currentWorkers;
    private int workersHired;

    public int getCurrentWorkers() {
        return currentWorkers;
    }

    private ArrayList<Citizen> hiredWorkers = new ArrayList<>();

    public List<Citizen> getHiredWorkers() {
        return List.copyOf(hiredWorkers);
    }

    public void addCurrentWorker() {
    }

    public void removeCurrentWorker() {
    }

    public void addHiredWorker() {
    }

    public void removeHiredWorker() {
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

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("workers", Integer.toString(getCurrentWorkers()));
        details.put("workers hired", Integer.toString(workersHired));
        details.put("workers capacity", Integer.toString(getWorkersCapacity()));
        return details;
    }
}
