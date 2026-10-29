package io.github.cybergrid.simulation.app.game.events;

public class VulnerableServerScannedEvent extends GameEvent {
    private final int serverId;

    public VulnerableServerScannedEvent(int serverId) {
        this.serverId = serverId;
    }

    public int getServerId() {
        return serverId;
    }
}
