package buildings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import buildings.privatebuilding.residential.Condominium;
import buildings.privatebuilding.residential.SmallHouse;
import buildings.privatebuilding.workplace.commercial.GroceryStore;
import buildings.privatebuilding.workplace.commercial.Mall;
import buildings.privatebuilding.workplace.industrial.Factory;
import buildings.privatebuilding.workplace.industrial.Warehouse;
import buildings.privatebuilding.workplace.office.BankBranch;
import buildings.privatebuilding.workplace.office.Skyrise;
import buildings.publicbuilding.service.healthcare.Clinic;
import buildings.publicbuilding.service.healthcare.Hospital;
import buildings.publicbuilding.service.police.BigPoliceStation;
import buildings.publicbuilding.service.police.SmallPoliceStation;
import buildings.publicbuilding.transportation.Road;
import buildings.publicbuilding.transportation.Street;
import city.City;

public abstract class Buildable {
    protected City city;

    private int crimeRate;

    abstract public int getWidth();

    abstract public int getLength();

    public abstract int getPrice();

    public int getCrimeRate() {
        return crimeRate;
    }

    public abstract void setCrimeRate(int crimeRateReduction);

    static public List<Class<? extends Buildable>> buildables = List.of(
            SmallHouse.class,
            Hospital.class,
            Clinic.class,
            Road.class,
            Street.class,
            Hospital.class,
            BigPoliceStation.class,
            SmallPoliceStation.class,
            Factory.class,
            Warehouse.class,
            GroceryStore.class,
            Mall.class,
            Condominium.class,
            Skyrise.class,
            BankBranch.class);
    static public Map<Class<? extends Buildable>, Supplier<Buildable>> registry = new HashMap<>();
    static {
        for (Class<? extends Buildable> BuildableType : buildables) {
            try {
                Class.forName(BuildableType.getName());
            } catch (ClassNotFoundException e) {
            }
        }
    }

    public static Buildable createBuilding(Class<? extends Buildable> Class) throws UnregisteredBuildingType {
        Supplier<Buildable> supplier = registry.get(Class);
        if (supplier == null)
            throw new UnregisteredBuildingType(Class);
        return supplier.get();
    }

    public static class UnregisteredBuildingType extends Exception {
        public UnregisteredBuildingType(Class<? extends Buildable> Class) {
            super("Unregistered Buildable subclass: " + Class.getCanonicalName());
        }
    }
}
