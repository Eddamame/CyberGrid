package io.github.cybergrid.simulation.app.game.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.app.game.ui.UiSoundHelper;
import io.github.cybergrid.simulation.app.game.ui.UiStyleHelper;
import io.github.cybergrid.simulation.app.game.ui.UiTextureHelper;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.scene.SceneState;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class GuideScene extends MenuStageScene {
    private final Color themeColor = GameUiTheme.THEME_COLOR;
    private Texture buttonUp;
    private Texture buttonDown;
    private Texture scrollKnob;
    private Texture scrollTrack;
    private Texture settingsIcon;
    private Texture buttonUpSettings;
    private Texture buttonDownSettings;

    public GuideScene(EngineContext context) {
        super(context, "GuideScene", 1.0f);
    }

    @Override
    protected void onLoadStage() {
        createStyles();

        Table root = new Table();
        root.setFillParent(true);

        String guideText = "Player Guide\n\n"
                + "Game Flow\n"
                + "1. Start by reading the mission briefing.\n"
                + "2. Begin the mission after the countdown.\n"
                + "3. Scan servers to find the vulnerable one.\n"
                + "4. Escape while the system tracks you.\n"
                + "5. Attack and complete the exploit.\n\n"
                + "Controls\n"
                + "WASD to move your character.\n"
                + "ENTER to breach a vulnerable server during scanning.\n"
                + "Mouse Left Click to attack during the final phase.\n"
                + "SPACE to open settings.\n"
                + "M and H to view mission and controls during gameplay.\n\n"
                + "How to Play\n"
                + "In Scanning, take your time to identify the correct target.\n"
                + "In Escape, focus on movement and survival while avoiding detection.\n"
                + "In Attack, balance movement and timing to complete the exploit.\n\n"
                + "Tips\n"
                + "Not every server is vulnerable. Observe carefully.\n"
                + "Use Obfuscated Code when you are under pressure.\n"
                + "Keep moving. Standing still makes you an easy target.\n\n"
                + "Outcome\n"
                + "Complete all phases to win the mission.\n"
                + "If you fail, you can restart and try again.";

        Label label = new Label(guideText, new Label.LabelStyle(GameUiTheme.getMenuFont(), themeColor));
        label.setWrap(true);
        label.setAlignment(Align.left);

        Table scrollTable = new Table();
        scrollTable.add(label).width(1100).pad(30);

        ScrollPane scrollPane = new ScrollPane(scrollTable, getScrollStyle());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);

        TextButton backBtn = new TextButton("< Back", getButtonStyle());
        settingsIcon = new Texture("settingsbtn.png");
        ImageButton settingsBtn = new ImageButton(getSettingsButtonStyle());
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiSoundHelper.playButtonClick(context);
                requestScene(SceneType.START);
            }
        });
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiSoundHelper.playButtonClick(context);
                requestScene(SceneType.SETTINGS);
            }
        });

        root.add(scrollPane).size(1100, 500).pad(20).row();
        root.add(backBtn).padTop(20);
        stage.addActor(root);

        Table topRight = new Table();
        topRight.setFillParent(true);
        topRight.top().right();
        topRight.add(settingsBtn).size(64, 64).padTop(16).padRight(16);
        stage.addActor(topRight);
    }

    @Override
    protected void onUnloadStage() {
        if (buttonUp != null) {
            buttonUp.dispose();
            buttonUp = null;
        }
        if (buttonDown != null) {
            buttonDown.dispose();
            buttonDown = null;
        }
        if (scrollKnob != null) {
            scrollKnob.dispose();
            scrollKnob = null;
        }
        if (scrollTrack != null) {
            scrollTrack.dispose();
            scrollTrack = null;
        }
        if (buttonUpSettings != null) {
            buttonUpSettings.dispose();
            buttonUpSettings = null;
        }
        if (buttonDownSettings != null) {
            buttonDownSettings.dispose();
            buttonDownSettings = null;
        }
        if (settingsIcon != null) {
            settingsIcon.dispose();
            settingsIcon = null;
        }
    }

    private void createStyles() {
        buttonUp = UiTextureHelper.outlined(250, 60, themeColor, 3);
        buttonDown = UiTextureHelper.filled(250, 60, themeColor);
        scrollKnob = UiTextureHelper.filled(12, 50, themeColor);
        scrollTrack = UiTextureHelper.filled(12, 10, new Color(0.2f, 0.2f, 0.2f, 1f));
        buttonUpSettings = UiTextureHelper.outlined(64, 64, themeColor, 3);
        buttonDownSettings = UiTextureHelper.filled(64, 64, themeColor);
    }

    private TextButton.TextButtonStyle getButtonStyle() {
        return UiStyleHelper.textButtonStyle(GameUiTheme.getMenuFont(), themeColor, buttonUp, buttonDown);
    }

    private ScrollPane.ScrollPaneStyle getScrollStyle() {
        return UiStyleHelper.scrollPaneStyle(scrollTrack, scrollKnob);
    }

    private ImageButton.ImageButtonStyle getSettingsButtonStyle() {
        return UiStyleHelper.imageButtonStyle(buttonUpSettings, buttonDownSettings, settingsIcon);
    }

    @Override
    public SceneState getState(SceneType nextSceneType) {
        if (nextSceneType == SceneType.SETTINGS) {
            return SceneState.PAUSE_AND_RESUME;
        }
        return super.getState(nextSceneType);
    }
}





