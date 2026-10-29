package io.github.cybergrid.simulation.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.scenes.AttackScene;
import io.github.cybergrid.simulation.app.game.scenes.EscapeScene;
import io.github.cybergrid.simulation.app.game.scenes.GuideScene;
import io.github.cybergrid.simulation.app.game.scenes.LoaderScene;
import io.github.cybergrid.simulation.app.game.scenes.LoseScene;
import io.github.cybergrid.simulation.app.game.scenes.MissionScene;
import io.github.cybergrid.simulation.app.game.scenes.ScanningScene;
import io.github.cybergrid.simulation.app.game.scenes.SettingsScene;
import io.github.cybergrid.simulation.app.game.scenes.StartScene;
import io.github.cybergrid.simulation.app.game.scenes.SummaryScene;
import io.github.cybergrid.simulation.app.game.scenes.WinScene;
import io.github.cybergrid.simulation.app.game.GameSession;
import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.render.EntityRenderSystem;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneFactory;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private BitmapFont font;
    private EngineContext context;
    private EntityRenderSystem entityRenderSystem;
    private GameSession gameSession;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        context = new EngineContext();
        context.setSharedRenderResources(batch, font);
        entityRenderSystem = new EntityRenderSystem(context);
        GameUiTheme.initialize();
        gameSession = new GameSession(context);
        context.registerService(GameSession.class, gameSession);
        registerScenes();
        context.getSceneManager().changeScene(SceneType.START, context);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        Scene currentScene = context.getSceneManager().getCurrentScene();
        if (currentScene == null) {
            return;
        }

        handleSceneRequest(currentScene);
        currentScene = context.getSceneManager().getCurrentScene();
        if (currentScene == null) {
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (currentScene.runsSimulationSystems()) {
            context.getEntityManager().update(dt);
            Viewport viewport = currentScene.getViewport();
            if (viewport != null) {
                context.getMovementManager().update(dt, viewport.getWorldWidth(), viewport.getWorldHeight());
            }
            context.getCollisionManager().update();
        }

        context.getSceneManager().update(dt);
        currentScene = context.getSceneManager().getCurrentScene();
        if (currentScene == null) {
            return;
        }

        handleSceneRequest(currentScene);
        currentScene = context.getSceneManager().getCurrentScene();
        if (currentScene == null) {
            return;
        }

        context.getSceneManager().render();

        if (currentScene.rendersWorldEntities() && currentScene.getViewport() != null) {
            entityRenderSystem.renderWorld(currentScene.getViewport());
            gameSession.getHeartsHud().render();
        }

        currentScene.renderOverlay();
    }

    private void registerScenes() {
        SceneFactory.register(SceneType.START, StartScene::new);
        SceneFactory.register(SceneType.MAIN, ScanningScene::new);
        SceneFactory.register(SceneType.SETTINGS, SettingsScene::new);
        SceneFactory.register(SceneType.MISSION, MissionScene::new);
        SceneFactory.register(SceneType.GUIDE, GuideScene::new);
        SceneFactory.register(SceneType.ESCAPE, EscapeScene::new);
        SceneFactory.register(SceneType.ATTACK, AttackScene::new);
        SceneFactory.register(SceneType.LOADER, LoaderScene::new);
        SceneFactory.register(SceneType.WIN, WinScene::new);
        SceneFactory.register(SceneType.LOSE, LoseScene::new);
        SceneFactory.register(SceneType.SUMMARY, SummaryScene::new);
    }

    private void handleSceneRequest(Scene scene) {
        SceneType requested = scene.getRequestedScene();
        if (requested == null) {
            return;
        }

        scene.clearRequests();
        context.getSceneManager().changeScene(requested, context);
    }

    @Override
    public void resize(int width, int height) {
        if (context != null) {
            context.getSceneManager().resize(width, height);
        }
        if (gameSession != null) {
            gameSession.resizeHud(width, height);
        }
    }

    @Override
    public void dispose() {
        if (entityRenderSystem != null) {
            entityRenderSystem.dispose();
        }
        if (gameSession != null) {
            gameSession.dispose();
        }
        batch.dispose();
        font.dispose();
        if (context != null) {
            context.getAudioManager().dispose();
        }
        GameUiTheme.dispose();
    }
}
