package net.botwithus.xapi.game.hud;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.MiniMenuEntry;
import net.botwithus.xapi.XApi;

import java.util.Collections;
import java.util.List;

public final class MiniMenuHelper {

    private MiniMenuHelper() {
    }

    public static List<MiniMenuEntry> getEntries(GameAPI api) {
        List<MiniMenuEntry> entries = api.getMiniMenu();
        return entries == null ? Collections.emptyList() : entries;
    }

    public static List<MiniMenuEntry> getEntries() {
        return getEntries(XApi.api());
    }

    public static boolean hasEntry(GameAPI api, String optionText) {
        return getEntries(api).stream()
                .anyMatch(entry -> entry.optionText() != null && entry.optionText().equalsIgnoreCase(optionText));
    }

    public static boolean hasEntry(String optionText) {
        return hasEntry(XApi.api(), optionText);
    }
}
