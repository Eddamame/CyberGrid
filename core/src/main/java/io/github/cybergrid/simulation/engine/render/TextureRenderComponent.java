package io.github.cybergrid.simulation.engine.render;

import com.badlogic.gdx.graphics.Texture;

import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class TextureRenderComponent implements RenderComponent {

    private final float maxWidth;
    private final float maxHeight;
    private final float uniformScale;

    public TextureRenderComponent() {
        this(-1f, -1f, 1f);
    }

    public TextureRenderComponent(float uniformScale) {
        this(-1f, -1f, uniformScale);
    }

    public TextureRenderComponent(float maxWidth, float maxHeight) {
        this(maxWidth, maxHeight, 1f);
    }

    public TextureRenderComponent(float maxWidth, float maxHeight, float uniformScale) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.uniformScale = uniformScale;
    }

    @Override
    public void render(Entity entity, RenderContext context) {
        if (!context.isSpriteLayer() || context.getSpriteBatch() == null) {
            return;
        }
        com.badlogic.gdx.graphics.g2d.SpriteBatch batch = context.getSpriteBatch();
        Texture texture = entity.getTexture();
        if (texture == null) {
            return;
        }

        CollisionComponent collision = null;
        if (entity instanceof ICollidable) {
            collision = ((ICollidable) entity).getCollisionComponent();
        }

        float[] size = resolveSize(texture, collision);
        float width = size[0];
        float height = size[1];

        float x = entity.getPosition().x - (width / 2f);
        float y = entity.getPosition().y - (height / 2f);
        batch.draw(texture, x, y, width, height);
    }

    @Override
    public boolean rendersLabel() {
        return false;
    }

    private float[] resolveSize(Texture texture, CollisionComponent collision) {
        float targetWidth = maxWidth;
        float targetHeight = maxHeight;

        if (targetWidth <= 0f && collision != null) {
            targetWidth = collision.getWidth();
        }
        if (targetHeight <= 0f && collision != null) {
            targetHeight = collision.getHeight();
        }
        if (targetWidth <= 0f) {
            targetWidth = texture.getWidth();
        }
        if (targetHeight <= 0f) {
            targetHeight = texture.getHeight();
        }

        float textureRatio = texture.getWidth() / (float) texture.getHeight();
        float boxRatio = targetWidth / targetHeight;

        float drawWidth;
        float drawHeight;
        if (textureRatio > boxRatio) {
            drawWidth = targetWidth;
            drawHeight = targetWidth / textureRatio;
        } else {
            drawHeight = targetHeight;
            drawWidth = targetHeight * textureRatio;
        }

        return new float[] { drawWidth * uniformScale, drawHeight * uniformScale };
    }
}