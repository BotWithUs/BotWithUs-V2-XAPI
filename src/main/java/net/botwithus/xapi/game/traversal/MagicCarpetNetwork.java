package net.botwithus.xapi.game.traversal;

import com.botwithus.bot.api.GameAPI;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.NpcQuery;

public final class MagicCarpetNetwork {

    private MagicCarpetNetwork() {
    }

    public static boolean isOpen(GameAPI api) {
        return api.isInterfaceOpen(1928);
    }

    public static boolean isOpen() {
        return isOpen(XApi.api());
    }

    public static boolean open(GameAPI api) {
        var npc = NpcQuery.newQuery(api).name("Rug merchant").option("Travel").results().nearest();
        return npc != null && npc.interact("Travel");
    }

    public static boolean open() {
        return open(XApi.api());
    }
}
