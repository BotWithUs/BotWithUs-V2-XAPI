package net.botwithus.xapi.game.entity;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.LocalPlayer;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.util.BwuDistance;
import net.botwithus.xapi.util.position.Positionable;

import java.util.Arrays;
import java.util.HashSet;

public final class BwuPlayer {

    private BwuPlayer() {
    }

    public static int getBossKills(GameAPI api) {
        return api.getVarp(6437);
    }

    public static int getBossKills() {
        return getBossKills(XApi.api());
    }

    public static boolean isTargetting(GameAPI api, String... npcName) {
        LocalPlayer player = api.getLocalPlayer();
        return player != null && Arrays.stream(npcName).anyMatch(name -> name.equalsIgnoreCase(player.overheadText()));
    }

    public static boolean isTargetting(String... npcName) {
        return isTargetting(XApi.api(), npcName);
    }

    public static boolean isTargettingNameContaining(GameAPI api, String partial) {
        LocalPlayer player = api.getLocalPlayer();
        return player != null && player.overheadText() != null && player.overheadText().toLowerCase().contains(partial.toLowerCase());
    }

    public static boolean isTargettingNameContaining(String partial) {
        return isTargettingNameContaining(XApi.api(), partial);
    }

    public static boolean isInAnimation(HashSet<Integer> animations) {
        return isCurrentAnimation(XApi.api(), animations);
    }

    public static boolean isCurrentAnimation(GameAPI api, HashSet<Integer> animations) {
        LocalPlayer player = api.getLocalPlayer();
        return player != null && animations.contains(player.animationId());
    }

    public static boolean isCurrentAnimation(HashSet<Integer> animations) {
        return isCurrentAnimation(XApi.api(), animations);
    }

    public static boolean isInAnimation(GameAPI api, HashSet<Integer> animations, int timeout) {
        return isCurrentAnimation(api, animations);
    }

    public static boolean isInAnimation(HashSet<Integer> animations, int timeout) {
        return isInAnimation(XApi.api(), animations, timeout);
    }

    public static boolean isInAnimation(GameAPI api, int[] animationIds, int timeout) {
        LocalPlayer player = api.getLocalPlayer();
        return player != null && Arrays.stream(animationIds).anyMatch(id -> id == player.animationId());
    }

    public static boolean isInAnimation(int[] animationIds, int timeout) {
        return isInAnimation(XApi.api(), animationIds, timeout);
    }

    public static boolean isAnimating(GameAPI api, int timeout) {
        LocalPlayer player = api.getLocalPlayer();
        return player != null && player.animationId() != -1;
    }

    public static boolean isAnimating(int timeout) {
        return isAnimating(XApi.api(), timeout);
    }

    public static boolean isInInstance(GameAPI api) {
        LocalPlayer player = api.getLocalPlayer();
        return player != null && (player.tileX() > 6400 || player.tileY() > 12800);
    }

    public static boolean isInInstance() {
        return isInInstance(XApi.api());
    }

    public static float getHealthPercent(GameAPI api) {
        LocalPlayer player = api.getLocalPlayer();
        if (player == null || player.maxHealth() <= 0) {
            return 0;
        }
        return ((float) player.health() / player.maxHealth()) * 100f;
    }

    public static float getHealthPercent() {
        return getHealthPercent(XApi.api());
    }

    public static boolean isPvpEnabled(GameAPI api) {
        return api.getVarbit(52975) == 1;
    }

    public static boolean isPvpEnabled() {
        return isPvpEnabled(XApi.api());
    }

    public static boolean isStunned(GameAPI api) {
        return api.getVarcInt(3748) > 0;
    }

    public static boolean isStunned() {
        return isStunned(XApi.api());
    }

    public static boolean isPoisoned(GameAPI api) {
        return api.getVarcInt(4681) > 0;
    }

    public static boolean isPoisoned() {
        return isPoisoned(XApi.api());
    }

    public static boolean isInCombat(GameAPI api) {
        return api.getVarbit(1899) != 0;
    }

    public static boolean isInCombat() {
        return isInCombat(XApi.api());
    }

    public static boolean isLocatableBetweenDestination(GameAPI api, Positionable locatable, Positionable destination) {
        LocalPlayer player = api.getLocalPlayer();
        if (player == null) {
            return false;
        }
        Positionable playerPosition = new Positionable() {
            @Override
            public int x() {
                return player.tileX();
            }

            @Override
            public int y() {
                return player.tileY();
            }

            @Override
            public int plane() {
                return player.plane();
            }
        };
        return BwuDistance.isLocatableBetween(playerPosition, locatable, destination)
                && BwuDistance.isLocatableCloser(playerPosition, locatable, destination);
    }

    public static boolean isLocatableBetweenDestination(Positionable locatable, Positionable destination) {
        return isLocatableBetweenDestination(XApi.api(), locatable, destination);
    }
}
