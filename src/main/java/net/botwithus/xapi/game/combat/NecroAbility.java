package net.botwithus.xapi.game.combat;

import com.botwithus.bot.api.GameAPI;

import java.util.HashMap;
import java.util.Map;

/**
 * Necromancy ability definitions with cooldown tracking and readiness checks.
 * <p>
 * Each ability stores its struct ID (for game cache / sprite lookup) and a
 * VARC pair for cooldown tracking. Cooldowns are measured in client cycles
 * (1 cycle = 20ms, so 50 cycles = 1 second).
 * <p>
 * Example usage:
 * <pre>
 * // Check if an ability is ready (not on cooldown, GCD clear)
 * if (NecroAbility.SOUL_SAP.isReady(api)) {
 *     ActionBar.useAbilityByStruct(api, NecroAbility.SOUL_SAP.structId());
 * }
 *
 * // Check cooldown remaining
 * int cdMs = NecroAbility.FINGER_OF_DEATH.getCooldownMs(api);
 *
 * // Use with NecroRotation for automated combat
 * NecroRotation rotation = NecroRotation.pvme(api);
 * rotation.tick(api);
 * </pre>
 *
 * @see NecroState for necromancy combat state (stacks, conjures, buffs)
 * @see NecroRotation for automated rotation execution
 */
public enum NecroAbility {

    // ── Global Cooldown ────────────────────────────────────────────────
    GCD(14881, 2091, 2092, "Global Cooldown", Category.SYSTEM, 0),

    // ── Basic Attacks ──────────────────────────────────────────────────
    /** Auto-attack (no cooldown tracking, GCD only). */
    BASIC_ATTACK(48293, 0, 0, "Basic\u00A0Attack", Category.BASIC, 0),

    // ── Basics ─────────────────────────────────────────────────────────
    /** Deals 62.4-124.8% damage. Generates 1 residual soul if not on cooldown. */
    TOUCH_OF_DEATH(48296, 7225, 7226, "Touch of Death", Category.BASIC, 0),
    /** Deals 20-100% damage. Generates 1 residual soul. Adds 2 necrosis stacks. */
    SOUL_SAP(48298, 7244, 7245, "Soul Sap", Category.BASIC, 0),
    /** Deals 44-220% damage (with Soul Sap debuff). Generates 1 residual soul. */
    SOUL_STRIKE(48299, 7247, 7248, "Soul Strike", Category.BASIC, 0),
    /** AoE heal — damages enemies, heals you for 150% of damage dealt. */
    BLOOD_SIPHON(48309, 7253, 7254, "Blood Siphon", Category.BASIC, 0),
    /** Melee-range AoE. Cycles through 3 tiers for increasing damage. */
    SPECTRAL_SCYTHE(48311, 7255, 7256, "Spectral Scythe", Category.BASIC, 0),
    /** Tier 2 Spectral Scythe (unlocked via varbit 11051). */
    SPECTRAL_SCYTHE_T2(48312, 7257, 7258, "Spectral Scythe", Category.BASIC, 0),
    /** Tier 3 Spectral Scythe (unlocked via varbit 11054). */
    SPECTRAL_SCYTHE_T3(48313, 7259, 7260, "Spectral Scythe", Category.BASIC, 0),

    // ── Thresholds ─────────────────────────────────────────────────────
    /** 283.2-345.6% damage. Consumes necrosis stacks for +10% per 2 stacks. Costs 60% adren. */
    FINGER_OF_DEATH(48297, 7242, 7243, "Finger of Death", Category.THRESHOLD, 60),
    /** Launches 5 souls for 5 x (52.8-72%) damage. Requires 5 residual souls. No adren cost. */
    VOLLEY_OF_SOULS(48301, 7249, 7250, "Volley of Souls", Category.THRESHOLD, 0),
    /** 40-200% AoE bleed. Blocked after use until internal cooldown clears. Costs 20% adren. */
    BLOAT(48308, 7251, 7252, "Bloat", Category.THRESHOLD, 20),
    /** 4 bouncing skulls, each 68.4-342% damage. Costs 60% adren. */
    DEATH_SKULLS(48314, 7261, 7262, "Death Skulls", Category.THRESHOLD, 60),

