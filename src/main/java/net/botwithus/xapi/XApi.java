package net.botwithus.xapi;

import com.botwithus.bot.api.Client;
import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.ScriptContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class XApi {

    private static final ThreadLocal<ScriptContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<GameAPI> CURRENT_API = new ThreadLocal<>();

    private XApi() {
    }

    public static void bind(ScriptContext context) {
        ScriptContext value = Objects.requireNonNull(context, "context");
        CONTEXT.set(value);
        CURRENT_API.set(value.getGameAPI());
    }

    public static void bind(GameAPI api) {
        CURRENT_API.set(Objects.requireNonNull(api, "api"));
    }

    public static void clear() {
        CURRENT_API.remove();
        CONTEXT.remove();
    }

    public static ScriptContext context() {
        ScriptContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("No active ScriptContext is bound to the current thread");
        }
        return context;
    }

    public static GameAPI api() {
        GameAPI api = CURRENT_API.get();
        if (api != null) {
            return api;
        }
        return context().getGameAPI();
    }

    public static GameAPI api(String clientName) {
        return client(clientName)
                .map(Client::getGameAPI)
                .orElseThrow(() -> new IllegalArgumentException("Unknown client: " + clientName));
    }

    public static Optional<Client> client(String clientName) {
        return context().getClientProvider().getClient(clientName);
    }

    public static Collection<Client> clients() {
        return context().getClientProvider().getClients();
    }

    public static Map<String, GameAPI> apis() {
        return clients().stream().collect(Collectors.toUnmodifiableMap(Client::getName, Client::getGameAPI));
    }

    public static <T> T using(GameAPI api, Supplier<T> action) {
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(action, "action");
        GameAPI previous = CURRENT_API.get();
        CURRENT_API.set(api);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT_API.remove();
            } else {
                CURRENT_API.set(previous);
            }
        }
    }

    public static void using(GameAPI api, Runnable action) {
        using(api, () -> {
            action.run();
            return null;
        });
    }
}
