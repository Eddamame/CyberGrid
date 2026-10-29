package io.github.cybergrid.simulation.engine.scene;

import io.github.cybergrid.simulation.engine.EngineContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class SceneFactory {

    private static final Map<SceneType, Function<EngineContext, Scene>> SCENES = new HashMap<>();

    private SceneFactory() {
    }

    public static void register(SceneType type, Function<EngineContext, Scene> constructor) {
        SCENES.put(type, constructor);
    }

    public static Scene create(SceneType type, EngineContext context) {
        Function<EngineContext, Scene> constructor = SCENES.get(type);
        if (constructor == null) {
            throw new RuntimeException("Scene not registered: " + type);
        }
        return constructor.apply(context);
    }
}
