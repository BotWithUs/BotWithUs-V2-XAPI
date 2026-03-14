package net.botwithus.xapi.game.combat;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.inventory.ActionTypes;
import com.botwithus.bot.api.model.Component;
import com.botwithus.bot.api.model.GameAction;
import com.botwithus.bot.api.model.StructType;
import com.botwithus.bot.api.query.ComponentFilter;

import java.util.List;

/**
 * Action bar utility for finding and using abilities/items on any visible action bar.
 * <p>
 * Supports two lookup strategies:
 * <ul>
 *   <li><b>Text-based</b> ({@link #useAbility(GameAPI, String)}) — searches by option text, simpler but less reliable</li>
 *   <li><b>Sprite-based</b> ({@link #useAbilityByStruct(GameAPI, int)}) — uses game cache struct → sprite ID, most reliable</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * // Use an ability by name (searches all bars)
 * ActionBar.useAbility(api, "Soul Sap");
 *
 * // Use by struct ID (more reliable, e.g. from NecroAbility enum)
 * ActionBar.useAbilityByStruct(api, NecroAbility.SOUL_SAP.structId());
 *
 * // Check adrenaline
 * if (ActionBar.hasAdrenaline(api, 50)) { ... }
 *
 * // Use an item on the action bar
 * ActionBar.useItem(api, "Shark", "Eat");
 * </pre>
 *
 * @see NecroAbility for necromancy-specific ability definitions
 */
public final class ActionBar {

    private ActionBar() {}

    // ── Interface IDs ──────────────────────────────────────────────────
    /** Primary action bar interface. */
    public static final int PRIMARY_BAR = 1430;
    /** All action bar interfaces (primary + additional bars). */
    public static final int[] ALL_BARS = {1430, 1670, 1671, 1672, 1673};

    // ── Varps / Varcs ──────────────────────────────────────────────────
    /** Varp: which action bar has the queued ability (0 = none). */
    public static final int QUEUED_BAR_VARP = 5861;
    /** Varp: slot index of queued ability on the bar. */
    public static final int QUEUED_INDEX_VARP = 4164;
    /** Varc: game-cycle tick — changes when an ability fires. */
    public static final int GC_TICK_VARC = 2092;
    /** Varp: current adrenaline (0-1000, divide by 10 for %). */
    public static final int ADRENALINE_VARP = 5862;

    // ── Struct param keys ──────────────────────────────────────────────
    /** Struct param key for ability sprite ID. */
    public static final String STRUCT_PARAM_SPRITE = "2802";
    /** Struct param key for ability display name. */
    public static final String STRUCT_PARAM_NAME = "2794";

    // ═══════════════════════════════════════════════════════════════════
    // Ability Usage (text-based)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Uses an ability by name on any action bar.
     * Searches all bars for a component whose option matches the ability name.
     *
     * @param api         the game API
     * @param abilityName the ability's display name (e.g. "Soul Sap", "Living Death")
     * @return true if the ability was found and clicked
     */
    public static boolean useAbility(GameAPI api, String abilityName) {
        for (int barId : ALL_BARS) {
            if (clickAbilityComponent(api, barId, abilityName)) return true;
        }
        return false;
    }

