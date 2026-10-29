package io.github.cybergrid.simulation.app.game.scenes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.entities.GameEntityBootstrap;
import io.github.cybergrid.simulation.app.game.entities.EnemyEntity;
import io.github.cybergrid.simulation.app.game.entities.ExitTriggerEntity;
import io.github.cybergrid.simulation.app.game.entities.FirewallStaticEntity;
import io.github.cybergrid.simulation.app.game.entities.PlayerEntity;
import io.github.cybergrid.simulation.app.game.events.EscapeExitReachedEvent;
import io.github.cybergrid.simulation.app.game.events.EscapeExitZoneUnlockedEvent;
import io.github.cybergrid.simulation.app.game.events.ObfuscatedCodeCollectedEvent;
import io.github.cybergrid.simulation.app.game.input.GameCommand;
import io.github.cybergrid.simulation.app.game.input.GameInputConfig;
import io.github.cybergrid.simulation.app.game.player.PlayerControl;
import io.github.cybergrid.simulation.app.game.GameSession;
import io.github.cybergrid.simulation.app.game.ui.ModalContent;
import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.app.game.ui.InGameModalHelper;
import io.github.cybergrid.simulation.app.game.ui.UiSoundHelper;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.TriggerEntity;
import io.github.cybergrid.simulation.engine.interfaces.TriggerCallback;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;
import io.github.cybergrid.simulation.engine.managers.CollisionManager;
import io.github.cybergrid.simulation.engine.managers.EntityManager;
import io.github.cybergrid.simulation.engine.managers.InputOutputManager;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;
import io.github.cybergrid.simulation.engine.movement.SteeringState;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneState;
import io.github.cybergrid.simulation.engine.scene.SceneType;
import io.github.cybergrid.simulation.engine.transition.FadeTransition;

public class EscapeScene extends Scene {

    private static final float GOAL_W = 220f;
    private static final float GOAL_H = 150f;
    private static final float GOAL_X = GameConstants.WORLD_WIDTH - GOAL_W - 34f;
    private static final float GOAL_Y = 42f;

    private static final float BG_SCROLL_SPEED = 140f;
    private static final float FIREWALL_BASE_SPEED = 265f;
    private static final float FIREWALL_SPEED_VARIATION = 45f;
    private static final float FIREWALL_MIN_X_GAP = 230f;
    private static final float FIREWALL_DESPAWN_X = -220f;
    private static final int FIREWALL_MAX_ACTIVE = 9;
    private static final float SAFE_EXIT_X_MARGIN = 30f;
    private static final float SAFE_EXIT_Y_MARGIN = 30f;
    private static final float EXIT_ZONE_UNLOCK_SECONDS = 40f;

    private static final float PANEL_BTN_W = 90f;
    private static final float PANEL_BTN_H = 44f;
    private static final float START_BTN_W = 220f;
    private static final float START_BTN_H = 52f;

    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final List<FirewallStreamEntry> firewallStream = new ArrayList<>();
    private final Random random = new Random();

    private Viewport viewport;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont hudFont;
    private BitmapFont bodyFont;
    private GlyphLayout glyphLayout;
    private Texture backgroundTexture;
    private InputOutputManager ioManager;
    private PlayerControl playerControl;

    private int playerId;
    private int enemyId;
    private int exitTriggerId;
    private int obfuscatedCodeId;
    private boolean exitTriggered;
    private boolean exitZoneDeployed;
    private boolean enemyTrackingSuppressed;
    private boolean suspendedForSettings;
    private boolean gameplayMusicStarted;
    private float escapeRuntime;
    private float obfuscatedCodeSpawnTimer;
    private float nextObfuscatedCodeSpawnIn;
    private float obfuscatedCodeLifetimeRemaining;

    private float backgroundScrollX;

    private boolean briefingActive;
    private boolean missionModalOpen;
    private boolean controlsModalOpen;
    private int briefingPageIndex;
    private float missionModalScrollOffset;
    private float controlsModalScrollOffset;
    private boolean draggingMissionScrollbar;
    private boolean draggingControlsScrollbar;
    private float missionScrollbarGrabOffsetY;
    private float controlsScrollbarGrabOffsetY;
    private final ModalContent missionModalContent = new ModalContent(
        "Escape Mission",
        Arrays.asList(
            "Your intrusion has triggered a full network lockdown.",
            "",
            "The Syndicate AI is tightening its pursuit.",
            "",
            "Move through the network and avoid getting caught.",
            "Firewall gates will block your path and force you to change direction.",
            "",
            "Look out for Obfuscated Code.",
            "Collecting it will hide your activity for a short time and stop the AI from tracking you.",
            "",
            "Stay alive until the exit opens.",
            "Reach it to continue the mission."
        )
    );
    private final ModalContent controlsModalContent = new ModalContent(
        "Controls",
        Arrays.asList(
            "WASD: Move",
            "",
            "Collect Obfuscated Code to temporarily avoid detection",
            "",
            "SPACE: Open settings",
            "",
            "M / H: Toggle mission and controls",
            "",
            "Click Start Mission to begin"
        )
    );

