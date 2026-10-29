package io.github.cybergrid.simulation.app.game.entities;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.entities.TriggerEntity;

public abstract class ServerEntityBase extends TriggerEntity {

    private static final float SCAN_DURATION = 3f;
    private static final float SCAN_RESET_GRACE = 0.35f;
    private static final float SIZE = 112f;
    private boolean isPlayerInside = false;
    private boolean triggered;
    private boolean scanning;
    private boolean scanned;
    private boolean visible;
    private float scanTimer;
    private float scanResetTimer;
    private Entity currentScanner;

    public ServerEntityBase(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        setCollisionComponent(new CollisionComponent(SIZE, SIZE, false, true, 0f, 0f));
        triggered = false;
        scanning = false;
        scanned = false;
        visible = true;
        scanTimer = 0f;
        scanResetTimer = 0f;
        currentScanner = null;
    }

    public void startScan(Entity activator) {
        if (!triggered && !scanning) {
            scanning = true;
            currentScanner = activator;
            scanResetTimer = 0f;
        }
    }

    public void stopScan() {
        if (scanning) {
            scanning = false;
            currentScanner = null;
        }
    }

    @Override
    public void onCollision(Entity other) {
        if (other instanceof PlayerEntity) {
            isPlayerInside = true;
            this.startScan(other);
        }
    }

    @Override
    public void onCollisionExit(Entity other) {
        if (other instanceof PlayerEntity) {
            isPlayerInside = false;
            this.stopScan();
        }
    }

    public boolean isPlayerInside() {
        return isPlayerInside;
    }

    @Override
    public void update(float dt) {
        if (triggered) {
            return;
        }

        if (scanning) {
            scanResetTimer = 0f;
            scanTimer += dt;
            onScanProgress(scanTimer / SCAN_DURATION);
            if (scanTimer >= SCAN_DURATION) {
                scanning = false;
                activate(currentScanner);
            }
            return;
        }

        // Small grace period prevents tiny overlap jitter from feeling like "extra delay".
        if (scanTimer > 0f) {
            scanResetTimer += dt;
            if (scanResetTimer >= SCAN_RESET_GRACE) {
                scanTimer = 0f;
                scanResetTimer = 0f;
                onScanInterrupted();
            }
        }
    }

    @Override
    public void activate(Entity activator) {
        if (!visible || scanned) return;
        triggered = true;
        scanned = true;
        onScanCompleted();
        System.out.println("Server Scan Complete");
        super.activate(activator);
    }

    public boolean isTriggered() {
        return triggered;
    }

    public boolean isScanning() {
        return scanning;
    }

    public boolean isScanned() {
        return scanned;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getScanProgress() {
        return triggered ? 1f : (scanTimer / SCAN_DURATION);
    }

    public boolean countsAsSecureNode() {
        return false;
    }

    public boolean isAttackEntryPoint() {
        return false;
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            setCollisionComponent(null);
        }
    }

    protected void resetTriggerState() {
        triggered = false;
        scanned = false;
        visible = true;
        scanning = false;
        scanTimer = 0f;
        scanResetTimer = 0f;
        currentScanner = null;
        setCollisionComponent(new CollisionComponent(SIZE, SIZE, false, true, 0f, 0f));
    }

    protected abstract void onScanCompleted();

    protected void onScanProgress(float progress) {
        // no additional behaviour for now
    }

    protected void onScanInterrupted() {
        // no additional behaviour for now
    }

    @Override
    public void destroy() {
        if (getTexture() != null) {
            getTexture().dispose();
            setTexture(null);
        }
    }
}
