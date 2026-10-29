package io.github.cybergrid.simulation.engine.interfaces;

public interface Transition {
    void start();
    void update(float dt);
    boolean isFinished();

    default float getOverlayAlpha() {
        return 0f;
    }
}
