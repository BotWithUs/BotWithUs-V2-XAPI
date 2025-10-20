package net.botwithus.xapi.game.inventory;

import net.botwithus.rs3.inventories.Inventory;
import net.botwithus.rs3.inventories.InventoryManager;
import net.botwithus.rs3.item.InventoryItem;
import net.botwithus.rs3.interfaces.Component;
import net.botwithus.rs3.interfaces.Interfaces;
import net.botwithus.rs3.minimenu.Action;
import net.botwithus.rs3.minimenu.MiniMenu;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class Backpack {

    /**
     * Retrieves the backpack inventory with ID 93.
     *
     * @return the backpack inventory
     */
    public static Inventory getInventory() {
        return InventoryManager.getInventory(93);
    }

    /**
     * Checks if the backpack is full.
     *
     * @return true if the backpack is full, false otherwise
     */
    public static boolean isFull() {
        Inventory backpack = getInventory();
        return backpack.getItems().stream().map(InventoryItem::getId).filter(i -> i != -1).count() == backpack.getDefinition().getCapacity();
    }

    /**
     * Checks if the backpack is empty.
     *
     * @return true if the backpack is empty, false otherwise
     */
    public static boolean isEmpty() {
        Inventory backpack = getInventory();
        return backpack.getItems().stream().map(InventoryItem::getId).allMatch(id -> id == -1);
    }

    /**
     * Retrieves all items in the backpack.
     *
     * @return a list of all items in the backpack
     */
    public static List<InventoryItem> getItems() {
        return getInventory().getItems().stream().filter(i -> !i.getName().isEmpty()).toList();
    }
    
    /**
     * Checks if the backpack contains any items with names matching the given predicate.
     *
     * @param spred the predicate to match item names
     * @param names the names to check
     * @return true if any item name matches the predicate, false otherwise
     */
    public static boolean contains(BiFunction<String, CharSequence, Boolean> spred, String... names) {
        if (names == null || names.length == 0) {
            return false;
        }
        var sanitizedNames = Arrays.stream(names)
                .filter(Objects::nonNull)
                .toList();
        if (sanitizedNames.isEmpty()) {
            return false;
        }
        Inventory backpack = getInventory();
        return backpack.getItems().stream()
                .map(InventoryItem::getName)
                .filter(Objects::nonNull)
                .anyMatch(name -> sanitizedNames.stream().anyMatch(candidate -> Boolean.TRUE.equals(spred.apply(name, candidate))));
    }

    /**
     * Checks if the backpack contains any items with the given names.
     *
     * @param names the names to check
     * @return true if any item name matches, false otherwise
     */
    public static boolean contains(String... names) {
        return contains(String::contentEquals, names);
    }

    /**
     * Checks if the backpack contains any items with the given IDs.
     *
     * @param ids the IDs to check
     * @return true if any item ID matches, false otherwise
     */
    public static boolean contains(int... ids) {
        Inventory backpack = getInventory();
        return backpack.getItems().stream().map(InventoryItem::getId).anyMatch(id -> Arrays.stream(ids).anyMatch(i -> i == id));
    }

    /**
     * Checks if the backpack contains any items with names matching the given patterns.
     *
     * @param namePatterns the patterns to match item names
     * @return true if any item name matches any of the patterns, false otherwise
     */
    public static boolean contains(Pattern... namePatterns) {
        Inventory backpack = getInventory();
        return backpack.getItems().stream().map(InventoryItem::getName).anyMatch(name -> Arrays.stream(namePatterns).anyMatch(p -> p.matcher(name).matches()));
    }

    /**
     * Retrieves the first item in the backpack with a name matching the given predicate.
     *
     * @param spred the predicate to match item names
     * @param names the names to check
     * @return the first matching item, or null if no match is found
     */
    public static InventoryItem getItem(BiFunction<String, CharSequence, Boolean> spred, String... names) {
        if (names == null || names.length == 0) {
            return null;
        }
        var sanitizedNames = Arrays.stream(names)
                .filter(Objects::nonNull)
                .toList();
        if (sanitizedNames.isEmpty()) {
            return null;
        }
        Inventory backpack = getInventory();
        return backpack.getItems().stream()
                .filter(item -> item.getName() != null && sanitizedNames.stream().anyMatch(candidate -> Boolean.TRUE.equals(spred.apply(item.getName(), candidate))))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves the first item in the backpack with the given name.
     *
     * @param names the names to check
     * @return the first matching item, or null if no match is found
     */
    public static InventoryItem getItem(String... names) {
        return getItem(String::contentEquals, names);
    }

    /**
     * Retrieves the first item in the backpack with the given ID.
     *
     * @param ids the IDs to check
     * @return the first matching item, or null if no match is found
     */
    public static InventoryItem getItem(int... ids) {
        Inventory backpack = getInventory();
        return backpack.getItems().stream().filter(item -> Arrays.stream(ids).anyMatch(i -> i == item.getId())).findFirst().orElse(null);
    }

    /**
     * Retrieves all items in the backpack that have the specified option.
     *
     * @param option the option to check for (e.g., "Eat", "Drink", "Use")
     * @return a list of items that have the specified option
     */
    public static List<InventoryItem> getItemsWithOption(String option) {
        return getInventory().getItems().stream()
                .filter(item -> item.getId() != -1 && !item.getName().isEmpty())
                .filter(item -> item.getOptions() != null && item.getOptions().contains(option))
                .toList();
    }

    /**
     * Interacts with an item in the backpack using the specified option.
     *
     * @param itemName the name of the item to interact with
     * @param option the option to use (e.g., "Eat", "Drink", "Use")
     * @return true if the interaction was successful, false otherwise
     */
    public static boolean interact(String itemName, String option) {
        InventoryItem item = getItem(itemName);
        return item != null && item.interact(option) > 0;
    }

    /**
     * Interacts with an item in the backpack by ID using the specified option.
     *
     * @param itemId the ID of the item to interact with
     * @param option the option to use (e.g., "Eat", "Drink", "Use")
     * @return true if the interaction was successful, false otherwise
     */
    public static boolean interact(int itemId, String option) {
        InventoryItem item = getItem(itemId);
        return item != null && item.interact(option) > 0;
    }

    /**
     * Drags a component from one location to another using the v2 MiniMenu system.
     * This is the v2 equivalent of the v1 Interfaces.dragComponents method.
     *
     * @param fromComponent the source component to drag from
     * @param toComponent the destination component to drag to
     * @return true if the drag operation was successful, false otherwise
     */
    public static boolean dragComponent(Component fromComponent, Component toComponent) {
        if (fromComponent == null || toComponent == null) {
            return false;
        }

        try {
            // First, set the target to the source component
            int fromInterfaceId = fromComponent.getRoot().getInterfaceId();
            int fromComponentId = fromComponent.getComponentId();
            int fromSubComponentId = fromComponent.getSubComponentId();

            // Initiate drag from source component
            int dragResult = MiniMenu.doAction(Action.COMPONENT_DRAG,
                fromSubComponentId,
                fromComponentId,
                fromInterfaceId);

            if (dragResult <= 0) {
                return false;
            }

            // Complete drag to destination component
            int toInterfaceId = toComponent.getRoot().getInterfaceId();
            int toComponentId = toComponent.getComponentId();
            int toSubComponentId = toComponent.getSubComponentId();

            int dropResult = MiniMenu.doAction(Action.COMPONENT,
                toSubComponentId,
                toComponentId,
                toInterfaceId);

            return dropResult > 0;

        } catch (Exception e) {
            return false;
        }
    }
}