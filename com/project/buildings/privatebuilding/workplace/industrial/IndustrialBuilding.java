package com.project.buildings.privatebuilding.workplace.industrial;

import com.project.buildings.privatebuilding.workplace.WorkplaceBuilding;
import com.project.city.City;

import com.project.utils.SerializedBlob;

public abstract class IndustrialBuilding extends WorkplaceBuilding {
    public IndustrialBuilding() {
        super();
    }

    protected IndustrialBuilding(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public static int calculateDemand(City city) {
        double laborShortage = WorkplaceBuilding.calculateLaborShortage(city);
        if (laborShortage == 0)
            return 100;
        double easeOfFindingWorker = 1 / WorkplaceBuilding.calculateLaborShortage(city);
        return (int) Math.clamp(easeOfFindingWorker * 100, 5, 100);
    }
}
