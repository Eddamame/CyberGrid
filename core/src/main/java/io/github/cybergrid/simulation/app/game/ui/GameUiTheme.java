package io.github.cybergrid.simulation.app.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public final class GameUiTheme {
    public static final Color PLAYER_COLOR = Color.valueOf("5ce1e6");
    public static final Color AI_COLOR = Color.valueOf("ff1c24");
    public static final Color WIN_COLOR = PLAYER_COLOR;
    public static final Color LOSE_COLOR = AI_COLOR;
    public static final Color THEME_COLOR = PLAYER_COLOR;

    private static BitmapFont titleFont;
    private static BitmapFont menuFont;
    private static BitmapFont hudFont;

    private GameUiTheme() {
    }

    public static void initialize() {
        if (titleFont != null && menuFont != null && hudFont != null) {
            return;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/CascadiaCode.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();

        params.size = 72;
        params.color = Color.WHITE;
        params.borderWidth = 2f;
        params.borderColor = new Color(0f, 0f, 0f, 0.8f);
        titleFont = generator.generateFont(params);

        params.size = 32;
        params.borderWidth = 1f;
        menuFont = generator.generateFont(params);

        params.size = 22;
        params.borderWidth = 1f;
        hudFont = generator.generateFont(params);

        generator.dispose();
    }

    public static BitmapFont getTitleFont() {
        return titleFont;
    }

    public static BitmapFont getMenuFont() {
        return menuFont;
    }

    public static BitmapFont getHudFont() {
        return hudFont;
    }

    public static void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (menuFont != null) {
            menuFont.dispose();
            menuFont = null;
        }
        if (hudFont != null) {
            hudFont.dispose();
            hudFont = null;
        }
    }
}
