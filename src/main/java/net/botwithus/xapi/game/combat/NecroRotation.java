package net.botwithus.xapi.game.combat;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.inventory.ActionTypes;
import com.botwithus.bot.api.model.Component;
import com.botwithus.bot.api.model.GameAction;
import com.botwithus.bot.api.model.StructType;
import com.botwithus.bot.api.query.ComponentFilter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Necromancy combat rotation manager. Handles ability caching, GCD tracking,
 * and automated rotation execution.
 * <p>
 * <b>Quick start — use the built-in PVME rotation:</b>
 * <pre>
 * // In onStart():
 * NecroRotation rotation = NecroRotation.pvme(api);
 *
 * // In onLoop() (every tick while in combat):
 * rotation.onTick();
 * rotation.execute();
 * </pre>
 * <p>
 * <b>Custom rotation with builder:</b>
 * <pre>
 * NecroRotation rotation = NecroRotation.builder(api)
 *     // Buffs first
 *     .use(NecroAbility.INVOKE_DEATH).when(NecroState::isInvokeDeathActive, false)
 *     .use(NecroAbility.SPLIT_SOUL).when(NecroState::isSplitSoulActive, false)
 *     // Ultimate
 *     .use(NecroAbility.LIVING_DEATH).whenAdren(100).when(NecroState::isLivingDeathActive, false)
 *     // Thresholds
 *     .use(NecroAbility.DEATH_SKULLS).whenAdren(60)
 *     .use(NecroAbility.FINGER_OF_DEATH).whenAdren(60).whenStacks(4)
 *     .use(NecroAbility.VOLLEY_OF_SOULS).whenSouls(5)
 *     .use(NecroAbility.BLOAT).whenAdren(20).when(NecroState::isBloatBlocked, false)
 *     // Commands
 *     .commandGhost()
 *     .commandSkeleton()
 *     .commandZombie()
 *     // Basics (fallback)
 *     .use(NecroAbility.TOUCH_OF_DEATH)
 *     .use(NecroAbility.SOUL_SAP)
 *     .use(NecroAbility.SOUL_STRIKE)
 *     .build();
 *
 * // In onLoop():
 * rotation.onTick();
 * rotation.execute();
 * </pre>
 * <p>
 * <b>Manual ability usage (no rotation):</b>
 * <pre>
 * NecroRotation mgr = new NecroRotation(api);
 * mgr.cache(); // call once on start
 *
 * // Check + use individually
 * if (mgr.isReady(NecroAbility.SOUL_SAP)) {
 *     mgr.useAbility(NecroAbility.SOUL_SAP);
 * }
 * </pre>
 *
 * @see NecroAbility for ability definitions
 * @see NecroState for combat state checks
 * @see ActionBar for general action bar utilities
 */
public class NecroRotation {

    /** Default GCD duration in ticks (3 ticks = standard ability). */
    public static final int DEFAULT_GCD = 3;

    /** Action bar interfaces to scan. */
    private static final int[] BAR_INTERFACES = ActionBar.ALL_BARS;

    private final GameAPI api;

    // Cached ability components (resolved during cache())
    private final Map<NecroAbility, Component> components = new EnumMap<>(NecroAbility.class);
    // Special abilities (no enum): display name -> Component
    private final Map<String, Component> specialComponents = new HashMap<>();

    // Rotation steps (set via builder or pvme())
    private List<Step> steps = List.of();

    // GCD tracking
    private int gcd = 0;
    private boolean cached = false;

