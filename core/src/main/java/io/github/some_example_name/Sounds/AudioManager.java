package io.github.some_example_name.Sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

public class AudioManager implements Disposable {
    private static AudioManager instance;

    private ObjectMap<String, Sound> sounds;
    private ObjectMap<String, Music> music;
    private ObjectMap<String, Music> ambient;
    private float soundVolume = 0.7f;
    private float musicVolume = 0.5f;
    private float ambientVolume = 1f;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private boolean ambientEnabled = true;

    private AudioManager() {
        sounds = new ObjectMap<>();
        music = new ObjectMap<>();
        ambient = new ObjectMap<>();
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
        if (!soundEnabled)
            return -1;

        Sound sound = sounds.get(name);
        if (sound != null) {
            return sound.play(volume * soundVolume);
        } else {
            Gdx.app.error("AUDIO", "Som não encontrado: " + name);
            return -1;
        }
    }

        public long playSound(String name, float volume, float pitch) {
        if (!soundEnabled)
            return -1;

        Sound sound = sounds.get(name);
        if (sound != null) {
            // Usa o método play que aceita volume, pitch e pan
            return sound.play(volume * soundVolume, pitch, 0f);
        } else {
            Gdx.app.error("AUDIO", "Som não encontrado: " + name);
            return -1;
        }
    }
        public long playSoundWithRandomPitch(String name, float volume, float minPitch, float maxPitch) {
        float randomPitch = MathUtils.random(minPitch, maxPitch);
        return playSound(name, volume, randomPitch);
    }



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
        if (!musicEnabled)
            return;

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

    public void loadAmbient(String name, String filePath) {
        if (ambient.containsKey(name)) {
            Gdx.app.log("AUDIO", "Som ambiente já carregado: " + name);
            return;
        }

        try {
            // ✅ VERIFICAR SE ESTAMOS NO THREAD PRINCIPAL E CONTEXTO ATIVO
            if (Gdx.app == null) {
                Gdx.app.error("AUDIO", "Contexto do LibGDX não disponível para: " + name);
                return;
            }

            Music ambientSound = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            ambient.put(name, ambientSound);
            ambientSound.setVolume(ambientVolume);
            ambientSound.setLooping(true);
            Gdx.app.log("AUDIO", "Som ambiente carregado: " + name + " -> " + filePath);

        } catch (Exception e) {
            Gdx.app.error("AUDIO", "Erro ao carregar som ambiente: " + name, e);
        }
    }

    public void playAmbient(String name) {
        // ✅ VERIFICAÇÃO DE CONTEXTO CRÍTICA
        if (Gdx.app == null) {
            Gdx.app.error("AUDIO", "Tentativa de tocar som sem contexto LibGDX: " + name);
            return;
        }

        if (!ambientEnabled) {
            Gdx.app.log("AUDIO", "Sons ambiente desativados, não reproduzindo: " + name);
            return;
        }

        Music ambientSound = ambient.get(name);
        if (ambientSound != null) {
            if (!ambientSound.isPlaying()) {
                try {
                    ambientSound.play();
                    Gdx.app.log("AUDIO", "Som ambiente iniciado: " + name);
                } catch (Exception e) {
                    Gdx.app.error("AUDIO", "Erro ao reproduzir som: " + name, e);
                }
            }
        } else {
            Gdx.app.error("AUDIO", "Som ambiente não encontrado: " + name);
        }
    }

    public void stopAmbient(String name) {
        Music ambientSound = ambient.get(name);
        if (ambientSound != null && ambientSound.isPlaying()) {
            ambientSound.stop();
            Gdx.app.log("AUDIO", "Som ambiente parado: " + name);
        }
    }

    public void pauseAmbient(String name) {
        Music ambientSound = ambient.get(name);
        if (ambientSound != null && ambientSound.isPlaying()) {
            ambientSound.pause();
            Gdx.app.log("AUDIO", "Som ambiente pausado: " + name);
        }
    }

