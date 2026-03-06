package net.botwithus.xapi.query;

import com.botwithus.bot.api.model.InventoryInfo;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.ResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class InventoryQuery implements Query<InventoryInfo, ResultSet<InventoryInfo>> {

    private final int[] ids;
    private Predicate<InventoryInfo> filter;

    public InventoryQuery(int... ids) {
        this.ids = ids;
        this.filter = info -> ids.length == 0 || contains(ids, info.inventoryId());
    }

    public static InventoryQuery newQuery(int... ids) {
        return new InventoryQuery(ids);
    }

    public InventoryQuery isFull(boolean full) {
        filter = filter.and(info -> (info.capacity() > 0 && info.itemCount() >= info.capacity()) == full);
        return this;
    }

    public InventoryQuery freeSlots(BiFunction<Integer, Integer, Boolean> matcher, int slots) {
        filter = filter.and(info -> Boolean.TRUE.equals(matcher.apply(info.capacity() - info.itemCount(), slots)));
        return this;
    }

    public InventoryQuery freeSlots(int slots) {
        return freeSlots((actual, expected) -> actual >= expected, slots);
    }

    public InventoryQuery contains(int... itemIds) {
        filter = filter.and(info -> {
            for (int itemId : itemIds) {
                if (!XApi.api().queryInventoryItems(com.botwithus.bot.api.query.InventoryFilter.builder()
                        .inventoryId(info.inventoryId())
                        .itemId(itemId)
                        .nonEmpty(true)
                        .maxResults(1)
                        .build()).isEmpty()) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    public InventoryQuery containsAll(int... itemIds) {
        filter = filter.and(info -> {
            for (int itemId : itemIds) {
                if (XApi.api().queryInventoryItems(com.botwithus.bot.api.query.InventoryFilter.builder()
                        .inventoryId(info.inventoryId())
                        .itemId(itemId)
                        .nonEmpty(true)
                        .maxResults(1)
                        .build()).isEmpty()) {
                    return false;
                }
            }
            return true;
        });
        return this;
    }

    public InventoryQuery contains(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        filter = filter.and(info -> XApi.api().queryInventoryItems(com.botwithus.bot.api.query.InventoryFilter.builder()
                        .inventoryId(info.inventoryId())
                        .nonEmpty(true)
                        .build()).stream()
                .map(item -> XApi.api().getItemType(item.itemId()).name())
                .anyMatch(name -> {
                    for (String candidate : names) {
                        if (candidate != null && Boolean.TRUE.equals(matcher.apply(name, candidate))) {
                            return true;
                        }
                    }
                    return false;
                }));
        return this;
    }

    public InventoryQuery contains(String... names) {
        return contains(String::contentEquals, names);
    }

    public InventoryQuery containsCategory(int... categories) {
        filter = filter.and(info -> XApi.api().queryInventoryItems(com.botwithus.bot.api.query.InventoryFilter.builder()
                        .inventoryId(info.inventoryId())
                        .nonEmpty(true)
                        .build()).stream()
                .map(item -> XApi.api().getItemType(item.itemId()).category())
                .anyMatch(category -> contains(categories, category)));
        return this;
    }

    public InventoryQuery containsAllCategory(int... categories) {
        filter = filter.and(info -> {
            List<Integer> present = XApi.api().queryInventoryItems(com.botwithus.bot.api.query.InventoryFilter.builder()
                            .inventoryId(info.inventoryId())
                            .nonEmpty(true)
                            .build()).stream()
                    .map(item -> XApi.api().getItemType(item.itemId()).category())
                    .toList();
            for (int category : categories) {
                if (!present.contains(category)) {
                    return false;
                }
            }
            return true;
        });
        return this;
    }

    @Override
    public ResultSet<InventoryInfo> results() {
        List<InventoryInfo> results = new ArrayList<>(XApi.api().queryInventories());
        results.removeIf(filter.negate());
        return new ResultSet<>(results);
    }

    @Override
    public Iterator<InventoryInfo> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(InventoryInfo inventoryInfo) {
        return filter.test(inventoryInfo);
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
