package buildings.publicbuilding.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import buildings.Buildable;
import buildings.publicbuilding.PublicBuilding;
import city.City;
import utils.Point;

import city.City;
import utils.SerializedBlob;

public abstract class PublicServiceBuilding extends PublicBuilding {
    public PublicServiceBuilding() {
        super();
    }

    protected PublicServiceBuilding(SerializedBlob blob, City city) {
        super(blob, city);
    }

    public abstract int getRange();

    public abstract Class<? extends PublicServiceBuilding> getPublicServiceTypeClass();

    @Override
    public Map<String, String> getDetailedInfo() {
        Map<String, String> details = super.getDetailedInfo();

        details.put("range", Integer.toString(getRange()));
        return details;
    }

    public static Function<Point, Double> getFieldFunctionForPublicServiceType(City city,
            Class<? extends PublicServiceBuilding> publicServiceType) {
        Map<Point, PublicServiceBuilding> publicServiceTypeBuildings = getAllPublicServiceTypeBuildings(city,
                publicServiceType);

        return getCombinedFieldFunctionForBuildings(publicServiceTypeBuildings);
    }

    public static Map<Point, PublicServiceBuilding> getAllPublicServiceTypeBuildings(City city,
            Class<? extends PublicServiceBuilding> publicServiceType) {
        HashMap<Point, PublicServiceBuilding> buildings = new HashMap<>();
        for (Entry<Point, Buildable> otherBuildingEntry : city.grid.buildings.entrySet()) {
            if (otherBuildingEntry.getValue() instanceof PublicServiceBuilding otherService
                    && publicServiceType == otherService.getPublicServiceTypeClass()) {
                buildings.put(otherBuildingEntry.getKey(), otherService);
            }
        }
        return buildings;
    }

    public static Function<Point, Double> getCombinedFieldFunctionForBuildings(
            Map<Point, PublicServiceBuilding> buildings) {

        ArrayList<Function<Point, Double>> influenceFields = new ArrayList<>();
        for (Entry<Point, PublicServiceBuilding> otherBuildingEntry : buildings.entrySet()) {
            influenceFields.add(PublicServiceBuilding.getFieldFunctionForBuilding(otherBuildingEntry.getKey(),
                    otherBuildingEntry.getValue()));
        }
        return t -> Math.clamp(
                influenceFields.stream().map(f -> f.apply(t)).reduce(0.0, Double::sum),
                0, 255.0);
    }

    public static Function<Point, Double> getFieldFunctionForBuilding(Point origin, PublicServiceBuilding building) {
        return t -> {
            double range = building.getRange();
            double distance = t.distFrom(origin);
            double val = Math.clamp(1 - distance / range, 0, 1);
            return Math.clamp(val * 255, 0, 255);
        };
    }
}
