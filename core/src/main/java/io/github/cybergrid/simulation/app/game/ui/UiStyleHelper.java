package io.github.cybergrid.simulation.app.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public final class UiStyleHelper {
    private UiStyleHelper() {
    }

    public static TextButton.TextButtonStyle textButtonStyle(BitmapFont font, Color themeColor, Texture up, Texture down) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.up = patchDrawable(up);
        style.down = patchDrawable(down);
        style.fontColor = themeColor;
        style.downFontColor = themeColor;
        style.overFontColor = themeColor;
        style.checkedFontColor = themeColor;
        return style;
    }

    public static ScrollPane.ScrollPaneStyle scrollPaneStyle(Texture track, Texture knob) {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        style.vScroll = patchDrawable(track);
        style.vScrollKnob = patchDrawable(knob);
        return style;
    }

    public static ImageButton.ImageButtonStyle imageButtonStyle(Texture up, Texture down, Texture icon) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = patchDrawable(up);
        style.down = patchDrawable(down);
        TextureRegionDrawable iconDrawable = new TextureRegionDrawable(new TextureRegion(icon));
        style.imageUp = iconDrawable;
        style.imageDown = iconDrawable.tint(Color.GRAY);
        return style;
    }

    private static NinePatchDrawable patchDrawable(Texture texture) {
        return new NinePatchDrawable(new NinePatch(texture, 3, 3, 3, 3));
    }
}

