package io.github.cybergrid.simulation.app.game.scenes;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.entities.GameEntityBootstrap;
import io.github.cybergrid.simulation.app.game.entities.BulletEntity;
import io.github.cybergrid.simulation.app.game.entities.EnemyEntity;
import io.github.cybergrid.simulation.app.game.entities.FirewallEntity;
import io.github.cybergrid.simulation.app.game.entities.PlayerEntity;
import io.github.cybergrid.simulation.app.game.entities.ServerBossEntity;
import io.github.cybergrid.simulation.app.game.events.BulletImpactEvent;
import io.github.cybergrid.simulation.app.game.input.GameCommand;
import io.github.cybergrid.simulation.app.game.input.GameInputConfig;
import io.github.cybergrid.simulation.app.game.player.PlayerControl;
import io.github.cybergrid.simulation.app.game.GameSession;
import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.app.game.ui.InGameModalHelper;
import io.github.cybergrid.simulation.app.game.ui.ModalContent;
import io.github.cybergrid.simulation.app.game.ui.UiSoundHelper;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;
import io.github.cybergrid.simulation.engine.managers.CollisionManager;
import io.github.cybergrid.simulation.engine.managers.EntityManager;
import io.github.cybergrid.simulation.engine.managers.InputOutputManager;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneState;
import io.github.cybergrid.simulation.engine.scene.SceneType;
import io.github.cybergrid.simulation.engine.transition.FadeTransition;

public class AttackScene extends Scene {
    private static final float WORLD_EDGE_MARGIN = GameConstants.WORLD_MARGIN;
    private static final float PLAYER_Y = 100f;
    private static final float ENEMY_X = 120f;
    private static final float ENEMY_Y = 100f;
    private static final float ENEMY_BASE_SPEED = GameConstants.ENEMY_SPEED;
    private static final float FIREWALL_LENGTH_SCALE_MIN = 1.15f;
    private static final float FIREWALL_LENGTH_SCALE_MAX = 1.85f;
    private static final float FIREWALL_HEIGHT_SCALE_MIN = 1.15f;
    private static final float FIREWALL_HEIGHT_SCALE_MAX = 1.55f;
    private static final float VULNERABLE_SERVER_Y = 610f;
    private static final float VULNERABLE_SERVER_RING_RADIUS = 36f;
    private static final int VULNERABLE_SERVER_MAX_HEALTH = GameConstants.BOSS_MAX_HEALTH;
    private static final float PANEL_BTN_W = 90f;
    private static final float PANEL_BTN_H = 44f;
    private static final float START_BTN_W = 220f;
    private static final float START_BTN_H = 52f;
    private static final String ENRAGE_TEXT = "! ENEMY ENRAGED - MOVING FASTER !";
    private static final float ENRAGE_BANNER_DURATION = 3f;
    private static final String EXPLOITED_TEXT = "VULNERABLE SERVER EXPLOITED";
    private static final float EXPLOIT_SEQUENCE_DURATION = 3.6f;

    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final List<Integer> projectileIds = new ArrayList<>();
    private final int[] firewallIds = new int[4];

    private Viewport viewport;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont hudFont;
    private BitmapFont bodyFont;
    private BitmapFont enrageFont;
    private GlyphLayout glyphLayout;
    private GlyphLayout enrageLayout;
    private Texture backgroundTexture;
    private InputOutputManager ioManager;
    private PlayerControl playerControl;
    private PlayerEntity player;
    private EnemyEntity enemy;
    private ServerBossEntity vulnerableServer;
    private boolean suspendedForSettings;
    private boolean briefingActive;
    private boolean missionModalOpen;
    private boolean controlsModalOpen;
    private boolean gameplayMusicStarted;
    private int briefingPageIndex;
    private float missionModalScrollOffset;
    private float controlsModalScrollOffset;
    private boolean draggingMissionScrollbar;
    private boolean draggingControlsScrollbar;
    private float missionScrollbarGrabOffsetY;
    private float controlsScrollbarGrabOffsetY;
    private final ModalContent missionModalContent = new ModalContent(
        "Attack Mission",
        List.of(
            "You are now in position to exploit the target server.",
            "",
            "Attack the server to execute your exploit.",
            "Keep moving and avoid incoming threats while you apply pressure.",
            "",
            "Each successful hit brings you closer to taking it down.",
            "",
            "Complete the attack before you are stopped."
        )
    );
    private final ModalContent controlsModalContent = new ModalContent(
        "Controls",
        List.of(
            "WASD: Move",
            "",
            "Mouse Left Click: Attack (hold to continue applying pressure)",
            "",
            "SPACE: Open settings",
            "",
            "M / H: Toggle mission and controls",
            "",
            "Click Start Mission to begin"
        )
    );

