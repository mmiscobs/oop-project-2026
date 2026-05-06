package buildings.privatebuilding.workplace.office;

import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.City;

public abstract class OfficeBuilding extends WorkplaceBuilding {
    public static int calculateDemand(City city) {
        double laborShortage = WorkplaceBuilding.calculateLaborShortage(city);
        if (laborShortage == 0)
            return 100;
        double easeOfFindingWorker = 1 / WorkplaceBuilding.calculateLaborShortage(city);
        return (int) Math.clamp(easeOfFindingWorker * 100, 5, 100);
    }
}