    // ── Ultimate ───────────────────────────────────────────────────────
    /** 30s buff: basics generate 2x resources, thresholds cost 0 adren. Costs 100% adren. */
    LIVING_DEATH(48324, 7263, 7264, "Living Death", Category.ULTIMATE, 100),

    // ── Conjures ───────────────────────────────────────────────────────
    /** Summons a skeleton warrior. Lasts until dismissed or re-conjured. */
    CONJURE_SKELETON_WARRIOR(48302, 7227, 7228, "Conjure Skeleton Warrior", Category.CONJURE, 0),
    /** Summons a putrid zombie. */
    CONJURE_PUTRID_ZOMBIE(48304, 7232, 7233, "Conjure Putrid Zombie", Category.CONJURE, 0),
    /** Summons a vengeful ghost. */
    CONJURE_VENGEFUL_GHOST(48306, 7237, 7238, "Conjure Vengeful Ghost", Category.CONJURE, 0),
    /** Summons a phantom guardian (blocks some damage). */
    CONJURE_PHANTOM_GUARDIAN(31820, 7791, 7792, "Conjure Phantom Guardian", Category.CONJURE, 0),

    // ── Commands ───────────────────────────────────────────────────────
    /** Commands skeleton: double damage for a period. Requires active skeleton. */
    COMMAND_SKELETON_WARRIOR(48303, 7229, 7230, "Command Skeleton Warrior", Category.COMMAND, 0),
    /** Commands zombie: AoE poison attack. Requires active zombie. */
    COMMAND_PUTRID_ZOMBIE(48305, 7234, 7235, "Command Putrid Zombie", Category.COMMAND, 0),
    /** Commands ghost: applies Ectoplasmator debuff. Requires active ghost. */
    COMMAND_VENGEFUL_GHOST(48307, 7239, 7240, "Command Vengeful Ghost", Category.COMMAND, 0),
    /** Commands phantom: blocks incoming attack. Requires active phantom. */
    COMMAND_PHANTOM_GUARDIAN(32342, 7793, 7794, "Command Phantom Guardian", Category.COMMAND, 0),

    // ── Specials ───────────────────────────────────────────────────────
    /** Next basic kills target below 10% HP and generates 5 souls. 60s cooldown. */
    INVOKE_DEATH(48330, 7275, 7276, "Invoke Death", Category.SPECIAL, 0),
    /** AoE damage field around you. Lasts 30s. */
    DARKNESS(48331, 7278, 7279, "Darkness", Category.SPECIAL, 0),
    /** Soul Split heals split into damage to target. 30s duration. */
    SPLIT_SOUL(48332, 7281, 7282, "Split Soul", Category.SPECIAL, 0),
    /** Links up to 5 targets — share damage. 10s duration. */
    THREADS_OF_FATE(48329, 7272, 7273, "Threads of Fate", Category.SPECIAL, 0),
    /** Sacrifices 50% current HP to heal conjures. */
    LIFE_TRANSFER(48328, 7266, 7267, "Life Transfer", Category.SPECIAL, 0),
    /** Shield ability — absorbs damage using necrosis stacks. */
    LESSER_BONE_SHIELD(48326, 7268, 7269, "Lesser Bone Shield", Category.SPECIAL, 0),
    /** Greater shield ability — stronger absorption. */
    GREATER_BONE_SHIELD(48327, 7270, 7271, "Greater Bone Shield", Category.SPECIAL, 0);

    // ═══════════════════════════════════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════════════════════════════════

    private final int structId;
    private final int varc1;
    private final int varc2;
    private final String displayName;
    private final Category category;
    private final int adrenalineCost;

