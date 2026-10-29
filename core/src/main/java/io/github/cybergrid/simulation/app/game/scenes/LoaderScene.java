package io.github.cybergrid.simulation.app.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.GameSession;
import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class LoaderScene extends Scene {
    private static final float COUNTDOWN_SECONDS = 3f;

    private Viewport viewport;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private GlyphLayout glyphLayout;
    private float timer;

    public LoaderScene(EngineContext context) {
        super(context);
        this.name = "LoaderScene";
    }

    @Override
    public void load() {
        if (isLoaded) {
            return;
        }
        viewport = new FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        spriteBatch = context.getSharedBatch();
        shapeRenderer = new ShapeRenderer();
        titleFont = GameUiTheme.getTitleFont();
        bodyFont = GameUiTheme.getMenuFont();
        glyphLayout = new GlyphLayout();
        timer = COUNTDOWN_SECONDS;
        context.getAudioManager().playSound("countdown.mp3");
        isLoaded = true;
    }

    @Override
    public void unload() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        isLoaded = false;
    }

    @Override
    public void update(float dt) {
        timer -= dt;
        if (timer <= 0f) {
            GameSession session = context.getService(GameSession.class);
            SceneType target = SceneType.START;
            if (session != null) {
                target = session.consumeLoaderTarget();
            }
            requestScene(target);
        }
    }

    @Override
    public void render() {
        if (spriteBatch == null || shapeRenderer == null || viewport == null) {
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.28f);
        shapeRenderer.rect(0f, 0f, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        int display = Math.max(1, (int) Math.ceil(timer));

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        bodyFont.setColor(GameUiTheme.WIN_COLOR);
        String header = "Mission starts in...";
        glyphLayout.setText(bodyFont, header);
        bodyFont.draw(spriteBatch, header, (GameConstants.WORLD_WIDTH - glyphLayout.width) / 2f, 470f);

        String countdown = String.valueOf(display);
        glyphLayout.setText(titleFont, countdown);
        titleFont.setColor(GameUiTheme.WIN_COLOR);
        titleFont.draw(spriteBatch, countdown, (GameConstants.WORLD_WIDTH - glyphLayout.width) / 2f, 400f);
        spriteBatch.end();
    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }
}



