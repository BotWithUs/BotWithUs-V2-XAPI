package net.botwithus.xapi.query;

import com.botwithus.bot.api.entities.Npc;
import com.botwithus.bot.api.entities.Npcs;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.EntityResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NpcQuery implements Query<Npc, EntityResultSet<Npc>> {

    private Predicate<Npc> filter = npc -> true;

    public static NpcQuery newQuery() {
        return new NpcQuery();
    }

    public NpcQuery index(int... indices) {
        filter = filter.and(npc -> matchesAny(npc.serverIndex(), indices));
        return this;
    }

    public NpcQuery typeId(int... ids) {
        filter = filter.and(npc -> matchesAny(npc.typeId(), ids));
        return this;
    }

    public NpcQuery name(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        filter = filter.and(npc -> matchesAny(npc.name(), matcher, names));
        return this;
    }

    public NpcQuery name(String... names) {
        return name(String::contentEquals, names);
    }

    public NpcQuery name(Pattern... patterns) {
        filter = filter.and(npc -> matchesAny(npc.name(), patterns));
        return this;
    }

    public NpcQuery option(BiFunction<String, CharSequence, Boolean> matcher, String... options) {
        filter = filter.and(npc -> npc.getOptions().stream().anyMatch(option -> matchesAny(option, matcher, options)));
        return this;
    }

    public NpcQuery option(String... options) {
        return option(String::contentEquals, options);
    }

    public NpcQuery option(Pattern... patterns) {
        filter = filter.and(npc -> npc.getOptions().stream().anyMatch(option -> matchesAny(option, patterns)));
        return this;
    }

    public NpcQuery overheadText(String... overheadTexts) {
        filter = filter.and(npc -> matchesAny(npc.getOverheadText(), String::contentEquals, overheadTexts));
        return this;
    }

    public NpcQuery isMoving(boolean moving) {
        filter = filter.and(npc -> npc.isMoving() == moving);
        return this;
    }

    public NpcQuery animationId(int... animationIds) {
        filter = filter.and(npc -> matchesAny(npc.getAnimation(), animationIds));
        return this;
    }

    public NpcQuery health(int min, int max) {
        filter = filter.and(npc -> {
            int hp = npc.getHealth();
            return hp >= min && hp <= max;
        });
        return this;
    }

    public NpcQuery and(NpcQuery other) {
        filter = filter.and(other.filter);
        return this;
    }

    public NpcQuery or(NpcQuery other) {
        filter = filter.or(other.filter);
        return this;
    }

    public NpcQuery invert() {
        filter = filter.negate();
        return this;
    }

    @Override
    public EntityResultSet<Npc> results() {
        List<Npc> results = new ArrayList<>(new Npcs(XApi.api()).query().all());
        results.removeIf(filter.negate());
        results.sort((a, b) -> Integer.compare(a.distanceToPlayer(), b.distanceToPlayer()));
        return new EntityResultSet<>(results);
    }

    @Override
    public Iterator<Npc> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(Npc npc) {
        return filter.test(npc);
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
