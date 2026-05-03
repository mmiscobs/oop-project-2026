package buildings.privatebuilding.workplace;

import buildings.privatebuilding.PrivateBuilding;

public abstract class WorkplaceBuilding extends PrivateBuilding {
    private int currentWorkers;
    private int workersHired;

    public int getCurrentWorkers() { return currentWorkers; }
    public void addCurrentWorker() {}
    public void removeCurrentWorker() {}

    public void addHiredWorker() {}
    public void removeHiredWorker() {}
    public abstract int getWorkersCapacity();
}