    private static final class FirewallStreamEntry {
        private final int id;
        private final float speed;

        private FirewallStreamEntry(int id, float speed) {
            this.id = id;
            this.speed = speed;
        }
    }

    private final IEventListener<EscapeExitZoneUnlockedEvent> exitZoneUnlockedListener =
        event -> spawnExitZoneTrigger();

    private final IEventListener<EscapeExitReachedEvent> exitReachedListener =
        event -> handleExitReached();

    private final IEventListener<ObfuscatedCodeCollectedEvent> obfuscatedCodeCollectedListener =
        event -> handleObfuscatedCodeCollected(event.getCollectorEntityId());

    public EscapeScene(EngineContext context) {
        super(context);
        this.name = "EscapeScene";
        this.entityManager = context.getEntityManager();
        this.collisionManager = context.getCollisionManager();
        setTransition(new FadeTransition(1.7f));
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
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture("scanningscenebg.png");
        backgroundScrollX = 0f;

        collisionManager.reset();
        spawnEntities();

        context.getEventManager().subscribe(EscapeExitZoneUnlockedEvent.class, exitZoneUnlockedListener);
        context.getEventManager().subscribe(EscapeExitReachedEvent.class, exitReachedListener);
        context.getEventManager().subscribe(ObfuscatedCodeCollectedEvent.class, obfuscatedCodeCollectedListener);

        ioManager = new InputOutputManager(context.getEventManager());
        GameInputConfig.setupDefaultControls(ioManager);
        Gdx.input.setInputProcessor(ioManager.getInputProcessor());
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
        GameSession session = context.getService(GameSession.class);
        if (session != null && session.consumeBriefingBypass(SceneType.ESCAPE)) {
            briefingActive = false;
        }

        suspendedForSettings = false;
        gameplayMusicStarted = false;
        isLoaded = true;
    }

    private void spawnEntities() {
        entityManager.cleanup();
        firewallStream.clear();
        exitTriggered = false;
        exitZoneDeployed = false;
        escapeRuntime = 0f;
        exitTriggerId = -1;
        obfuscatedCodeId = -1;
        enemyTrackingSuppressed = false;
        obfuscatedCodeSpawnTimer = 0f;
        obfuscatedCodeLifetimeRemaining = 0f;
        scheduleNextObfuscatedCodeSpawn();

        playerId = GameEntityBootstrap.createPlayer(context);
        entityManager.getEntity(playerId).setPosition(new Vector2(120f, 110f));
        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (player != null) {
            player.setSpeed(GameConstants.PLAYER_SPEED * GameConstants.ESCAPE_PLAYER_SPEED_MULTIPLIER);
            GameSession session = context.getService(GameSession.class);
            if (session != null && session.getPersistentPlayerHealth() > 0) {
                player.setHealth(session.getPersistentPlayerHealth());
            }
        }

        enemyId = GameEntityBootstrap.createEnemy(context);
        entityManager.getEntity(enemyId).setPosition(new Vector2(140f, 630f));
        EnemyEntity enemy = entityManager.getEntityAs(enemyId, EnemyEntity.class);
        if (enemy != null) {
            enemy.setTargetEntity(playerId);
            context.getMovementManager().getComponent(enemyId).setMaxSpeed(GameConstants.ENEMY_SPEED * GameConstants.ESCAPE_ENEMY_SPEED_MULTIPLIER);
        }

        for (int i = 0; i < 6; i++) {
            spawnFirewallAt(GameConstants.WORLD_WIDTH + 180f + (i * 260f));
        }
    }

    @Override
    public void update(float dt) {
        if (transition != null && !transition.isFinished()) {
            transition.update(dt);
        }

        if (ioManager == null || (transition != null && !transition.isFinished())) {
            return;
        }

        updateBackgroundScroll(dt);

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

        ensureGameplayMusic();

        if (playerControl != null) {
            playerControl.update();
        }

        escapeRuntime += dt;
        if (!exitZoneDeployed && escapeRuntime >= EXIT_ZONE_UNLOCK_SECONDS) {
            context.getEventManager().dispatch(new EscapeExitZoneUnlockedEvent());
        }

        updateFirewallStream(dt);
        updateObfuscatedCode(dt);
        updateEnemyTracking();

        ioManager.update();
    }

