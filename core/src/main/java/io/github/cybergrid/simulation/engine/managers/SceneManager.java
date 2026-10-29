package io.github.cybergrid.simulation.engine.managers;

import java.util.HashMap;
import java.util.Map;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.events.ScenePauseEvent;
import io.github.cybergrid.simulation.engine.events.SceneResumeEvent;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneFactory;
import io.github.cybergrid.simulation.engine.scene.SceneState;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class SceneManager {

    private Scene currentScene = null;
    private SceneType currentSceneType = null;
    private SceneType previousSceneType = null;
    private final Map<SceneType, Scene> keptAliveScenes = new HashMap<>();
    private final Map<SceneType, Scene> pausedScenes = new HashMap<>();

    public void changeScene(SceneType newType, EngineContext context) {
        if (newType == null || context == null) {
            return;
        }

        if (currentSceneType != null && currentSceneType != newType) {
            previousSceneType = currentSceneType;
        }

        boolean resumingPausedScene = pausedScenes.containsKey(newType);
        Scene nextScene = resolveScene(newType, context);
        if (nextScene == null) {
            return;
        }

        if (currentScene != null && currentScene != nextScene) {
            applyExitPolicy(currentSceneType, currentScene, newType, context);
        }

        currentScene = nextScene;
        currentSceneType = newType;
        String sceneLabel = describeScene(currentScene, currentSceneType);

        if (!currentScene.isLoaded()) {
            System.out.println("[Scene Manager] Initializing scene: " + sceneLabel);
            currentScene.load();
            System.out.println("[Scene Manager] Scene initialized: " + sceneLabel);
        } else if (resumingPausedScene) {
            System.out.println("[Scene Manager] Resuming scene: " + sceneLabel);
        } else {
            System.out.println("[Scene Manager] Entering scene: " + sceneLabel);
        }

        if (resumingPausedScene) {
            pausedScenes.remove(newType);
            context.getEventManager().dispatch(new SceneResumeEvent());
            currentScene.onResume();
        }

        if (currentScene.getTransition() != null) {
            currentScene.getTransition().start();
        }
    }

    private Scene resolveScene(SceneType newType, EngineContext context) {
        Scene paused = pausedScenes.get(newType);
        if (paused != null) {
            return paused;
        }

        Scene keptAlive = keptAliveScenes.get(newType);
        if (keptAlive != null) {
            return keptAlive;
        }

        return SceneFactory.create(newType, context);
    }

    private void applyExitPolicy(
            SceneType previousType,
            Scene previousScene,
            SceneType nextType,
            EngineContext context
    ) {
        SceneState persistence = previousScene.getState(nextType);
        switch (persistence) {
            case KEEP_ALIVE:
                if (previousType != null) {
                    keptAliveScenes.put(previousType, previousScene);
                    pausedScenes.remove(previousType);
                }
                break;
            case PAUSE_AND_RESUME:
                previousScene.onSuspend();
                context.getEventManager().dispatch(new ScenePauseEvent());
                if (previousType != null) {
                    pausedScenes.put(previousType, previousScene);
                    keptAliveScenes.remove(previousType);
                }
                break;
            case RECREATE:
            default:
                System.out.println("[Scene Manager] Unloading scene: " + describeScene(previousScene, previousType));
                previousScene.unload();
                if (previousType != null) {
                    keptAliveScenes.remove(previousType);
                    pausedScenes.remove(previousType);
                }
                break;
        }
    }

    private String describeScene(Scene scene, SceneType type) {
        if (scene == null) {
            return type == null ? "UnknownScene" : type.name();
        }

        String sceneName = scene.getName();
        if (sceneName == null || sceneName.isBlank()) {
            sceneName = scene.getClass().getSimpleName();
        }

        if (type == null) {
            return sceneName;
        }
        return sceneName + " [" + type.name() + "]";
    }

    public void update(float dt) {
        if (currentScene != null) {
            currentScene.update(dt);
        }
    }

    public void render() {
        if (currentScene != null) {
            currentScene.render();
        }
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public SceneType getCurrentSceneType() {
        return currentSceneType;
    }

    public SceneType getPreviousSceneType() {
        return previousSceneType;
    }

    public void resize(int width, int height) {
        if (currentScene != null) {
            currentScene.resize(width, height);
        }
    }
}