    NecroAbility(int structId, int varc1, int varc2, String displayName, Category category, int adrenalineCost) {
        this.structId = structId;
        this.varc1 = varc1;
        this.varc2 = varc2;
        this.displayName = displayName;
        this.category = category;
        this.adrenalineCost = adrenalineCost;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Getters
    // ═══════════════════════════════════════════════════════════════════

    /** The ability's struct type ID (used for game cache lookups and sprite-based matching). */
    public int structId()       { return structId; }
    /** First VARC for cooldown calculation. */
    public int varc1()          { return varc1; }
    /** Second VARC for cooldown calculation. */
    public int varc2()          { return varc2; }
    /** Display name as shown in game. */
    public String displayName() { return displayName; }
    /** Ability category (basic, threshold, ultimate, etc.). */
    public Category category()  { return category; }
    /** Adrenaline cost as percentage (0-100). */
    public int adrenalineCost() { return adrenalineCost; }

    // ═══════════════════════════════════════════════════════════════════
    // Cooldown Checks
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns remaining cooldown in client cycles (1 cycle = 20ms).
     * Returns 0 if the ability is ready.
     *
     * @param api the game API
     * @return remaining cooldown in cycles, or 0 if ready
     */
    public int getCooldown(GameAPI api) {
        if (varc1 == 0 && varc2 == 0) return 0;
        int v1 = api.getVarcInt(varc1);
        int v2 = api.getVarcInt(varc2);
        int base = v2 - v1;
        int elapsed = api.getGameCycle() - v1;
        return elapsed >= base ? 0 : base - elapsed;
    }

    /**
     * Returns remaining cooldown in milliseconds.
     *
     * @param api the game API
     * @return remaining cooldown in ms, or 0 if ready
     */
    public int getCooldownMs(GameAPI api) {
        return getCooldown(api) * 20;
    }

    /**
     * Returns true if this ability is on cooldown (own cooldown OR GCD).
     *
     * @param api the game API
     * @return true if on any cooldown
     */
    public boolean isOnCooldown(GameAPI api) {
        if (this == GCD) return getCooldown(api) > 0;
        return Math.max(GCD.getCooldown(api), getCooldown(api)) > 0;
    }

    /**
     * Returns true if only this ability's own cooldown is active (ignores GCD).
     * Use this when GCD is tracked externally (e.g. by {@link NecroRotation}).
     *
     * @param api the game API
     * @return true if own cooldown is still ticking
     */
    public boolean isOwnCooldownActive(GameAPI api) {
        return getCooldown(api) > 0;
    }

    /**
     * Returns true if this ability is ready to use (no cooldown and no GCD).
     * This is the simplest check — use this for manual ability usage.
     *
     * @param api the game API
     * @return true if ready
     */
    public boolean isReady(GameAPI api) {
        return !isOnCooldown(api);
    }

    /**
     * Returns true if this ability is ready (own cooldown only, ignores GCD).
     * Use this when GCD is tracked externally.
     *
     * @param api the game API
     * @return true if own cooldown is clear
     */
    public boolean isReadyIgnoringGcd(GameAPI api) {
        if (this == GCD) return getCooldown(api) <= 0;
        return !isOwnCooldownActive(api);
    }

    /**
     * Returns true if the GCD is currently active.
     *
     * @param api the game API
     * @return true if GCD is ticking
     */
    public static boolean isGcdActive(GameAPI api) {
        return GCD.getCooldown(api) > 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Lookup
    // ═══════════════════════════════════════════════════════════════════

    private static final Map<String, NecroAbility> BY_NAME = new HashMap<>();
    private static final Map<Integer, NecroAbility> BY_STRUCT = new HashMap<>();

    static {
        for (NecroAbility a : values()) {
            BY_NAME.putIfAbsent(a.displayName.toLowerCase(), a);
            BY_NAME.putIfAbsent(a.name().toLowerCase(), a);
            BY_STRUCT.putIfAbsent(a.structId, a);
        }
    }

    /**
     * Finds a necromancy ability by display name (case-insensitive).
     * Also matches enum constant names (e.g. "SOUL_SAP").
     *
     * @param name ability name
     * @return the ability, or null if not found
     */
    public static NecroAbility byName(String name) {
        if (name == null) return null;
        return BY_NAME.get(name.toLowerCase());
    }

    /**
     * Finds a necromancy ability by struct ID.
     *
     * @param structId the struct type ID
     * @return the ability, or null if not found
     */
    public static NecroAbility byStructId(int structId) {
        return BY_STRUCT.get(structId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Category
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Necromancy ability categories.
     */
    public enum Category {
        SYSTEM,
        /** Basic abilities (no adrenaline cost, generate adrenaline). */
        BASIC,
        /** Threshold abilities (cost adrenaline, strong damage). */
        THRESHOLD,
        /** Ultimate abilities (cost 100% adrenaline, very powerful). */
        ULTIMATE,
        /** Conjure abilities (summon undead helpers). */
        CONJURE,
        /** Command abilities (order conjured undead to perform actions). */
        COMMAND,
        /** Special abilities (buffs, debuffs, utility). */
        SPECIAL
    }
}
