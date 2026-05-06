package buildings;

import java.util.ArrayList;
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
import buildings.publicbuilding.service.PublicServiceBuilding;
import buildings.publicbuilding.service.healthcare.Clinic;
import buildings.publicbuilding.service.healthcare.Hospital;
import buildings.publicbuilding.service.police.BigPoliceStation;
import buildings.publicbuilding.service.police.PoliceStation;
import buildings.publicbuilding.service.police.SmallPoliceStation;
import buildings.publicbuilding.transportation.Road;
import buildings.publicbuilding.transportation.Street;
import city.Citizen;
import city.City;
import utils.Point;

public abstract class Buildable {
    private int crimeRate;

    protected List<Citizen> visitors = new ArrayList<>();

    public List<Citizen> getVisitors() {
        return visitors;
    }

    public void addVisitor(Citizen citizen) {
        visitors.add(citizen);
    }

    public void removeVisitor(Citizen citizen) {
        visitors.remove(citizen);
    }

    public void runSimulationTick(City city) {
        final int MAX_CRIME_RATE_DENSITY = 60;
        final int PERSENTAGE = 100;
        final int INCREASE_SPEED = 10;
        crimeRate += Math
                .ceil((getVisitors().size() / (double) MAX_CRIME_RATE_DENSITY - crimeRate / 100.0) / INCREASE_SPEED
                        * PERSENTAGE);
        crimeRate = Math.clamp(crimeRate, 0, 100);
        Point locationPoint = city.grid.getBuildingOrigin(this);
        int policeCoverage = PublicServiceBuilding.getFieldFunctionForPublicServiceType(city, PoliceStation.class)
                .apply(locationPoint).intValue();
        crimeRate -= (int) Math.ceilDiv(Math.max(crimeRate, policeCoverage) - crimeRate, 4);
        crimeRate = Math.clamp(crimeRate, 0, 100);
    }

    abstract public int getWidth();

    abstract public int getLength();

    public abstract int getPrice();

    private boolean isDestroyed = true;

    public void destroy() {
        this.isDestroyed = true;
        for (Citizen visitor : visitors) {
            visitor.location = visitor.home;
        }
    }

    public boolean getIsDestroyed() {
        return this.isDestroyed;
    }

    public int getCrimeRate() {
        return crimeRate;
    }

    public Map<String, String> getDetailedInfo() {
        HashMap<String, String> details = new HashMap<>();

        details.put("name", this.getClass().getSimpleName());
        details.put("width", Integer.toString(this.getWidth()));
        details.put("length", Integer.toString(this.getLength()));
        details.put("crime rate", Integer.toString(this.getCrimeRate()));
        details.put("price", Integer.toString(this.getPrice()));
        details.put("present citizens", Integer.toString(getVisitors().size()));
        return details;
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
