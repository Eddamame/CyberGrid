package io.github.cybergrid.simulation.app.game.scenes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.entities.GameEntityBootstrap;
import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.entities.EnemyEntity;
import io.github.cybergrid.simulation.app.game.entities.PlayerEntity;
import io.github.cybergrid.simulation.app.game.entities.ServerEntityBase;
import io.github.cybergrid.simulation.app.game.input.GameCommand;
import io.github.cybergrid.simulation.app.game.input.GameInputConfig;
import io.github.cybergrid.simulation.app.game.player.PlayerControl;
import io.github.cybergrid.simulation.app.game.entities.ServerScanCoordinator;
import io.github.cybergrid.simulation.app.game.GameSession;
import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.app.game.ui.InGameModalHelper;
import io.github.cybergrid.simulation.app.game.ui.ModalContent;
import io.github.cybergrid.simulation.app.game.ui.UiSoundHelper;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.managers.CollisionManager;
import io.github.cybergrid.simulation.engine.managers.EntityManager;
import io.github.cybergrid.simulation.engine.managers.InputOutputManager;
import io.github.cybergrid.simulation.engine.managers.MovementManager;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneState;
import io.github.cybergrid.simulation.engine.scene.SceneType;
import io.github.cybergrid.simulation.engine.transition.FadeTransition;

public class ScanningScene extends Scene {

    private static final int SERVER_PAIR_COUNT = 5;
    private static final int VULNERABLE_SERVER_COUNT = 2;
    private static final float SERVER_COLLISION_SIZE = 112f;
    private static final float FIREWALL_COLLISION_WIDTH = 132f;
    private static final float FIREWALL_COLLISION_HEIGHT = 52f;
    private static final float SERVER_SCAN_SECONDS = 3f;
    private static final float PANEL_BTN_W = 90f;
    private static final float PANEL_BTN_H = 44f;
    private static final float START_BTN_W = 220f;
    private static final float START_BTN_H = 52f;

    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;

    private SpriteBatch spriteBatch;
    private BitmapFont hudFont;
    private BitmapFont bodyFont;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout glyphLayout;
    private Viewport viewport;
    private InputOutputManager ioManager;
    private PlayerControl playerControl;
    private ServerScanCoordinator scanCoordinator;

    private Texture backgroundTex;

    private int playerId;
    private int enemyId;
    private final List<Integer> serverIds = new ArrayList<>();
    private final List<Integer> firewallIds = new ArrayList<>();

    private int scannedSecureCount;
    private int scannedVulnerableCount;
    private boolean missionModalOpen;
    private boolean controlsModalOpen;
    private boolean suspendedForSettings;
    private boolean gameplayMusicStarted;

    private ModalContent missionModalContent;
    private ModalContent controlsModalContent;
    private int briefingPageIndex;
    private boolean briefingActive;
    private float missionModalScrollOffset;
    private float controlsModalScrollOffset;
    private boolean draggingMissionScrollbar;
    private boolean draggingControlsScrollbar;
    private float missionScrollbarGrabOffsetY;
    private float controlsScrollbarGrabOffsetY;
    private final Random random = new Random();

    private static final class SpawnPair {
        private final float serverX;
        private final float serverY;
        private final float firewallX;
        private final float firewallY;
        private final boolean vulnerable;

        private SpawnPair(float serverX, float serverY, float firewallX, float firewallY, boolean vulnerable) {
            this.serverX = serverX;
            this.serverY = serverY;
            this.firewallX = firewallX;
            this.firewallY = firewallY;
            this.vulnerable = vulnerable;
        }
    }

    public ScanningScene(EngineContext context) {
        super(context);
        this.name = "ScanningScene";
        setTransition(new FadeTransition(1.35f));
        this.entityManager = context.getEntityManager();
        this.movementManager = context.getMovementManager();
        this.collisionManager = context.getCollisionManager();
    }

