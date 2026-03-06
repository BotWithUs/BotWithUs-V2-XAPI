package net.botwithus.xapi.game.entity;

import com.botwithus.bot.api.model.LocalPlayer;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.util.BwuDistance;
import net.botwithus.xapi.util.position.Positionable;

import java.util.Arrays;
import java.util.HashSet;

public final class BwuPlayer {

    private BwuPlayer() {
    }

    public static int getBossKills() {
        return XApi.api().getVarp(6437);
    }

    public static boolean isTargetting(String... npcName) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        return player != null && Arrays.stream(npcName).anyMatch(name -> name.equalsIgnoreCase(player.overheadText()));
    }

    public static boolean isTargettingNameContaining(String partial) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        return player != null && player.overheadText() != null && player.overheadText().toLowerCase().contains(partial.toLowerCase());
    }

    public static boolean isInAnimation(HashSet<Integer> animations) {
        return isCurrentAnimation(animations);
    }

    public static boolean isCurrentAnimation(HashSet<Integer> animations) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        return player != null && animations.contains(player.animationId());
    }

    public static boolean isInAnimation(HashSet<Integer> animations, int timeout) {
        return isCurrentAnimation(animations);
    }

    public static boolean isInAnimation(int[] animationIds, int timeout) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        return player != null && Arrays.stream(animationIds).anyMatch(id -> id == player.animationId());
    }

    public static boolean isAnimating(int timeout) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        return player != null && player.animationId() != -1;
    }

    public static boolean isInInstance() {
        LocalPlayer player = XApi.api().getLocalPlayer();
        return player != null && (player.tileX() > 6400 || player.tileY() > 12800);
    }

    public static float getHealthPercent() {
        LocalPlayer player = XApi.api().getLocalPlayer();
        if (player == null || player.maxHealth() <= 0) {
            return 0;
        }
        return ((float) player.health() / player.maxHealth()) * 100f;
    }

    public static boolean isPvpEnabled() {
        return XApi.api().getVarbit(52975) == 1;
    }

    public static boolean isStunned() {
        return XApi.api().getVarcInt(3748) > 0;
    }

    public static boolean isPoisoned() {
        return XApi.api().getVarcInt(4681) > 0;
    }

    public static boolean isInCombat() {
        return XApi.api().getVarbit(1899) != 0;
    }

    public static boolean isLocatableBetweenDestination(Positionable locatable, Positionable destination) {
        LocalPlayer player = XApi.api().getLocalPlayer();
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
}
