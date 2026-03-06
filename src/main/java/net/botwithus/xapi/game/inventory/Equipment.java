package net.botwithus.xapi.game.inventory;

import com.botwithus.bot.api.model.InventoryItem;
import net.botwithus.xapi.XApi;

import java.util.List;
import java.util.regex.Pattern;

public final class Equipment {

    private Equipment() {
    }

    public static List<InventoryItem> getItems() {
        return equipment().getItems();
    }

    public static boolean contains(int itemId) {
        return equipment().contains(itemId);
    }

    public static InventoryItem getItem(com.botwithus.bot.api.inventory.Equipment.Slot slot) {
        return equipment().getSlot(slot);
    }

    public static boolean interact(com.botwithus.bot.api.inventory.Equipment.Slot slot, int option) {
        return equipment().interact(slot, option);
    }

    public static boolean interact(com.botwithus.bot.api.inventory.Equipment.Slot slot, String option) {
        return equipment().interact(slot, option);
    }

    public static boolean interact(int itemId, String option) {
        return equipment().interact(itemId, option);
    }

    public static boolean equip(int itemId) {
        return Backpack.interact(itemId, "Wear") || Backpack.interact(itemId, "Wield") || Backpack.interact(itemId, "Equip");
    }

    public static boolean equip(Pattern pattern) {
        InventoryItem item = Backpack.getItems().stream()
                .filter(candidate -> pattern.matcher(XApi.api().getItemType(candidate.itemId()).name()).matches())
                .findFirst()
                .orElse(null);
        return item != null && equip(item.itemId());
    }

    private static com.botwithus.bot.api.inventory.Equipment equipment() {
        return new com.botwithus.bot.api.inventory.Equipment(XApi.api());
    }
}
