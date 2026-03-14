package net.botwithus.xapi.game.combat;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.Component;
import com.botwithus.bot.api.query.ComponentFilter;

import java.util.List;

/**
 * Reads necromancy combat state from game variables (varps, varbits, varcs).
 * <p>
 * All methods are static and require a {@link GameAPI} instance.
 * <p>
 * Example usage:
 * <pre>
 * // Check resources
 * int souls = NecroState.getResidualSouls(api);
 * int stacks = NecroState.getNecrosisStacks(api);
 *
 * // Check conjure states
 * if (NecroState.isGhostSummoned(api) && NecroState.canCommandGhost(api)) {
 *     ActionBar.useAbilityByStruct(api, NecroAbility.COMMAND_VENGEFUL_GHOST.structId());
 * }
 *
 * // Check active buffs
 * if (!NecroState.isLivingDeathActive(api) && ActionBar.hasAdrenaline(api, 100)) {
 *     ActionBar.useAbilityByStruct(api, NecroAbility.LIVING_DEATH.structId());
 * }
 *
 * // Check adrenaline (general combat)
 * if (NecroState.hasAdrenaline(api, 60)) { ... }
 * </pre>
 *
 * @see NecroAbility for ability definitions and cooldowns
 * @see NecroRotation for automated rotation execution
 */
public final class NecroState {

    private NecroState() {}

    // ═══════════════════════════════════════════════════════════════════
    // Adrenaline
    // ═══════════════════════════════════════════════════════════════════

    /** Varp: adrenaline (0-1000, divide by 10 for %). */
    public static final int ADRENALINE_VARP = 679;

    /** Returns adrenaline percentage (0-100). */
    public static int getAdrenaline(GameAPI api) {
        return api.getVarp(ADRENALINE_VARP) / 10;
    }

