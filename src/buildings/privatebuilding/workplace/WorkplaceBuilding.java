package buildings.privatebuilding.workplace;

import java.util.Map;

import buildings.privatebuilding.PrivateBuilding;

public abstract class WorkplaceBuilding extends PrivateBuilding {
    private int currentWorkers;
    private int workersHired;

    public int getCurrentWorkers() {
        return currentWorkers;
    }

    public void addCurrentWorker() {
    }

    public void removeCurrentWorker() {
    }

    public void addHiredWorker() {
    }

    public void removeHiredWorker() {
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