    /**
     * Creates a new rotation manager.
     * Call {@link #cache()} before using abilities.
     *
     * @param api the game API
     */
    public NecroRotation(GameAPI api) {
        this.api = api;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Factory Methods
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Creates a ready-to-use PVME necromancy rotation.
     * Automatically caches abilities on creation.
     * <p>
     * Priority: Conjure Army → Invoke Death → Split Soul → Living Death →
     * Death Skulls → Finger of Death (12 stacks) → Volley of Souls (5 souls) →
     * Bloat → Commands → Life Transfer → Touch of Death → Soul Sap → Soul Strike
     *
     * @param api the game API
     * @return configured rotation, ready for {@link #execute()}
     */
    public static NecroRotation pvme(GameAPI api) {
        NecroRotation r = new NecroRotation(api);
        r.cacheSpecial("Conjure Undead Army");

        List<Step> pvmeSteps = new ArrayList<>();

        // Conjure upkeep — re-summon when army expires
        pvmeSteps.add(Step.special("Conjure Undead Army",
                (a, m) -> !NecroState.isGhostSummoned(a) && m.isSpecialCached("Conjure Undead Army")));

        // Buffs — maintain Invoke Death + Split Soul
        pvmeSteps.add(Step.ability(NecroAbility.INVOKE_DEATH,
                (a, m) -> m.isReady(NecroAbility.INVOKE_DEATH) && !NecroState.isInvokeDeathActive(a)));
        pvmeSteps.add(Step.ability(NecroAbility.SPLIT_SOUL,
                (a, m) -> m.isReady(NecroAbility.SPLIT_SOUL) && !NecroState.isSplitSoulActive(a)));

        // Ultimate
        pvmeSteps.add(Step.ability(NecroAbility.LIVING_DEATH,
                (a, m) -> m.isReady(NecroAbility.LIVING_DEATH) && NecroState.hasAdrenaline(a, 100)
                        && !NecroState.isLivingDeathActive(a)));

        // Thresholds
        pvmeSteps.add(Step.ability(NecroAbility.DEATH_SKULLS,
                (a, m) -> m.isReady(NecroAbility.DEATH_SKULLS) && NecroState.hasAdrenaline(a, 60)));
        pvmeSteps.add(Step.ability(NecroAbility.FINGER_OF_DEATH,
                (a, m) -> m.isReady(NecroAbility.FINGER_OF_DEATH) && NecroState.hasAdrenaline(a, 60)
                        && NecroState.getNecrosisStacks(a) >= 12));
        pvmeSteps.add(Step.ability(NecroAbility.VOLLEY_OF_SOULS,
                (a, m) -> m.isReady(NecroAbility.VOLLEY_OF_SOULS) && NecroState.getResidualSouls(a) >= 5));
        pvmeSteps.add(Step.ability(NecroAbility.BLOAT,
                (a, m) -> m.isReady(NecroAbility.BLOAT) && NecroState.hasAdrenaline(a, 20)
                        && !NecroState.isBloatBlocked(a)));

        // Commands — Ghost > Skeleton > Zombie
        pvmeSteps.add(Step.ability(NecroAbility.COMMAND_VENGEFUL_GHOST,
                (a, m) -> m.isReady(NecroAbility.COMMAND_VENGEFUL_GHOST)
                        && NecroState.isGhostSummoned(a) && NecroState.canCommandGhost(a)));
        pvmeSteps.add(Step.ability(NecroAbility.COMMAND_SKELETON_WARRIOR,
                (a, m) -> m.isReady(NecroAbility.COMMAND_SKELETON_WARRIOR)
                        && NecroState.isSkeletonSummoned(a) && NecroState.canCommandSkeleton(a)));
        pvmeSteps.add(Step.ability(NecroAbility.COMMAND_PUTRID_ZOMBIE,
                (a, m) -> m.isReady(NecroAbility.COMMAND_PUTRID_ZOMBIE)
                        && NecroState.isZombieSummoned(a) && NecroState.canCommandZombie(a)));

        // Life Transfer — heal conjures
        pvmeSteps.add(Step.ability(NecroAbility.LIFE_TRANSFER,
                (a, m) -> m.isReady(NecroAbility.LIFE_TRANSFER) && NecroState.hasAnySummon(a)));

        // Basics
        pvmeSteps.add(Step.whenReady(NecroAbility.TOUCH_OF_DEATH));
        pvmeSteps.add(Step.whenReady(NecroAbility.SOUL_SAP));
        pvmeSteps.add(Step.whenReady(NecroAbility.SOUL_STRIKE));

        r.steps = List.copyOf(pvmeSteps);
        r.cache();
        return r;
    }

    /**
     * Creates a rotation builder for custom ability priority.
     *
     * @param api the game API
     * @return a new builder
     */
    public static Builder builder(GameAPI api) {
        return new Builder(api);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Caching
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Scans all action bars and caches components for necromancy abilities.
     * Uses sprite-based lookup (game cache) for reliability, falls back to text.
     * <p>
     * Call this once on script start and whenever the action bar layout changes.
     */
    public void cache() {
        components.clear();

        for (NecroAbility ability : NecroAbility.values()) {
            if (ability == NecroAbility.GCD) continue;

            // Try sprite-based lookup first
            Component comp = findByStruct(ability.structId());
            if (comp == null) {
                // Fall back to text
                comp = findByText(ability.displayName());
            }
            if (comp != null) {
                components.put(ability, comp);
            }
        }

        cached = true;
    }

    /**
     * Caches a special ability by name (text-based lookup).
     * Use for abilities not in the {@link NecroAbility} enum (e.g. "Conjure Undead Army").
     *
     * @param abilityName the ability's display name
     * @return true if found on an action bar
     */
    public boolean cacheSpecial(String abilityName) {
        if (specialComponents.containsKey(abilityName)) return true;
        Component comp = findByText(abilityName);
        if (comp != null) {
            specialComponents.put(abilityName, comp);
            return true;
        }
        return false;
    }

    /** Returns true if abilities have been cached. */
    public boolean isCached() { return cached; }

    // ═══════════════════════════════════════════════════════════════════
    // Availability Checks
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns true if the ability is cached (on action bar) and its own
     * cooldown is clear. Does not check GCD — that's handled by {@link #execute()}.
     *
     * @param ability the ability to check
     * @return true if ready to use
     */
    public boolean isReady(NecroAbility ability) {
        return components.containsKey(ability) && ability.isReadyIgnoringGcd(api);
    }

    /**
     * Returns true if the ability is on an action bar (cached).
     */
    public boolean isCachedAbility(NecroAbility ability) {
        return components.containsKey(ability);
    }

    /**
     * Returns true if a special ability (by name) is cached.
     */
    public boolean isSpecialCached(String name) {
        return specialComponents.containsKey(name);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GCD Tracking
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Call once per game tick to decrement the GCD counter.
     * Must be called in your script's tick handler for rotation timing to work.
     */
    public void onTick() {
        if (gcd > 0) gcd--;
    }

    /** Returns true if the GCD has expired and an ability can fire. */
    public boolean isGcdReady() { return gcd <= 0; }

    /** Returns remaining GCD ticks. */
    public int getGcd() { return gcd; }

    // ═══════════════════════════════════════════════════════════════════
    // Manual Ability Usage
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Clicks the cached component for an ability. Does not check GCD.
     *
     * @param ability the ability to use
     * @return true if the component was found and clicked
     */
    public boolean useAbility(NecroAbility ability) {
        Component comp = components.get(ability);
        if (comp == null) return false;
        ActionBar.clickComponent(api, comp);
        return true;
    }

    /**
     * Uses an ability with GCD. Only fires if GCD is ready.
     *
     * @param ability the ability to use
     * @return true if fired successfully
     */
    public boolean useAbilityWithGcd(NecroAbility ability) {
        if (!isGcdReady()) return false;
        if (!useAbility(ability)) return false;
        gcd = DEFAULT_GCD;
        return true;
    }

    /**
     * Uses a special ability (by name). Does not check GCD.
     *
     * @param name the ability display name
     * @return true if found and clicked
     */
    public boolean useSpecial(String name) {
        Component comp = specialComponents.get(name);
        if (comp == null) return false;
        ActionBar.clickComponent(api, comp);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Rotation Execution
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Executes the configured rotation. Evaluates each step in priority order
     * and fires the first one whose condition is met.
     * <p>
     * GCD is enforced automatically — if GCD is still ticking, returns null.
     * <p>
     * Call this in your script's combat loop after {@link #onTick()}.
     *
     * @return the ability name that was fired, or null if nothing was available
     */
    public String execute() {
        if (!isGcdReady()) return null;

        for (Step step : steps) {
            if (!step.condition.test(api, this)) continue;

            boolean fired;
            if (step.ability != null) {
                fired = useAbility(step.ability);
            } else {
                fired = useSpecial(step.name);
            }

            if (fired) {
                gcd = DEFAULT_GCD;
                return step.name;
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step (internal rotation unit)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * A single step in a rotation: an ability + condition that must be met.
     */
    record Step(String name, NecroAbility ability, StepCondition condition) {

        static Step whenReady(NecroAbility ability) {
            return new Step(ability.displayName(), ability,
                    (api, mgr) -> mgr.isReady(ability));
        }

        static Step ability(NecroAbility ability, StepCondition condition) {
            return new Step(ability.displayName(), ability, condition);
        }

        static Step special(String name, StepCondition condition) {
            return new Step(name, null, condition);
        }
    }

    /**
     * Condition for a rotation step.
     */
    @FunctionalInterface
    public interface StepCondition {
        boolean test(GameAPI api, NecroRotation mgr);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Builder
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Fluent builder for custom necromancy rotations.
     * <p>
     * Steps are evaluated in the order they are added — put highest priority
     * abilities first (buffs → ultimates → thresholds → commands → basics).
     * <p>
     * Example:
     * <pre>
     * NecroRotation rotation = NecroRotation.builder(api)
     *     .use(NecroAbility.LIVING_DEATH).whenAdren(100).when(NecroState::isLivingDeathActive, false)
     *     .use(NecroAbility.DEATH_SKULLS).whenAdren(60)
     *     .use(NecroAbility.FINGER_OF_DEATH).whenAdren(60).whenStacks(4)
     *     .use(NecroAbility.VOLLEY_OF_SOULS).whenSouls(5)
     *     .commandGhost()
     *     .commandSkeleton()
     *     .use(NecroAbility.TOUCH_OF_DEATH)
     *     .use(NecroAbility.SOUL_SAP)
     *     .build();
     * </pre>
     */
    public static class Builder {

        private final GameAPI api;
        private final List<Step> steps = new ArrayList<>();
        private final List<String> specials = new ArrayList<>();

        // State for the current step being built
        private NecroAbility currentAbility;
        private String currentSpecialName;
        private final List<StepCondition> currentConditions = new ArrayList<>();

        private Builder(GameAPI api) {
            this.api = api;
        }

        /**
         * Adds an ability to the rotation. Subsequent {@code when*} calls
         * add conditions to this ability. The step is finalized when the
         * next {@code use()} or {@code build()} is called.
         */
        public Builder use(NecroAbility ability) {
            finalizeCurrentStep();
            currentAbility = ability;
            currentSpecialName = null;
            // Default condition: ability is ready (cached + off cooldown)
            currentConditions.add((a, m) -> m.isReady(ability));
            return this;
        }

        /**
         * Adds a special ability (by name) to the rotation.
         */
        public Builder useSpecial(String name) {
            finalizeCurrentStep();
            currentAbility = null;
            currentSpecialName = name;
            specials.add(name);
            currentConditions.add((a, m) -> m.isSpecialCached(name));
            return this;
        }

        /**
         * Requires minimum adrenaline percentage for the current step.
         */
        public Builder whenAdren(int percent) {
            currentConditions.add((a, m) -> NecroState.hasAdrenaline(a, percent));
            return this;
        }

        /**
         * Requires minimum necrosis stacks for the current step.
         */
        public Builder whenStacks(int minStacks) {
            currentConditions.add((a, m) -> NecroState.getNecrosisStacks(a) >= minStacks);
            return this;
        }

        /**
         * Requires minimum residual souls for the current step.
         */
        public Builder whenSouls(int minSouls) {
            currentConditions.add((a, m) -> NecroState.getResidualSouls(a) >= minSouls);
            return this;
        }

        /**
         * Adds a custom condition using a {@link NecroState} method.
         * <p>
         * Use {@code expectedValue = false} to require the state to be inactive:
         * <pre>
         * .when(NecroState::isLivingDeathActive, false)  // only when Living Death is NOT active
         * .when(NecroState::isBloatBlocked, false)       // only when Bloat is NOT blocked
         * </pre>
         *
         * @param stateCheck    a method reference like {@code NecroState::isLivingDeathActive}
         * @param expectedValue true = require active, false = require inactive
         */
        public Builder when(java.util.function.Function<GameAPI, Boolean> stateCheck, boolean expectedValue) {
            currentConditions.add((a, m) -> stateCheck.apply(a) == expectedValue);
            return this;
        }

        /**
         * Adds a raw custom condition.
         */
        public Builder when(StepCondition condition) {
            currentConditions.add(condition);
            return this;
        }

        /**
         * Shorthand: adds Command Vengeful Ghost with proper checks.
         */
        public Builder commandGhost() {
            finalizeCurrentStep();
            steps.add(Step.ability(NecroAbility.COMMAND_VENGEFUL_GHOST,
                    (a, m) -> m.isReady(NecroAbility.COMMAND_VENGEFUL_GHOST)
                            && NecroState.isGhostSummoned(a) && NecroState.canCommandGhost(a)));
            return this;
        }

        /**
         * Shorthand: adds Command Skeleton Warrior with proper checks.
         */
        public Builder commandSkeleton() {
            finalizeCurrentStep();
            steps.add(Step.ability(NecroAbility.COMMAND_SKELETON_WARRIOR,
                    (a, m) -> m.isReady(NecroAbility.COMMAND_SKELETON_WARRIOR)
                            && NecroState.isSkeletonSummoned(a) && NecroState.canCommandSkeleton(a)));
            return this;
        }

        /**
         * Shorthand: adds Command Putrid Zombie with proper checks.
         */
        public Builder commandZombie() {
            finalizeCurrentStep();
            steps.add(Step.ability(NecroAbility.COMMAND_PUTRID_ZOMBIE,
                    (a, m) -> m.isReady(NecroAbility.COMMAND_PUTRID_ZOMBIE)
                            && NecroState.isZombieSummoned(a) && NecroState.canCommandZombie(a)));
            return this;
        }

        /**
         * Shorthand: adds Command Phantom Guardian with proper checks.
         */
        public Builder commandPhantom() {
            finalizeCurrentStep();
            steps.add(Step.ability(NecroAbility.COMMAND_PHANTOM_GUARDIAN,
                    (a, m) -> m.isReady(NecroAbility.COMMAND_PHANTOM_GUARDIAN)
                            && NecroState.isPhantomSummoned(a)));
            return this;
        }

        /**
         * Builds the rotation, caches abilities, and returns a ready-to-use instance.
         */
        public NecroRotation build() {
            finalizeCurrentStep();

            NecroRotation rotation = new NecroRotation(api);
            rotation.steps = List.copyOf(steps);

            // Cache special abilities
            for (String name : specials) {
                rotation.cacheSpecial(name);
            }

            rotation.cache();
            return rotation;
        }

        private void finalizeCurrentStep() {
            if (currentAbility == null && currentSpecialName == null) return;

            List<StepCondition> conditions = List.copyOf(currentConditions);
            StepCondition combined = (a, m) -> {
                for (StepCondition c : conditions) {
                    if (!c.test(a, m)) return false;
                }
                return true;
            };

            if (currentAbility != null) {
                steps.add(Step.ability(currentAbility, combined));
            } else {
                steps.add(Step.special(currentSpecialName, combined));
            }

            currentAbility = null;
            currentSpecialName = null;
            currentConditions.clear();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Internal — Component lookup
    // ═══════════════════════════════════════════════════════════════════

    private Component findByStruct(int structId) {
        try {
            StructType struct = api.getStructType(structId);
            if (struct == null || struct.params() == null) return null;
            Object val = struct.params().get(ActionBar.STRUCT_PARAM_SPRITE);
            int spriteId;
            if (val instanceof Integer i) spriteId = i;
            else if (val instanceof Number n) spriteId = n.intValue();
            else return null;

            for (int barId : BAR_INTERFACES) {
                List<Component> comps = api.queryComponents(
                        ComponentFilter.builder()
                                .interfaceId(barId)
                                .spriteId(spriteId)
                                .maxResults(1)
                                .build());
                if (comps != null && !comps.isEmpty()) return comps.getFirst();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Component findByText(String name) {
        String pattern = "(?i).*" + escapeRegex(name) + ".*";
        for (int barId : BAR_INTERFACES) {
            List<Component> comps = api.queryComponents(
                    ComponentFilter.builder()
                            .interfaceId(barId)
                            .optionPattern(pattern)
                            .maxResults(1)
                            .build());
            if (comps != null && !comps.isEmpty()) return comps.getFirst();
        }
        return null;
    }

    private static String escapeRegex(String input) {
        return input.replace("\u00A0", "\\s+")
                .replaceAll("([\\\\\\[\\](){}.*+?^$|])", "\\\\$1");
    }
}
