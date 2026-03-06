package net.botwithus.xapi.game.traversal;

import com.botwithus.bot.api.model.Component;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.game.traversal.enums.LodestoneType;
import net.botwithus.xapi.query.ComponentQuery;
import net.botwithus.xapi.script.permissive.base.PermissiveScript;

public final class LodestoneNetwork {

    private LodestoneNetwork() {
    }

    public static boolean isOpen() {
        return XApi.api().isInterfaceOpen(1092);
    }

    public static boolean isAvailable(LodestoneType type) {
        int result = XApi.api().getVarbit(type.getVarbitId());
        return switch (type) {
            case LUNAR_ISLE -> result >= 100;
            case BANDIT_CAMP -> result >= 15;
            default -> result == 1;
        };
    }

    public static boolean open() {
        Component component = ComponentQuery.newQuery(1465).option("Lodestone network").results().first();
        if (component == null) {
            return false;
        }
        XApi.api().queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1, component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
        return true;
    }

    public static boolean teleport(PermissiveScript script, LodestoneType type) {
        if (!isOpen() && !open()) {
            return false;
        }
        Component component = ComponentQuery.newQuery(LodestoneType.getInterfaceId()).id(type.getComponentId()).results().first();
        if (component == null) {
            return false;
        }
        XApi.api().queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1, component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
        script.delay(20);
        return true;
    }

    public static boolean teleportToPreviousDestination() {
        Component component = ComponentQuery.newQuery(1465).option("Previous Destination").results().first();
        if (component == null) {
            return false;
        }
        XApi.api().queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1, component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
        return true;
    }
}
