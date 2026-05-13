package com.project.city;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.project.buildings.Buildable;
import com.project.utils.Point;
import com.project.utils.SerializedBlob;

public class CityGrid {
    private final Map<Point, Buildable> buildings = new HashMap<>();
    public final Map<Point, Buildable> buildingsView = Collections.unmodifiableMap(buildings);
    public final int sizeX;
    public final int sizeY;
    private PathFinder pathFinder = new PathFinder(this);

    CityGrid(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public CityGrid(SerializedBlob blob, City city) {
        sizeX = blob.map().get("sizeX").intValue();
        sizeY = blob.map().get("sizeY").intValue();
        for (SerializedBlob entry : blob.map().get("buildings").array()) {
            buildings.put(new Point(entry.map().get("key")), Buildable.fromBlob(entry.map().get("value"), city));
        }
    }

    public SerializedBlob toBlob(SerializedBlob.Factory Factory) {
        return Factory.fromMap(Map.of(
                "sizeX", Factory.intValue(sizeX),
                "sizeY", Factory.intValue(sizeY),
                "buildings",
                Factory.array(buildings.entrySet().stream()
                        .map(e -> Factory
                                .fromMap(Map.of("key", e.getKey().toBlob(Factory), "value",
                                        e.getValue().toBlob(Factory))))
                        .toList())));
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
