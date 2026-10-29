package io.github.cybergrid.simulation.app.game.ui;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class InGameModalHelper {
    private static final float PANEL_WIDTH = 860f;
    private static final float PANEL_HEIGHT = 420f;
    private static final float BODY_TOP_PADDING = 62f;
    private static final float BODY_SIDE_PADDING = 28f;
    private static final float BODY_BOTTOM_PADDING = 88f;
    private static final float BODY_LINE_SPACING = 18f;
    private static final float SCROLLBAR_TRACK_W = 8f;
    private static final float SCROLLBAR_MIN_THUMB_H = 22f;
    private static final float SCROLLBAR_RIGHT_PADDING = 14f;

    public static final class ScrollbarMetrics {
        private final float clampedScroll;
        private final float maxScroll;
        private final float trackX;
        private final float trackY;
        private final float trackWidth;
        private final float trackHeight;
        private final float thumbY;
        private final float thumbHeight;
        private final boolean hasScrollbar;

        private ScrollbarMetrics(
            float clampedScroll,
            float maxScroll,
            float trackX,
            float trackY,
            float trackWidth,
            float trackHeight,
            float thumbY,
            float thumbHeight,
            boolean hasScrollbar
        ) {
            this.clampedScroll = clampedScroll;
            this.maxScroll = maxScroll;
            this.trackX = trackX;
            this.trackY = trackY;
            this.trackWidth = trackWidth;
            this.trackHeight = trackHeight;
            this.thumbY = thumbY;
            this.thumbHeight = thumbHeight;
            this.hasScrollbar = hasScrollbar;
        }

        public float getClampedScroll() {
            return clampedScroll;
        }

        public float getMaxScroll() {
            return maxScroll;
        }

        public float getTrackX() {
            return trackX;
        }

        public float getTrackY() {
            return trackY;
        }

        public float getTrackWidth() {
            return trackWidth;
        }

        public float getTrackHeight() {
            return trackHeight;
        }

        public float getThumbY() {
            return thumbY;
        }

        public float getThumbHeight() {
            return thumbHeight;
        }

        public boolean hasScrollbar() {
            return hasScrollbar;
        }

        public boolean containsTrack(float x, float y) {
            if (!hasScrollbar) {
                return false;
            }
            return x >= trackX && x <= trackX + trackWidth && y >= trackY && y <= trackY + trackHeight;
        }

        public boolean containsThumb(float x, float y) {
            if (!hasScrollbar) {
                return false;
            }
            return x >= trackX && x <= trackX + trackWidth && y >= thumbY && y <= thumbY + thumbHeight;
        }

        public float scrollForThumbY(float rawThumbY) {
            if (!hasScrollbar || maxScroll <= 0f) {
                return 0f;
            }
            float travel = Math.max(0f, trackHeight - thumbHeight);
            if (travel <= 0f) {
                return 0f;
            }
            float clampedThumbY = MathUtils.clamp(rawThumbY, trackY, trackY + travel);
            float t = (trackY + travel - clampedThumbY) / travel;
            return MathUtils.clamp(t * maxScroll, 0f, maxScroll);
        }
    }

    private InGameModalHelper() {
    }

    public static ScrollbarMetrics computeScrollbarMetrics(
        Viewport viewport,
        BitmapFont bodyFont,
        GlyphLayout glyphLayout,
        List<String> lines,
        float scrollOffset
    ) {
        if (viewport == null || bodyFont == null || glyphLayout == null) {
            return new ScrollbarMetrics(0f, 0f, 0f, 0f, SCROLLBAR_TRACK_W, 0f, 0f, 0f, false);
        }

        float panelX = (viewport.getWorldWidth() - PANEL_WIDTH) / 2f;
        float panelY = (viewport.getWorldHeight() - PANEL_HEIGHT) / 2f;
        float titleY = panelY + PANEL_HEIGHT - 42f;
        float textAreaTop = titleY - BODY_TOP_PADDING + 4f;
        float textAreaBottom = panelY + BODY_BOTTOM_PADDING;
        float textAreaHeight = Math.max(1f, textAreaTop - textAreaBottom);
        float textWidth = PANEL_WIDTH - (BODY_SIDE_PADDING * 2f) - SCROLLBAR_TRACK_W - 14f;

        float contentHeight = 0f;
        if (lines != null) {
            for (String line : lines) {
                String safeLine = line == null ? "" : line;
                glyphLayout.setText(bodyFont, safeLine, Color.WHITE, textWidth, Align.left, true);
                contentHeight += glyphLayout.height + BODY_LINE_SPACING;
            }
        }

        float maxScroll = Math.max(0f, contentHeight - textAreaHeight);
        float clampedScroll = MathUtils.clamp(scrollOffset, 0f, maxScroll);
        float trackX = panelX + PANEL_WIDTH - SCROLLBAR_RIGHT_PADDING - SCROLLBAR_TRACK_W;
        float trackY = textAreaBottom;
        float trackH = textAreaHeight;

        if (maxScroll <= 0f) {
            return new ScrollbarMetrics(clampedScroll, maxScroll, trackX, trackY, SCROLLBAR_TRACK_W, trackH, trackY, 0f, false);
        }

        float thumbH = Math.max(SCROLLBAR_MIN_THUMB_H, (textAreaHeight / contentHeight) * trackH);
        float travel = Math.max(0f, trackH - thumbH);
        float t = maxScroll <= 0f ? 0f : clampedScroll / maxScroll;
        float thumbY = trackY + (trackH - thumbH) - (travel * t);
        return new ScrollbarMetrics(clampedScroll, maxScroll, trackX, trackY, SCROLLBAR_TRACK_W, trackH, thumbY, thumbH, true);
    }

    public static float render(
        Viewport viewport,
        ShapeRenderer shapeRenderer,
        SpriteBatch spriteBatch,
        BitmapFont titleFont,
        BitmapFont bodyFont,
        GlyphLayout glyphLayout,
        String title,
        List<String> lines,
        float scrollOffset
    ) {
        if (viewport == null || shapeRenderer == null || spriteBatch == null || titleFont == null || bodyFont == null || glyphLayout == null) {
            return 0f;
        }

        float panelX = (viewport.getWorldWidth() - PANEL_WIDTH) / 2f;
        float panelY = (viewport.getWorldHeight() - PANEL_HEIGHT) / 2f;
        float titleY = panelY + PANEL_HEIGHT - 42f;
        float textAreaTop = titleY - BODY_TOP_PADDING + 4f;
        float textAreaBottom = panelY + BODY_BOTTOM_PADDING;
        float textX = panelX + BODY_SIDE_PADDING;
        float textWidth = PANEL_WIDTH - (BODY_SIDE_PADDING * 2f) - SCROLLBAR_TRACK_W - 14f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.88f);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        shapeRenderer.setColor(1f, 1f, 1f, 0.96f);
        shapeRenderer.rect(panelX, panelY + PANEL_HEIGHT - 2f, PANEL_WIDTH, 2f);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, 2f);
        shapeRenderer.rect(panelX, panelY, 2f, PANEL_HEIGHT);
        shapeRenderer.rect(panelX + PANEL_WIDTH - 2f, panelY, 2f, PANEL_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        ScrollbarMetrics metrics = computeScrollbarMetrics(viewport, bodyFont, glyphLayout, lines, scrollOffset);
        float clampedScroll = metrics.getClampedScroll();
        if (metrics.hasScrollbar()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 1f, 1f, 0.16f);
            shapeRenderer.rect(metrics.getTrackX(), metrics.getTrackY(), metrics.getTrackWidth(), metrics.getTrackHeight());
            shapeRenderer.setColor(0.52f, 0.84f, 1f, 0.86f);
            shapeRenderer.rect(metrics.getTrackX(), metrics.getThumbY(), metrics.getTrackWidth(), metrics.getThumbHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float textY = textAreaTop + clampedScroll;

        titleFont.draw(spriteBatch, title, panelX + 28f, titleY);
        if (lines != null) {
            for (String line : lines) {
                String safeLine = line == null ? "" : line;
                glyphLayout.setText(bodyFont, safeLine, Color.WHITE, textWidth, Align.left, true);
                float lineTop = textY;
                float lineBottom = textY - glyphLayout.height;
                if (lineBottom <= textAreaTop && lineTop >= textAreaBottom) {
                    bodyFont.draw(spriteBatch, safeLine, textX, textY, textWidth, Align.left, true);
                }
                textY -= glyphLayout.height + BODY_LINE_SPACING;
            }
        }
        spriteBatch.end();

        return clampedScroll;
    }

    public static void render(
        Viewport viewport,
        ShapeRenderer shapeRenderer,
        SpriteBatch spriteBatch,
        BitmapFont titleFont,
        BitmapFont bodyFont,
        GlyphLayout glyphLayout,
        String title,
        List<String> lines
    ) {
        render(viewport, shapeRenderer, spriteBatch, titleFont, bodyFont, glyphLayout, title, lines, 0f);
    }

    public static void renderBriefingNavigation(
        Viewport viewport,
        ShapeRenderer shapeRenderer,
        SpriteBatch spriteBatch,
        BitmapFont font,
        int currentPage,
        int lastPage,
        float panelBtnW,
        float panelBtnH,
        float startBtnW,
        float startBtnH,
        float leftX,
        float leftY,
        float rightX,
        float rightY,
        float startX,
        float startY
    ) {
        if (viewport == null || shapeRenderer == null || spriteBatch == null || font == null) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.18f, 0.45f, 0.58f, 0.95f);
        if (currentPage > 0) {
            shapeRenderer.rect(leftX, leftY, panelBtnW, panelBtnH);
        }
        if (currentPage < lastPage) {
            shapeRenderer.rect(rightX, rightY, panelBtnW, panelBtnH);
        } else {
            shapeRenderer.rect(startX, startY, startBtnW, startBtnH);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        if (currentPage > 0) {
            font.draw(spriteBatch, "<", leftX + 16f, leftY + 30f);
        }
        if (currentPage < lastPage) {
            font.draw(spriteBatch, ">", rightX + 16f, rightY + 30f);
        } else {
            font.draw(spriteBatch, "Start Mission", startX + 26f, startY + 34f);
        }
        spriteBatch.end();
    }
}

