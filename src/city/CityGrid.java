package city;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import buildings.Buildable;
import utils.Point;

public class CityGrid {
    private City city;
    public Map<Point, Buildable> buildings;
    private int sizeX;
    private int sizeY;

    public CityGrid(City city, int sizeX, int sizeY) {
        this.city = city;
        this.buildings = new HashMap<>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public Buildable getBuildingAt(Point place) {
        for (Entry<Point, Buildable> entry : buildings.entrySet())
            if (place.isWithin(entry.getKey(), entry.getValue().getLength(), entry.getValue().getWidth()))
                return entry.getValue();
        return null;
    }

    public void placeBuildingAt(Point place, Buildable building) {
        if (place.x > sizeX || place.y > sizeY)
            return;
        if (buildings.containsKey(place))
            return;
        for (Point point : Point.allPointsWithin(place, building.getLength(), building.getWidth())) {
            Buildable existingBuilding = getBuildingAt(point);
            if (existingBuilding != null)
                return;
        }
        buildings.put(place, building);
    }

    public void removeBuildingAt(Point place) {
        for (Entry<Point, Buildable> entry : buildings.entrySet())
            if (place.isWithin(entry.getKey(), entry.getValue().getLength(), entry.getValue().getWidth()))
                buildings.remove(entry.getKey());
    }

    public List<Citizen> getCitizensAt(Point place) {
        Buildable building = getBuildingAt(place);
        if (building == null)
            return List.of();
        return city.getCitizens().stream().filter(c -> c.getLocation() == building).toList();
    }
}
