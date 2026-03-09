package net.botwithus.xapi.game.inventory;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.inventory.Equipment;
import com.botwithus.bot.api.inventory.Equipment.Slot;
import com.botwithus.bot.api.model.InventoryItem;
import net.botwithus.xapi.XApi;

import java.util.List;
import java.util.regex.Pattern;

public final class BwuEquipment {

    private BwuEquipment() {
    }

    public static List<InventoryItem> getItems(GameAPI api) {
        return equipment(api).getItems();
    }

    public static List<InventoryItem> getItems() {
        return getItems(XApi.api());
    }

    public static boolean contains(GameAPI api, int itemId) {
        return equipment(api).contains(itemId);
    }

    public static boolean contains(int itemId) {
        return contains(XApi.api(), itemId);
    }

    public static InventoryItem getItem(GameAPI api, Slot slot) {
        return equipment(api).getSlot(slot);
    }

    public static InventoryItem getItem(Slot slot) {
        return getItem(XApi.api(), slot);
    }

    public static boolean interact(GameAPI api, Slot slot, int option) {
        return equipment(api).interact(slot, option);
    }

    public static boolean interact(Slot slot, int option) {
        return interact(XApi.api(), slot, option);
    }

    public static boolean interact(GameAPI api, Slot slot, String option) {
        return equipment(api).interact(slot, option);
    }

    public static boolean interact(Slot slot, String option) {
        return interact(XApi.api(), slot, option);
    }

    public static boolean interact(GameAPI api, int itemId, String option) {
        return equipment(api).interact(itemId, option);
    }

    public static boolean interact(int itemId, String option) {
        return interact(XApi.api(), itemId, option);
    }

    public static boolean equip(GameAPI api, int itemId) {
        return BwuBackpack.interact(api, itemId, "Wear")
                || BwuBackpack.interact(api, itemId, "Wield")
                || BwuBackpack.interact(api, itemId, "Equip");
    }

    public static boolean equip(int itemId) {
        return equip(XApi.api(), itemId);
    }

    public static boolean equip(GameAPI api, Pattern pattern) {
        InventoryItem item = BwuBackpack.getItems(api).stream()
                .filter(candidate -> pattern.matcher(api.getItemType(candidate.itemId()).name()).matches())
                .findFirst()
                .orElse(null);
        return item != null && equip(api, item.itemId());
    }

    public static boolean equip(Pattern pattern) {
        return equip(XApi.api(), pattern);
    }

    private static Equipment equipment(GameAPI api) {
        return new Equipment(api);
    }
}
