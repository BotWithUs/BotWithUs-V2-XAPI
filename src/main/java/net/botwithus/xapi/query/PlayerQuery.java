package net.botwithus.xapi.query;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.entities.Player;
import com.botwithus.bot.api.entities.Players;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.EntityResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PlayerQuery implements Query<Player, EntityResultSet<Player>> {

    private final GameAPI api;
    private Predicate<Player> filter = player -> true;

    private PlayerQuery(GameAPI api) {
        this.api = api;
    }

    public static PlayerQuery newQuery() {
        return new PlayerQuery(XApi.api());
    }

    public static PlayerQuery newQuery(GameAPI api) {
        return new PlayerQuery(api);
    }

    public PlayerQuery name(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        filter = filter.and(player -> matchesAny(player.name(), matcher, names));
        return this;
    }

    public PlayerQuery name(String... names) {
        return name(String::contentEquals, names);
    }

    public PlayerQuery name(Pattern... patterns) {
        filter = filter.and(player -> matchesAny(player.name(), patterns));
        return this;
    }

    public PlayerQuery index(int... indices) {
        filter = filter.and(player -> matchesAny(player.serverIndex(), indices));
        return this;
    }

    public PlayerQuery isMoving(boolean moving) {
        filter = filter.and(player -> player.isMoving() == moving);
        return this;
    }

    public PlayerQuery animationId(int... animationIds) {
        filter = filter.and(player -> matchesAny(player.getAnimation(), animationIds));
        return this;
    }

    public PlayerQuery health(int min, int max) {
        filter = filter.and(player -> {
            int hp = player.getHealth();
            return hp >= min && hp <= max;
        });
        return this;
    }

    public PlayerQuery and(PlayerQuery other) {
        filter = filter.and(other.filter);
        return this;
    }

    public PlayerQuery or(PlayerQuery other) {
        filter = filter.or(other.filter);
        return this;
    }

    public PlayerQuery invert() {
        filter = filter.negate();
        return this;
    }

    @Override
    public EntityResultSet<Player> results() {
        List<Player> results = new ArrayList<>(new Players(api).query().all());
        results.removeIf(filter.negate());
        results.sort((a, b) -> Integer.compare(a.distanceToPlayer(), b.distanceToPlayer()));
        return new EntityResultSet<>(results);
    }

    @Override
    public Iterator<Player> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(Player player) {
        return filter.test(player);
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
