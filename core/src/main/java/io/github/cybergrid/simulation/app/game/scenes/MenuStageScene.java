package io.github.cybergrid.simulation.app.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.transition.FadeTransition;

public abstract class MenuStageScene extends Scene {
    protected Stage stage;
    protected ShapeRenderer shapeRenderer;
    private InputMultiplexer inputMultiplexer;

    protected MenuStageScene(EngineContext context, String sceneName, float transitionDuration) {
        super(context);
        this.name = sceneName;
        setTransition(new FadeTransition(transitionDuration));
    }

    @Override
    public final void load() {
        stage = new Stage(new FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT));
        shapeRenderer = new ShapeRenderer();
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return keycode == Input.Keys.ESCAPE;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);
        onLoadStage();
        isLoaded = true;
    }

    protected abstract void onLoadStage();

    @Override
    public void update(float dt) {
        if (transition != null && !transition.isFinished()) {
            transition.update(dt);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        renderTransition();
    }

    protected void renderTransition() {
        if (transition == null || transition.isFinished()) {
            return;
        }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, transition.getOverlayAlpha());
        shapeRenderer.rect(0f, 0f, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void unload() {
        onUnloadStage();
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        inputMultiplexer = null;
        isLoaded = false;
    }

    @Override
    public void onResume() {
        if (inputMultiplexer != null) {
            Gdx.input.setInputProcessor(inputMultiplexer);
        }
    }

    protected void onUnloadStage() {
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public Viewport getViewport() {
        return stage == null ? null : stage.getViewport();
    }
}





