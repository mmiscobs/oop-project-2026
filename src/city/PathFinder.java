package city;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import buildings.Buildable;
import buildings.publicbuilding.transportation.PublicTransportation;
import utils.Point;

public class PathFinder {
    private CityGrid grid;

    PathFinder(CityGrid grid) {
        this.grid = grid;
    }

    private Map<Point, Buildable> tileIndexCache;
    private Map<Buildable, Point> originCache;
    private Map<Buildable, List<Buildable>> adjacencyCache;
    private Map<Buildable, Map<Buildable, Buildable>> flowFieldCache;

    public void invalidateCache() {
        tileIndexCache = null;
        originCache = null;
        adjacencyCache = null;
        flowFieldCache = null;
    }

    public Buildable buildingAt(Point p) {
        return tileIndex().get(p);
    }

    public Buildable nextStep(Buildable from, Buildable to) {
        if (from == to)
            return to;
        Map<Buildable, Buildable> field = flowFieldTo(to);
        return field.get(from);
    }

    public Deque<Buildable> findPath(Buildable from, Buildable to) {
        if (from == to)
            return new ArrayDeque<>();
        Map<Buildable, Buildable> field = flowFieldTo(to);
        if (!field.containsKey(from))
            return null;

        Deque<Buildable> path = new ArrayDeque<>();
        Buildable cursor = from;
        while (cursor != to) {
            cursor = field.get(cursor);
            path.add(cursor);
        }
        return path;
    }

    public boolean isStillWalkable(Buildable b, Buildable target) {
        return b == target || b instanceof PublicTransportation;
    }

    private Map<Point, Buildable> tileIndex() {
        if (tileIndexCache != null)
            return tileIndexCache;
        Map<Point, Buildable> ti = new HashMap<>();
        for (Map.Entry<Point, Buildable> e : grid.buildings.entrySet()) {
            Point o = e.getKey();
            Buildable b = e.getValue();
            for (int dx = 0; dx < b.getWidth(); dx++) {
                for (int dy = 0; dy < b.getLength(); dy++) {
                    ti.put(new Point(o.x + dx, o.y + dy), b);
                }
            }
        }
        return tileIndexCache = ti;
    }

    private Map<Buildable, Point> origins() {
        if (originCache != null)
            return originCache;
        Map<Buildable, Point> map = new IdentityHashMap<>();
        for (Map.Entry<Point, Buildable> e : grid.buildings.entrySet()) {
            map.put(e.getValue(), e.getKey());
        }
        return originCache = map;
    }

    private Map<Buildable, List<Buildable>> adjacency() {
        if (adjacencyCache != null)
            return adjacencyCache;
        Map<Point, Buildable> ti = tileIndex();
        Map<Buildable, Point> orig = origins();
        Map<Buildable, List<Buildable>> adj = new IdentityHashMap<>();

        for (Buildable b : orig.keySet()) {
            Set<Buildable> neighbors = Collections.newSetFromMap(new IdentityHashMap<>());
            Point o = orig.get(b);
            int w = b.getWidth(), l = b.getLength();
            for (int dx = 0; dx < w; dx++) {
                for (int dy = 0; dy < l; dy++) {
                    boolean onEdge = dx == 0 || dy == 0 || dx == w - 1 || dy == l - 1;
                    if (!onEdge)
                        continue;
                    int x = o.x + dx, y = o.y + dy;
                    addNeighbor(ti, new Point(x + 1, y), b, neighbors);
                    addNeighbor(ti, new Point(x - 1, y), b, neighbors);
                    addNeighbor(ti, new Point(x, y + 1), b, neighbors);
                    addNeighbor(ti, new Point(x, y - 1), b, neighbors);
                }
            }
            adj.put(b, new ArrayList<>(neighbors));
        }
        return adjacencyCache = adj;
    }

    private void addNeighbor(Map<Point, Buildable> ti, Point p,
            Buildable self, Set<Buildable> out) {
        Buildable a = ti.get(p);
        if (a != null && a != self)
            out.add(a);
    }

    private Map<Buildable, Buildable> flowFieldTo(Buildable target) {
        if (flowFieldCache == null)
            flowFieldCache = new IdentityHashMap<>();
        Map<Buildable, Buildable> cached = flowFieldCache.get(target);
        if (cached != null)
            return cached;

        Map<Buildable, List<Buildable>> adj = adjacency();
        Map<Buildable, Buildable> next = new IdentityHashMap<>();
        Deque<Buildable> queue = new ArrayDeque<>();
        queue.add(target);
        next.put(target, target);

        while (!queue.isEmpty()) {
            Buildable b = queue.poll();
            for (Buildable a : adj.getOrDefault(b, List.of())) {
                if (next.containsKey(a))
                    continue;
                next.put(a, b);
                if (a instanceof PublicTransportation) {
                    queue.add(a);
                }
            }
        }

        flowFieldCache.put(target, next);
        return next;
    }
}
