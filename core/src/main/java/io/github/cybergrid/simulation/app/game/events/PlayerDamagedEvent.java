package io.github.cybergrid.simulation.app.game.events;

public class PlayerDamagedEvent extends GameEvent {
    private final int currentHealth;

    public PlayerDamagedEvent(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}
