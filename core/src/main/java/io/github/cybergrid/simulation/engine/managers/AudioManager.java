package io.github.cybergrid.simulation.engine.managers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    private float soundVolume = 0.5f;
    private float musicVolume = 0.5f;
    private final Map<String, Sound> loadedSounds = new HashMap<>();
    private final Map<String, Music> loadedMusic = new HashMap<>();
    private Music currentMusic;
    private String currentMusicPath;

    public AudioManager() {
    }

    // Changes music volume and updates the currently playing track (for background music)
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
        System.out.println("[Audio Manager] Music volume adjusted to " + formatVolume(this.musicVolume));
    }

    // Sets the volume for sound effects (for the collision sound effect btw ai and player entity)
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
        System.out.println("[Audio Manager] Sound volume adjusted to " + formatVolume(this.soundVolume));
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public long playSound(String path) {
        Sound sound = getOrLoadSound(path);
        return (sound != null) ? sound.play(soundVolume) : -1;
    }

    public void stopSound(String path) {
        Sound sound = loadedSounds.get(path);
        if (sound != null) {
            sound.stop();
        }
    }

    public void playMusic(String path) {
        if (path != null && path.equals(currentMusicPath) && currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.setVolume(musicVolume);
            return;
        }

        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }

        if (!loadedMusic.containsKey(path)) {
            try {
                Music m = Gdx.audio.newMusic(Gdx.files.internal(path));
                loadedMusic.put(path, m);
            } catch (Exception e) {
                Gdx.app.error("Audio", "Could not load music: " + path);
            }
        }

        currentMusic = loadedMusic.get(path);
        currentMusicPath = path;
        if (currentMusic != null) {
            currentMusic.setLooping(true);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusicPath = null;
    }

    private String formatVolume(float volume) {
        return Math.round(volume * 100f) + "%";
    }

    private Sound getOrLoadSound(String path) {
        if (!loadedSounds.containsKey(path)) {
            try {
                loadedSounds.put(path, Gdx.audio.newSound(Gdx.files.internal(path)));
            } catch (Exception e) {
                Gdx.app.error("Audio", "Could not load sound: " + path);
                return null;
            }
        }
        return loadedSounds.get(path);
    }

    public void dispose() {
        for (Sound s : loadedSounds.values()) {
            s.dispose();
        }
        for (Music m : loadedMusic.values()) {
            m.dispose();
        }
        loadedSounds.clear();
        loadedMusic.clear();
        currentMusic = null;
        currentMusicPath = null;
    }
}
