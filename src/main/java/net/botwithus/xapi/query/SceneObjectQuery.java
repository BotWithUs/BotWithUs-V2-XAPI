package net.botwithus.xapi.query;

import com.botwithus.bot.api.entities.SceneObject;
import com.botwithus.bot.api.entities.SceneObjects;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.EntityResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SceneObjectQuery implements Query<SceneObject, EntityResultSet<SceneObject>> {

    private Predicate<SceneObject> filter = object -> true;

    public static SceneObjectQuery newQuery() {
        return new SceneObjectQuery();
    }

    public SceneObjectQuery typeId(int... typeIds) {
        filter = filter.and(object -> matchesAny(object.typeId(), typeIds));
        return this;
    }

    public SceneObjectQuery animation(int... animations) {
        return this;
    }

    public SceneObjectQuery hidden(boolean hidden) {
        filter = filter.and(object -> object.isHidden() == hidden);
        return this;
    }

    public SceneObjectQuery name(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        filter = filter.and(object -> matchesAny(object.name(), matcher, names));
        return this;
    }

    public SceneObjectQuery name(String... names) {
        return name(String::contentEquals, names);
    }

    public SceneObjectQuery name(Pattern... patterns) {
        filter = filter.and(object -> matchesAny(object.name(), patterns));
        return this;
    }

    public SceneObjectQuery option(BiFunction<String, CharSequence, Boolean> matcher, String... options) {
        filter = filter.and(object -> object.getOptions().stream().anyMatch(option -> matchesAny(option, matcher, options)));
        return this;
    }

    public SceneObjectQuery option(String... options) {
        return option(String::contentEquals, options);
    }

    public SceneObjectQuery option(Pattern... patterns) {
        filter = filter.and(object -> object.getOptions().stream().anyMatch(option -> matchesAny(option, patterns)));
        return this;
    }

    public SceneObjectQuery and(SceneObjectQuery other) {
        filter = filter.and(other.filter);
        return this;
    }

    public SceneObjectQuery or(SceneObjectQuery other) {
        filter = filter.or(other.filter);
        return this;
    }

    public SceneObjectQuery invert() {
        filter = filter.negate();
        return this;
    }

    @Override
    public EntityResultSet<SceneObject> results() {
        List<SceneObject> results = new ArrayList<>(new SceneObjects(XApi.api()).query().all());
        results.removeIf(filter.negate());
        results.sort((a, b) -> Integer.compare(a.distanceToPlayer(), b.distanceToPlayer()));
        return new EntityResultSet<>(results);
    }

    @Override
    public Iterator<SceneObject> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(SceneObject sceneObject) {
        return filter.test(sceneObject);
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
