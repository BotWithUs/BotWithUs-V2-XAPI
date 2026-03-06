package net.botwithus.xapi.game.hud;

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

    public static boolean isOpen() {
        for (int interfaceId : DIALOG_INTERFACES) {
            if (XApi.api().isInterfaceOpen(interfaceId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean select() {
        if (!isOpen()) {
            return false;
        }
        XApi.api().queueAction(new GameAction(ActionTypes.DIALOGUE, 0, -1, resolveDefaultHash()));
        return true;
    }

    public static List<String> getOptions() {
        if (!XApi.api().isInterfaceOpen(1188)) {
            return Collections.emptyList();
        }
        List<String> options = new ArrayList<>();
        for (Component component : ComponentQuery.newQuery(1188).id(6, 33, 35, 37, 39).results()) {
            String text = XApi.api().getComponentText(component.interfaceId(), component.componentId());
            if (text != null && !text.isBlank()) {
                options.add(text);
            }
        }
        return options;
    }

    public static boolean hasOption(String string) {
        return getOptions().stream().anyMatch(option -> option.contentEquals(string));
    }

    public static boolean interact(String optionText) {
        List<String> options = getOptions();
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).contains(optionText)) {
                return interact(i);
            }
        }
        return false;
    }

    public static boolean interact(int index) {
        if (!XApi.api().isInterfaceOpen(1188) || index < 0 || index >= OPTION_HASHES.length) {
            return false;
        }
        XApi.api().queueAction(new GameAction(ActionTypes.DIALOGUE, 0, -1, OPTION_HASHES[index]));
        return true;
    }

    public static String getText() {
        if (XApi.api().isInterfaceOpen(1184)) {
            return text(1184, 10);
        }
        if (XApi.api().isInterfaceOpen(1189)) {
            return text(1189, 3);
        }
        if (XApi.api().isInterfaceOpen(1186)) {
            return text(1186, 3);
        }
        return null;
    }

    public static String getTitle() {
        for (int interfaceId : DIALOG_INTERFACES) {
            if (XApi.api().isInterfaceOpen(interfaceId)) {
                Component component = ComponentQuery.newQuery(interfaceId).results().first();
                if (component != null) {
                    return text(component.interfaceId(), component.componentId());
                }
            }
        }
        return null;
    }

    private static String text(int interfaceId, int componentId) {
        return XApi.api().getComponentText(interfaceId, componentId);
    }

    private static int resolveDefaultHash() {
        if (XApi.api().isInterfaceOpen(1184)) return 77594639;
        if (XApi.api().isInterfaceOpen(1186)) return 77725700;
        if (XApi.api().isInterfaceOpen(1189)) return 77922323;
        if (XApi.api().isInterfaceOpen(1191)) return 78053391;
        return 0;
    }
}