    private GameEventBus gameEventBus;
    private int playerId;
    private int enemyId;
    private int vulnerableServerId;
    private boolean enemyEnraged = false;
    private float enrageBannerTimer = 0f;
    private boolean exploitSequenceActive = false;
    private float exploitSequenceTimer = 0f;
    private final IEventListener<BulletImpactEvent> bulletImpactListener =
        event -> context.getAudioManager().playSound("bullet_hit.mp3");

    public AttackScene(EngineContext context) {
        super(context);
        this.name = "AttackScene";
        this.entityManager = context.getEntityManager();
        this.collisionManager = context.getCollisionManager();
        setTransition(new FadeTransition(1.5f));
    }

    @Override
    public void load() {
        if (isLoaded) {
            return;
        }

        viewport = new FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        spriteBatch = context.getSharedBatch();
        hudFont = GameUiTheme.getHudFont();
        bodyFont = GameUiTheme.getMenuFont();
        glyphLayout = new GlyphLayout();
        enrageFont = new BitmapFont();
        enrageFont.setColor(GameUiTheme.AI_COLOR);
        enrageFont.getData().setScale(2.4f);
        enrageLayout = new GlyphLayout(enrageFont, ENRAGE_TEXT, GameUiTheme.AI_COLOR, GameConstants.WORLD_WIDTH - 120f, Align.center, true);
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture("attackscenebg.png");

        collisionManager.reset();
        spawnEntities();

        ioManager = new InputOutputManager(context.getEventManager());
        GameInputConfig.setupDefaultControls(ioManager);
        Gdx.input.setInputProcessor(ioManager.getInputProcessor());
        GameSession session = context.getService(GameSession.class);
        gameEventBus = session != null ? session.getGameEventBus() : null;
        if (gameEventBus != null) {
            gameEventBus.subscribe(BulletImpactEvent.class, bulletImpactListener);
        }
        playerControl = new PlayerControl(ioManager, context.getMovementManager(), playerId);
        briefingPageIndex = 0;
        briefingActive = true;
        missionModalOpen = false;
        controlsModalOpen = false;
        missionModalScrollOffset = 0f;
        controlsModalScrollOffset = 0f;
        draggingMissionScrollbar = false;
        draggingControlsScrollbar = false;
        missionScrollbarGrabOffsetY = 0f;
        controlsScrollbarGrabOffsetY = 0f;
        GameSession briefingSession = context.getService(GameSession.class);
        if (briefingSession != null && briefingSession.consumeBriefingBypass(SceneType.ATTACK)) {
            briefingActive = false;
        }

        suspendedForSettings = false;
        gameplayMusicStarted = false;
        isLoaded = true;
    }