    /** Returns true if adrenaline >= required percentage. */
    public static boolean hasAdrenaline(GameAPI api, int percent) {
        return getAdrenaline(api) >= percent;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Residual Souls & Necrosis Stacks
    // ═══════════════════════════════════════════════════════════════════

    /** Varp: residual soul count (0-5). */
    public static final int RESIDUAL_SOULS_VARP = 11035;
    /** Varp: necrosis stacks on target (0-12). */
    public static final int NECROSIS_STACKS_VARP = 10986;

    /**
     * Returns the current residual soul count (0-5).
     * Volley of Souls requires 5 souls.
     */
    public static int getResidualSouls(GameAPI api) {
        return api.getVarp(RESIDUAL_SOULS_VARP);
    }

    /**
     * Returns necrosis stacks on target (0-12).
     * Finger of Death consumes stacks for +10% damage per 2 stacks.
     */
    public static int getNecrosisStacks(GameAPI api) {
        return api.getVarp(NECROSIS_STACKS_VARP);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Conjure State (summoned = varp > 0)
    // ═══════════════════════════════════════════════════════════════════

    public static final int SKELETON_SUMMON_VARP = 10994;
    public static final int PHANTOM_SUMMON_VARP = 11000;
    public static final int ZOMBIE_SUMMON_VARP = 11006;
    public static final int GHOST_SUMMON_VARP = 11018;

    /** Returns true if a skeleton warrior is currently conjured. */
    public static boolean isSkeletonSummoned(GameAPI api) { return api.getVarp(SKELETON_SUMMON_VARP) > 0; }
    /** Returns true if a phantom guardian is currently conjured. */
    public static boolean isPhantomSummoned(GameAPI api)  { return api.getVarp(PHANTOM_SUMMON_VARP) > 0; }
    /** Returns true if a putrid zombie is currently conjured. */
    public static boolean isZombieSummoned(GameAPI api)   { return api.getVarp(ZOMBIE_SUMMON_VARP) > 0; }
    /** Returns true if a vengeful ghost is currently conjured. */
    public static boolean isGhostSummoned(GameAPI api)    { return api.getVarp(GHOST_SUMMON_VARP) > 0; }

    /** Returns true if any conjure is active. */
    public static boolean hasAnySummon(GameAPI api) {
        return isSkeletonSummoned(api) || isPhantomSummoned(api)
                || isZombieSummoned(api) || isGhostSummoned(api);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Command Cooldowns (varp == 0 means ready to command)
    // ═══════════════════════════════════════════════════════════════════

    public static final int COMMAND_SKELETON_VARP = 11002;
    public static final int COMMAND_ZOMBIE_VARP = 11009;
    public static final int COMMAND_GHOST_VARP = 11021;

    /** Returns true if Command Skeleton Warrior is ready (not on internal cooldown). */
    public static boolean canCommandSkeleton(GameAPI api) { return api.getVarp(COMMAND_SKELETON_VARP) == 0; }
    /** Returns true if Command Putrid Zombie is ready. */
    public static boolean canCommandZombie(GameAPI api)   { return api.getVarp(COMMAND_ZOMBIE_VARP) == 0; }
    /** Returns true if Command Vengeful Ghost is ready. */
    public static boolean canCommandGhost(GameAPI api)    { return api.getVarp(COMMAND_GHOST_VARP) == 0; }

    // ═══════════════════════════════════════════════════════════════════
    // Active Buffs / Debuffs
    // ═══════════════════════════════════════════════════════════════════

    /** Varp: Living Death (> -1 = active, counts down remaining ticks). */
    public static final int LIVING_DEATH_VARP = 11059;
    /** Varp: Darkness (>= 1 = active). */
    public static final int DARKNESS_VARP = 11074;
    /** Varbit: Invoke Death applied to next basic (1 = active). */
    public static final int INVOKE_DEATH_VARBIT = 53247;
    /** Varbit: Bloat internal cooldown (1 = blocked, can't use again yet). */
    public static final int BLOAT_VARBIT = 53245;
    /** Varp: Bone Shield active (> 0 = absorbing damage). */
    public static final int BONE_SHIELD_VARP = 11212;
    /** Varp: Death Spark count (Living Death sparks). */
    public static final int DEATH_SPARK_VARP = 11085;

    /** Returns true if Living Death buff is currently active (30s window). */
    public static boolean isLivingDeathActive(GameAPI api) { return api.getVarp(LIVING_DEATH_VARP) > -1; }
    /** Returns true if Darkness is currently active. */
    public static boolean isDarknessActive(GameAPI api)    { return api.getVarp(DARKNESS_VARP) >= 1; }
    /** Returns true if Invoke Death is applied (next basic will execute). */
    public static boolean isInvokeDeathActive(GameAPI api) { return api.getVarbit(INVOKE_DEATH_VARBIT) == 1; }
    /** Returns true if Bloat is on internal cooldown (can't be used). */
    public static boolean isBloatBlocked(GameAPI api)      { return api.getVarbit(BLOAT_VARBIT) == 1; }
    /** Returns true if Bone Shield is actively absorbing damage. */
    public static boolean isBoneShieldActive(GameAPI api)  { return api.getVarp(BONE_SHIELD_VARP) > 0; }
    /** Returns the current death spark count. */
    public static int getDeathSparks(GameAPI api)          { return api.getVarp(DEATH_SPARK_VARP); }

    // ═══════════════════════════════════════════════════════════════════
    // Spectral Scythe Tier Unlocks
    // ═══════════════════════════════════════════════════════════════════

    public static final int SPECTRAL_SCYTHE_T2_VARBIT = 11051;
    public static final int SPECTRAL_SCYTHE_T3_VARBIT = 11054;

    /** Returns true if Spectral Scythe tier 2 is unlocked. */
    public static boolean isSpectralScytheT2Unlocked(GameAPI api) { return api.getVarp(SPECTRAL_SCYTHE_T2_VARBIT) == 1; }
    /** Returns true if Spectral Scythe tier 3 is unlocked. */
    public static boolean isSpectralScytheT3Unlocked(GameAPI api) { return api.getVarp(SPECTRAL_SCYTHE_T3_VARBIT) == 1; }

    /**
     * Returns the highest unlocked Spectral Scythe tier.
     */
    public static NecroAbility getSpectralScytheTier(GameAPI api) {
        if (isSpectralScytheT3Unlocked(api)) return NecroAbility.SPECTRAL_SCYTHE_T3;
        if (isSpectralScytheT2Unlocked(api)) return NecroAbility.SPECTRAL_SCYTHE_T2;
        return NecroAbility.SPECTRAL_SCYTHE;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Weapon Special Cooldowns (varc-based, compare to game cycle)
    // ═══════════════════════════════════════════════════════════════════

    /** Varc: Death Grasp / T90 weapon special end cycle. */
    public static final int DEATH_GRASP_VARC = 7285;
    /** Varc: Death Essence / T95 weapon special end cycle. */
    public static final int DEATH_ESSENCE_VARC = 7287;
    /** Varc: Split Soul active end cycle. */
    public static final int SPLIT_SOUL_VARC = 7283;
    /** Varc: Invoke Lord of Bones end cycle. */
    public static final int INVOKE_LORD_VARC = 7349;

    /** Returns true if T90 weapon special (Death Grasp / EoF) is ready. */
    public static boolean isDeathGraspReady(GameAPI api) {
        return (api.getVarcInt(DEATH_GRASP_VARC) - api.getGameCycle()) <= 0;
    }

    /** Returns true if T95 weapon special (Death Essence) is ready. */
    public static boolean isDeathEssenceReady(GameAPI api) {
        return (api.getVarcInt(DEATH_ESSENCE_VARC) - api.getGameCycle()) <= 0;
    }

    /** Returns true if Split Soul is currently active (varc-based). */
    public static boolean isSplitSoulActive(GameAPI api) {
        return (api.getVarcInt(SPLIT_SOUL_VARC) - api.getGameCycle()) > 0;
    }

    /** Returns true if Invoke Lord of Bones is off cooldown. */
    public static boolean isInvokeLordOfBonesReady(GameAPI api) {
        return (api.getVarcInt(INVOKE_LORD_VARC) - api.getGameCycle()) <= 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // General Combat State
    // ═══════════════════════════════════════════════════════════════════

    /** Varp that changes when an ability is successfully cast. */
    public static final int ABILITY_CAST_VARP = 4501;

    /** Returns the current ability cast value. Monitor changes to detect successful casts. */
    public static int getAbilityCastValue(GameAPI api) {
        return api.getVarp(ABILITY_CAST_VARP);
    }

    /** Returns true if any ability is currently queued on an action bar. */
    public static boolean isAbilityQueued(GameAPI api) {
        return api.getVarp(ActionBar.QUEUED_BAR_VARP) > 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Buff Bar (interface 284) — sprite-based detection
    // ═══════════════════════════════════════════════════════════════════

    /** Buff bar interface ID. */
    public static final int BUFF_BAR_INTERFACE = 284;

    /**
     * Returns true if a buff with the given sprite ID is visible on the buff bar.
     *
     * @param api      the game API
     * @param spriteId the buff's sprite ID
     * @return true if the buff is active and visible
     */
    public static boolean isBuffActive(GameAPI api, int spriteId) {
        List<Component> comps = api.queryComponents(
                ComponentFilter.builder()
                        .interfaceId(BUFF_BAR_INTERFACE)
                        .spriteId(spriteId)
                        .visibleOnly(true)
                        .maxResults(1)
                        .build());
        return comps != null && !comps.isEmpty();
    }
}
