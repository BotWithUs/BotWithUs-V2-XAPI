package net.botwithus.xapi.game.hud;

import net.botwithus.rs3.interfaces.Component;
import net.botwithus.rs3.interfaces.ComponentType;
import net.botwithus.rs3.interfaces.Interfaces;
import net.botwithus.rs3.minimenu.MiniMenu;
import net.botwithus.rs3.minimenu.Action;
import net.botwithus.xapi.query.ComponentQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Dialog class provides methods to interact with the dialogue interfaces in the game.
 * It allows checking if any dialogue interface is open, selecting options, retrieving options,
 * and interacting with the dialogue based on the current interface.
 */
public class Dialog {
    /**
     * Checks if any of the dialogue interfaces are open.
     *
     * @return True if any of the dialogue interfaces (1184, 1186, 1188, 1189, 1191) are open, false otherwise.
     */
    public static boolean isOpen() {
        return Interfaces.isOpen(1184) || Interfaces.isOpen(1186) || Interfaces.isOpen(1188) || Interfaces.isOpen(1189) || Interfaces.isOpen(1191);
    }

    /**
     * Checks if the dialogue interface is open and selects the appropriate option based on the current interface.
     *
     * @return True if the interaction was successful, false otherwise.
     */
    public static boolean select() {
        if (Interfaces.isOpen(1184)) {
            return MiniMenu.doAction(Action.DIALOGUE, 0, -1, 77594639) > 0;
        } else if (Interfaces.isOpen(1186)) {
            return MiniMenu.doAction(Action.DIALOGUE, 0, -1, 77725700) > 0;
        } else if (Interfaces.isOpen(1189)) {
            return MiniMenu.doAction(Action.DIALOGUE, 0, -1, 77922323) > 0;
        } else if (Interfaces.isOpen(1191)) {
            return MiniMenu.doAction(Action.DIALOGUE, 0, -1, 78053391) > 0;
        }
        return false;
    }

    /**
     * Retrieves the options from the current dialogue interface if it is open.
     *
     * @return A list of options from the dialogue interface, or an empty list if the interface is not open.
     */
    @NotNull
    public static List<String> getOptions() {
        if (Interfaces.isOpen(1188)) {
            List<String> options = new ArrayList<>(ComponentQuery.newQuery(1188).id(6,33,35,37,39).type(ComponentType.TEXT).results().stream().map(Component::getText).toList());
            options.removeIf(result -> result.getBytes(StandardCharsets.UTF_8).length < 1);
            return options;
        }
        return Collections.emptyList();
    }

    /**
     * Checks if the given string is present in the options of the current dialogue interface.
     *
     * @param string The string to check for in the options.
     * @return True if the string is found in the options, false otherwise.
     */
    public static boolean hasOption(String string) {
        return getOptions().stream().anyMatch(i -> i.contentEquals(string));
    }

    /**
     * Interacts with the dialogue interface using the text of the option.
     *
     * @param optionText The text of the option to interact with.
     *                   If the option is not found, it will return false.
     * @return True if the interaction was successful, false otherwise.
     */
    public static boolean interact(String optionText) {
        if (Interfaces.isOpen(1188)) {
            var result = ComponentQuery.newQuery(1188).type(ComponentType.TEXT).text(String::contentEquals, optionText).results().first();
            if (result != null) {
                int slot = -1;
                var options = getOptions();
                int size = options.size();
                for (int i = 0; i < size; i++) {
                    if (options.get(i).contains(optionText)) {
                        slot = i;
                    }
                }
                return interact(slot);
            }
        }
        return false;
    }

    /**
     * Interacts with the dialogue interface with the given index. The index is the position of the option in the dialogue, starting at 0.
     *
     * @param index The index of the option to interact with.
     *              If the index is -1, the first option will be selected.
     * @return True if the interaction was successful, false otherwise.
     */
    public static boolean interact(int index) {
        if (Interfaces.isOpen(1188)) {
            if (index != -1) {
                int[] opcode = new int[]{77856776, 77856781, 77856786, 77856791, 77856796};
                return MiniMenu.doAction(Action.DIALOGUE, 0, -1, opcode[index]) > 0;
            }
        }
        return false;
    }

    /**
     * Retrieves the text from a component if the interface is open.
     *
     * @return The text from the component, or an empty string if the interface is not open.
     */
    @Nullable
    public static String getText() {
        if (Interfaces.isOpen(1184)) {
            var result = ComponentQuery.newQuery(1184).id(10).results().first();
            if (result != null && result.getText() != null) {
                return result.getText();
            }
        } else if (Interfaces.isOpen(1189)) {
            var result = ComponentQuery.newQuery(1189).id(3).results().first();
            if (result != null && result.getText() != null) {
                return result.getText();

            }
        } else if (Interfaces.isOpen(1186)) {
            var result = ComponentQuery.newQuery(1186).id(3).results().first();
            if (result != null && result.getText() != null) {
                return result.getText();
            }
        }
        return null;
    }

    /**
     * Retrieves the title from the current dialogue interface if it is open.
     *
     * @return The title of the dialogue interface, or null if the interface is not open or the title is not found.
     */
    @Nullable
    public static String getTitle() {
        Component result = null;
        if (Interfaces.isOpen(1184)) {
            result = ComponentQuery.newQuery(1184).type(ComponentType.TEXT).results().first();
        } else if (Interfaces.isOpen(1188)) {
            result = ComponentQuery.newQuery(1188).type(ComponentType.TEXT).results().first();
        } else if (Interfaces.isOpen(1189)) {
            result = ComponentQuery.newQuery(1189).type(ComponentType.TEXT).results().first();
        } else if (Interfaces.isOpen(1191)) {
            result = ComponentQuery.newQuery(1191).type(ComponentType.TEXT).results().first();
        }
        return result != null ? result.getText() : null;
    }
}
