package io.github.cybergrid.simulation.app.game.events;

public class VulnerableServerDiscoveredEvent extends GameEvent {
    private final int serverId;

    public VulnerableServerDiscoveredEvent(int serverId) {
        this.serverId = serverId;
    }

    public int getServerId() {
        return serverId;
    }
}
