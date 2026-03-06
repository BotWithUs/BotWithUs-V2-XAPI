package net.botwithus.xapi.game.inventory;

import com.botwithus.bot.api.model.Component;
import com.botwithus.bot.api.model.InventoryItem;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.ComponentQuery;
import net.botwithus.xapi.query.InventoryItemQuery;
import net.botwithus.xapi.query.NpcQuery;
import net.botwithus.xapi.query.SceneObjectQuery;
import net.botwithus.xapi.query.result.ResultSet;
import net.botwithus.xapi.script.permissive.base.PermissiveScript;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Bank {

    public static final int INVENTORY_ID = com.botwithus.bot.api.inventory.Bank.INVENTORY_ID;
    public static final int INTERFACE_INDEX = com.botwithus.bot.api.inventory.Bank.INTERFACE_ID;
    public static final int COMPONENT_INDEX = com.botwithus.bot.api.inventory.Bank.BANK_COMPONENT;

    private static final Pattern BANK_NAME_PATTERN = Pattern.compile("^(?!.*deposit).*(bank|counter).*$", Pattern.CASE_INSENSITIVE);
    private static final String LAST_PRESET_OPTION = "Load Last Preset from";

    private Bank() {
    }

    public static boolean open() {
        var obj = SceneObjectQuery.newQuery().name(BANK_NAME_PATTERN).option("Use").or(SceneObjectQuery.newQuery().name(BANK_NAME_PATTERN).option("Bank")).results().nearest();
        if (obj != null && (obj.interact("Bank") || obj.interact("Use"))) {
            return true;
        }
        var npc = NpcQuery.newQuery().option("Bank").results().nearest();
        return npc != null && npc.interact("Bank");
    }

    public static boolean isOpen() {
        return bank().isOpen();
    }

    public static boolean close() {
        XApi.api().queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1, -1, INTERFACE_INDEX << 16 | 11));
        return true;
    }

    public static boolean loadLastPreset() {
        var obj = SceneObjectQuery.newQuery().option(LAST_PRESET_OPTION).results().nearest();
        if (obj != null && obj.interact(LAST_PRESET_OPTION)) {
            return true;
        }
        var npc = NpcQuery.newQuery().option(LAST_PRESET_OPTION).results().nearest();
        return npc != null && npc.interact(LAST_PRESET_OPTION);
    }

    public static InventoryItem[] getItems() {
        return InventoryItemQuery.newQuery(INVENTORY_ID).results().stream()
                .filter(item -> item.itemId() != -1)
                .toArray(InventoryItem[]::new);
    }

    public static int count(ResultSet<InventoryItem> results) {
        return results.stream().mapToInt(InventoryItem::quantity).sum();
    }

    public static InventoryItem first(InventoryItemQuery query) {
        return query.results().first();
    }

    public static boolean isEmpty() {
        return getItems().length == 0;
    }

    public static boolean interact(int slot, int option) {
        InventoryItem item = InventoryItemQuery.newQuery(INVENTORY_ID).slot(slot).results().first();
        return item != null && bank().withdraw(item.itemId(), mapOption(option));
    }

    public static boolean contains(InventoryItemQuery query) {
        return count(query.results()) > 0;
    }

    public static boolean contains(String... itemNames) {
        return !InventoryItemQuery.newQuery(INVENTORY_ID).name(itemNames).results().isEmpty();
    }

    public static boolean contains(Pattern itemNamePattern) {
        return !InventoryItemQuery.newQuery(INVENTORY_ID).name(itemNamePattern).results().isEmpty();
    }

    public static int getCount(String... itemNames) {
        return count(InventoryItemQuery.newQuery(INVENTORY_ID).name(itemNames).results());
    }

    public static int getCount(Pattern namePattern) {
        return count(InventoryItemQuery.newQuery(INVENTORY_ID).name(namePattern).results());
    }

    public static boolean withdraw(InventoryItemQuery query, int option) {
        InventoryItem item = query.results().first();
        return item != null && bank().withdraw(item.itemId(), mapOption(option));
    }

    public static boolean withdraw(String itemName, int option) {
        return withdraw(InventoryItemQuery.newQuery(INVENTORY_ID).name(itemName), option);
    }

    public static boolean withdraw(int itemId, int option) {
        return withdraw(InventoryItemQuery.newQuery(INVENTORY_ID).id(itemId), option);
    }

    public static boolean withdraw(Pattern pattern, int option) {
        return withdraw(InventoryItemQuery.newQuery(INVENTORY_ID).name(pattern), option);
    }

    public static boolean withdrawAll(String name) {
        return bank().withdrawAll(firstIdByName(name));
    }

    public static boolean withdrawAll(int id) {
        return bank().withdrawAll(id);
    }

    public static boolean withdrawAll(Pattern pattern) {
        InventoryItem item = InventoryItemQuery.newQuery(INVENTORY_ID).name(pattern).results().first();
        return item != null && bank().withdrawAll(item.itemId());
    }

    public static boolean depositAll() {
        return bank().depositAll();
    }

    public static boolean depositEquipment() {
        return bank().depositEquipment();
    }

    public static boolean depositBackpack() {
        return bank().depositAll();
    }

    public static boolean deposit(PermissiveScript script, ComponentQuery query, int option) {
        Component item = query.results().first();
        return item != null && deposit(script, item, option);
    }

    public static boolean depositAll(PermissiveScript script, ComponentQuery query) {
        return deposit(script, query, 1);
    }

    public static boolean deposit(PermissiveScript script, Component component, int option) {
        boolean queued = component != null && bank().deposit(component.itemId(), mapOption(option));
        if (queued) {
            script.delay(1);
        }
        return queued;
    }

    public static boolean depositAll(PermissiveScript script, String... itemNames) {
        Set<Integer> ids = Arrays.stream(itemNames)
                .map(Bank::firstIdByName)
                .filter(id -> id > -1)
                .collect(Collectors.toSet());
        return ids.stream().allMatch(id -> bank().deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAll(PermissiveScript script, int... itemIds) {
        return Arrays.stream(itemIds).allMatch(id -> bank().deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAll(PermissiveScript script, Pattern... patterns) {
        Set<Integer> ids = Backpack.getItems().stream()
                .filter(item -> {
                    String name = XApi.api().getItemType(item.itemId()).name();
                    return Arrays.stream(patterns).anyMatch(pattern -> pattern.matcher(name).matches());
                })
                .map(InventoryItem::itemId)
                .collect(Collectors.toSet());
        return ids.stream().allMatch(id -> bank().deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAllExcept(PermissiveScript script, String... itemNames) {
        Set<String> protectedNames = Arrays.stream(itemNames).collect(Collectors.toSet());
        return Backpack.getItems().stream()
                .filter(item -> !protectedNames.contains(XApi.api().getItemType(item.itemId()).name()))
                .map(InventoryItem::itemId)
                .distinct()
                .allMatch(id -> bank().deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAllExcept(PermissiveScript script, int... ids) {
        Set<Integer> protectedIds = Arrays.stream(ids).boxed().collect(Collectors.toSet());
        return Backpack.getItems().stream()
                .filter(item -> !protectedIds.contains(item.itemId()))
                .map(InventoryItem::itemId)
                .distinct()
                .allMatch(id -> bank().deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAllExcept(PermissiveScript script, Pattern... patterns) {
        return Backpack.getItems().stream()
                .filter(item -> {
                    String name = XApi.api().getItemType(item.itemId()).name();
                    return Arrays.stream(patterns).noneMatch(pattern -> pattern.matcher(name).matches());
                })
                .map(InventoryItem::itemId)
                .distinct()
                .allMatch(id -> bank().deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean deposit(PermissiveScript script, int itemId, int option) {
        return bank().deposit(itemId, mapOption(option));
    }

    public static boolean deposit(PermissiveScript script, String name, BiFunction<String, CharSequence, Boolean> matcher, int option) {
        Integer itemId = Backpack.getItems().stream()
                .filter(item -> Boolean.TRUE.equals(matcher.apply(XApi.api().getItemType(item.itemId()).name(), name)))
                .map(InventoryItem::itemId)
                .findFirst()
                .orElse(-1);
        return itemId > -1 && bank().deposit(itemId, mapOption(option));
    }

    public static boolean deposit(PermissiveScript script, String name, int option) {
        return deposit(script, name, String::contentEquals, option);
    }

    public static boolean loadPreset(PermissiveScript script, int presetNumber) {
        return bank().withdrawPreset(presetNumber);
    }

    public static int getVarbitValue(int slot, int varbitId) {
        return XApi.api().getItemVarValue(INVENTORY_ID, slot, varbitId);
    }

    public static boolean setTransferOption(TransferOptionType transferOptionType) {
        return bank().setTransferMode(switch (transferOptionType) {
            case ONE -> com.botwithus.bot.api.inventory.Bank.TransferAmount.ONE;
            case FIVE -> com.botwithus.bot.api.inventory.Bank.TransferAmount.FIVE;
            case TEN -> com.botwithus.bot.api.inventory.Bank.TransferAmount.TEN;
            case ALL -> com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL;
            case X -> com.botwithus.bot.api.inventory.Bank.TransferAmount.CUSTOM;
        });
    }

    private static com.botwithus.bot.api.inventory.Bank bank() {
        return new com.botwithus.bot.api.inventory.Bank(XApi.api());
    }

    private static int firstIdByName(String name) {
        InventoryItem item = InventoryItemQuery.newQuery(INVENTORY_ID).name(name).results().first();
        return item == null ? -1 : item.itemId();
    }

    private static com.botwithus.bot.api.inventory.Bank.TransferAmount mapOption(int option) {
        return switch (option) {
            case 2 -> com.botwithus.bot.api.inventory.Bank.TransferAmount.ONE;
            case 3 -> com.botwithus.bot.api.inventory.Bank.TransferAmount.FIVE;
            case 4 -> com.botwithus.bot.api.inventory.Bank.TransferAmount.TEN;
            case 5 -> com.botwithus.bot.api.inventory.Bank.TransferAmount.CUSTOM;
            default -> com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL;
        };
    }
}

enum TransferOptionType {
    ONE,
    FIVE,
    TEN,
    ALL,
    X
}
