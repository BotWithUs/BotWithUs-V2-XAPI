package net.botwithus.xapi.game.traversal;

import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.NpcQuery;

public final class MagicCarpetNetwork {

    private MagicCarpetNetwork() {
    }

    public static boolean isOpen() {
        return XApi.api().isInterfaceOpen(1928);
    }

    public static boolean open() {
        var npc = NpcQuery.newQuery().name("Rug merchant").option("Travel").results().nearest();
        return npc != null && npc.interact("Travel");
    }
}
