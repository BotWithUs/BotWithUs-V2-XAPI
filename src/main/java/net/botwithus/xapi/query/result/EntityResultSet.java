package net.botwithus.xapi.query.result;

import com.botwithus.bot.api.entities.EntityContext;
import com.botwithus.bot.api.entities.GroundItems;
import com.botwithus.bot.api.model.LocalPlayer;
import net.botwithus.xapi.util.BwuDistance;
import net.botwithus.xapi.util.position.Positionable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityResultSet<T> extends ResultSet<T> {

    public EntityResultSet(List<T> results) {
        super(results);
    }

    public T nearestTo(Positionable other) {
        if (other == null || results.isEmpty()) {
            return null;
        }
        return results.stream()
                .min(Comparator.comparingInt(candidate -> BwuDistance.distance(asPositionable(candidate), other)))
                .orElse(null);
    }

    public T nearest() {
        return first();
    }

    public EntityResultSet<T> removeAll(ResultSet<T> set) {
        List<T> copy = new ArrayList<>(results);
        copy.removeAll(set.results);
        return new EntityResultSet<>(copy);
    }

    public EntityResultSet<T> remove(T toRemove) {
        List<T> copy = new ArrayList<>(results);
        copy.remove(toRemove);
        return new EntityResultSet<>(copy);
    }

    private static Positionable asPositionable(Object value) {
        if (value instanceof Positionable positionable) {
            return positionable;
        }
        if (value instanceof EntityContext entity) {
            return new Positionable() {
                @Override
                public int x() {
                    return entity.tileX();
                }

                @Override
                public int y() {
                    return entity.tileY();
                }

                @Override
                public int plane() {
                    return entity.plane();
                }
            };
        }
        if (value instanceof GroundItems.Entry entry) {
            return new Positionable() {
                @Override
                public int x() {
                    return entry.tileX();
                }

                @Override
                public int y() {
                    return entry.tileY();
                }

                @Override
                public int plane() {
                    return entry.plane();
                }
            };
        }
        if (value instanceof LocalPlayer player) {
            return new Positionable() {
                @Override
                public int x() {
                    return player.tileX();
                }

                @Override
                public int y() {
                    return player.tileY();
                }

                @Override
                public int plane() {
                    return player.plane();
                }
            };
        }
        throw new IllegalArgumentException("Unsupported entity result type: " + value);
    }
}