    /**
     * Checks whether an ability is on any action bar.
     *
     * @param api         the game API
     * @param abilityName the ability's display name
     * @return true if found on any bar
     */
    public static boolean containsAbility(GameAPI api, String abilityName) {
        for (int barId : ALL_BARS) {
            if (findAbilityComponent(api, barId, abilityName) != null) return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Ability Usage (sprite-based — more reliable)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Uses an ability by its struct ID. Reads the sprite ID from the game cache
     * and clicks the matching action bar component.
     * <p>
     * This is the most reliable method — sprites are unique per ability and
     * are immune to display name changes or text encoding issues.
     *
     * @param api      the game API
     * @param structId the ability's struct type ID (e.g. {@code NecroAbility.SOUL_SAP.structId()})
     * @return true if the ability was found and clicked
     */
    public static boolean useAbilityByStruct(GameAPI api, int structId) {
        Integer spriteId = getSpriteFromStruct(api, structId);
        if (spriteId == null) return false;

        for (int barId : ALL_BARS) {
            List<Component> comps = api.queryComponents(
                    ComponentFilter.builder()
                            .interfaceId(barId)
                            .spriteId(spriteId)
                            .maxResults(1)
                            .build());
            if (comps != null && !comps.isEmpty()) {
                clickComponent(api, comps.getFirst());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether an ability with the given struct ID is on any action bar.
     *
     * @param api      the game API
     * @param structId the ability's struct type ID
     * @return true if found on any bar
     */
    public static boolean containsAbilityByStruct(GameAPI api, int structId) {
        Integer spriteId = getSpriteFromStruct(api, structId);
        if (spriteId == null) return false;

        for (int barId : ALL_BARS) {
            List<Component> comps = api.queryComponents(
                    ComponentFilter.builder()
                            .interfaceId(barId)
                            .spriteId(spriteId)
                            .maxResults(1)
                            .build());
            if (comps != null && !comps.isEmpty()) return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Item Usage
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Uses an item on the action bar by name and option (e.g. "Shark", "Eat").
     * Searches all bars.
     *
     * @param api      the game API
     * @param itemName the item name (e.g. "Shark", "Saradomin brew flask")
     * @param option   the interaction option (e.g. "Eat", "Drink")
     * @return true if the item component was found and clicked
     */
    public static boolean useItem(GameAPI api, String itemName, String option) {
        String pattern = "(?i).*" + escapeRegex(option) + ".*" + escapeRegex(itemName) + ".*"
                + "|(?i).*" + escapeRegex(itemName) + ".*" + escapeRegex(option) + ".*";
        for (int barId : ALL_BARS) {
            List<Component> components = api.queryComponents(
                    ComponentFilter.builder()
                            .interfaceId(barId)
                            .optionPattern(pattern)
                            .maxResults(1)
                            .build());
            if (components != null && !components.isEmpty()) {
                clickComponent(api, components.getFirst());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether an item is on any action bar.
     *
     * @param api      the game API
     * @param itemName the item name
     * @return true if found on any bar
     */
    public static boolean containsItem(GameAPI api, String itemName) {
        for (int barId : ALL_BARS) {
            List<Component> components = api.queryComponents(
                    ComponentFilter.builder()
                            .interfaceId(barId)
                            .optionPattern("(?i).*" + escapeRegex(itemName) + ".*")
                            .maxResults(1)
                            .build());
            if (components != null && !components.isEmpty()) return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Adrenaline
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns current adrenaline percentage (0-100).
     */
    public static int getAdrenaline(GameAPI api) {
        return api.getVarp(ADRENALINE_VARP) / 10;
    }

    /**
     * Returns true if the player has at least the given adrenaline percentage.
     */
    public static boolean hasAdrenaline(GameAPI api, int percentRequired) {
        return getAdrenaline(api) >= percentRequired;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Queued Ability / GC Tick
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns true if any ability is currently queued.
     */
    public static boolean isAbilityQueued(GameAPI api) {
        return api.getVarp(QUEUED_BAR_VARP) > 0;
    }

    /**
     * Returns the current GC tick varc value.
     * Useful for detecting when an ability fires (value changes each cast).
     */
    public static int getGcTick(GameAPI api) {
        return api.getVarcInt(GC_TICK_VARC);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Game Cache Helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Reads the sprite ID (param 2802) from a struct in the game cache.
     *
     * @param api      the game API
     * @param structId the ability's struct type ID
     * @return sprite ID, or null if not found
     */
    public static Integer getSpriteFromStruct(GameAPI api, int structId) {
        StructType struct = api.getStructType(structId);
        if (struct == null || struct.params() == null) return null;
        Object val = struct.params().get(STRUCT_PARAM_SPRITE);
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        return null;
    }

    /**
     * Reads the display name (param 2794) from a struct in the game cache.
     *
     * @param api      the game API
     * @param structId the ability's struct type ID
     * @return ability name, or null if not found
     */
    public static String getNameFromStruct(GameAPI api, int structId) {
        StructType struct = api.getStructType(structId);
        if (struct == null || struct.params() == null) return null;
        Object val = struct.params().get(STRUCT_PARAM_NAME);
        return val instanceof String s ? s : null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Internal helpers
    // ═══════════════════════════════════════════════════════════════════

    private static Component findAbilityComponent(GameAPI api, int interfaceId, String abilityName) {
        String pattern = "(?i).*" + escapeRegex(abilityName) + ".*";
        List<Component> components = api.queryComponents(
                ComponentFilter.builder()
                        .interfaceId(interfaceId)
                        .optionPattern(pattern)
                        .maxResults(1)
                        .build());
        if (components == null || components.isEmpty()) return null;
        return components.getFirst();
    }

    private static boolean clickAbilityComponent(GameAPI api, int interfaceId, String abilityName) {
        Component comp = findAbilityComponent(api, interfaceId, abilityName);
        if (comp == null) return false;
        clickComponent(api, comp);
        return true;
    }

    static void clickComponent(GameAPI api, Component comp) {
        int hash = comp.interfaceId() << 16 | comp.componentId();
        api.queueAction(new GameAction(
                ActionTypes.COMPONENT, 1, comp.subComponentId(), hash));
    }

    private static String escapeRegex(String input) {
        return input.replaceAll("([\\\\\\[\\](){}.*+?^$|])", "\\\\$1");
    }
}
