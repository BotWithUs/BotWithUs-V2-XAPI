package net.botwithus.xapi.game.hud;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.model.ChatMessage;
import net.botwithus.xapi.XApi;

import java.util.Collections;
import java.util.List;

public final class Chat {

    private Chat() {
    }

    public static List<ChatMessage> getHistory(GameAPI api, int messageType, int maxResults) {
        List<ChatMessage> history = api.queryChatHistory(messageType, maxResults);
        return history == null ? Collections.emptyList() : history;
    }

    public static List<ChatMessage> getHistory(int messageType, int maxResults) {
        return getHistory(XApi.api(), messageType, maxResults);
    }

    public static ChatMessage getLastMessage(GameAPI api) {
        List<ChatMessage> history = api.queryChatHistory(-1, 1);
        return history == null || history.isEmpty() ? null : history.get(0);
    }

    public static ChatMessage getLastMessage() {
        return getLastMessage(XApi.api());
    }

    public static ChatMessage getLastMessage(GameAPI api, int messageType) {
        List<ChatMessage> history = api.queryChatHistory(messageType, 1);
        return history == null || history.isEmpty() ? null : history.get(0);
    }

    public static ChatMessage getLastMessage(int messageType) {
        return getLastMessage(XApi.api(), messageType);
    }

    public static int getHistorySize(GameAPI api) {
        return api.getChatHistorySize();
    }

    public static int getHistorySize() {
        return getHistorySize(XApi.api());
    }
}