    private void spawnExitZoneTrigger() {
        exitTriggerId = GameEntityBootstrap.createExitTrigger(context);
        entityManager.getEntity(exitTriggerId).setPosition(new Vector2(GOAL_X + (GOAL_W / 2f), GOAL_Y + (GOAL_H / 2f)));
        ExitTriggerEntity exitTrigger = entityManager.getEntityAs(exitTriggerId, ExitTriggerEntity.class);
        if (exitTrigger != null) {
            TriggerCallback callback = (trigger, activator) -> {
                if (activator != null && activator.getID() == playerId && !exitTriggered) {
                    exitTriggered = true;
                    context.getEventManager().dispatch(new EscapeExitReachedEvent());
                }
            };
            exitTrigger.setCallback(callback);
        }
        exitZoneDeployed = true;
    }

    private void scheduleNextObfuscatedCodeSpawn() {
        float min = GameConstants.OBFUSCATED_CODE_SPAWN_MIN_SECONDS;
        float max = GameConstants.OBFUSCATED_CODE_SPAWN_MAX_SECONDS;
        nextObfuscatedCodeSpawnIn = min + random.nextFloat() * (max - min);
        obfuscatedCodeSpawnTimer = 0f;
    }

    private void updateObfuscatedCode(float dt) {
        if (obfuscatedCodeId > 0) {
            obfuscatedCodeLifetimeRemaining -= dt;
            if (obfuscatedCodeLifetimeRemaining <= 0f || entityManager.getEntity(obfuscatedCodeId) == null) {
                if (entityManager.getEntity(obfuscatedCodeId) != null) {
                    entityManager.destroyEntity(obfuscatedCodeId);
                }
                obfuscatedCodeId = -1;
                scheduleNextObfuscatedCodeSpawn();
            }
            return;
        }

        obfuscatedCodeSpawnTimer += dt;
        if (obfuscatedCodeSpawnTimer < nextObfuscatedCodeSpawnIn) {
            return;
        }

        spawnObfuscatedCodeTrigger();
    }

    private void spawnObfuscatedCodeTrigger() {
        Vector2 spawnPos = resolveObfuscatedCodeSpawnPosition();
        obfuscatedCodeId = GameEntityBootstrap.createObfuscatedCode(context);
        entityManager.getEntity(obfuscatedCodeId).setPosition(spawnPos);
        obfuscatedCodeLifetimeRemaining = GameConstants.OBFUSCATED_CODE_LIFETIME_SECONDS;

        Entity triggerEntity = entityManager.getEntity(obfuscatedCodeId);
        if (triggerEntity instanceof TriggerEntity) {
            TriggerCallback callback = (trigger, activator) -> {
                if (activator != null && activator.getID() == playerId) {
                    context.getEventManager().dispatch(new ObfuscatedCodeCollectedEvent(playerId));
                }
            };
            ((TriggerEntity) triggerEntity).setCallback(callback);
        }
    }

    private Vector2 resolveObfuscatedCodeSpawnPosition() {
        for (int attempt = 0; attempt < 20; attempt++) {
            float x = 120f + random.nextFloat() * (GameConstants.WORLD_WIDTH - 240f);
            float y = 90f + random.nextFloat() * (GameConstants.WORLD_HEIGHT - 180f);
            Vector2 candidate = new Vector2(x, y);
            if (isInsideExitSafetyCorridor(candidate)) {
                continue;
            }
            Entity player = entityManager.getEntity(playerId);
            Entity enemy = entityManager.getEntity(enemyId);
            if (player != null && candidate.dst(player.getPosition()) < 160f) {
                continue;
            }
            if (enemy != null && candidate.dst(enemy.getPosition()) < 160f) {
                continue;
            }
            return candidate;
        }
        return new Vector2(GameConstants.WORLD_WIDTH * 0.6f, GameConstants.WORLD_HEIGHT * 0.45f);
    }

    private void handleObfuscatedCodeCollected(int collectorEntityId) {
        if (collectorEntityId != playerId) {
            return;
        }

        if (obfuscatedCodeId > 0 && entityManager.getEntity(obfuscatedCodeId) != null) {
            entityManager.destroyEntity(obfuscatedCodeId);
        }
        obfuscatedCodeId = -1;
        obfuscatedCodeLifetimeRemaining = 0f;
        scheduleNextObfuscatedCodeSpawn();

        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (player != null) {
            player.activateObfuscation(GameConstants.OBFUSCATION_SHIELD_SECONDS);
        }
        context.getAudioManager().playSound("obfuscated_code_protection.mp3");
    }

