package io.github.cybergrid.simulation.app.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public final class UiTextureHelper {
    private UiTextureHelper() {
    }

    public static Texture outlined(int width, int height, Color color, int borderThickness) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        for (int i = 0; i < borderThickness; i++) {
            pixmap.drawRectangle(i, i, width - (i * 2), height - (i * 2));
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pixmap.dispose();
        return texture;
    }

    public static Texture filled(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        pixmap.dispose();
        return texture;
    }
}

