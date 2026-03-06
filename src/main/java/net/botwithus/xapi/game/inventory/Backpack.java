package net.botwithus.xapi.game.inventory;

import com.botwithus.bot.api.GameAPI;
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

    public static boolean isFull(GameAPI api) {
        return container(api).isFull();
    }

    public static boolean isFull() {
        return isFull(XApi.api());
    }

    public static boolean isEmpty(GameAPI api) {
        return container(api).isEmpty();
    }

    public static boolean isEmpty() {
        return isEmpty(XApi.api());
    }

    public static List<InventoryItem> getItems(GameAPI api) {
        return container(api).getItems();
    }

    public static List<InventoryItem> getItems() {
        return getItems(XApi.api());
    }

    public static boolean contains(GameAPI api, BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        return getItems(api).stream()
                .map(item -> api.getItemType(item.itemId()).name())
                .filter(Objects::nonNull)
                .anyMatch(name -> Arrays.stream(names).filter(Objects::nonNull)
                        .anyMatch(candidate -> Boolean.TRUE.equals(matcher.apply(name, candidate))));
    }

    public static boolean contains(BiFunction<String, CharSequence, Boolean> matcher, String... names) {
        return contains(XApi.api(), matcher, names);
    }

    public static boolean contains(GameAPI api, String... names) {
        return contains(api, String::contentEquals, names);
    }

    public static boolean contains(String... names) {
        return contains(XApi.api(), names);
    }

    public static boolean contains(GameAPI api, int... ids) {
        return getItems(api).stream().anyMatch(item -> contains(ids, item.itemId()));
    }

    public static boolean contains(int... ids) {
        return contains(XApi.api(), ids);
    }

    public static boolean contains(GameAPI api, Pattern... patterns) {
        return getItems(api).stream()
                .map(item -> api.getItemType(item.itemId()).name())
                .anyMatch(name -> matches(name, patterns));
    }

    public static boolean contains(Pattern... patterns) {
        return contains(XApi.api(), patterns);
    }

    public static InventoryItem getItem(GameAPI api, String... names) {
        return getItems(api).stream()
                .filter(item -> contains(names, api.getItemType(item.itemId()).name()))
                .findFirst()
                .orElse(null);
    }

    public static InventoryItem getItem(String... names) {
        return getItem(XApi.api(), names);
    }

    public static InventoryItem getItem(GameAPI api, int... ids) {
        return getItems(api).stream()
                .filter(item -> contains(ids, item.itemId()))
                .findFirst()
                .orElse(null);
    }

    public static InventoryItem getItem(int... ids) {
        return getItem(XApi.api(), ids);
    }

    public static List<InventoryItem> getItemsWithOption(GameAPI api, String option) {
        return getItems(api).stream()
                .filter(item -> api.getItemType(item.itemId()).inventoryOptions().stream()
                        .anyMatch(candidate -> candidate != null && candidate.equalsIgnoreCase(option)))
                .toList();
    }

    public static List<InventoryItem> getItemsWithOption(String option) {
        return getItemsWithOption(XApi.api(), option);
    }

    public static boolean interact(GameAPI api, String itemName, String option) {
        InventoryItem item = getItem(api, itemName);
        return item != null && interact(api, item.itemId(), option);
    }

    public static boolean interact(String itemName, String option) {
        return interact(XApi.api(), itemName, option);
    }

    public static boolean interact(GameAPI api, int itemId, String option) {
        Component component = findComponentByItem(api, itemId);
        if (component == null) {
            return false;
        }
        List<String> options = api.getComponentOptions(component.interfaceId(), component.componentId());
        for (int i = 0; i < options.size(); i++) {
            if (option.equalsIgnoreCase(options.get(i))) {
                api.queueAction(new GameAction(ActionTypes.COMPONENT, i + 1, component.subComponentId(),
                        component.interfaceId() << 16 | component.componentId()));
                return true;
            }
        }
        return false;
    }

    public static boolean interact(int itemId, String option) {
        return interact(XApi.api(), itemId, option);
    }

    public static boolean dragComponent(GameAPI api, Component fromComponent, Component toComponent) {
        if (fromComponent == null || toComponent == null) {
            return false;
        }
        api.queueAction(new GameAction(ActionTypes.COMPONENT_DRAG, fromComponent.subComponentId(), fromComponent.componentId(),
                fromComponent.interfaceId() << 16 | fromComponent.componentId()));
        api.queueAction(new GameAction(ActionTypes.COMPONENT, 1, toComponent.subComponentId(),
                toComponent.interfaceId() << 16 | toComponent.componentId()));
        return true;
    }

    public static boolean dragComponent(Component fromComponent, Component toComponent) {
        return dragComponent(XApi.api(), fromComponent, toComponent);
    }

    private static com.botwithus.bot.api.inventory.Backpack container(GameAPI api) {
        return new com.botwithus.bot.api.inventory.Backpack(api);
    }

    private static Component findComponentByItem(GameAPI api, int itemId) {
        List<Component> components = api.queryComponents(com.botwithus.bot.api.query.ComponentFilter.builder()
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
