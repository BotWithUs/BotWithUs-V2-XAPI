package net.botwithus.xapi.util;

import net.botwithus.rs3.world.Area;
import net.botwithus.rs3.world.Distance;
import net.botwithus.rs3.world.Locatable;

public class BwuDistance {
    public static boolean isLocatableCloser(Locatable primary, Locatable secondary, Locatable destination) {
        var primaryToDestination = Distance.between(primary, destination);
        var secondaryToDestination = Distance.between(secondary, destination);
        return primaryToDestination >= secondaryToDestination;
    }

    public static boolean isLocatableBetween(Locatable primary, Locatable secondary, Locatable destination) {
        if (primary == null || secondary == null || destination == null || primary.getCoordinate() == null || secondary.getCoordinate() == null || destination.getCoordinate() == null)
            return false;

        var center = new Area.Rectangular(primary.getCoordinate(), destination.getCoordinate()).getCentroid();
        if (center == null)
            return false;

        var primaryToCenter = Distance.between(primary, center);
        var secondaryToCenter = Distance.between(secondary, center);
        return primaryToCenter >= secondaryToCenter;
    }
}