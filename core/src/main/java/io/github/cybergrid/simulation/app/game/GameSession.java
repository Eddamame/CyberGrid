package io.github.cybergrid.simulation.app.game;

import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.cybergrid.simulation.app.game.combat.HealthManager;
import io.github.cybergrid.simulation.app.game.entities.GameEntityBootstrap;
import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.app.game.events.PlayerDeathEvent;
import io.github.cybergrid.simulation.app.game.events.VulnerableServerDiscoveredEvent;
import io.github.cybergrid.simulation.app.game.ui.HeartsHud;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class GameSession {
    private final EngineContext context;
    private final GameEventBus gameEventBus;
    private final HealthManager healthManager;
    private final HeartsHud heartsHud;

    private int persistentPlayerHealth = 0;
    private SceneType loaderTargetScene = SceneType.START;
    private SceneType briefingBypassScene = null;

    public GameSession(EngineContext context) {
        this.context = context;

        gameEventBus = new GameEventBus();
        GameEntityBootstrap.initialize(context, gameEventBus);

        healthManager = new HealthManager(context.getEventManager(), gameEventBus);
        heartsHud = new HeartsHud(gameEventBus, new FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT), GameConstants.PLAYER_MAX_HEALTH);

        gameEventBus.subscribe(PlayerDeathEvent.class, playerDeathListener);
        gameEventBus.subscribe(VulnerableServerDiscoveredEvent.class, vulnerableServerDiscoveredListener);
    }

    public int getPersistentPlayerHealth() {
        return persistentPlayerHealth;
    }

    public void setPersistentPlayerHealth(int health) {
        this.persistentPlayerHealth = health;
    }

    public void resetRun() {
        persistentPlayerHealth = 0;
        loaderTargetScene = SceneType.START;
        briefingBypassScene = null;
        heartsHud.reset();
    }

    public void prepareLoader(SceneType targetScene) {
        prepareLoader(targetScene, true);
    }

    public void prepareLoader(SceneType targetScene, boolean bypassBriefingOnLoad) {
        if (targetScene == null) {
            return;
        }
        loaderTargetScene = targetScene;
        briefingBypassScene = bypassBriefingOnLoad ? targetScene : null;
    }

    public SceneType consumeLoaderTarget() {
        SceneType target = loaderTargetScene;
        loaderTargetScene = SceneType.START;
        return target;
    }

    public boolean consumeBriefingBypass(SceneType sceneType) {
        if (briefingBypassScene == sceneType) {
            briefingBypassScene = null;
            return true;
        }
        return false;
    }

    public void resizeHud(int width, int height) {
        heartsHud.resize(width, height);
    }

    public GameEventBus getGameEventBus() {
        return gameEventBus;
    }

    public HeartsHud getHeartsHud() {
        return heartsHud;
    }

    public void dispose() {
        gameEventBus.unsubscribe(PlayerDeathEvent.class, playerDeathListener);
        gameEventBus.unsubscribe(VulnerableServerDiscoveredEvent.class, vulnerableServerDiscoveredListener);
        healthManager.dispose();
        heartsHud.dispose();
    }

    private final IEventListener<PlayerDeathEvent> playerDeathListener =
        event -> endGame();

    private final IEventListener<VulnerableServerDiscoveredEvent> vulnerableServerDiscoveredListener =
        event -> onVulnerableServerDiscovered();

    private void endGame() {
        context.getAudioManager().stopMusic();
        requestCurrentScene(SceneType.LOSE);
    }

    private void onVulnerableServerDiscovered() {
        context.getAudioManager().playSound("vuln_server_discovery.mp3");
    }

    private void requestCurrentScene(SceneType targetScene) {
        Scene currentScene = context.getSceneManager().getCurrentScene();
        if (currentScene != null) {
            currentScene.requestScene(targetScene);
        }
    }
}
