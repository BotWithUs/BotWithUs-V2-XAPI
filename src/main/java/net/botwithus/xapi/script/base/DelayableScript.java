package net.botwithus.xapi.script.base;

import com.botwithus.bot.api.BotScript;
import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.ScriptContext;
import net.botwithus.xapi.XApi;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public abstract class DelayableScript implements BotScript {

    private ScriptContext context;
    private Callable<Boolean> delayUntil;
    private Callable<Boolean> delayWhile;
    private long delayUntilAt;

    @Override
    public final void onStart(ScriptContext ctx) {
        this.context = Objects.requireNonNull(ctx, "ctx");
        XApi.bind(ctx);
        onInitialize();
        onActivation();
    }

    @Override
    public final int onLoop() {
        XApi.bind(context);
        try {
            if (delayUntil != null) {
                if (delayUntil.call() || expired()) {
                    delayUntil = null;
                    delayUntilAt = 0L;
                } else {
                    return 50;
                }
            }

            if (delayWhile != null) {
                if (!delayWhile.call() || expired()) {
                    delayWhile = null;
                    delayUntilAt = 0L;
                } else {
                    return 50;
                }
            }

            if (delayUntilAt > 0L && !expired()) {
                return (int) Math.min(250L, Math.max(10L, delayUntilAt - System.currentTimeMillis()));
            }

            delayUntilAt = 0L;
            doRun();
            return Math.max(10, defaultLoopDelayMs());
        } catch (Exception e) {
            println("Exception: " + e.getMessage());
            e.printStackTrace();
            return Math.max(250, defaultLoopDelayMs());
        }
    }

    @Override
    public final void onStop() {
        try {
            onDeactivation();
        } finally {
            try {
                onShutdown();
            } finally {
                XApi.clear();
            }
        }
    }

    public void delayUntil(Callable<Boolean> condition, int timeoutTicks) {
        this.delayUntil = condition;
        this.delayWhile = null;
        this.delayUntilAt = System.currentTimeMillis() + ticksToMillis(timeoutTicks);
    }

    public void delayWhile(Callable<Boolean> condition, int timeoutTicks) {
        this.delayWhile = condition;
        this.delayUntil = null;
        this.delayUntilAt = System.currentTimeMillis() + ticksToMillis(timeoutTicks);
    }

    public void delay(int ticks) {
        this.delayUntil = null;
        this.delayWhile = null;
        this.delayUntilAt = System.currentTimeMillis() + ticksToMillis(ticks);
    }

    public void delay(int min, int max) {
        delay(ThreadLocalRandom.current().nextInt(min, max + 1));
    }

    protected long ticksToMillis(int ticks) {
        return Math.max(0L, ticks) * 600L;
    }

    protected boolean expired() {
        return delayUntilAt > 0L && System.currentTimeMillis() >= delayUntilAt;
    }

    protected int defaultLoopDelayMs() {
        return 300;
    }

    protected ScriptContext context() {
        return context;
    }

    protected GameAPI gameApi() {
        return context.getGameAPI();
    }

    protected GameAPI gameApi(String clientName) {
        return XApi.api(clientName);
    }

    protected void onInitialize() {
    }

    protected void onShutdown() {
    }

    public void onActivation() {
    }

    public void onDeactivation() {
    }

    public void println(String message) {
        System.out.println("[" + getClass().getSimpleName() + "] " + message);
    }

    public abstract void doRun();
}