    private void spawnEntities() {
        entityManager.cleanup();
        float centerX = viewport.getWorldWidth() * 0.5f;
        enemyEnraged = false;
        enrageBannerTimer = 0f;
        exploitSequenceActive = false;
        exploitSequenceTimer = 0f;

        playerId = GameEntityBootstrap.createPlayer(context);
        entityManager.getEntity(playerId).setPosition(new Vector2(centerX, PLAYER_Y));
        player = entityManager.getEntityAs(playerId, PlayerEntity.class);

        enemyId = GameEntityBootstrap.createEnemy(context);
        entityManager.getEntity(enemyId).setPosition(new Vector2(ENEMY_X, ENEMY_Y));
        enemy = entityManager.getEntityAs(enemyId, EnemyEntity.class);
        if (enemy != null) {
            enemy.setTargetEntity(playerId);
            context.getMovementManager().getComponent(enemyId).setMaxSpeed(ENEMY_BASE_SPEED);
        }

        projectileIds.clear();
        for (int i = 0; i < 12; i++) {
            int projectileId = GameEntityBootstrap.createBullet(context);
            projectileIds.add(projectileId);

            BulletEntity bullet = entityManager.getEntityAs(projectileId, BulletEntity.class);
            if (bullet != null) {
                bullet.resetProjectile();
            }
        }

        float[] firewallYPositions = {200f, 300f, 400f, 500f};
        for (int i = 0; i < firewallIds.length; i++) {
            firewallIds[i] = createFirewall(firewallYPositions[i], GameConstants.FIREWALL_SPEEDS[i] * GameConstants.ATTACK_FIREWALL_SPEED_MULTIPLIER);
        }

        vulnerableServerId = GameEntityBootstrap.createServerBoss(context);
        entityManager.getEntity(vulnerableServerId).setPosition(new Vector2(centerX, VULNERABLE_SERVER_Y));
        vulnerableServer = entityManager.getEntityAs(vulnerableServerId, ServerBossEntity.class);
        if (vulnerableServer != null) {
            vulnerableServer.setMaxHealth(VULNERABLE_SERVER_MAX_HEALTH);
        }

        if (player != null) {
            player.setProjectileIds(projectileIds);

            // Restore persistent health
            GameSession session = context.getService(GameSession.class);
            if (session != null) {
                int persistentPlayerHealth = session.getPersistentPlayerHealth();
                if (persistentPlayerHealth > 0) {
                    player.setHealth(persistentPlayerHealth);
                }
            }
        }

        for (int firewallId : firewallIds) {
            FirewallEntity firewall = entityManager.getEntityAs(firewallId, FirewallEntity.class);
            if (firewall != null) {
                firewall.initPath(GameConstants.WORLD_WIDTH);
            }
        }
    }

    private int createFirewall(float y, float speed) {
        float randomX = MathUtils.random(WORLD_EDGE_MARGIN, GameConstants.WORLD_WIDTH - WORLD_EDGE_MARGIN);
        int firewallId = GameEntityBootstrap.createMovingFirewall(context);
        entityManager.getEntity(firewallId).setPosition(new Vector2(randomX, y));
        FirewallEntity firewall = entityManager.getEntityAs(firewallId, FirewallEntity.class);
        if (firewall != null) {
            float lengthScale = MathUtils.random(FIREWALL_LENGTH_SCALE_MIN, FIREWALL_LENGTH_SCALE_MAX);
            float heightScale = MathUtils.random(FIREWALL_HEIGHT_SCALE_MIN, FIREWALL_HEIGHT_SCALE_MAX);
            firewall.setSizeScales(lengthScale, heightScale);
        }
        context.getMovementManager().getComponent(firewallId).setMaxSpeed(speed);
        return firewallId;
    }

