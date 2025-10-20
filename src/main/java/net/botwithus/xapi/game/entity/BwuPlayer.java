package net.botwithus.xapi.game.entity;

import net.botwithus.rs3.cache.assets.vars.VarDomainType;
import net.botwithus.rs3.entities.PathingEntity;
import net.botwithus.rs3.entities.LocalPlayer;
import net.botwithus.rs3.entities.EntityType;
import net.botwithus.rs3.world.World;
import net.botwithus.rs3.client.Client;
// Dialog API usage temporarily commented out for compilation
// import net.botwithus.rs3.game.hud.Dialog;
import net.botwithus.rs3.vars.VarDomain;
import net.botwithus.rs3.world.Locatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.botwithus.rs3.world.Distance;
import net.botwithus.xapi.util.BwuDistance;
import java.util.Arrays;
import java.util.HashSet;

public class BwuPlayer {

    private static final Logger logger = LoggerFactory.getLogger(BwuPlayer.class);

    /**
     * Helper method to get the current target using v2 API
     */
    private static PathingEntity getTarget(LocalPlayer player) {
        if (player == null) return null;

        EntityType targetType = player.getTargetType();
        int targetIndex = player.getTargetServerIndex();

        if (targetIndex <= 0) return null;

        if (targetType == EntityType.NPC_ENTITY) {
            return World.getNpc(targetIndex);
        } else if (targetType == EntityType.PLAYER_ENTITY) {
            return World.getPlayer(targetIndex);
        }

        return null;
    }

    public static int getBossKills() {
        return VarDomain.getVarValue(6437);
    }

    public static boolean isTargetting(PathingEntity npc) {
        var local = LocalPlayer.self();
        if (local == null)
            return false;
        var target = getTarget(local);
        return target != null && target.equals(npc); // && !Dialog.isOpen();
    }

    public static boolean isTargetting(String... npcName) {
        var local = LocalPlayer.self();
        if (local == null) return false;

        PathingEntity target = getTarget(local);
        if (target == null) return false;

        String targetName = target.getName();
        return targetName != null && Arrays.asList(npcName).contains(targetName); // && !Dialog.isOpen();
    }

    public static boolean isTargettingNameContaining(String partial) {
        partial = partial.toLowerCase();
        var local = LocalPlayer.self();
        if (local == null) return false;

        PathingEntity target = getTarget(local);
        if (target == null) return false;

        String targetName = target.getName();
        return targetName != null && targetName.toLowerCase().contains(partial); // && !Dialog.isOpen();
    }

    public static boolean isInAnimation(HashSet<Integer> animations){
        return isInAnimation(animations, 2000);
    }

    public static boolean isCurrentAnimation(HashSet<Integer> animations) {
        var local = LocalPlayer.self();
        return local != null && animations.contains(local.getAnimationId()); // && !Dialog.isOpen();
    }

    public static boolean isInAnimation(HashSet<Integer> animations, int timeout) {
        // TODO: Implement delayUntil equivalent for v2 API
        var player = LocalPlayer.self();
        return player != null && animations.contains(player.getAnimationId()); // && !Dialog.isOpen();
    }

    public static boolean isInAnimation(int[] animationIds, int timeout) {
        // TODO: Implement delayUntil equivalent for v2 API
        var player = LocalPlayer.self();
        return player != null && Arrays.stream(animationIds).anyMatch(i -> i == player.getAnimationId()); // && !Dialog.isOpen();
    }

    public static boolean isAnimating(int timeout) {
        // TODO: Implement delayUntil equivalent for v2 API
        var player = LocalPlayer.self();
        return player != null && player.getAnimationId() != -1; // && !Dialog.isOpen();
    }

    public static boolean isInInstance() {
        try {
            var player = LocalPlayer.self();
            if (player == null) {
                return false;
            }
            var pCoord = player.getCoordinate();
            return pCoord.x() > 6400 || pCoord.y() > 12800;
        } catch (Exception e) {
            logger.info("Error checking if player is in instance: " + e.getMessage());
            return false;
        }
    }

    /**
     * @return the player's current health percentage 0-100
     */
    public static float getHealthPercent() {
        var player = LocalPlayer.self();
        if (player == null) {
            return 0;
        }
        return ((float) player.getHealth() / (float) player.getMaxHealth()) * 100;
    }


    public static boolean isPvpEnabled() {
        return VarDomain.getVarBitValue(52975) == 1;
    }

    public static boolean isStunned() {
        return (VarDomain.getVarClient(3748) - Client.getClientCycle()) > 0;
    }

    public static boolean isPoisoned() {
        return (VarDomain.getVarClient(4681) - Client.getClientCycle()) > 0;
    }

    public static boolean isInCombat() {
        return VarDomain.getVarBitValue(1899) != 0;
    }

    public static boolean isLocatableBetweenDestination(Locatable locatable, Locatable destination) {
        var player = LocalPlayer.self();
        if (player == null) {
            return false;
        }
        return BwuDistance.isLocatableBetween(player, locatable, destination) && BwuDistance.isLocatableCloser(player, locatable, destination);
    }
}