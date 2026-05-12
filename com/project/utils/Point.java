package com.project.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(SerializedBlob blob) {
        x = blob.map().get("x").intValue();
        y = blob.map().get("y").intValue();
    }

    public SerializedBlob toBlob(SerializedBlob.Factory Factory) {
        return Factory.fromMap(Map.of("x", Factory.intValue(x), "y", Factory.intValue(y)));
    }

    public double distFrom(Point other) {
        return Math.sqrt(Math.pow(y - other.y, 2) + Math.pow(x - other.x, 2));
    }

    public int hashCode() {
        return Integer.hashCode(x) + Integer.hashCode(y);
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && ((Point) obj).x == x && ((Point) obj).y == y;
    }

    public boolean isWithin(Point from, int l, int w) {
        return x >= from.x && y >= from.y && x < from.x + w && y < from.y + l;
    }

    public static Iterable<Point> allPointsWithin(Point from, int l, int w) {
        return new Iterable<Point>() {
            public Iterator<Point> iterator() {
                return new Iterator<Point>() {
                    private int progress = 0;
                    private final int end = l * w;

                    public boolean hasNext() {
                        return progress < end;
                    }

                    public Point next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        int dx = progress % l;
                        int dy = progress / l;
                        progress++;
                        return new Point(from.x + dx, from.y + dy);
                    }
                };
            }
        };
    }
}
