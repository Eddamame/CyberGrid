package io.github.cybergrid.simulation.engine.transition;

import com.badlogic.gdx.math.Interpolation;

import io.github.cybergrid.simulation.engine.interfaces.Transition;

public class FadeTransition implements Transition {
    private float alpha = 1f;
    private float duration;
    private float elapsed = 0f;
    private boolean finished = false;

    public FadeTransition(float duration) {
        this.duration = duration;
    }

    @Override
    public void start() {
        alpha = 1f;
        elapsed = 0f;
        finished = false;
    }

    @Override
    public void update(float dt) {
        if (finished) return;

        elapsed += dt;
        float progress = Math.min(1f, elapsed / duration);

        alpha = 1f - Interpolation.pow2Out.apply(progress);

        if (elapsed >= duration) {
            alpha = 0f;
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public float getOverlayAlpha() {
        return alpha;
    }
}
