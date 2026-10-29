package io.github.cybergrid.simulation.app.game.entities;

import com.badlogic.gdx.graphics.Texture;

import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.app.game.events.VulnerableServerDiscoveredEvent;
import io.github.cybergrid.simulation.app.game.events.VulnerableServerScannedEvent;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.render.TextureRenderComponent;

/**
 * Vulnerable server:
 * - When scanned, it becomes "cracked".
 * - Renderer swaps the normal server PNG for the cracked server PNG.
 * - Dispatches events so game flow can react to discovery and completion.
 */
public class VulnerableServerEntity extends ServerEntityBase {

    private static final float DRAW_SIZE = 250f;
    private final GameEventBus gameEventBus;
    private boolean cracked;
    private boolean discoveryEventPublished;

    public VulnerableServerEntity(EngineContext context, int id, EntityType type, GameEventBus gameEventBus) {
        super(context, id, type);
        this.gameEventBus = gameEventBus;
        this.cracked = false;
        this.discoveryEventPublished = false;
        setTexture(new Texture("secure_server.png"));
        setRenderComponent(new TextureRenderComponent(DRAW_SIZE, DRAW_SIZE));
    }

    @Override
    protected void onScanCompleted() {
        if (!cracked) {
            cracked = true;
            revealCrackTexture();
            publishDiscoveryOnce();
        }
        gameEventBus.dispatch(new VulnerableServerScannedEvent(getID()));
    }

    @Override
    protected void onScanProgress(float progress) {
        if (!cracked) {
            cracked = true;
            revealCrackTexture();
            publishDiscoveryOnce();
        }
    }

    @Override
    protected void onScanInterrupted() {
        cracked = false;
        if (getTexture() != null) {
            getTexture().dispose();
        }
        setTexture(new Texture("secure_server.png"));
    }

    public boolean isCracked() {
        return cracked;
    }

    private void revealCrackTexture() {
        if (getTexture() != null) {
            getTexture().dispose();
        }
        setTexture(new Texture("vuln_server.png"));
    }

    private void publishDiscoveryOnce() {
        if (discoveryEventPublished) {
            return;
        }
        gameEventBus.dispatch(new VulnerableServerDiscoveredEvent(getID()));
        discoveryEventPublished = true;
    }

    @Override
    public boolean isAttackEntryPoint() {
        return true;
    }
}
