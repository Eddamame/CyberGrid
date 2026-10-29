package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

/**
 * Secure server:
 * - When fully scanned, it is marked as "verified" and hidden.
 * - Dispatches a scan event so game flow can track progress.
 */
public class SecureServerEntity extends ServerEntityBase {

    private static final float DRAW_SIZE = 250f;
    private boolean verified;

    public SecureServerEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        this.verified = false;
        setTexture(new Texture("secure_server.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_SIZE, DRAW_SIZE));
    }

    @Override
    protected void onScanCompleted() {
        verified = true;
        setVisible(false);
        if (getTexture() != null) {
            getTexture().dispose();
            setTexture(null);
        }
    }

    @Override
    protected void onScanProgress(float progress) {
        // No extra behaviour needed for secure servers
    }

    @Override
    protected void onScanInterrupted() {
        // No extra behavior needed for secure servers.
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean countsAsSecureNode() {
        return true;
    }
}
