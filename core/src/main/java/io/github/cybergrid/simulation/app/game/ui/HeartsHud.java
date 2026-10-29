package io.github.cybergrid.simulation.app.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.events.GameEventBus;
import io.github.cybergrid.simulation.app.game.events.PlayerDamagedEvent;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;

public class HeartsHud implements IEventListener<PlayerDamagedEvent> {
    private final GameEventBus gameEventBus;
    private final Stage stage;
    private final Texture heartFull;
    private final Texture heartEmpty;
    private final Drawable heartFullDrawable;
    private final Drawable heartEmptyDrawable;
    private final Image[] heartImages;
    private final int maxHealth;

    public HeartsHud(GameEventBus gameEventBus, Viewport viewport, int maxHealth) {
        this.gameEventBus = gameEventBus;
        this.maxHealth = maxHealth;
        this.stage = new Stage(viewport);
        this.heartFull = new Texture("heart_full.png");
        this.heartEmpty = new Texture("heart_empty.png");
        this.heartFullDrawable = new TextureRegionDrawable(new TextureRegion(heartFull));
        this.heartEmptyDrawable = new TextureRegionDrawable(new TextureRegion(heartEmpty));
        this.heartImages = new Image[maxHealth];

        Table table = new Table();
        table.top().right().pad(10);
        table.setFillParent(true);

        for (int i = 0; i < maxHealth; i++) {
            heartImages[i] = new Image(heartFullDrawable);
            table.add(heartImages[i]).size(32).pad(4);
        }

        stage.addActor(table);
        gameEventBus.subscribe(PlayerDamagedEvent.class, this);
    }

    @Override
    public void onEvent(PlayerDamagedEvent event) {
        int clamped = Math.max(0, Math.min(maxHealth, event.getCurrentHealth()));
        for (int i = 0; i < maxHealth; i++) {
            heartImages[i].setDrawable(i < clamped ? heartFullDrawable : heartEmptyDrawable);
        }
    }

    public void reset() {
        for (int i = 0; i < maxHealth; i++) {
            heartImages[i].setDrawable(heartFullDrawable);
        }
    }

    public void render() {
        stage.getViewport().apply();
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        gameEventBus.unsubscribe(PlayerDamagedEvent.class, this);
        heartFull.dispose();
        heartEmpty.dispose();
        stage.dispose();
    }
}
