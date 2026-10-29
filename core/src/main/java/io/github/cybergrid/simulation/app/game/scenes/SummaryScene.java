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

public class SummaryScene extends MenuStageScene {
    private final Color themeColor = GameUiTheme.WIN_COLOR;
    private Texture buttonUp;
    private Texture buttonDown;
    private Texture scrollKnob;
    private Texture scrollTrack;
    private Texture settingsIcon;
    private Texture buttonUpSettings;
    private Texture buttonDownSettings;

    public SummaryScene(EngineContext context) {
        super(context, "SummaryScene", 0.9f);
    }

    @Override
    protected void onLoadStage() {
        createStyles();
        context.getAudioManager().playMusic("summary_scene_bgm.mp3");
        Table root = new Table();
        root.setFillParent(true);

        String summaryText = "Mission Complete.\n\n"
                + "What you just experienced is a simplified version of how a real cyber intrusion can unfold.\n\n"
                + "Phase 1: Scanning\n"
                + "During the first phase, you performed reconnaissance.\n"
                + "In cybersecurity, reconnaissance is the process of exploring a network to discover systems,\n"
                + "identify weak points, and find possible entry paths before launching an attack.\n\n"
                + "Phase 2: Escape\n"
                + "Once your activity was noticed, the network responded.\n"
                + "The Syndicate AI represented an Intrusion Detection System (IDS), which is designed to detect\n"
                + "suspicious behavior and track threats inside a network.\n"
                + "The Obfuscated Code pickups represented obfuscation, a technique used to hide or disguise malicious activity\n"
                + "so it becomes harder for security systems to detect.\n\n"
                + "Phase 3: Attack\n"
                + "In the final phase, you exploited the vulnerable server.\n"
                + "The packets you fired as bullets represented malicious network traffic or exploit payloads being sent toward the target.\n"
                + "In real-world attacks, exploitation often involves sending crafted requests or data packets to abuse a weakness\n"
                + "and gain control over a system or service.\n\n"
                + "Cybersecurity is not only about attacking systems.\n"
                + "It also involves understanding how networks are defended, how threats are detected,\n"
                + "and how attackers and defenders constantly adapt to one another.\n\n"
                + "Every system has strengths. Every system has weaknesses.\n"
                + "Learning how both sides operate is what builds real cybersecurity awareness.\n\n"
                + "So the question is-\n\n"
                + "Are you ready to step into the real world of cybersecurity?\n";

        Label body = new Label(summaryText, new Label.LabelStyle(GameUiTheme.getMenuFont(), GameUiTheme.WIN_COLOR));
        body.setWrap(true);
        body.setAlignment(Align.topLeft);

        Table content = new Table();
        content.add(body).width(1050f).pad(24f);

        ScrollPane pane = new ScrollPane(content, UiStyleHelper.scrollPaneStyle(scrollTrack, scrollKnob));
        pane.setFadeScrollBars(false);
        pane.setScrollbarsOnTop(true);

        TextButton doneBtn = new TextButton("Back To Menu", getButtonStyle());
        settingsIcon = new Texture("settingsbtn.png");
        ImageButton settingsBtn = new ImageButton(getSettingsButtonStyle());
        doneBtn.addListener(new ClickListener() {
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

        root.add(new Label("Mission Debrief", new Label.LabelStyle(GameUiTheme.getTitleFont(), themeColor))).padBottom(14f).row();
        root.add(pane).size(1120f, 430f).padBottom(24f).row();
        root.add(doneBtn).size(320f, 60f);

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
        buttonUp = UiTextureHelper.outlined(320, 60, themeColor, 3);
        buttonDown = UiTextureHelper.filled(320, 60, themeColor);
        scrollKnob = UiTextureHelper.filled(12, 50, themeColor);
        scrollTrack = UiTextureHelper.filled(12, 10, new Color(0.2f, 0.2f, 0.2f, 1f));
        buttonUpSettings = UiTextureHelper.outlined(64, 64, themeColor, 3);
        buttonDownSettings = UiTextureHelper.filled(64, 64, themeColor);
    }

    private TextButton.TextButtonStyle getButtonStyle() {
        return UiStyleHelper.textButtonStyle(GameUiTheme.getMenuFont(), themeColor, buttonUp, buttonDown);
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