    private void updateEnemyTracking() {
        EnemyEntity enemy = entityManager.getEntityAs(enemyId, EnemyEntity.class);
        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (enemy == null || player == null) {
            return;
        }

        boolean shouldSuppress = player.isObfuscated();
        if (shouldSuppress == enemyTrackingSuppressed) {
            return;
        }

        enemyTrackingSuppressed = shouldSuppress;
        if (shouldSuppress) {
            enemy.setTargetEntity(-1);
            MovementComponent movement = context.getMovementManager().getComponent(enemyId);
            if (movement != null) {
                movement.setSteeringBehavior(SteeringState.IDLE);
                movement.getVelocity().setZero();
            }
        } else {
            enemy.setTargetEntity(playerId);
        }
    }

    private void updateBackgroundScroll(float dt) {
        backgroundScrollX -= BG_SCROLL_SPEED * dt;
        if (backgroundScrollX <= -GameConstants.WORLD_WIDTH) {
            backgroundScrollX += GameConstants.WORLD_WIDTH;
        }
    }

    private void updateFirewallStream(float dt) {
        for (int i = firewallStream.size() - 1; i >= 0; i--) {
            FirewallStreamEntry entry = firewallStream.get(i);
            Entity firewall = entityManager.getEntity(entry.id);
            if (firewall == null) {
                firewallStream.remove(i);
                continue;
            }

            Vector2 pos = firewall.getPosition();
            pos.x -= entry.speed * dt;
            firewall.setPosition(pos);

            if (pos.x < FIREWALL_DESPAWN_X) {
                entityManager.destroyEntity(entry.id);
                firewallStream.remove(i);
            }
        }

        maybeSpawnFirewall();
    }

    private void maybeSpawnFirewall() {
        if (firewallStream.size() >= FIREWALL_MAX_ACTIVE) {
            return;
        }

        float maxX = -9999f;
        for (FirewallStreamEntry entry : firewallStream) {
            Entity firewall = entityManager.getEntity(entry.id);
            if (firewall != null) {
                maxX = Math.max(maxX, firewall.getPosition().x);
            }
        }

        if (maxX > GameConstants.WORLD_WIDTH + 120f) {
            return;
        }

        float spawnX = GameConstants.WORLD_WIDTH + 150f + random.nextFloat() * 160f;
        spawnFirewallAt(spawnX);
    }

    private void spawnFirewallAt(float spawnX) {
        float[] lanes = new float[] { 90f, 170f, 250f, 330f, 410f, 490f, 570f, 650f };
        float chosenY = lanes[random.nextInt(lanes.length)];

        for (int attempt = 0; attempt < 16; attempt++) {
            float candidate = lanes[random.nextInt(lanes.length)];
            if (isSpawnLaneSafe(spawnX, candidate)) {
                chosenY = candidate;
                break;
            }
        }

        int firewallId = GameEntityBootstrap.createStaticFirewall(context);
        entityManager.getEntity(firewallId).setPosition(new Vector2(spawnX, chosenY));
        entityManager.getEntityAs(firewallId, FirewallStaticEntity.class);

        float speed = FIREWALL_BASE_SPEED + (random.nextFloat() * FIREWALL_SPEED_VARIATION * 2f) - FIREWALL_SPEED_VARIATION;
        firewallStream.add(new FirewallStreamEntry(firewallId, speed));
    }

    private boolean isSpawnLaneSafe(float x, float y) {
        if (isInsideExitSafetyCorridor(new Vector2(x, y))) {
            return false;
        }

        for (FirewallStreamEntry entry : firewallStream) {
            Entity other = entityManager.getEntity(entry.id);
            if (other == null) {
                continue;
            }
            Vector2 pos = other.getPosition();
            float dx = Math.abs(pos.x - x);
            float dy = Math.abs(pos.y - y);
            if (dx < FIREWALL_MIN_X_GAP && dy < 95f) {
                return false;
            }
        }
        return true;
    }

    private boolean isInsideExitSafetyCorridor(Vector2 point) {
        return point.x > GOAL_X - SAFE_EXIT_X_MARGIN
            && point.x < GOAL_X + GOAL_W + SAFE_EXIT_X_MARGIN
            && point.y > GOAL_Y - SAFE_EXIT_Y_MARGIN
            && point.y < GOAL_Y + GOAL_H + SAFE_EXIT_Y_MARGIN;
    }

    private void persistPlayerHealth() {
        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (player == null) {
            return;
        }
        GameSession session = context.getService(GameSession.class);
        if (session != null) {
            session.setPersistentPlayerHealth(player.getHealth());
        }
    }

