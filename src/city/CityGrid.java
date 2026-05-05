package city;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import buildings.Buildable;
import utils.Point;

public class CityGrid {
    private City city;
    public Map<Point, Buildable> buildings;
    public final int sizeX;
    public final int sizeY;
    private PathFinder pathFinder = new PathFinder(this);

    public CityGrid(City city, int sizeX, int sizeY) {
        this.city = city;
        this.buildings = new HashMap<>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public Buildable getNextStepFromTo(Buildable from, Buildable to) {
        return pathFinder.nextStep(from, to);
    }

    public Buildable getBuildingAt(Point place) {
        for (Entry<Point, Buildable> entry : buildings.entrySet())
            if (place.isWithin(entry.getKey(), entry.getValue().getLength(), entry.getValue().getWidth()))
                return entry.getValue();
        return null;
    }

    public Point getBuildingOrigin(Buildable building) {
        for (Entry<Point, Buildable> entry : buildings.entrySet())
            if (entry.getValue() == building)
                return entry.getKey();
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
        pathFinder.invalidateCache();
    }

    public void removeBuildingAt(Point place) {
        Iterator<Entry<Point, Buildable>> iterator = buildings.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Point, Buildable> entry = iterator.next();
            if (place.isWithin(entry.getKey(), entry.getValue().getLength(), entry.getValue().getWidth()))
                iterator.remove();
        }
        pathFinder.invalidateCache();
    }
}
