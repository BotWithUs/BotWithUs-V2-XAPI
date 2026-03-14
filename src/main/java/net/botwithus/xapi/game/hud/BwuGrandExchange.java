package net.botwithus.xapi.game.hud;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.GrandExchangeOffer;
import net.botwithus.xapi.XApi;

import java.util.Collections;
import java.util.List;

public final class BwuGrandExchange {

    private static final int GE_INTERFACE_ID = 105;

    private BwuGrandExchange() {
    }

    public static boolean isOpen(GameAPI api) {
        return api.isInterfaceOpen(GE_INTERFACE_ID);
    }

    public static boolean isOpen() {
        return isOpen(XApi.api());
    }

    public static List<GrandExchangeOffer> getOffers(GameAPI api) {
        List<GrandExchangeOffer> offers = api.getGrandExchangeOffers();
        return offers == null ? Collections.emptyList() : offers;
    }

    public static List<GrandExchangeOffer> getOffers() {
        return getOffers(XApi.api());
    }

    public static GrandExchangeOffer findOffer(GameAPI api, int itemId) {
        return getOffers(api).stream()
                .filter(offer -> offer.itemId() == itemId)
                .findFirst()
                .orElse(null);
    }

    public static GrandExchangeOffer findOffer(int itemId) {
        return findOffer(XApi.api(), itemId);
    }

    public static boolean hasFreeSlot(GameAPI api) {
        return getOffers(api).stream().anyMatch(offer -> offer.status() == 0);
    }

    public static boolean hasFreeSlot() {
        return hasFreeSlot(XApi.api());
    }

    public static int getRemainingQuantity(GrandExchangeOffer offer) {
        if (offer == null) {
            return 0;
        }
        return offer.count() - offer.completedCount();
    }
}
