package buildings.privatebuilding.residential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import buildings.Buildable;
import buildings.privatebuilding.PrivateBuilding;
import buildings.privatebuilding.workplace.WorkplaceBuilding;
import city.Citizen;
import city.City;
import utils.SerializedBlob;

public abstract class ResidentialBuilding extends PrivateBuilding {
    public ResidentialBuilding() {
        super();
    }

    protected ResidentialBuilding(SerializedBlob blob, City city) {
        super(blob, city);
        residents.addAll(blob.map().get("residents").array().stream().map(b -> Citizen.fromBlob(b, city)).toList());
    }

    public SerializedBlob toBlob() {
        return super.toBlob()
                .extendMap(Map.of("residents", SerializedBlob.array(residents.stream().map(r -> r.toBlob()).toList())));
    }

    public abstract int getCapacity();

    public static int calculateDemand(City city) {
        double housingShortage = calculateHousingShortage(city);
        if (housingShortage == 0)
            housingShortage = 0.01;
        double housingDemand = 1 / housingShortage;
        double easeOfFindingJob = WorkplaceBuilding.calculateLaborShortage(city);
        return (int) Math.clamp(easeOfFindingJob * housingDemand * 100, 5, 100);
    }

    public static double calculateHousingShortage(City city) {
        int totalVacancies = 0;
        int totalPopulation = city.homelessPeople.size();
        for (Buildable building : city.builtBuildings()) {
            if (building instanceof ResidentialBuilding r) {
                totalVacancies += r.getCapacity();
                totalPopulation += r.residents.size();
            }
        }
        int freeVacancies = Math.max(0, totalVacancies - totalPopulation);
        if (totalPopulation == 0)
            return 0.25;
        double easeOfFindingHousing = (double) freeVacancies / totalPopulation;
        return easeOfFindingHousing;
    }

    private ArrayList<Citizen> residents = new ArrayList<>();

    public List<Citizen> getResidents() {
        return residents;
    }

    public void destroy() {
        super.destroy();
        for (Citizen resident : residents) {
            resident.evict();
        }
    }

    public List<Citizen> evictResidents() {
        List<Citizen> evictedResidents = List.copyOf(residents);
        residents.clear();
        for (Citizen citizen : evictedResidents) {
            citizen.evict();
        }
        return evictedResidents;
    }

    public void addResident(Citizen citizen) {
        if (residents.size() < getCapacity())
            residents.add(citizen);
        if (citizen.location.get() == this)
            visitors.add(citizen);
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
