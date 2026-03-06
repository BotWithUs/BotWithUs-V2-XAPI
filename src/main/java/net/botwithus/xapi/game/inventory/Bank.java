package net.botwithus.xapi.game.inventory;

import com.botwithus.bot.api.GameAPI;
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

    public static boolean open(GameAPI api) {
        var obj = SceneObjectQuery.newQuery(api).name(BANK_NAME_PATTERN)
                .option("Use").or(SceneObjectQuery.newQuery(api).name(BANK_NAME_PATTERN).option("Bank")).results().nearest();
        if (obj != null && (obj.interact("Bank") || obj.interact("Use"))) {
            return true;
        }
        var npc = NpcQuery.newQuery(api).option("Bank").results().nearest();
        return npc != null && npc.interact("Bank");
    }

    public static boolean open() {
        return open(XApi.api());
    }

    public static boolean isOpen(GameAPI api) {
        return bank(api).isOpen();
    }

    public static boolean isOpen() {
        return isOpen(XApi.api());
    }

    public static boolean close(GameAPI api) {
        api.queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1, -1, INTERFACE_INDEX << 16 | 11));
        return true;
    }

    public static boolean close() {
        return close(XApi.api());
    }

    public static boolean loadLastPreset(GameAPI api) {
        var obj = SceneObjectQuery.newQuery(api).option(LAST_PRESET_OPTION).results().nearest();
        if (obj != null && obj.interact(LAST_PRESET_OPTION)) {
            return true;
        }
        var npc = NpcQuery.newQuery(api).option(LAST_PRESET_OPTION).results().nearest();
        return npc != null && npc.interact(LAST_PRESET_OPTION);
    }

    public static boolean loadLastPreset() {
        return loadLastPreset(XApi.api());
    }

    public static InventoryItem[] getItems(GameAPI api) {
        return InventoryItemQuery.newQuery(api, INVENTORY_ID).results().stream()
                .filter(item -> item.itemId() != -1)
                .toArray(InventoryItem[]::new);
    }

    public static InventoryItem[] getItems() {
        return getItems(XApi.api());
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

    public static boolean interact(GameAPI api, int slot, int option) {
        InventoryItem item = InventoryItemQuery.newQuery(api, INVENTORY_ID).slot(slot).results().first();
        return item != null && bank(api).withdraw(item.itemId(), mapOption(option));
    }

    public static boolean interact(int slot, int option) {
        return interact(XApi.api(), slot, option);
    }

    public static boolean contains(GameAPI api, InventoryItemQuery query) {
        return count(query.results()) > 0;
    }

    public static boolean contains(InventoryItemQuery query) {
        return contains(XApi.api(), query);
    }

    public static boolean contains(GameAPI api, String... itemNames) {
        return !InventoryItemQuery.newQuery(api, INVENTORY_ID).name(itemNames).results().isEmpty();
    }

    public static boolean contains(String... itemNames) {
        return contains(XApi.api(), itemNames);
    }

    public static boolean contains(GameAPI api, Pattern itemNamePattern) {
        return !InventoryItemQuery.newQuery(api, INVENTORY_ID).name(itemNamePattern).results().isEmpty();
    }

    public static boolean contains(Pattern itemNamePattern) {
        return contains(XApi.api(), itemNamePattern);
    }

    public static int getCount(GameAPI api, String... itemNames) {
        return count(InventoryItemQuery.newQuery(api, INVENTORY_ID).name(itemNames).results());
    }

    public static int getCount(String... itemNames) {
        return getCount(XApi.api(), itemNames);
    }

    public static int getCount(GameAPI api, Pattern namePattern) {
        return count(InventoryItemQuery.newQuery(api, INVENTORY_ID).name(namePattern).results());
    }

    public static int getCount(Pattern namePattern) {
        return getCount(XApi.api(), namePattern);
    }

    public static boolean withdraw(GameAPI api, InventoryItemQuery query, int option) {
        InventoryItem item = query.results().first();
        return item != null && bank(api).withdraw(item.itemId(), mapOption(option));
    }

    public static boolean withdraw(InventoryItemQuery query, int option) {
        return withdraw(XApi.api(), query, option);
    }

    public static boolean withdraw(GameAPI api, String itemName, int option) {
        return withdraw(api, InventoryItemQuery.newQuery(api, INVENTORY_ID).name(itemName), option);
    }

    public static boolean withdraw(String itemName, int option) {
        return withdraw(XApi.api(), itemName, option);
    }

    public static boolean withdraw(GameAPI api, int itemId, int option) {
        return withdraw(api, InventoryItemQuery.newQuery(api, INVENTORY_ID).id(itemId), option);
    }

    public static boolean withdraw(int itemId, int option) {
        return withdraw(XApi.api(), itemId, option);
    }

    public static boolean withdraw(GameAPI api, Pattern pattern, int option) {
        return withdraw(api, InventoryItemQuery.newQuery(api, INVENTORY_ID).name(pattern), option);
    }

    public static boolean withdraw(Pattern pattern, int option) {
        return withdraw(XApi.api(), pattern, option);
    }

    public static boolean withdrawAll(GameAPI api, String name) {
        return bank(api).withdrawAll(firstIdByName(api, name));
    }

    public static boolean withdrawAll(String name) {
        return withdrawAll(XApi.api(), name);
    }

    public static boolean withdrawAll(GameAPI api, int id) {
        return bank(api).withdrawAll(id);
    }

    public static boolean withdrawAll(int id) {
        return withdrawAll(XApi.api(), id);
    }

    public static boolean withdrawAll(GameAPI api, Pattern pattern) {
        InventoryItem item = InventoryItemQuery.newQuery(api, INVENTORY_ID).name(pattern).results().first();
        return item != null && bank(api).withdrawAll(item.itemId());
    }

    public static boolean withdrawAll(Pattern pattern) {
        return withdrawAll(XApi.api(), pattern);
    }

    public static boolean depositAll(GameAPI api) {
        return bank(api).depositAll();
    }

    public static boolean depositAll() {
        return depositAll(XApi.api());
    }

    public static boolean depositEquipment(GameAPI api) {
        return bank(api).depositEquipment();
    }

    public static boolean depositEquipment() {
        return depositEquipment(XApi.api());
    }

    public static boolean depositBackpack(GameAPI api) {
        return bank(api).depositAll();
    }

    public static boolean depositBackpack() {
        return depositBackpack(XApi.api());
    }

    public static boolean deposit(GameAPI api, PermissiveScript script, ComponentQuery query, int option) {
        Component item = query.results().first();
        return item != null && deposit(api, script, item, option);
    }

    public static boolean deposit(PermissiveScript script, ComponentQuery query, int option) {
        return deposit(XApi.api(), script, query, option);
    }

    public static boolean depositAll(PermissiveScript script, ComponentQuery query) {
        return deposit(script, query, 1);
    }

    public static boolean deposit(GameAPI api, PermissiveScript script, Component component, int option) {
        boolean queued = component != null && bank(api).deposit(component.itemId(), mapOption(option));
        if (queued) {
            script.delay(1);
        }
        return queued;
    }

    public static boolean deposit(PermissiveScript script, Component component, int option) {
        return deposit(XApi.api(), script, component, option);
    }

    public static boolean depositAll(GameAPI api, PermissiveScript script, String... itemNames) {
        Set<Integer> ids = Arrays.stream(itemNames)
                .map(name -> firstIdByName(api, name))
                .filter(id -> id > -1)
                .collect(Collectors.toSet());
        return ids.stream().allMatch(id -> bank(api).deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAll(PermissiveScript script, String... itemNames) {
        return depositAll(XApi.api(), script, itemNames);
    }

    public static boolean depositAll(GameAPI api, PermissiveScript script, int... itemIds) {
        return Arrays.stream(itemIds).allMatch(id -> bank(api).deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAll(PermissiveScript script, int... itemIds) {
        return depositAll(XApi.api(), script, itemIds);
    }

    public static boolean depositAll(GameAPI api, PermissiveScript script, Pattern... patterns) {
        Set<Integer> ids = Backpack.getItems(api).stream()
                .filter(item -> {
                    String name = api.getItemType(item.itemId()).name();
                    return Arrays.stream(patterns).anyMatch(pattern -> pattern.matcher(name).matches());
                })
                .map(InventoryItem::itemId)
                .collect(Collectors.toSet());
        return ids.stream().allMatch(id -> bank(api).deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAll(PermissiveScript script, Pattern... patterns) {
        return depositAll(XApi.api(), script, patterns);
    }

    public static boolean depositAllExcept(GameAPI api, PermissiveScript script, String... itemNames) {
        Set<String> protectedNames = Arrays.stream(itemNames).collect(Collectors.toSet());
        return Backpack.getItems(api).stream()
                .filter(item -> !protectedNames.contains(api.getItemType(item.itemId()).name()))
                .map(InventoryItem::itemId)
                .distinct()
                .allMatch(id -> bank(api).deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAllExcept(PermissiveScript script, String... itemNames) {
        return depositAllExcept(XApi.api(), script, itemNames);
    }

    public static boolean depositAllExcept(GameAPI api, PermissiveScript script, int... ids) {
        Set<Integer> protectedIds = Arrays.stream(ids).boxed().collect(Collectors.toSet());
        return Backpack.getItems(api).stream()
                .filter(item -> !protectedIds.contains(item.itemId()))
                .map(InventoryItem::itemId)
                .distinct()
                .allMatch(id -> bank(api).deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAllExcept(PermissiveScript script, int... ids) {
        return depositAllExcept(XApi.api(), script, ids);
    }

    public static boolean depositAllExcept(GameAPI api, PermissiveScript script, Pattern... patterns) {
        return Backpack.getItems(api).stream()
                .filter(item -> {
                    String name = api.getItemType(item.itemId()).name();
                    return Arrays.stream(patterns).noneMatch(pattern -> pattern.matcher(name).matches());
                })
                .map(InventoryItem::itemId)
                .distinct()
                .allMatch(id -> bank(api).deposit(id, com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL));
    }

    public static boolean depositAllExcept(PermissiveScript script, Pattern... patterns) {
        return depositAllExcept(XApi.api(), script, patterns);
    }

    public static boolean deposit(GameAPI api, PermissiveScript script, int itemId, int option) {
        return bank(api).deposit(itemId, mapOption(option));
    }

    public static boolean deposit(PermissiveScript script, int itemId, int option) {
        return deposit(XApi.api(), script, itemId, option);
    }

    public static boolean deposit(GameAPI api, PermissiveScript script, String name, BiFunction<String, CharSequence, Boolean> matcher, int option) {
        Integer itemId = Backpack.getItems(api).stream()
                .filter(item -> Boolean.TRUE.equals(matcher.apply(api.getItemType(item.itemId()).name(), name)))
                .map(InventoryItem::itemId)
                .findFirst()
                .orElse(-1);
        return itemId > -1 && bank(api).deposit(itemId, mapOption(option));
    }

    public static boolean deposit(PermissiveScript script, String name, BiFunction<String, CharSequence, Boolean> matcher, int option) {
        return deposit(XApi.api(), script, name, matcher, option);
    }

    public static boolean deposit(PermissiveScript script, String name, int option) {
        return deposit(script, name, String::contentEquals, option);
    }

    public static boolean loadPreset(GameAPI api, PermissiveScript script, int presetNumber) {
        return bank(api).withdrawPreset(presetNumber);
    }

    public static boolean loadPreset(PermissiveScript script, int presetNumber) {
        return loadPreset(XApi.api(), script, presetNumber);
    }

    public static int getVarbitValue(GameAPI api, int slot, int varbitId) {
        return api.getItemVarValue(INVENTORY_ID, slot, varbitId);
    }

    public static int getVarbitValue(int slot, int varbitId) {
        return getVarbitValue(XApi.api(), slot, varbitId);
    }

    public static boolean setTransferOption(GameAPI api, TransferOptionType transferOptionType) {
        return bank(api).setTransferMode(switch (transferOptionType) {
            case ONE -> com.botwithus.bot.api.inventory.Bank.TransferAmount.ONE;
            case FIVE -> com.botwithus.bot.api.inventory.Bank.TransferAmount.FIVE;
            case TEN -> com.botwithus.bot.api.inventory.Bank.TransferAmount.TEN;
            case ALL -> com.botwithus.bot.api.inventory.Bank.TransferAmount.ALL;
            case X -> com.botwithus.bot.api.inventory.Bank.TransferAmount.CUSTOM;
        });
    }

    public static boolean setTransferOption(TransferOptionType transferOptionType) {
        return setTransferOption(XApi.api(), transferOptionType);
    }

    private static com.botwithus.bot.api.inventory.Bank bank(GameAPI api) {
        return new com.botwithus.bot.api.inventory.Bank(api);
    }

    private static int firstIdByName(GameAPI api, String name) {
        InventoryItem item = InventoryItemQuery.newQuery(api, INVENTORY_ID).name(name).results().first();
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
