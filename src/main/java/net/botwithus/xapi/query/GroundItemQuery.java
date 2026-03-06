package net.botwithus.xapi.query;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.entities.GroundItems;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.ResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GroundItemQuery implements Query<GroundItems.Entry, ResultSet<GroundItems.Entry>> {

    private final GameAPI api;
    private Predicate<GroundItems.Entry> filter = item -> true;

    private GroundItemQuery(GameAPI api) {
        this.api = api;
    }

    public static GroundItemQuery newQuery() {
        return new GroundItemQuery(XApi.api());
    }

    public static GroundItemQuery newQuery(GameAPI api) {
        return new GroundItemQuery(api);
    }

    public GroundItemQuery id(int... ids) {
        filter = filter.and(item -> matchesAny(item.itemId(), ids));
        return this;
    }

    public GroundItemQuery quantity(BiFunction<Integer, Integer, Boolean> matcher, int quantity) {
        filter = filter.and(item -> Boolean.TRUE.equals(matcher.apply(item.quantity(), quantity)));
        return this;
    }

    public GroundItemQuery quantity(int quantity) {
        return quantity(Integer::equals, quantity);
    }

    public GroundItemQuery category(int... categories) {
        filter = filter.and(item -> matchesAny(api.getItemType(item.itemId()).category(), categories));
        return this;
    }

    public GroundItemQuery name(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        filter = filter.and(item -> matchesAny(item.name(), matcher, names));
        return this;
    }

    public GroundItemQuery name(String... names) {
        return name(String::contentEquals, names);
    }

    public GroundItemQuery name(Pattern... patterns) {
        filter = filter.and(item -> matchesAny(item.name(), patterns));
        return this;
    }

    public GroundItemQuery distance(double distance) {
        filter = filter.and(item -> item.distanceToPlayer() <= distance);
        return this;
    }

    public GroundItemQuery valid(boolean valid) {
        filter = filter.and(item -> valid);
        return this;
    }

    public GroundItemQuery and(GroundItemQuery other) {
        filter = filter.and(other.filter);
        return this;
    }

    public GroundItemQuery or(GroundItemQuery other) {
        filter = filter.or(other.filter);
        return this;
    }

    public GroundItemQuery invert() {
        filter = filter.negate();
        return this;
    }

    @Override
    public ResultSet<GroundItems.Entry> results() {
        List<GroundItems.Entry> results = new ArrayList<>(new GroundItems(api).query().all());
        results.removeIf(filter.negate());
        results.sort((a, b) -> Integer.compare(a.distanceToPlayer(), b.distanceToPlayer()));
        return new ResultSet<>(results);
    }

    @Override
    public Iterator<GroundItems.Entry> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(GroundItems.Entry groundItem) {
        return filter.test(groundItem);
    }

    private static boolean matchesAny(int actual, int... expected) {
        for (int value : expected) {
            if (value == actual) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAny(String actual, BiFunction<String, CharSequence, Boolean> matcher, String... expected) {
        if (actual == null) {
            return false;
        }
        for (String value : expected) {
            if (value != null && Boolean.TRUE.equals(matcher.apply(actual, value))) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAny(String actual, Pattern... patterns) {
        if (actual == null) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern != null && pattern.matcher(actual).matches()) {
                return true;
            }
        }
        return false;
    }
}