    public void resumeAmbient(String name) {
        Music ambientSound = ambient.get(name);
        if (ambientSound != null && !ambientSound.isPlaying()) {
            ambientSound.play();
            Gdx.app.log("AUDIO", "Som ambiente retomado: " + name);
        }
    }

    // ✅ CONFIGURAÇÕES ESPECÍFICAS PARA AMBIENT
    public void setAmbientVolume(float volume) {
        this.ambientVolume = volume;
        for (Music ambientSound : ambient.values()) {
            ambientSound.setVolume(ambientVolume);
        }
        Gdx.app.log("AUDIO", "Volume ambiente ajustado para: " + volume);
    }

    public void setAmbientEnabled(boolean enabled) {
        this.ambientEnabled = enabled;
        if (!enabled) {
            for (Music ambientSound : ambient.values()) {
                ambientSound.stop();
            }
            Gdx.app.log("AUDIO", "Sons ambiente desativados");
        } else {
            Gdx.app.log("AUDIO", "Sons ambiente ativados");
        }
    }

    public float getAmbientVolume() {
        return ambientVolume;
    }

    public boolean isAmbientEnabled() {
        return ambientEnabled;
    }

    // === CONFIGURAÇÕES GERAIS ===
    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        for (Music musicTrack : music.values()) {
            musicTrack.setVolume(musicVolume);
        }
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

    public void stopSound(long soundId) {
        if (soundId != -1) {
            for (Sound sound : sounds.values()) {
                sound.stop();
            }
            Gdx.app.log("AUDIO", "Stopped all sounds");
        }
    }

    public void stopAllAmbientSounds() {
        Gdx.app.log("AUDIO", "🛑 Parando TODOS os sons ambiente...");

        // Percorre todas as entradas do mapa
        for (ObjectMap.Entry<String, Music> entry : ambient.entries()) {
            try {
                Music ambientSound = entry.value;
                String soundName = entry.key;

                if (ambientSound != null && ambientSound.isPlaying()) {
                    ambientSound.stop();
                    Gdx.app.log("AUDIO", "  - Parado: " + soundName);
                }
            } catch (Exception e) {
                Gdx.app.error("AUDIO", "Erro ao parar som ambiente", e);
            }
        }
    }

    public void forceStopAmbient(String name) {
        Music ambientSound = ambient.get(name);
        if (ambientSound != null) {
            try {
                ambientSound.stop();
                Gdx.app.log("AUDIO", "🛑 Som ambiente forçadamente parado: " + name);
            } catch (Exception e) {
                Gdx.app.error("AUDIO", "Erro ao forçar parada de " + name, e);
            }
        }
    }

    private String getKeyForValue(ObjectMap<String, Music> map, Music value) {
        for (ObjectMap.Entry<String, Music> entry : map.entries()) {
            if (entry.value == value) {
                return entry.key;
            }
        }
        return null;
    }

    public void debugAudioState() {
        System.out.println("🎵 DEBUG AUDIO MANAGER:");
        System.out.println("   - Sounds carregados: " + sounds.size);
        System.out.println("   - Music carregadas: " + music.size);
        System.out.println("   - Ambient carregados: " + ambient.size);
        System.out.println("   - Volume Ambiente: " + ambientVolume);
        System.out.println("   - Ambiente habilitado: " + ambientEnabled);

        // Verificar a fogueira especificamente
        if (ambient.containsKey("fogueira_sound")) {
            Music fogueira = ambient.get("fogueira_sound");
            System.out.println("   - Fogueira carregada: SIM");
            System.out.println("   - Fogueira tocando: " + fogueira.isPlaying());
        } else {
            System.out.println("   - Fogueira carregada: NÃO");
        }

        // Verificar contexto LibGDX
        System.out.println("   - Gdx.audio disponível: " + (Gdx.audio != null));
        System.out.println("   - Gdx.app disponível: " + (Gdx.app != null));
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

        // ✅ LIMPA SONS AMBIENTE
        for (Music ambientSound : ambient.values()) {
            ambientSound.dispose();
        }
        ambient.clear();

        Gdx.app.log("AUDIO", "AudioManager disposed");
    }
}