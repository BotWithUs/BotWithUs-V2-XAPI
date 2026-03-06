package net.botwithus.xapi.script;

import com.botwithus.bot.api.GameAPI;
import com.botwithus.bot.api.ScriptContext;
import com.botwithus.bot.api.model.InventoryItem;
import com.botwithus.bot.api.model.LocalPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.botwithus.scripts.Info;
import net.botwithus.ui.workspace.Workspace;
import net.botwithus.xapi.script.permissive.base.PermissiveScript;
import net.botwithus.xapi.script.ui.BwuGraphicsContext;
import net.botwithus.xapi.script.ui.interfaces.BuildableUI;
import net.botwithus.xapi.util.statistic.BotStat;
import net.botwithus.xapi.util.time.Stopwatch;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class BwuScript extends PermissiveScript {
    private BwuGraphicsContext graphicsContext;
    private Map<Integer, InventoryItem> previousInventory = Map.of();

    public Stopwatch STOPWATCH;
    public LocalPlayer player;
    public BotStat botStatInfo = new BotStat();

    public String getName() {
        return getClass().getSimpleName();
    }

    public Info getInfo() {
        return getClass().getAnnotation(Info.class);
    }

    public void onDraw(Workspace workspace) {
        if (graphicsContext == null) {
            graphicsContext = new BwuGraphicsContext(this, workspace);
        }
        graphicsContext.draw();
    }

    public abstract void onDrawConfig(Workspace workspace);

    @Override
    protected void onInitialize() {
        super.onInitialize();
        try {
            performLoadPersistentData();
        } catch (Exception e) {
            println("Failed to load persistent data");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreTick() {
        player = gameApi().getLocalPlayer();
        pollInventoryEvents();
        return super.onPreTick() && player != null;
    }

    @Override
    public void onActivation() {
        super.onActivation();
        if (STOPWATCH == null) {
            STOPWATCH = Stopwatch.startNew();
        } else {
            STOPWATCH.resume();
        }
    }

    @Override
    public void onDeactivation() {
        super.onDeactivation();
        if (STOPWATCH != null) {
            STOPWATCH.pause();
        }
        performSavePersistentData();
    }

    protected GameAPI gameApi() {
        return context().getGameAPI();
    }

    protected GameAPI gameApi(String clientName) {
        return net.botwithus.xapi.XApi.api(clientName);
    }

    protected ScriptContext scriptContext() {
        return context();
    }

    private void pollInventoryEvents() {
        Map<Integer, InventoryItem> current = new HashMap<>();
        for (InventoryItem item : gameApi().queryInventoryItems(
                com.botwithus.bot.api.query.InventoryFilter.builder()
                        .inventoryId(net.botwithus.xapi.game.inventory.Backpack.INVENTORY_ID)
                        .nonEmpty(false)
                        .build())) {
            current.put(item.slot(), item);
            InventoryItem previous = previousInventory.get(item.slot());
            if (previous == null) {
                continue;
            }
            if (previous.itemId() <= -1 && item.itemId() > -1) {
                onItemAcquired(item);
            } else if (previous.itemId() > -1 && item.itemId() <= -1) {
                onItemRemoved(previous);
            } else if (previous.itemId() != item.itemId() || previous.quantity() != item.quantity()) {
                onItemChange(previous, item);
            }
        }
        previousInventory = current;
    }

    public void performSavePersistentData() {
        try {
            JsonObject obj = new JsonObject();
            savePersistentData(obj);

            Path path = Paths.get(System.getProperty("user.home"), ".botwithus", "configs", getName() + "_settings.json");
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(obj, writer);
            }
        } catch (Exception e) {
            println("Failed to save persistent data");
            e.printStackTrace();
        }
    }

    public void performLoadPersistentData() {
        try {
            Path path = Paths.get(System.getProperty("user.home"), ".botwithus", "configs", getName() + "_settings.json");
            if (!Files.exists(path)) {
                return;
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                if (obj != null) {
                    loadPersistentData(obj);
                }
            }
        } catch (Exception e) {
            println("Failed to load persistent data");
            e.printStackTrace();
        }
    }

    public abstract BuildableUI getBuildableUI();

    public abstract void savePersistentData(JsonObject obj);

    public abstract void loadPersistentData(JsonObject obj);

    protected void onItemAcquired(InventoryItem item) {
    }

    protected void onItemRemoved(InventoryItem item) {
    }

    protected void onItemChange(InventoryItem previous, InventoryItem current) {
    }
}
