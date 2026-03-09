package net.botwithus.xapi.util;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.ScreenPosition;
import net.botwithus.xapi.XApi;

import java.util.List;

public final class Projection {

    private Projection() {
    }

    public static ScreenPosition worldToScreen(GameAPI api, int tileX, int tileY) {
        return api.getWorldToScreen(tileX, tileY);
    }

    public static ScreenPosition worldToScreen(int tileX, int tileY) {
        return worldToScreen(XApi.api(), tileX, tileY);
    }

    public static List<ScreenPosition> batchWorldToScreen(GameAPI api, List<int[]> coordinates) {
        return api.batchWorldToScreen(coordinates);
    }

    public static List<ScreenPosition> batchWorldToScreen(List<int[]> coordinates) {
        return batchWorldToScreen(XApi.api(), coordinates);
    }
}
