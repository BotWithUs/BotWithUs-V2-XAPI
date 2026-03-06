package net.botwithus.xapi;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.ScriptContext;

import java.util.Objects;

public final class XApi {

    private static final ThreadLocal<ScriptContext> CONTEXT = new ThreadLocal<>();

    private XApi() {
    }

    public static void bind(ScriptContext context) {
        CONTEXT.set(Objects.requireNonNull(context, "context"));
    }

    public static void clear() {
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
        return context().getGameAPI();
    }
}
