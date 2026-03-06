package net.botwithus.xapi.game.traversal;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.Component;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.game.traversal.enums.LodestoneType;
import net.botwithus.xapi.query.ComponentQuery;
import net.botwithus.xapi.script.permissive.base.PermissiveScript;

public final class LodestoneNetwork {

    private LodestoneNetwork() {
    }

    public static boolean isOpen(GameAPI api) {
        return api.isInterfaceOpen(1092);
    }

    public static boolean isOpen() {
        return isOpen(XApi.api());
    }

    public static boolean isAvailable(GameAPI api, LodestoneType type) {
        int result = api.getVarbit(type.getVarbitId());
        return switch (type) {
            case LUNAR_ISLE -> result >= 100;
            case BANDIT_CAMP -> result >= 15;
            default -> result == 1;
        };
    }

    public static boolean isAvailable(LodestoneType type) {
        return isAvailable(XApi.api(), type);
    }

    public static boolean open(GameAPI api) {
        Component component = ComponentQuery.newQuery(api, 1465).option("Lodestone network").results().first();
        if (component == null) {
            return false;
        }
        api.queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1,
                component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
        return true;
    }

    public static boolean open() {
        return open(XApi.api());
    }

    public static boolean teleport(GameAPI api, PermissiveScript script, LodestoneType type) {
        if (!isOpen(api) && !open(api)) {
            return false;
        }
        Component component = ComponentQuery.newQuery(api, LodestoneType.getInterfaceId()).id(type.getComponentId()).results().first();
        if (component == null) {
            return false;
        }
        api.queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1,
                component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
        script.delay(20);
        return true;
    }

    public static boolean teleport(PermissiveScript script, LodestoneType type) {
        return teleport(XApi.api(), script, type);
    }

    public static boolean teleportToPreviousDestination(GameAPI api) {
        Component component = ComponentQuery.newQuery(api, 1465).option("Previous Destination").results().first();
        if (component == null) {
            return false;
        }
        api.queueAction(new com.botwithus.bot.api.model.GameAction(com.botwithus.bot.api.inventory.ActionTypes.COMPONENT, 1,
                component.subComponentId(), component.interfaceId() << 16 | component.componentId()));
        return true;
    }

    public static boolean teleportToPreviousDestination() {
        return teleportToPreviousDestination(XApi.api());
    }
}
