package buildings.privatebuilding.workplace.industrial;

import buildings.Buildable;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.City;

public abstract class IndustrialBuilding extends WorkplaceBuilding {
    public static int calculateDemand(City city) {
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
            return 100;
        double easeOfFindingWorker = (double) totalPopulation / totalJobPositions;
        return (int) Math.clamp(easeOfFindingWorker * 100, 5, 100);
    }
}
