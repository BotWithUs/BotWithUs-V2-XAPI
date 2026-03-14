package net.botwithus.xapi.game;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.LoginState;
import com.botwithus.bot.api.model.World;
import net.botwithus.xapi.XApi;

import java.util.Collections;
import java.util.List;

public final class BwuWorld {

    private BwuWorld() {
    }

    public static World getCurrent(GameAPI api) {
        return api.getCurrentWorld();
    }

    public static World getCurrent() {
        return getCurrent(XApi.api());
    }

    public static List<World> getAll(GameAPI api) {
        List<World> worlds = api.queryWorlds(true);
        return worlds == null ? Collections.emptyList() : worlds;
    }

    public static List<World> getAll() {
        return getAll(XApi.api());
    }

    public static void hop(GameAPI api, int worldId) {
        api.setWorld(worldId);
    }

    public static void hop(int worldId) {
        hop(XApi.api(), worldId);
    }

    public static LoginState getLoginState(GameAPI api) {
        return api.getLoginState();
    }

    public static LoginState getLoginState() {
        return getLoginState(XApi.api());
    }
}