    @Override
    public void render() {
        if (spriteBatch == null || backgroundTexture == null) {
            return;
        }

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.setColor(1f, 1f, 1f, 0.45f);
        spriteBatch.draw(backgroundTexture, backgroundScrollX, 0f, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        spriteBatch.draw(backgroundTexture, backgroundScrollX + GameConstants.WORLD_WIDTH, 0f, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    @Override
    public void renderOverlay() {
        renderObjectiveHud();
        renderObfuscationShield();
        renderEntityLabels();
        renderContextPanels();
        renderTransitionOverlay();
    }

    private void renderObjectiveHud() {
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        hudFont.draw(spriteBatch, "[M] Mission   [H] Controls", 24f, 670f);
        if (exitZoneDeployed) {
            hudFont.draw(spriteBatch, "EXITZONE", GOAL_X + 56f, GOAL_Y + GOAL_H + 24f);
        } else {
            int remaining = Math.max(0, (int) Math.ceil(EXIT_ZONE_UNLOCK_SECONDS - escapeRuntime));
            hudFont.draw(spriteBatch, "ExitZone unlocks in: " + remaining + "s", 24f, 640f);
        }
        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (player != null && player.isObfuscated()) {
            int shieldRemaining = Math.max(0, (int) Math.ceil(player.getObfuscationRemainingSeconds()));
            hudFont.draw(spriteBatch, "Temporary obfuscation: " + shieldRemaining + "s", 24f, 610f);
        }
        spriteBatch.end();
    }

    private void renderObfuscationShield() {
        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (player == null || !player.isObfuscated() || shapeRenderer == null) {
            return;
        }

        Vector2 pos = player.getPosition();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.20f, 0.95f, 1.00f, 0.12f);
        shapeRenderer.circle(pos.x, pos.y, 54f);
        shapeRenderer.setColor(0.35f, 1.00f, 1.00f, 0.28f);
        shapeRenderer.circle(pos.x, pos.y, 62f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderEntityLabels() {
        Entity player = entityManager.getEntity(playerId);
        Entity enemy = entityManager.getEntity(enemyId);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        if (player != null) {
            drawEntityLabel("Player", player.getPosition().x, player.getPosition().y + 82f, GameUiTheme.PLAYER_COLOR);
        }
        if (enemy != null) {
            drawEntityLabel("AI", enemy.getPosition().x, enemy.getPosition().y + 82f, GameUiTheme.AI_COLOR);
        }
        spriteBatch.end();
    }

    private void drawEntityLabel(String label, float centerX, float baselineY, Color color) {
        Color old = hudFont.getColor().cpy();
        hudFont.setColor(color);
        glyphLayout.setText(hudFont, label);
        hudFont.draw(spriteBatch, label, centerX - (glyphLayout.width / 2f), baselineY);
        hudFont.setColor(old);
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
                session.prepareLoader(SceneType.ESCAPE);
            }
            requestSceneWithMusicStop(SceneType.LOADER);
        }
    }

    private boolean contains(Vector2 point, float x, float y, float width, float height) {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
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

    @Override
    public void unload() {
        if (suspendedForSettings) {
            return;
        }

        context.getAudioManager().stopMusic();
        gameplayMusicStarted = false;

        context.getEventManager().unsubscribe(EscapeExitZoneUnlockedEvent.class, exitZoneUnlockedListener);
        context.getEventManager().unsubscribe(EscapeExitReachedEvent.class, exitReachedListener);
        context.getEventManager().unsubscribe(ObfuscatedCodeCollectedEvent.class, obfuscatedCodeCollectedListener);

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        if (ioManager != null) {
            ioManager.dispose();
            ioManager = null;
        }
        playerControl = null;
        firewallStream.clear();

        entityManager.cleanup();
        isLoaded = false;
    }

    @Override
    public boolean runsSimulationSystems() {
        return !briefingActive && !missionModalOpen && !controlsModalOpen;
    }

    @Override
    public boolean rendersWorldEntities() {
        return true;
    }

    private void ensureGameplayMusic() {
        if (gameplayMusicStarted) {
            return;
        }
        context.getAudioManager().playMusic("escape_scene_bgm.mp3");
        gameplayMusicStarted = true;
    }

    private void handleExitReached() {
        context.getAudioManager().playSound("exit.mp3");
        persistPlayerHealth();
        requestSceneWithMusicStop(SceneType.ATTACK);
    }

    private void requestSceneWithMusicStop(SceneType targetScene) {
        context.getAudioManager().stopMusic();
        gameplayMusicStarted = false;
        requestScene(targetScene);
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
}





