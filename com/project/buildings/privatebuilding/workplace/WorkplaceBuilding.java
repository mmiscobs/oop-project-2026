package com.project.buildings.privatebuilding.workplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.project.buildings.Buildable;
import com.project.buildings.privatebuilding.PrivateBuilding;
import com.project.buildings.privatebuilding.residential.ResidentialBuilding;
import com.project.city.Citizen;
import com.project.city.City;

import com.project.utils.SerializedBlob;

public abstract class WorkplaceBuilding extends PrivateBuilding {
    public WorkplaceBuilding() {
        super();
    }

    protected WorkplaceBuilding(SerializedBlob blob, City city) {
        super(blob, city);
        hiredWorkers
                .addAll(blob.map().get("hiredWorkers").array().stream().map(b -> Citizen.fromBlob(b, city)).toList());
    }

    public SerializedBlob toBlob(SerializedBlob.Factory Factory) {
        return super.toBlob(Factory).extendMap(
                Map.of("hiredWorkers", Factory.array(hiredWorkers.stream().map(w -> w.toBlob(Factory)).toList())));
    }

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