    @Override
    public void load() {
        if (isLoaded) {
            return;
        }

        spriteBatch = context.getSharedBatch();
        hudFont = GameUiTheme.getHudFont();
        bodyFont = GameUiTheme.getMenuFont();
        if (spriteBatch == null || bodyFont == null || hudFont == null) {
            throw new IllegalStateException("Shared render resources are not configured in EngineContext.");
        }

        shapeRenderer = new ShapeRenderer();
        glyphLayout = new GlyphLayout();
        viewport = new FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        backgroundTex = new Texture("scanningscenebg.png");

        ioManager = new InputOutputManager(context.getEventManager());
        GameInputConfig.setupDefaultControls(ioManager);
        Gdx.input.setInputProcessor(ioManager.getInputProcessor());

        if (context.getSceneManager().getPreviousSceneType() == SceneType.START) {
            GameSession session = context.getService(GameSession.class);
            if (session != null) {
                session.resetRun();
            }
        }

        resetSimulationState();
        configureSceneContext();
        spawnEntities();
        playerControl = new PlayerControl(ioManager, movementManager, playerId);
        scanCoordinator = new ServerScanCoordinator(entityManager);
        briefingPageIndex = 0;
        briefingActive = true;
        missionModalScrollOffset = 0f;
        controlsModalScrollOffset = 0f;
        draggingMissionScrollbar = false;
        draggingControlsScrollbar = false;
        missionScrollbarGrabOffsetY = 0f;
        controlsScrollbarGrabOffsetY = 0f;
        GameSession session = context.getService(GameSession.class);
        if (session != null && session.consumeBriefingBypass(SceneType.MAIN)) {
            briefingActive = false;
        }

        suspendedForSettings = false;
        gameplayMusicStarted = false;
        isLoaded = true;
    }

    private void configureSceneContext() {
        missionModalContent = new ModalContent("Scanning Mission", Arrays.asList(
                "You are inside the network.",
                "",
                "Your objective is to find a vulnerable server.",
                "",
                "Move into a server zone and stay inside it for a few seconds to scan it.",
                "Some servers are secure and will show no weakness.",
                "One of them will reveal a vulnerability.",
                "",
                "Once you find it, move in and press ENTER to investigate the vulnerability further.",
                "",
                "Do not rush.",
                "Finding the right target is the key to the entire operation."
        ));
        controlsModalContent = new ModalContent("Controls", Arrays.asList(
                "WASD: Move",
                "",
                "ENTER: Breach a discovered vulnerable server",
                "",
                "SPACE: Open settings",
                "",
                "M / H: Toggle mission and controls",
                "",
                "Click Start Mission to begin"
        ));
    }

    private void resetSimulationState() {
        entityManager.cleanup();
        collisionManager.reset();
        serverIds.clear();
        firewallIds.clear();
        scannedSecureCount = 0;
        scannedVulnerableCount = 0;
        missionModalOpen = false;
        controlsModalOpen = false;
    }

    private void spawnEntities() {
        playerId = GameEntityBootstrap.createPlayer(context);
        enemyId = GameEntityBootstrap.createEnemy(context);

        entityManager.getEntity(playerId).setPosition(new Vector2(240f, 100f));
        PlayerEntity player = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (player != null) {
            player.setSpeed(player.getSpeed() * GameConstants.ESCAPE_PLAYER_SPEED_MULTIPLIER);
        }

        EnemyEntity enemy = entityManager.getEntityAs(enemyId, EnemyEntity.class);
        if (enemy != null) {
            enemy.setPosition(new Vector2(1100f, 120f));
            enemy.setTargetEntity(playerId);
        }

        for (SpawnPair pair : generateRandomSpawnPairs()) {
            int serverId = pair.vulnerable
                ? GameEntityBootstrap.createVulnerableServer(context)
                : GameEntityBootstrap.createSecureServer(context);
            entityManager.getEntity(serverId).setPosition(new Vector2(pair.serverX, pair.serverY));
            serverIds.add(serverId);

            int firewallId = GameEntityBootstrap.createStaticFirewall(context);
            entityManager.getEntity(firewallId).setPosition(new Vector2(pair.firewallX, pair.firewallY));
            firewallIds.add(firewallId);
        }
    }

