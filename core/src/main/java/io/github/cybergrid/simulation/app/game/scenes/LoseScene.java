package io.github.cybergrid.simulation.app.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.cybergrid.simulation.app.game.GameSession;
import io.github.cybergrid.simulation.app.game.ui.GameUiTheme;
import io.github.cybergrid.simulation.app.game.ui.UiSoundHelper;
import io.github.cybergrid.simulation.app.game.ui.UiStyleHelper;
import io.github.cybergrid.simulation.app.game.ui.UiTextureHelper;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.scene.SceneState;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class LoseScene extends MenuStageScene {
    private final Color themeColor = GameUiTheme.AI_COLOR;
    private Texture buttonUp;
    private Texture buttonDown;
    private Texture settingsIcon;
    private Texture buttonUpSettings;
    private Texture buttonDownSettings;

    public LoseScene(EngineContext context) {
        super(context, "LoseScene", 0.9f);
    }

    @Override
    protected void onLoadStage() {
        createStyles();
        context.getAudioManager().playMusic("lose_scene_bgm.mp3");
        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("MISSION FAILED", new Label.LabelStyle(GameUiTheme.getTitleFont(), themeColor));
        Label subtitle = new Label("The target system held strong. Regroup and retry.", new Label.LabelStyle(GameUiTheme.getMenuFont(), GameUiTheme.LOSE_COLOR));

        TextButton restartBtn = new TextButton("Restart", getButtonStyle());
        TextButton quitBtn = new TextButton("Quit", getButtonStyle());
        settingsIcon = new Texture("settingsbtn.png");
        ImageButton settingsBtn = new ImageButton(getSettingsButtonStyle());

        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiSoundHelper.playButtonClick(context);
                context.getAudioManager().stopMusic();
                GameSession session = context.getService(GameSession.class);
                if (session != null) {
                    session.resetRun();
                }
                requestScene(SceneType.MAIN);
            }
        });

        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiSoundHelper.playButtonClick(context);
                Gdx.app.exit();
            }
        });
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiSoundHelper.playButtonClick(context);
                requestScene(SceneType.SETTINGS);
            }
        });

        root.add(title).padBottom(18f).row();
        root.add(subtitle).padBottom(40f).row();
        root.add(restartBtn).size(300, 60).padBottom(16f).row();
        root.add(quitBtn).size(300, 60);
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
        buttonUp = UiTextureHelper.outlined(300, 60, themeColor, 3);
        buttonDown = UiTextureHelper.filled(300, 60, themeColor);
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