    @Override
    public void unload() {
        if (suspendedForSettings) {
            return;
        }

        context.getAudioManager().stopMusic();
        gameplayMusicStarted = false;
        if (gameEventBus != null) {
            gameEventBus.unsubscribe(BulletImpactEvent.class, bulletImpactListener);
            gameEventBus = null;
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        if (ioManager != null) {
            ioManager.dispose();
            ioManager = null;
        }
        playerControl = null;
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        if (enrageFont != null) {
            enrageFont.dispose();
            enrageFont = null;
        }
        enrageLayout = null;

        entityManager.cleanup();
        isLoaded = false;
    }

    @Override
    public void update(float dt) {
        if (transition != null && !transition.isFinished()) {
            transition.update(dt);
        }

        if (ioManager == null || (transition != null && !transition.isFinished())) {
            return;
        }

        if (ioManager.isCommandJustPressed(GameCommand.OPEN_SETTINGS)) {
            suspendedForSettings = true;
            requestScene(SceneType.SETTINGS);
            return;
        }

        if (briefingActive) {
            updateModalScrollInput();
            updateBriefingInput();
            ioManager.update();
            return;
        }

        if (ioManager.isCommandJustPressed(GameCommand.TOGGLE_MISSION)) {
            missionModalOpen = !missionModalOpen;
            if (missionModalOpen) {
                controlsModalOpen = false;
            }
        }

        if (ioManager.isCommandJustPressed(GameCommand.TOGGLE_HELP)) {
            controlsModalOpen = !controlsModalOpen;
            if (controlsModalOpen) {
                missionModalOpen = false;
            }
        }

        if (missionModalOpen || controlsModalOpen) {
            updateModalScrollInput();
            ioManager.update();
            return;
        }

        if (vulnerableServer != null && vulnerableServer.isDefeated()) {
            if (!exploitSequenceActive) {
                startExploitSequence();
            }
        }

        if (exploitSequenceActive) {
            exploitSequenceTimer = Math.max(0f, exploitSequenceTimer - dt);
            if (exploitSequenceTimer <= 0f) {
                exploitSequenceActive = false;
                requestSceneWithMusicStop(SceneType.WIN);
                ioManager.update();
                return;
            }
            ioManager.update();
            return;
        }

        ensureGameplayMusic();

        if (playerControl != null) {
            playerControl.update();
        }
        if (enrageBannerTimer > 0f) {
            enrageBannerTimer = Math.max(0f, enrageBannerTimer - dt);
        }
        updateEnemyEnrageState();
        despawnProjectileAtWorldEdge();

        if (player != null && ioManager.isCommandActive(GameCommand.CLICK)) {
            Vector2 targetWorld = viewport.unproject(ioManager.getPointerPosition(new Vector2()));
            player.attemptShoot(targetWorld);
        }

        ioManager.update();
    }

    @Override
    public void render() {
        if (spriteBatch == null || backgroundTexture == null) {
            return;
        }

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.draw(backgroundTexture, 0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        spriteBatch.end();
    }

    @Override
    public void renderOverlay() {
        renderHudChips();
        renderEntityLabels();
        if (!briefingActive && !missionModalOpen && !controlsModalOpen) {
            renderTargetServerTracker();
        }
        renderContextPanels();
        renderEnrageBanner();
        renderExploitSequenceOverlay();
        renderTransitionOverlay();
    }

    @Override
    public boolean runsSimulationSystems() {
        return !briefingActive && !missionModalOpen && !controlsModalOpen && !exploitSequenceActive;
    }

    @Override
    public boolean rendersWorldEntities() {
        return true;
    }

    private void ensureGameplayMusic() {
        if (gameplayMusicStarted) {
            return;
        }
        context.getAudioManager().playMusic("mainscenebgm.mp3");
        gameplayMusicStarted = true;
    }

    @Override
    public void onResume() {
        suspendedForSettings = false;
        if (ioManager != null) {
            ioManager.resetInputStates();
            Gdx.input.setInputProcessor(ioManager.getInputProcessor());
        }
    }

    @Override
    public SceneState getState(SceneType nextSceneType) {
        if (nextSceneType == SceneType.SETTINGS) {
            return SceneState.PAUSE_AND_RESUME;
        }
        return SceneState.RECREATE;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    private void despawnProjectileAtWorldEdge() {
        for (int projectileId : projectileIds) {
            BulletEntity bullet = entityManager.getEntityAs(projectileId, BulletEntity.class);
            if (bullet == null || bullet.isAvailable()) {
                continue;
            }

            Vector2 position = bullet.getPosition();
            if (position.x <= WORLD_EDGE_MARGIN
                || position.x >= GameConstants.WORLD_WIDTH - WORLD_EDGE_MARGIN
                || position.y <= WORLD_EDGE_MARGIN
                || position.y >= GameConstants.WORLD_HEIGHT - WORLD_EDGE_MARGIN) {
                bullet.resetProjectile();
            }
        }
    }

    private void updateEnemyEnrageState() {
        if (vulnerableServer != null && !enemyEnraged && vulnerableServer.getHealth() <= GameConstants.ENEMY_ENRAGE_HEALTH_THRESHOLD) {
            enemyEnraged = true;
            enrageBannerTimer = ENRAGE_BANNER_DURATION;
            context.getMovementManager().getComponent(enemyId)
                .setMaxSpeed(GameConstants.ENEMY_MAX_SPEED);
            context.getAudioManager().playMusic("attack_bgm_intense.mp3");
        }
    }

    private void startExploitSequence() {
        exploitSequenceActive = true;
        exploitSequenceTimer = EXPLOIT_SEQUENCE_DURATION;
        context.getAudioManager().stopMusic();
        gameplayMusicStarted = false;
        context.getAudioManager().playSound("vuln_server_exploited.mp3");

        if (enemy != null) {
            enemy.setTargetEntity(-1);
        }

        if (context.getMovementManager().getComponent(playerId) != null) {
            context.getMovementManager().getComponent(playerId).setMaxSpeed(0f);
            context.getMovementManager().getComponent(playerId).getVelocity().set(0f, 0f);
        }

        if (context.getMovementManager().getComponent(enemyId) != null) {
            context.getMovementManager().setSteeringBehavior(enemyId, io.github.cybergrid.simulation.engine.movement.SteeringState.IDLE);
            context.getMovementManager().getComponent(enemyId).setMaxSpeed(0f);
            context.getMovementManager().getComponent(enemyId).getVelocity().set(0f, 0f);
        }

        for (int firewallId : firewallIds) {
            if (context.getMovementManager().getComponent(firewallId) != null) {
                context.getMovementManager().setSteeringBehavior(firewallId, io.github.cybergrid.simulation.engine.movement.SteeringState.IDLE);
                context.getMovementManager().getComponent(firewallId).setMaxSpeed(0f);
                context.getMovementManager().getComponent(firewallId).getVelocity().set(0f, 0f);
            }
        }

        for (int projectileId : projectileIds) {
            if (context.getMovementManager().getComponent(projectileId) != null) {
                context.getMovementManager().setSteeringBehavior(projectileId, io.github.cybergrid.simulation.engine.movement.SteeringState.IDLE);
                context.getMovementManager().getComponent(projectileId).setMaxSpeed(0f);
                context.getMovementManager().getComponent(projectileId).getVelocity().set(0f, 0f);
            }
        }
    }

    private void renderEnrageBanner() {
        if (enrageBannerTimer <= 0f || spriteBatch == null || enrageFont == null || enrageLayout == null || viewport == null || shapeRenderer == null) {
            return;
        }

        float alpha = Math.min(1f, enrageBannerTimer / ENRAGE_BANNER_DURATION);
        Color aiColor = GameUiTheme.AI_COLOR;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(aiColor.r * 0.18f, aiColor.g * 0.18f, aiColor.b * 0.18f, 0.55f * alpha);
        shapeRenderer.rect(0f, GameConstants.WORLD_HEIGHT * 0.5f, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT * 0.28f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        Color previousColor = enrageFont.getColor().cpy();
        enrageFont.setColor(aiColor.r * 0.35f, aiColor.g * 0.35f, aiColor.b * 0.35f, alpha);
        enrageFont.draw(spriteBatch, ENRAGE_TEXT, 63f, GameConstants.WORLD_HEIGHT * 0.79f - 3f, GameConstants.WORLD_WIDTH - 120f, Align.center, true);
        enrageFont.setColor(aiColor.r, aiColor.g, aiColor.b, alpha);
        enrageFont.draw(spriteBatch, ENRAGE_TEXT, 60f, GameConstants.WORLD_HEIGHT * 0.8f, GameConstants.WORLD_WIDTH - 120f, Align.center, true);
        enrageFont.setColor(previousColor);
        spriteBatch.end();
    }

    private void renderExploitSequenceOverlay() {
        if (!exploitSequenceActive || viewport == null || shapeRenderer == null || spriteBatch == null || hudFont == null) {
            return;
        }

        float alpha = Math.min(1f, exploitSequenceTimer / EXPLOIT_SEQUENCE_DURATION);
        float pulse = 0.88f + (0.12f * (float) Math.sin((EXPLOIT_SEQUENCE_DURATION - exploitSequenceTimer) * 14f));
        float centerX = vulnerableServer != null ? vulnerableServer.getPosition().x : GameConstants.WORLD_WIDTH * 0.5f;
        float centerY = vulnerableServer != null ? vulnerableServer.getPosition().y : GameConstants.WORLD_HEIGHT * 0.62f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(GameUiTheme.PLAYER_COLOR.r, GameUiTheme.PLAYER_COLOR.g, GameUiTheme.PLAYER_COLOR.b, 0.18f * pulse);
        shapeRenderer.circle(centerX, centerY, 130f * pulse, 60);
        shapeRenderer.setColor(0f, 0f, 0f, 0.32f * alpha);
        shapeRenderer.rect(0f, GameConstants.WORLD_HEIGHT * 0.48f, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT * 0.24f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        drawEntityLabel("Exploit Complete", centerX, centerY + 168f, GameUiTheme.PLAYER_COLOR);
        hudFont.setColor(GameUiTheme.PLAYER_COLOR);
        glyphLayout.setText(hudFont, EXPLOITED_TEXT);
        hudFont.draw(spriteBatch, EXPLOITED_TEXT, (GameConstants.WORLD_WIDTH - glyphLayout.width) * 0.5f, GameConstants.WORLD_HEIGHT * 0.63f);
        hudFont.setColor(Color.WHITE);
        spriteBatch.end();
    }

    private void renderEntityLabels() {
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        if (player != null) {
            drawEntityLabel("Player", player.getPosition().x, player.getPosition().y + 86f, GameUiTheme.PLAYER_COLOR);
        }
        if (enemy != null) {
            drawEntityLabel("AI", enemy.getPosition().x, enemy.getPosition().y + 86f, GameUiTheme.AI_COLOR);
        }

        spriteBatch.end();
    }

    private void drawEntityLabel(String label, float centerX, float baselineY, Color color) {
        if (label == null || label.isEmpty()) {
            return;
        }

        float oldR = hudFont.getColor().r;
        float oldG = hudFont.getColor().g;
        float oldB = hudFont.getColor().b;
        float oldA = hudFont.getColor().a;

        hudFont.setColor(color);
        glyphLayout.setText(hudFont, label);
        hudFont.draw(spriteBatch, label, centerX - (glyphLayout.width / 2f), baselineY);
        hudFont.setColor(oldR, oldG, oldB, oldA);
    }

    private void renderTargetServerTracker() {
        if (vulnerableServer == null || shapeRenderer == null) {
            return;
        }

        Vector2 targetServerPosition = vulnerableServer.getPosition();
        float ratio = vulnerableServer.getHealthRatio();
        float progress = 1f - ratio;
        float outerRadius = VULNERABLE_SERVER_RING_RADIUS + 8f;
        float midRadius = VULNERABLE_SERVER_RING_RADIUS + 4f;
        float innerRadius = VULNERABLE_SERVER_RING_RADIUS;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.circle(targetServerPosition.x, targetServerPosition.y, outerRadius + 3f, 64);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.18f, 0.18f, 0.18f, 0.95f);
        shapeRenderer.circle(targetServerPosition.x, targetServerPosition.y, outerRadius, 64);
        shapeRenderer.circle(targetServerPosition.x, targetServerPosition.y, midRadius, 64);
        shapeRenderer.circle(targetServerPosition.x, targetServerPosition.y, innerRadius, 64);

        if (progress > 0f) {
            shapeRenderer.setColor(GameUiTheme.PLAYER_COLOR);
            shapeRenderer.arc(targetServerPosition.x, targetServerPosition.y, outerRadius, 90f, -360f * progress, 64);
            shapeRenderer.arc(targetServerPosition.x, targetServerPosition.y, midRadius, 90f, -360f * progress, 64);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        int percent = Math.round(progress * 100f);
        String trackerText = percent + "%";
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        drawEntityLabel(trackerText, targetServerPosition.x, targetServerPosition.y + outerRadius + 24f, GameUiTheme.PLAYER_COLOR);
        spriteBatch.end();
    }

    private void renderTransitionOverlay() {
        if (transition == null || transition.isFinished() || shapeRenderer == null) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, transition.getOverlayAlpha());
        shapeRenderer.rect(0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void updateBriefingInput() {
        if (!ioManager.isCommandJustPressed(GameCommand.CLICK)) {
            return;
        }

        Vector2 pointerWorld = viewport.unproject(ioManager.getPointerPosition(new Vector2()));
        float panelWidth = 860f;
        float panelHeight = 420f;
        float panelX = (viewport.getWorldWidth() - panelWidth) / 2f;
        float panelY = (viewport.getWorldHeight() - panelHeight) / 2f;

        float leftX = panelX + 28f;
        float leftY = panelY + 24f;
        float rightX = panelX + panelWidth - 28f - PANEL_BTN_W;
        float rightY = leftY;
        float startX = panelX + (panelWidth - START_BTN_W) / 2f;
        float startY = panelY + 24f;

        if (briefingPageIndex > 0 && contains(pointerWorld, leftX, leftY, PANEL_BTN_W, PANEL_BTN_H)) {
            UiSoundHelper.playButtonClick(context);
            briefingPageIndex--;
            return;
        }

        if (briefingPageIndex < 1 && contains(pointerWorld, rightX, rightY, PANEL_BTN_W, PANEL_BTN_H)) {
            UiSoundHelper.playButtonClick(context);
            briefingPageIndex++;
            return;
        }

        if (briefingPageIndex == 1 && contains(pointerWorld, startX, startY, START_BTN_W, START_BTN_H)) {
            UiSoundHelper.playButtonClick(context);
            GameSession session = context.getService(GameSession.class);
            if (session != null) {
                session.prepareLoader(SceneType.ATTACK);
            }
            requestSceneWithMusicStop(SceneType.LOADER);
        }
    }

    private void requestSceneWithMusicStop(SceneType targetScene) {
        context.getAudioManager().stopMusic();
        gameplayMusicStarted = false;
        requestScene(targetScene);
    }

    private boolean contains(Vector2 point, float x, float y, float width, float height) {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
    }

    private void renderBriefingOverlay() {
        ModalContent modal = briefingPageIndex == 0 ? missionModalContent : controlsModalContent;

        if (briefingPageIndex == 0) {
            missionModalScrollOffset = InGameModalHelper.render(
                viewport,
                shapeRenderer,
                spriteBatch,
                bodyFont,
                hudFont,
                glyphLayout,
                modal.getTitle(),
                modal.getLines(),
                missionModalScrollOffset
            );
        } else {
            controlsModalScrollOffset = InGameModalHelper.render(
                viewport,
                shapeRenderer,
                spriteBatch,
                bodyFont,
                hudFont,
                glyphLayout,
                modal.getTitle(),
                modal.getLines(),
                controlsModalScrollOffset
            );
        }

        float panelWidth = 860f;
        float panelHeight = 420f;
        float panelX = (viewport.getWorldWidth() - panelWidth) / 2f;
        float panelY = (viewport.getWorldHeight() - panelHeight) / 2f;

        float leftX = panelX + 28f;
        float leftY = panelY + 24f;
        float rightX = panelX + panelWidth - 28f - PANEL_BTN_W;
        float rightY = leftY;
        float startX = panelX + (panelWidth - START_BTN_W) / 2f;
        float startY = panelY + 24f;

        InGameModalHelper.renderBriefingNavigation(
            viewport,
            shapeRenderer,
            spriteBatch,
            hudFont,
            briefingPageIndex,
            1,
            PANEL_BTN_W,
            PANEL_BTN_H,
            START_BTN_W,
            START_BTN_H,
            leftX,
            leftY,
            rightX,
            rightY,
            startX,
            startY
        );
    }

    private void renderHudChips() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.58f);
        shapeRenderer.rect(18f, 40f, 150f, 26f);
        shapeRenderer.rect(18f, 10f, 150f, 26f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        hudFont.draw(spriteBatch, "[M] Mission   [H] Controls", 28f, 32f);
        spriteBatch.end();
    }

    private void renderContextPanels() {
        if (briefingActive) {
            renderBriefingOverlay();
            return;
        }

        if (!missionModalOpen && !controlsModalOpen) {
            return;
        }

        ModalContent modal = missionModalOpen ? missionModalContent : controlsModalContent;
        if (missionModalOpen) {
            missionModalScrollOffset = InGameModalHelper.render(
                viewport,
                shapeRenderer,
                spriteBatch,
                bodyFont,
                hudFont,
                glyphLayout,
                modal.getTitle(),
                modal.getLines(),
                missionModalScrollOffset
            );
        } else {
            controlsModalScrollOffset = InGameModalHelper.render(
                viewport,
                shapeRenderer,
                spriteBatch,
                bodyFont,
                hudFont,
                glyphLayout,
                modal.getTitle(),
                modal.getLines(),
                controlsModalScrollOffset
            );
        }
    }

    private void updateModalScrollInput() {
        if (ioManager == null || viewport == null || bodyFont == null || glyphLayout == null) {
            return;
        }

        boolean showingMission = briefingPageIndex == 0 || missionModalOpen;
        List<String> lines = showingMission ? missionModalContent.getLines() : controlsModalContent.getLines();
        float currentOffset = showingMission ? missionModalScrollOffset : controlsModalScrollOffset;

        InGameModalHelper.ScrollbarMetrics metrics = InGameModalHelper.computeScrollbarMetrics(
            viewport,
            bodyFont,
            glyphLayout,
            lines,
            currentOffset
        );

        float scrollY = ioManager.consumeScrollYDelta();
        if (scrollY != 0f) {
            currentOffset += scrollY * 52f;
        }

        Vector2 pointerWorld = viewport.unproject(ioManager.getPointerPosition(new Vector2()));
        boolean pointerDown = ioManager.isCommandActive(GameCommand.CLICK);
        boolean pointerJustPressed = ioManager.isCommandJustPressed(GameCommand.CLICK);

        if (showingMission) {
            draggingControlsScrollbar = false;
            if (pointerJustPressed && metrics.hasScrollbar()) {
                if (metrics.containsThumb(pointerWorld.x, pointerWorld.y)) {
                    draggingMissionScrollbar = true;
                    missionScrollbarGrabOffsetY = pointerWorld.y - metrics.getThumbY();
                } else if (metrics.containsTrack(pointerWorld.x, pointerWorld.y)) {
                    draggingMissionScrollbar = true;
                    missionScrollbarGrabOffsetY = metrics.getThumbHeight() * 0.5f;
                    currentOffset = metrics.scrollForThumbY(pointerWorld.y - missionScrollbarGrabOffsetY);
                }
            }
            if (!pointerDown) {
                draggingMissionScrollbar = false;
            }
            if (draggingMissionScrollbar && pointerDown && metrics.hasScrollbar()) {
                currentOffset = metrics.scrollForThumbY(pointerWorld.y - missionScrollbarGrabOffsetY);
            }
        } else {
            draggingMissionScrollbar = false;
            if (pointerJustPressed && metrics.hasScrollbar()) {
                if (metrics.containsThumb(pointerWorld.x, pointerWorld.y)) {
                    draggingControlsScrollbar = true;
                    controlsScrollbarGrabOffsetY = pointerWorld.y - metrics.getThumbY();
                } else if (metrics.containsTrack(pointerWorld.x, pointerWorld.y)) {
                    draggingControlsScrollbar = true;
                    controlsScrollbarGrabOffsetY = metrics.getThumbHeight() * 0.5f;
                    currentOffset = metrics.scrollForThumbY(pointerWorld.y - controlsScrollbarGrabOffsetY);
                }
            }
            if (!pointerDown) {
                draggingControlsScrollbar = false;
            }
            if (draggingControlsScrollbar && pointerDown && metrics.hasScrollbar()) {
                currentOffset = metrics.scrollForThumbY(pointerWorld.y - controlsScrollbarGrabOffsetY);
            }
        }

        float clamped = InGameModalHelper.computeScrollbarMetrics(
            viewport,
            bodyFont,
            glyphLayout,
            lines,
            currentOffset
        ).getClampedScroll();

        if (showingMission) {
            missionModalScrollOffset = clamped;
        } else {
            controlsModalScrollOffset = clamped;
        }
    }
}