    private List<SpawnPair> generateRandomSpawnPairs() {
        List<SpawnPair> pairs = new ArrayList<>();
        List<Rectangle> occupiedBounds = new ArrayList<>();
        List<Boolean> vulnerableSlots = new ArrayList<>();

        for (int i = 0; i < SERVER_PAIR_COUNT; i++) {
            vulnerableSlots.add(i < VULNERABLE_SERVER_COUNT);
        }
        Collections.shuffle(vulnerableSlots, random);

        float[][] directionVectors = new float[][] {
                {1f, 0f}, {-1f, 0f}, {0f, 1f}, {0f, -1f},
                {0.8f, 0.6f}, {-0.8f, 0.6f}, {0.8f, -0.6f}, {-0.8f, -0.6f}
        };

        for (int i = 0; i < SERVER_PAIR_COUNT; i++) {
            boolean spawned = false;
            for (int attempt = 0; attempt < 500 && !spawned; attempt++) {
                float serverX = randomRange(180f, GameConstants.WORLD_WIDTH - 180f);
                float serverY = randomRange(150f, GameConstants.WORLD_HEIGHT - 110f);
                float[] dir = directionVectors[random.nextInt(directionVectors.length)];
                float distance = randomRange(165f, 225f);
                float firewallX = serverX + (dir[0] * distance);
                float firewallY = serverY + (dir[1] * distance);

                if (!insideWorld(firewallX, firewallY, FIREWALL_COLLISION_WIDTH / 2f, FIREWALL_COLLISION_HEIGHT / 2f)) {
                    continue;
                }

                Rectangle serverRect = centeredRect(serverX, serverY, SERVER_COLLISION_SIZE, SERVER_COLLISION_SIZE);
                Rectangle firewallRect = centeredRect(firewallX, firewallY, FIREWALL_COLLISION_WIDTH, FIREWALL_COLLISION_HEIGHT);
                if (overlapsAny(serverRect, occupiedBounds, 28f) || overlapsAny(firewallRect, occupiedBounds, 28f)) {
                    continue;
                }
                if (serverRect.overlaps(firewallRect)) {
                    continue;
                }

                pairs.add(new SpawnPair(serverX, serverY, firewallX, firewallY, vulnerableSlots.get(i)));
                occupiedBounds.add(serverRect);
                occupiedBounds.add(firewallRect);
                spawned = true;
            }
        }

        if (pairs.size() == SERVER_PAIR_COUNT) {
            return pairs;
        }

        return Arrays.asList(
                new SpawnPair(230f, 570f, 230f, 420f, false),
                new SpawnPair(470f, 250f, 660f, 250f, true),
                new SpawnPair(700f, 560f, 520f, 560f, false),
                new SpawnPair(930f, 235f, 930f, 395f, true),
                new SpawnPair(1120f, 545f, 930f, 545f, false)
        );
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private boolean insideWorld(float x, float y, float halfWidth, float halfHeight) {
        return x - halfWidth >= 0f
                && x + halfWidth <= GameConstants.WORLD_WIDTH
                && y - halfHeight >= 0f
                && y + halfHeight <= GameConstants.WORLD_HEIGHT;
    }

    private Rectangle centeredRect(float x, float y, float width, float height) {
        return new Rectangle(x - (width / 2f), y - (height / 2f), width, height);
    }

    private boolean overlapsAny(Rectangle rect, List<Rectangle> existing, float padding) {
        Rectangle expanded = new Rectangle(rect.x - padding, rect.y - padding, rect.width + (padding * 2f), rect.height + (padding * 2f));
        for (Rectangle other : existing) {
            if (expanded.overlaps(other)) {
                return true;
            }
        }
        return false;
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

        ensureGameplayMusic();

        if (playerControl != null) {
            playerControl.update();
        }
        updateScanStatus();

        if (ioManager.isCommandJustPressed(GameCommand.INTERACT) && isPlayerInsideTriggeredAttackEntry()) {
            context.getAudioManager().playSound("vuln_server_entry.mp3");
            requestScene(SceneType.ESCAPE);
            return;
        }

        ioManager.update();
    }

    private void updateScanStatus() {
        if (scanCoordinator == null) {
            return;
        }

        ServerScanCoordinator.ScanSnapshot snapshot = scanCoordinator.evaluate(serverIds);
        scannedSecureCount = snapshot.getSecureScannedCount();
        scannedVulnerableCount = snapshot.getVulnerableTriggeredCount();

        if (!snapshot.isAttackEntryDiscovered()) {
            return;
        }

        // Save player health before leaving the scene
        PlayerEntity playerEntity = entityManager.getEntityAs(playerId, PlayerEntity.class);
        if (playerEntity != null) {
            GameSession session = context.getService(GameSession.class);
            if (session != null) {
                session.setPersistentPlayerHealth(playerEntity.getHealth());
            }
        }
    }

    @Override
    public void render() {
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.setColor(1f, 1f, 1f, 0.35f);
        spriteBatch.draw(backgroundTex, 0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    @Override
    public void renderOverlay() {
        renderEntityLabels();
        renderServerScanLoaders();
        renderHudChips();
        renderStatusHud();
        renderContextPanels();
        renderTransitionOverlay();
    }

    private void renderEntityLabels() {
        Entity player = entityManager.getEntity(playerId);
        Entity enemy = entityManager.getEntity(enemyId);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        if (player != null) {
            Vector2 playerPos = player.getPosition();
            drawEntityLabel("Player", playerPos.x, playerPos.y + 86f, GameUiTheme.PLAYER_COLOR);
        }
        if (enemy != null) {
            Vector2 enemyPos = enemy.getPosition();
            drawEntityLabel("AI", enemyPos.x, enemyPos.y + 86f, GameUiTheme.AI_COLOR);
        }
        spriteBatch.end();
    }

    private void drawEntityLabel(String label, float centerX, float baselineY, Color color) {
        float oldR = hudFont.getColor().r;
        float oldG = hudFont.getColor().g;
        float oldB = hudFont.getColor().b;
        float oldA = hudFont.getColor().a;

        hudFont.setColor(color);
        glyphLayout.setText(hudFont, label);
        hudFont.draw(spriteBatch, label, centerX - (glyphLayout.width / 2f), baselineY);
        hudFont.setColor(oldR, oldG, oldB, oldA);
    }

    private void renderServerScanLoaders() {
        if (shapeRenderer == null || spriteBatch == null) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int serverId : serverIds) {
            ServerEntityBase server = entityManager.getEntityAs(serverId, ServerEntityBase.class);
            if (server == null || !server.isScanning() || server.isTriggered()) {
                continue;
            }

            Vector2 pos = server.getPosition();
            float panelW = 124f;
            float panelH = 56f;
            float panelX = pos.x - (panelW / 2f);
            float panelY = pos.y + 66f;

            shapeRenderer.setColor(0f, 0f, 0f, 0.44f);
            shapeRenderer.rect(panelX, panelY, panelW, panelH);

            shapeRenderer.setColor(0.62f, 0.85f, 1f, 0.95f);
            float progress = Math.max(0f, Math.min(1f, server.getScanProgress()));
            shapeRenderer.rect(panelX + 6f, panelY + 6f, (panelW - 12f) * progress, 8f);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        for (int serverId : serverIds) {
            ServerEntityBase server = entityManager.getEntityAs(serverId, ServerEntityBase.class);
            if (server == null || !server.isScanning() || server.isTriggered()) {
                continue;
            }

            Vector2 pos = server.getPosition();
            float progress = Math.max(0f, Math.min(1f, server.getScanProgress()));
            int remaining = Math.max(1, (int) Math.ceil((1f - progress) * SERVER_SCAN_SECONDS));

            String header = "Scanning...";
            glyphLayout.setText(hudFont, header);
            hudFont.draw(spriteBatch, header, pos.x - (glyphLayout.width / 2f), pos.y + 110f);

            String countdown = String.valueOf(remaining);
            glyphLayout.setText(hudFont, countdown);
            hudFont.draw(spriteBatch, countdown, pos.x - (glyphLayout.width / 2f), pos.y + 90f);
        }

        spriteBatch.end();
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

    private void renderStatusHud() {
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        hudFont.draw(spriteBatch, "Secure nodes scanned: " + scannedSecureCount, 28f, 700f);
        hudFont.draw(spriteBatch, "Vulnerable nodes found: " + scannedVulnerableCount, 28f, 672f);
        if (isPlayerInsideTriggeredAttackEntry()) {
            hudFont.draw(spriteBatch, "Press ENTER to breach this node", 28f, 644f);
        }

        spriteBatch.end();
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
                session.prepareLoader(SceneType.MAIN);
            }
            requestScene(SceneType.LOADER);
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

        if (missionModalOpen) {
            missionModalScrollOffset = InGameModalHelper.render(
                viewport,
                shapeRenderer,
                spriteBatch,
                bodyFont,
                hudFont,
                glyphLayout,
                missionModalContent.getTitle(),
                missionModalContent.getLines(),
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
                controlsModalContent.getTitle(),
                controlsModalContent.getLines(),
                controlsModalScrollOffset
            );
        }
    }

    private void renderBriefingOverlay() {
        ModalContent template = briefingPageIndex == 0 ? missionModalContent : controlsModalContent;
        if (briefingPageIndex == 0) {
            missionModalScrollOffset = InGameModalHelper.render(
                viewport,
                shapeRenderer,
                spriteBatch,
                bodyFont,
                hudFont,
                glyphLayout,
                template.getTitle(),
                template.getLines(),
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
                template.getTitle(),
                template.getLines(),
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
        if (transition != null && !transition.isFinished()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, transition.getOverlayAlpha());
            shapeRenderer.rect(0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    @Override
    public void unload() {
        if (suspendedForSettings) {
            return;
        }

        context.getAudioManager().stopMusic();
        gameplayMusicStarted = false;
        if (backgroundTex != null) {
            backgroundTex.dispose();
            backgroundTex = null;
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
        scanCoordinator = null;

        entityManager.cleanup();
        isLoaded = false;
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
        context.getAudioManager().playMusic("scanning_scene_bgm.mp3");
        gameplayMusicStarted = true;
    }

    public boolean isPlayerInsideTriggeredAttackEntry() {
        for (int serverId : serverIds) {
            ServerEntityBase server = entityManager.getEntityAs(serverId, ServerEntityBase.class);
            
            // Ask the server directly!
            if (server != null && server.isAttackEntryPoint() && server.isTriggered()) {
                if (server.isPlayerInside()) {
                    return true;
                }
            }
        }
        return false;
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








