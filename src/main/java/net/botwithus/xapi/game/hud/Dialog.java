package net.botwithus.xapi.game.hud;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.inventory.ActionTypes;
import com.botwithus.bot.api.model.Component;
import com.botwithus.bot.api.model.GameAction;
import net.botwithus.xapi.XApi;
import net.botwithus.xapi.query.ComponentQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Dialog {

    private static final int[] DIALOG_INTERFACES = {1184, 1186, 1188, 1189, 1191};
    private static final int[] OPTION_HASHES = {77856776, 77856781, 77856786, 77856791, 77856796};

    private Dialog() {
    }

    public static boolean isOpen(GameAPI api) {
        for (int interfaceId : DIALOG_INTERFACES) {
            if (api.isInterfaceOpen(interfaceId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOpen() {
        return isOpen(XApi.api());
    }

    public static boolean select(GameAPI api) {
        if (!isOpen(api)) {
            return false;
        }
        api.queueAction(new GameAction(ActionTypes.DIALOGUE, 0, -1, resolveDefaultHash(api)));
        return true;
    }

    public static boolean select() {
        return select(XApi.api());
    }

    public static List<String> getOptions(GameAPI api) {
        if (!api.isInterfaceOpen(1188)) {
            return Collections.emptyList();
        }
        List<String> options = new ArrayList<>();
        for (Component component : ComponentQuery.newQuery(api, 1188).id(6, 33, 35, 37, 39).results()) {
            String text = api.getComponentText(component.interfaceId(), component.componentId());
            if (text != null && !text.isBlank()) {
                options.add(text);
            }
        }
        return options;
    }

    public static List<String> getOptions() {
        return getOptions(XApi.api());
    }

    public static boolean hasOption(String string) {
        return getOptions().stream().anyMatch(option -> option.contentEquals(string));
    }

    public static boolean interact(GameAPI api, String optionText) {
        List<String> options = getOptions(api);
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).contains(optionText)) {
                return interact(api, i);
            }
        }
        return false;
    }

    public static boolean interact(String optionText) {
        return interact(XApi.api(), optionText);
    }

    public static boolean interact(int index) {
        return interact(XApi.api(), index);
    }

    public static boolean interact(GameAPI api, int index) {
        if (!api.isInterfaceOpen(1188) || index < 0 || index >= OPTION_HASHES.length) {
            return false;
        }
        api.queueAction(new GameAction(ActionTypes.DIALOGUE, 0, -1, OPTION_HASHES[index]));
        return true;
    }

    public static String getText() {
        return getText(XApi.api());
    }

    public static String getText(GameAPI api) {
        if (api.isInterfaceOpen(1184)) {
            return text(api, 1184, 10);
        }
        if (api.isInterfaceOpen(1189)) {
            return text(api, 1189, 3);
        }
        if (api.isInterfaceOpen(1186)) {
            return text(api, 1186, 3);
        }
        return null;
    }

    public static String getTitle() {
        return getTitle(XApi.api());
    }

    public static String getTitle(GameAPI api) {
        for (int interfaceId : DIALOG_INTERFACES) {
            if (api.isInterfaceOpen(interfaceId)) {
                Component component = ComponentQuery.newQuery(api, interfaceId).results().first();
                if (component != null) {
                    return text(api, component.interfaceId(), component.componentId());
                }
            }
        }
        return null;
    }

    private static String text(GameAPI api, int interfaceId, int componentId) {
        return api.getComponentText(interfaceId, componentId);
    }

    private static int resolveDefaultHash(GameAPI api) {
        if (api.isInterfaceOpen(1184)) return 77594639;
        if (api.isInterfaceOpen(1186)) return 77725700;
        if (api.isInterfaceOpen(1189)) return 77922323;
        if (api.isInterfaceOpen(1191)) return 78053391;
        return 0;
    }
}
