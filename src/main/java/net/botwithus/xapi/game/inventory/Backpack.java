package net.botwithus.xapi.game.inventory;

import com.botwithus.bot.api.inventory.ActionTypes;
import com.botwithus.bot.api.model.Component;
import com.botwithus.bot.api.model.GameAction;
import com.botwithus.bot.api.model.InventoryItem;
import net.botwithus.xapi.XApi;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public final class Backpack {

    public static final int INVENTORY_ID = com.botwithus.bot.api.inventory.Backpack.INVENTORY_ID;
    public static final int INTERFACE_ID = com.botwithus.bot.api.inventory.Backpack.INTERFACE_ID;
    public static final int COMPONENT_ID = com.botwithus.bot.api.inventory.Backpack.COMPONENT_ID;

    private Backpack() {
    }

    public static boolean isFull() {
        return container().isFull();
    }

    public static boolean isEmpty() {
        return container().isEmpty();
    }

    public static List<InventoryItem> getItems() {
        return container().getItems();
    }

    public static boolean contains(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        return getItems().stream()
                .map(item -> XApi.api().getItemType(item.itemId()).name())
                .filter(Objects::nonNull)
                .anyMatch(name -> Arrays.stream(names).filter(Objects::nonNull).anyMatch(candidate -> Boolean.TRUE.equals(matcher.apply(name, candidate))));
    }

    public static boolean contains(String... names) {
        return contains(String::contentEquals, names);
    }

    public static boolean contains(int... ids) {
        return getItems().stream().anyMatch(item -> contains(ids, item.itemId()));
    }

    public static boolean contains(Pattern... patterns) {
        return getItems().stream()
                .map(item -> XApi.api().getItemType(item.itemId()).name())
                .anyMatch(name -> matches(name, patterns));
    }

    public static InventoryItem getItem(String... names) {
        return getItems().stream()
                .filter(item -> contains(names, XApi.api().getItemType(item.itemId()).name()))
                .findFirst()
                .orElse(null);
    }

    public static InventoryItem getItem(int... ids) {
        return getItems().stream()
                .filter(item -> contains(ids, item.itemId()))
                .findFirst()
                .orElse(null);
    }

    public static List<InventoryItem> getItemsWithOption(String option) {
        return getItems().stream()
                .filter(item -> XApi.api().getItemType(item.itemId()).inventoryOptions().stream()
                        .anyMatch(candidate -> candidate != null && candidate.equalsIgnoreCase(option)))
                .toList();
    }

    public static boolean interact(String itemName, String option) {
        InventoryItem item = getItem(itemName);
        return item != null && interact(item.itemId(), option);
    }

    public static boolean interact(int itemId, String option) {
        Component component = findComponentByItem(itemId);
        if (component == null) {
            return false;
        }
        List<String> options = XApi.api().getComponentOptions(component.interfaceId(), component.componentId());
        for (int i = 0; i < options.size(); i++) {
            if (option.equalsIgnoreCase(options.get(i))) {
                XApi.api().queueAction(new GameAction(ActionTypes.COMPONENT, i + 1, component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
                return true;
            }
        }
        return false;
    }

    public static boolean dragComponent(Component fromComponent, Component toComponent) {
        if (fromComponent == null || toComponent == null) {
            return false;
        }
        XApi.api().queueAction(new GameAction(ActionTypes.COMPONENT_DRAG, fromComponent.subComponentId(), fromComponent.componentId(), fromComponent.interfaceId() << 16 | fromComponent.componentId()));
        XApi.api().queueAction(new GameAction(ActionTypes.COMPONENT, 1, toComponent.subComponentId(), toComponent.interfaceId() << 16 | toComponent.componentId()));
        return true;
    }

    private static com.botwithus.bot.api.inventory.Backpack container() {
        return new com.botwithus.bot.api.inventory.Backpack(XApi.api());
    }

    private static Component findComponentByItem(int itemId) {
        List<Component> components = XApi.api().queryComponents(com.botwithus.bot.api.query.ComponentFilter.builder()
                .interfaceId(INTERFACE_ID)
                .itemId(itemId)
                .build());
        return components.isEmpty() ? null : components.getFirst();
    }

    private static boolean contains(int[] values, int actual) {
        for (int value : values) {
            if (value == actual) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(String[] values, String actual) {
        for (String value : values) {
            if (value != null && value.equals(actual)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String value, Pattern... patterns) {
        if (value == null) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern != null && pattern.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }
}
