package net.botwithus.xapi.game.inventory;

import net.botwithus.rs3.inventories.Inventory;
import net.botwithus.rs3.inventories.InventoryManager;
import net.botwithus.rs3.item.InventoryItem;
import net.botwithus.rs3.vars.VarDomain;
import net.botwithus.xapi.util.Regex;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class Equipment {

    /**
     * Retrieves the equipment inventory with ID 94.
     *
     * @return the equipment inventory
     */
    public static Inventory getInventory() {
        return InventoryManager.getInventory(94);
    }

    /**
     * Gets the item in the specified slot.
     *
     * @param slot The slot to get the item from.
     * @return The item in the slot, or null if there is no item in the slot.
     */
    public static InventoryItem getItemIn(Slot slot) {
        return getInventory().getItem(slot.getIndex());
    }

    /**
     * Checks if the given item name is present in the equipment.
     *
     * @param name The name of the item to check for.
     * @return true if the item is present in the equipment, false otherwise.
     */
    public static boolean contains(String name) {
        return getInventory().getItems().stream()
                .filter(item -> item.getId() != -1 && item.getName() != null)
                .anyMatch(item -> item.getName().equals(name));
    }

    /**
     * Checks if the equipment contains an item with a name matching the given pattern.
     *
     * @param pattern The pattern to match the item name against.
     * @return True if an item with a matching name is present in the equipment, false otherwise.
     */
    public static boolean contains(Pattern pattern) {
        return getInventory().getItems().stream()
                .filter(item -> item.getId() != -1 && item.getName() != null)
                .anyMatch(item -> pattern.matcher(item.getName()).matches());
    }

    /**
     * Interacts with the item in the given slot.
     *
     * @param slot   The slot to interact with.
     * @param option The option to interact with.
     * @return True if the interaction was successful, false otherwise.
     */
    public static boolean interact(Slot slot, String option) {
        InventoryItem item = getItemIn(slot);
        return item != null && item.interact(option) > 0;
    }

    /**
     * Interacts with the item in the given slot using a pattern-matched option.
     *
     * @param slot   The slot to interact with.
     * @param option The pattern to match the option against.
     * @return True if the interaction was successful, false otherwise.
     */
    public static boolean interact(Slot slot, Pattern option) {
        InventoryItem item = getItemIn(slot);
        if (item != null && item.getOptions() != null) {
            for (String availableOption : item.getOptions()) {
                if (availableOption != null && option.matcher(availableOption).matches()) {
                    return item.interact(availableOption) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Removes an item from the specified slot.
     *
     * @param slot The slot to unequip from.
     * @return true if the item was successfully unequipped, false otherwise.
     */
    public static boolean unequip(Slot slot) {
        return interact(slot, "Remove");
    }

    /**
     * Equips an item in the specified slot.
     *
     * @param slot The slot to equip to.
     * @return true if the item was successfully equipped, false otherwise.
     */
    public static boolean equip(Slot slot) {
        return interact(slot, Regex.getPatternForExactStrings("Wear", "Wield", "Equip"));
    }

    /**
     * Gets the value of a varbit for an item in the specified slot.
     *
     * @param slot     The equipment slot to check.
     * @param varbitId The varbit ID to check.
     * @return The value of the varbit, or -1 if the varbit is not present or item is null.
     */
    public static int getVarbitValue(int slot, int varbitId) {
        Inventory inventory = getInventory();
        if (inventory == null) {
            return -1;
        }
        return inventory.getVarbitValue(slot, varbitId);
    }

    /**
     * Represents a slot in the player's equipment.
     */
    public enum Slot {
        HEAD(0, 24431),
        CAPE(1, 24432),
        NECK(2, 24433),
        WEAPON(3, 24434),
        BODY(4, 24436),
        SHIELD(5, 24437),
        LEGS(7, 24438),
        HANDS(9, 24439),
        FEET(10, 24440),
        RING(12, 24435),
        AMMUNITION(13, 24441),
        AURA(14, 24442),
        POCKET(17, 24443);

        private final int index;

        Slot(int index, int textureId) {
            this.index = index;
        }

        public static Slot resolve(int index) {
            return Arrays.stream(values()).filter((slot) -> slot.index == index).findAny().orElse(null);
        }

        public final int getIndex() {
            return this.index;
        }

        @Override
        public String toString() {
            return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        }
    }
}