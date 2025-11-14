// AudioManager.java
package io.github.some_example_name.Sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;


public class AudioManager implements Disposable {
    private static AudioManager instance;
    
    private ObjectMap<String, Sound> sounds;
    private ObjectMap<String, Music> music;
    private float soundVolume = 0.7f;
    private float musicVolume = 0.5f;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    
    private AudioManager() {
        sounds = new ObjectMap<>();
        music = new ObjectMap<>();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    // === SONS DE EFEITO ===
    public void loadSound(String name, String filePath) {
        if (sounds.containsKey(name)) {
            Gdx.app.log("AUDIO", "Som já carregado: " + name);
            return;
        }
        
        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
            sounds.put(name, sound);
            Gdx.app.log("AUDIO", "Som carregado: " + name + " -> " + filePath);
        } catch (Exception e) {
            Gdx.app.error("AUDIO", "Erro ao carregar som: " + name, e);
        }
    }
    
    public long playSound(String name) {
        return playSound(name, soundVolume);
    }
    
    public long playSound(String name, float volume) {
        if (!soundEnabled) return -1;
        
        Sound sound = sounds.get(name);
        if (sound != null) {
            return sound.play(volume * soundVolume);
        } else {
            Gdx.app.error("AUDIO", "Som não encontrado: " + name);
            return -1;
        }
    }
    
    // public void stopSound(long soundId) {
    //     Sound sound = sounds.get(soundId);
    //     if (sound != null) {
    //         sound.stop(soundId);
    //     }
    // }
    
    // === MÚSICA ===
    public void loadMusic(String name, String filePath) {
        if (music.containsKey(name)) {
            Gdx.app.log("AUDIO", "Música já carregada: " + name);
            return;
        }
        
        try {
            Music musicTrack = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            music.put(name, musicTrack);
            musicTrack.setVolume(musicVolume);
            musicTrack.setLooping(true);
            Gdx.app.log("AUDIO", "Música carregada: " + name + " -> " + filePath);
        } catch (Exception e) {
            Gdx.app.error("AUDIO", "Erro ao carregar música: " + name, e);
        }
    }
    
    public void playMusic(String name) {
        if (!musicEnabled) return;
        
        Music musicTrack = music.get(name);
        if (musicTrack != null && !musicTrack.isPlaying()) {
            musicTrack.play();
        }
    }
    
    public void stopMusic(String name) {
        Music musicTrack = music.get(name);
        if (musicTrack != null && musicTrack.isPlaying()) {
            musicTrack.stop();
        }
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        for (Music musicTrack : music.values()) {
            musicTrack.setVolume(musicVolume);
        }
    }
    
    // === CONFIGURAÇÕES ===
    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            for (Music musicTrack : music.values()) {
                musicTrack.stop();
            }
        }
    }
    
    // === LIMPEZA ===
    @Override
    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
        
        for (Music musicTrack : music.values()) {
            musicTrack.dispose();
        }
        music.clear();
        
        Gdx.app.log("AUDIO", "AudioManager disposed");
    }
}