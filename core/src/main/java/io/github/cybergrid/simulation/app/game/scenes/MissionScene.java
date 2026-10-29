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

public class MissionScene extends MenuStageScene {
    
    private final Color themeColor = GameUiTheme.THEME_COLOR;
    private Texture buttonUp;
    private Texture buttonDown;
    private Texture scrollKnob;
    private Texture scrollTrack;
    private Texture settingsIcon;
    private Texture buttonUpSettings;
    private Texture buttonDownSettings;

    public MissionScene(EngineContext context) {
        super(context, "MissionScene", 1.0f);
    }

    @Override
    protected void onLoadStage() {
        createStyles();

        context.getAudioManager().playMusic("missionscenebgm.mp3");
        context.getAudioManager().playSound("missionscenevoice.mp3");

        Table root = new Table();
        root.setFillParent(true);

    String missionText = "Welcome back, Agent.\n\n"
            + "Intelligence has identified a hostile cyber syndicate known as the Black Helix.\n"
            + "For months, they have operated in the shadows, building a hidden network used to store stolen data,\n"
            + "distribute attack tools, and coordinate large-scale cyber operations.\n\n"
            + "At the center of their infrastructure lies a critical server.\n"
            + "If it remains active, the Black Helix will soon launch a coordinated attack capable of disrupting\n"
            + "major communication and financial systems.\n\n"
            + "You have been deployed into CyberGrid - a controlled digital environment that mirrors\n"
            + "the syndicate's network architecture.\n\n"
            + "Your mission, should you choose to accept it,\n"
            + "is to find the weakness, get in, and take it down.\n\n"
            + "Your operation begins with locating an entry point into their network.\n\n"
            + "Phase 1: Scanning\n"
            + "Move through the network and scan servers to locate a weakness.\n"
            + "Most systems are secure. One is not. Identify it.\n\n"
            + "Phase 2: Escape\n"
            + "Once you breach the system, your presence will be detected.\n"
            + "The Syndicate AI will begin tracking you across the network.\n"
            + "Stay ahead and find a path out.\n\n"
            + "Phase 3: Attack\n"
            + "With the vulnerable server identified, launch your attack.\n"
            + "Break through their defenses and complete the exploit before they shut you down.\n\n"
            + "This operation will determine whether their network is stopped or allowed to continue.\n\n"
            + "Good luck, Agent.";

        Label label = new Label(missionText, new Label.LabelStyle(GameUiTheme.getMenuFont(), themeColor));
        label.setWrap(true);
        label.setAlignment(Align.topLeft);

        Table scrollTable = new Table();
        scrollTable.defaults().pad(20);
        scrollTable.add(label).growX().width(1000);

        ScrollPane scrollPane = new ScrollPane(scrollTable, getScrollStyle());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.setScrollingDisabled(true, false);

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

        root.add(scrollPane).width(1100).height(500).pad(20).row();
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
        context.getAudioManager().stopSound("missionscenevoice.mp3");
        context.getAudioManager().stopMusic();
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





