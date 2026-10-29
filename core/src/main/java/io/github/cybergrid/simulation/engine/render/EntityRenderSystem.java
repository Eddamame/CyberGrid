package io.github.cybergrid.simulation.engine.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.managers.EntityManager;

public class EntityRenderSystem {
    private final EntityManager entityManager;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final RenderContext spriteContext;
    private final RenderContext shapeContext;

    public EntityRenderSystem(EngineContext context) {
        this.entityManager = context.getEntityManager();
        this.shapeRenderer = new ShapeRenderer();
        this.batch = context.getSharedBatch();
        this.font = context.getSharedFont();
        this.spriteContext = RenderContext.sprite(this.batch);
        this.shapeContext = RenderContext.shape(this.shapeRenderer);
    }

    public void renderWorld(Viewport viewport) {
        if (viewport == null) {
            return;
        }
        renderEntitySprites(viewport);
        renderEntityShapes(viewport);
        renderEntityLabels(viewport);
    }

    private void renderEntitySprites(Viewport viewport) {
        if (batch == null) {
            return;
        }

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        for (Entity entity : entityManager.getAllEntities()) {
            RenderComponent renderComponent = entity.getRenderComponent();
            if (renderComponent != null) {
                renderComponent.render(entity, spriteContext);
            }
        }

        batch.end();
    }

    private void renderEntityShapes(Viewport viewport) {
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity entity : entityManager.getAllEntities()) {
            RenderComponent renderComponent = entity.getRenderComponent();
            if (renderComponent != null) {
                renderComponent.render(entity, shapeContext);
            }
        }
        shapeRenderer.end();
    }

    private void renderEntityLabels(Viewport viewport) {
        if (batch == null || font == null) {
            return;
        }

        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        Color originalColor = new Color(font.getColor());

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        for (Entity entity : entityManager.getAllEntities()) {
            RenderComponent renderComponent = entity.getRenderComponent();
            if (renderComponent != null && !renderComponent.rendersLabel()) {
                continue;
            }
            String label = entity.getDisplayName();
            if (label == null || label.isBlank()) {
                continue;
            }
            float x = entity.getPosition().x - 20f;
            float y = entity.getPosition().y + 40f;
            font.draw(batch, label, x, y);
        }

        batch.end();

        font.setColor(originalColor);
        font.getData().setScale(originalScaleX, originalScaleY);
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}


