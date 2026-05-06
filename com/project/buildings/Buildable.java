package com.project.buildings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.project.buildings.privatebuilding.residential.Condominium;
import com.project.buildings.privatebuilding.residential.SmallHouse;
import com.project.buildings.privatebuilding.workplace.commercial.GroceryStore;
import com.project.buildings.privatebuilding.workplace.commercial.Mall;
import com.project.buildings.privatebuilding.workplace.industrial.Factory;
import com.project.buildings.privatebuilding.workplace.industrial.Warehouse;
import com.project.buildings.privatebuilding.workplace.office.BankBranch;
import com.project.buildings.privatebuilding.workplace.office.Skyrise;
import com.project.buildings.publicbuilding.service.PublicServiceBuilding;
import com.project.buildings.publicbuilding.service.healthcare.Clinic;
import com.project.buildings.publicbuilding.service.healthcare.Hospital;
import com.project.buildings.publicbuilding.service.police.BigPoliceStation;
import com.project.buildings.publicbuilding.service.police.PoliceStation;
import com.project.buildings.publicbuilding.service.police.SmallPoliceStation;
import com.project.buildings.publicbuilding.transportation.Road;
import com.project.buildings.publicbuilding.transportation.Street;
import com.project.city.Citizen;
import com.project.city.City;
import com.project.utils.Point;
import com.project.utils.SerializedBlob;

public abstract class Buildable {
    private int crimeRate;
    public final String uuid;

    public Buildable() {
        uuid = UUID.randomUUID().toString();
    }

    protected Buildable(SerializedBlob blob, City city) {
        uuid = blob.map().get("uuid").string();
        visitors.addAll(blob.map().get("visitors").array().stream().map(b -> Citizen.fromBlob(b, city)).toList());
        crimeRate = blob.map().get("crimeRate").intValue();
        isDestroyed = blob.map().get("isDestroyed").booleanValue();
    }

    public SerializedBlob toBlob() {
        return SerializedBlob.fromMap(Map.of(
                "type", SerializedBlob.string(getClass().getSimpleName()),
                "uuid", SerializedBlob.string(uuid),
                "visitors", SerializedBlob.array(visitors.stream().map(v -> v.toBlob()).toList()),
                "crimeRate", SerializedBlob.intValue(crimeRate),
                "isDestroyed", SerializedBlob.booleanValue(isDestroyed)));
    }

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
            visitor.location = visitor.home.upcast();
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
    static public Map<Class<? extends Buildable>, BiFunction<SerializedBlob, City, Buildable>> blobRegistry = new HashMap<>();
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

    public static class BuildableRef<T extends Buildable> {
        private final City city;
        private final String buildableId;
        private final String buildableType;

        public BuildableRef(Buildable buildable, City city) {
            if (buildable != null) {
                buildableId = buildable.uuid;
                buildableType = buildable.getClass().getSimpleName();
            } else {
                buildableId = null;
                buildableType = null;
            }
            this.city = city;
            cache.put(this, buildable);
        }

        public SerializedBlob toBlob() {
            return SerializedBlob.fromMap(
                    Map.of("id", SerializedBlob.string(buildableId), "type", SerializedBlob.string(buildableType)));
        }

        private static Map<BuildableRef<?>, Buildable> cache = new WeakHashMap<>();

        public BuildableRef(SerializedBlob blob, City city) {
            this.buildableId = blob.map().get("id").string();
            this.buildableType = blob.map().get("type").string();
            this.city = city;
        }

        public T get() {
            if (cache.containsKey(this))
                return (T) cache.get(this);
            if (buildableId == null)
                return null;
            for (Buildable buildable : city.grid.buildings.values())
                if (buildable.uuid.equals(buildableId) && buildable.getClass().getSimpleName().equals(buildableType)) {
                    cache.put(this, buildable);
                    return (T) buildable;
                }
            return null;
        }

        public BuildableRef<Buildable> upcast() {
            return (BuildableRef<Buildable>) this;
        }
    }

    public static Buildable fromBlob(SerializedBlob serializedBlob, City city) {
        for (Entry<Class<? extends Buildable>, BiFunction<SerializedBlob, City, Buildable>> entry : blobRegistry
                .entrySet()) {
            if (entry.getKey().getSimpleName().equals(serializedBlob.map().get("type").string())) {
                return entry.getValue().apply(serializedBlob, city);
            }
        }
        return null;
    }
}
