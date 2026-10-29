package io.github.cybergrid.simulation.engine.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class RenderContext {
    public enum Layer {
        SPRITE,
        SHAPE
    }

    private final Layer layer;
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;

    private RenderContext(Layer layer, SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        this.layer = layer;
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
    }

    public static RenderContext sprite(SpriteBatch spriteBatch) {
        return new RenderContext(Layer.SPRITE, spriteBatch, null);
    }

    public static RenderContext shape(ShapeRenderer shapeRenderer) {
        return new RenderContext(Layer.SHAPE, null, shapeRenderer);
    }

    public Layer getLayer() {
        return layer;
    }

    public boolean isSpriteLayer() {
        return layer == Layer.SPRITE;
    }

    public boolean isShapeLayer() {
        return layer == Layer.SHAPE;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}


