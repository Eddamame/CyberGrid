package io.github.cybergrid.simulation.app.game.combat;

public interface PlayerHealth {
    int getHealth();
    void setHealth(int health);

    default boolean isDamageImmune() {
        return false;
    }
}
