package net.botwithus.xapi.query;

import com.botwithus.bot.api.model.InventoryItem;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.base.Query;
import net.botwithus.xapi.query.result.ResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class InventoryItemQuery implements Query<InventoryItem, ResultSet<InventoryItem>> {

    private final int[] inventoryIds;
    private Predicate<InventoryItem> filter = item -> true;

    public InventoryItemQuery(int... inventoryIds) {
        this.inventoryIds = inventoryIds;
    }

    public static InventoryItemQuery newQuery(int... inventoryIds) {
        return new InventoryItemQuery(inventoryIds);
    }

    public InventoryItemQuery id(int... ids) {
        filter = filter.and(item -> {
            for (int id : ids) {
                if (item.itemId() == id) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    public InventoryItemQuery slot(int... slots) {
        filter = filter.and(item -> {
            for (int slot : slots) {
                if (item.slot() == slot) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    public InventoryItemQuery name(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        filter = filter.and(item -> {
            String name = item.itemId() > -1 ? XApi.api().getItemType(item.itemId()).name() : null;
            if (name == null) {
                return false;
            }
            for (String expected : names) {
                if (expected != null && Boolean.TRUE.equals(matcher.apply(name, expected))) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    public InventoryItemQuery name(String... names) {
        return name(String::contentEquals, names);
    }

    public InventoryItemQuery name(Pattern... patterns) {
        filter = filter.and(item -> {
            String name = item.itemId() > -1 ? XApi.api().getItemType(item.itemId()).name() : null;
            if (name == null) {
                return false;
            }
            for (Pattern pattern : patterns) {
                if (pattern != null && pattern.matcher(name).matches()) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    @Override
    public ResultSet<InventoryItem> results() {
        List<InventoryItem> items = new ArrayList<>();
        for (int inventoryId : inventoryIds) {
            items.addAll(XApi.api().queryInventoryItems(com.botwithus.bot.api.query.InventoryFilter.builder()
                    .inventoryId(inventoryId)
                    .nonEmpty(false)
                    .build()));
        }
        items.removeIf(filter.negate());
        return new ResultSet<>(items);
    }

    @Override
    public Iterator<InventoryItem> iterator() {
        return results().iterator();
    }

    @Override
    public boolean test(InventoryItem inventoryItem) {
        return filter.test(inventoryItem);
    }
}
