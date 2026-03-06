package net.botwithus.xapi.script.ui;

import net.botwithus.scripts.Info;
import net.botwithus.ui.workspace.Workspace;
import net.botwithus.xapi.script.BwuScript;

public class BwuGraphicsContext {
    private final BwuScript script;
    private final Workspace workspace;

    public BwuGraphicsContext(BwuScript script, Workspace workspace) {
        this.script = script;
        this.workspace = workspace;
        Info info = script.getInfo();
        workspace.setName(info != null && !info.name().isBlank() ? info.name() : script.getName());
    }

    public void draw() {
        if (script.getBuildableUI() != null) {
            script.getBuildableUI().buildUI();
        }
        script.onDrawConfig(workspace);
    }
}
