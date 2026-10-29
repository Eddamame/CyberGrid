package io.github.cybergrid.simulation.app.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.cybergrid.simulation.app.game.GameConstants;
import io.github.cybergrid.simulation.app.game.input.GameInputConfig;
import io.github.cybergrid.simulation.app.game.ui.UiSoundHelper;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.managers.InputOutputManager;
import io.github.cybergrid.simulation.engine.scene.Scene;
import io.github.cybergrid.simulation.engine.scene.SceneType;

public class SettingsScene extends Scene {

    private Stage stage;
    private Skin skin;
    private InputOutputManager ioManager;
    private Slider bgmSlider;
    private Slider sfxSlider;
    private SceneType returnSceneType;

    public SettingsScene(EngineContext context) {
        super(context);
        this.name = "SettingsScene";
    }

    @Override
    public void load() {
        stage = new Stage(new FitViewport(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT));

        ioManager = new InputOutputManager(context.getEventManager());
        GameInputConfig.setupDefaultControls(ioManager);
        returnSceneType = context.getSceneManager().getPreviousSceneType();
        if (returnSceneType == null || returnSceneType == SceneType.SETTINGS) {
            returnSceneType = SceneType.START;
        }

        // Multiplexer allows both UI (Stage) and Keys (ioManager) to work
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(ioManager.getInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);

        createSkin();
        setupUI();

        isLoaded = true;
    }

    private void setupUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("SETTINGS", skin);
        table.add(titleLabel).padBottom(80).row();

        bgmSlider = new Slider(0f, 1f, 0.05f, false, skin);
        bgmSlider.setValue(context.getAudioManager().getMusicVolume());

        bgmSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = bgmSlider.getValue();
                context.getAudioManager().setMusicVolume(val);
            }
        });

        sfxSlider = new Slider(0f, 1f, 0.05f, false, skin);
        sfxSlider.setValue(context.getAudioManager().getSoundVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                context.getAudioManager().setSoundVolume(sfxSlider.getValue());
            }
        });

        table.add(new Label("BGM Volume", skin)).padBottom(12).row();
        table.add(bgmSlider).width(800).height(50).padBottom(36).row();
        table.add(new Label("SFX Volume", skin)).padBottom(12).row();
        table.add(sfxSlider).width(800).height(50).padBottom(42).row();

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = skin.getFont("default");
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.CYAN;

        TextButton backBtn = new TextButton("Back", buttonStyle);
        TextButton quitBtn = new TextButton("Quit Game", buttonStyle);

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                UiSoundHelper.playButtonClick(context.getAudioManager());
                requestScene(returnSceneType);
            }
        });

        quitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                UiSoundHelper.playButtonClick(context.getAudioManager());
                Gdx.app.exit();
            }
        });

        table.add(backBtn).padBottom(14).row();
        table.add(quitBtn);
    }

    private void createSkin() {
        skin = new Skin();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/CascadiaCode.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 34;
        params.color = Color.WHITE;
        com.badlogic.gdx.graphics.g2d.BitmapFont font = generator.generateFont(params);
        generator.dispose();
        skin.add("default", font);

        Pixmap bgPixmap = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(Color.GRAY);
        bgPixmap.fill();
        skin.add("slider-bg", new Texture(bgPixmap));

        Pixmap knobPixmap = new Pixmap(30, 50, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(Color.CYAN);
        knobPixmap.fill();
        skin.add("slider-knob", new Texture(knobPixmap));

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("slider-bg");
        sliderStyle.knob = skin.newDrawable("slider-knob");
        skin.add("default-horizontal", sliderStyle);

        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        bgPixmap.dispose();
        knobPixmap.dispose();
    }

    @Override
    public void update(float dt) {
        stage.act(dt);
        ioManager.update(); // Clear justPressed for next frame
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.draw();
    }

    @Override
    public void unload() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (ioManager != null) {
            ioManager.dispose();
        }
        isLoaded = false;
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public Viewport getViewport() {
        return stage.getViewport();
    }
}





