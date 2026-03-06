package net.botwithus.xapi.util;

import net.botwithus.xapi.util.position.Positionable;

public final class BwuDistance {

    private BwuDistance() {
    }

    public static boolean isLocatableCloser(Positionable primary, Positionable secondary, Positionable destination) {
        return distance(primary, destination) >= distance(secondary, destination);
    }

    public static boolean isLocatableBetween(Positionable primary, Positionable secondary, Positionable destination) {
        if (primary == null || secondary == null || destination == null) {
            return false;
        }
        int centerX = (primary.x() + destination.x()) / 2;
        int centerY = (primary.y() + destination.y()) / 2;
        return chebyshev(primary.x(), primary.y(), centerX, centerY)
                >= chebyshev(secondary.x(), secondary.y(), centerX, centerY);
    }

    public static int distance(Positionable a, Positionable b) {
        return chebyshev(a.x(), a.y(), b.x(), b.y());
    }

    public static int chebyshev(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
}
