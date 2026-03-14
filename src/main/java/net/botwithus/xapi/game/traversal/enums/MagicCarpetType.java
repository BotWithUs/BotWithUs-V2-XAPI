package net.botwithus.xapi.game.traversal.enums;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.inventory.ActionTypes;
import com.botwithus.bot.api.model.GameAction;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.game.traversal.MagicCarpetNetwork;

public enum MagicCarpetType {
    AL_KHARID(1928, 28),
    NORTH_POLLNIVNEACH(1928, 60),
    SOUTH_POLLNIVNEACH(1928, 68),
    NARDAH(1928, 68),
    BEDABIN_CAMP(1928, 44),
    UZER(1928, 52),
    MENAPHOS(1928, 84),
    SOPHANEM(1928, 92);

    private final int interfaceIndex;
    private final int componentIndex;

    MagicCarpetType(int interfaceIndex, int componentIndex) {
        this.interfaceIndex = interfaceIndex;
        this.componentIndex = componentIndex;
    }

    public int getId() {
        return interfaceIndex << 16 | componentIndex;
    }

    public boolean teleport(GameAPI api) {
        if (!MagicCarpetNetwork.isOpen(api) && !MagicCarpetNetwork.open(api)) {
            return false;
        }
        api.queueAction(new GameAction(ActionTypes.COMPONENT, 1, -1, getId()));
        return true;
    }

    public boolean teleport() {
        return teleport(XApi.api());
    }
}
