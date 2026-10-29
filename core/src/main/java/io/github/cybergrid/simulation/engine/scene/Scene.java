package io.github.cybergrid.simulation.engine.scene;

import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.interfaces.Transition;

public abstract class Scene {

    protected final EngineContext context;
    protected String name;
    protected boolean isLoaded = false;
    protected Transition transition = null;
    protected SceneType nextScene = null;

    protected Scene(EngineContext context) {
        this.context = context;
    }

    public abstract void load();

    public abstract void unload();

    public abstract void update(float dt);

    public abstract void render();

    public void renderOverlay() {
    }

    public boolean runsSimulationSystems() {
        return false;
    }

    public boolean rendersWorldEntities() {
        return runsSimulationSystems();
    }

    public void onSuspend() {
    }

    public void onResume() {
    }

    public SceneState getState() {
        return SceneState.RECREATE;
    }

    public SceneState getState(SceneType nextSceneType) {
        return getState();
    }

    public void requestScene(SceneType type) {
        nextScene = type;
    }

    public SceneType getRequestedScene() {
        return nextScene;
    }

    public void clearRequests() {
        nextScene = null;
    }

    public void setTransition(Transition t) {
        this.transition = t;
    }

    public Transition getTransition() {
        return this.transition;
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public abstract Viewport getViewport();

    public void resize(int width, int height) {
    }
}
