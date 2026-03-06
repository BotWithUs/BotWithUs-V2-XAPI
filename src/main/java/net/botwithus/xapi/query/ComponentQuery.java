package net.botwithus.xapi.query;

import com.botwithus.bot.api.model.Component;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.ResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ComponentQuery implements Query<Component, ResultSet<Component>> {

    private final int[] interfaceIds;
    private Predicate<Component> filter;

    public ComponentQuery(int... interfaceIds) {
        this.interfaceIds = interfaceIds;
        this.filter = component -> interfaceIds.length == 0 || contains(interfaceIds, component.interfaceId());
    }

    public static ComponentQuery newQuery(int... interfaceIds) {
        return new ComponentQuery(interfaceIds);
    }

    public ComponentQuery id(int... ids) {
        filter = filter.and(component -> contains(ids, component.componentId()));
        return this;
    }

    public ComponentQuery subComponentId(int... ids) {
        filter = filter.and(component -> contains(ids, component.subComponentId()));
        return this;
    }

    public ComponentQuery type(int... types) {
        filter = filter.and(component -> contains(types, component.type()));
        return this;
    }

    public ComponentQuery hidden(boolean hidden) {
        return this;
    }

    public ComponentQuery itemId(int... itemIds) {
        filter = filter.and(component -> contains(itemIds, component.itemId()));
        return this;
    }

    public ComponentQuery itemAmount(int... amounts) {
        filter = filter.and(component -> contains(amounts, component.itemCount()));
        return this;
    }

    public ComponentQuery spriteId(int... spriteIds) {
        filter = filter.and(component -> contains(spriteIds, component.spriteId()));
        return this;
    }

    public ComponentQuery text(BiFunction<String, CharSequence, Boolean> matcher, String... text) {
        filter = filter.and(component -> {
            String value = XApi.api().getComponentText(component.interfaceId(), component.componentId());
            if (value == null) {
                return false;
            }
            for (String candidate : text) {
                if (candidate != null && Boolean.TRUE.equals(matcher.apply(value, candidate))) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    public ComponentQuery option(BiFunction<String, CharSequence, Boolean> matcher, String... options) {
        filter = filter.and(component -> XApi.api().getComponentOptions(component.interfaceId(), component.componentId()).stream()
                .anyMatch(option -> {
                    for (String candidate : options) {
                        if (candidate != null && Boolean.TRUE.equals(matcher.apply(option, candidate))) {
                            return true;
                        }
                    }
                    return false;
                }));
        return this;
    }

    public ComponentQuery option(String... options) {
        return option(String::contentEquals, options);
    }

    public ComponentQuery itemName(String name, BiFunction<String, CharSequence, Boolean> matcher) {
        filter = filter.and(component -> component.itemId() > -1
                && Boolean.TRUE.equals(matcher.apply(XApi.api().getItemType(component.itemId()).name(), name)));
        return this;
    }

    public ComponentQuery itemName(String name) {
        return itemName(name, String::contentEquals);
    }

    @Override
    public ResultSet<Component> results() {
        List<Component> results = new ArrayList<>();
        for (int interfaceId : interfaceIds) {
            results.addAll(XApi.api().queryComponents(com.botwithus.bot.api.query.ComponentFilter.builder()
                    .interfaceId(interfaceId)
                    .maxResults(500)
                    .build()));
        }
        results.removeIf(filter.negate());
        return new ResultSet<>(results);
    }

    @Override
    public Iterator<Component> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(Component component) {
        return filter.test(component);
    }

    private static boolean contains(int[] values, int actual) {
        for (int value : values) {
            if (value == actual) {
                return true;
            }
        }
        return false;
    }
}
