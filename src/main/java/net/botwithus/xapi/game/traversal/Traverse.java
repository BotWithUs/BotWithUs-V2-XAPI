package net.botwithus.xapi.game.traversal;

import com.botwithus.bot.api.inventory.ActionTypes;
import com.botwithus.bot.api.model.GameAction;
import com.botwithus.bot.api.model.LocalPlayer;
import net.botwithus.xapi.XApi;

import java.util.concurrent.ThreadLocalRandom;

public final class Traverse {

    private static final int MAX_LOCAL_DISTANCE = 80;
    private static final int MAX_STEP_SIZE = 16;
    private static final int MIN_STEP_SIZE = 10;

    private Traverse() {
    }

    public static boolean to(int tileX, int tileY, int plane) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        if (player == null || player.plane() != plane) {
            return false;
        }
        int distance = Math.max(Math.abs(tileX - player.tileX()), Math.abs(tileY - player.tileY()));
        boolean useMinimap = distance >= ThreadLocalRandom.current().nextInt(22, 28);
        int stepSize = ThreadLocalRandom.current().nextInt(MIN_STEP_SIZE, MAX_STEP_SIZE + 1);
        return bresenhamTo(tileX, tileY, plane, useMinimap, stepSize);
    }

    public static boolean bresenhamTo(int tileX, int tileY, int plane, boolean minimap, int stepSize) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        if (player == null || player.plane() != plane) {
            return false;
        }
        int dx = tileX - player.tileX();
        int dy = tileY - player.tileY();
        int distance = (int) Math.hypot(dx, dy);
        if (distance > stepSize) {
            int stepX = player.tileX() + dx * stepSize / distance;
            int stepY = player.tileY() + dy * stepSize / distance;
            return walkTo(stepX, stepY, plane, minimap);
        }
        return walkTo(tileX, tileY, plane, minimap);
    }

    public static boolean walkTo(int tileX, int tileY, int plane, boolean minimap) {
        LocalPlayer player = XApi.api().getLocalPlayer();
        if (player == null || player.plane() != plane) {
            return false;
        }
        int distance = Math.max(Math.abs(tileX - player.tileX()), Math.abs(tileY - player.tileY()));
        if (distance < 2) {
            return true;
        }
        if (distance > MAX_LOCAL_DISTANCE) {
            return bresenhamTo(tileX, tileY, plane, minimap, ThreadLocalRandom.current().nextInt(MIN_STEP_SIZE, MAX_STEP_SIZE));
        }
        XApi.api().queueAction(new GameAction(ActionTypes.WALK, minimap ? 1 : 0, tileX, tileY));
        return true;
    }
}
