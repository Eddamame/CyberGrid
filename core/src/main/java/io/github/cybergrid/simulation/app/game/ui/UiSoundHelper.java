package io.github.cybergrid.simulation.app.game.ui;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.managers.AudioManager;

public final class UiSoundHelper {
    public static final String BUTTON_CLICK_SFX = "button_click.mp3";

    private UiSoundHelper() {
    }

    public static void playButtonClick(EngineContext context) {
        if (context == null || context.getAudioManager() == null) {
            return;
        }
        context.getAudioManager().playSound(BUTTON_CLICK_SFX);
    }

    public static void playButtonClick(AudioManager audioManager) {
        if (audioManager == null) {
            return;
        }
        audioManager.playSound(BUTTON_CLICK_SFX);
    }
}

