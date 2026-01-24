package io.github.some_example_name.Entities.Particulas;

import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;

public class BloodSoundManager {
    private static AudioManager audio = AudioManager.getInstance();
      private static int splashCounter = 0;
    private static final int SPLASH_EVERY_NTH = 3; // Toca a cada 3 partículas
    
    // Constantes para variação de pitch
    private static final float MIN_SPLASH_PITCH = 0.85f;
    private static final float MAX_SPLASH_PITCH = 1.15f;
    private static final float MIN_POOL_PITCH = 0.9f;
    private static final float MAX_POOL_PITCH = 1.1f;
    
    // Tocar som de splash quando sangue é criado
 public static void playSplashSound(float intensity) {
        splashCounter++;
        
        if (splashCounter % SPLASH_EVERY_NTH != 0) {
            return; // Só toca a cada 3 partículas
        }
        
        float volume = Math.min(intensity * 0.5f, 0.15f); // Volume ainda mais baixo
        float pitch = MathUtils.random(0.9f, 1.1f);
        
        audio.playSound(GameGameSoundsPaths.Sounds.BLOOD_SPLASH, volume, pitch);
    }

     private static boolean poolSoundPlayedThisFrame = false;
    
    // Tocar som de poça quando ela se forma
      public static void playPoolSound(float poolSize) {
        if (poolSoundPlayedThisFrame) {
            return; // Já tocou uma poça neste frame
        }
        
        poolSoundPlayedThisFrame = true;
        float volume = 0.15f;
        float pitch = MathUtils.random(0.95f, 1.05f);
        
        audio.playSound(GameGameSoundsPaths.Sounds.BLOOD_POOL, volume, pitch);
        
        // Reseta no próximo frame
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                poolSoundPlayedThisFrame = false;
            }
        }, 0.016f); // Próximo frame (60 FPS)
    }
    
    // Método alternativo usando o helper do AudioManager
    public static void playSplashSoundAlt(float intensity) {
        if (intensity > 0.1f) {
            float volume = Math.min(intensity, 0.8f);
            audio.playSoundWithRandomPitch(
                GameGameSoundsPaths.Sounds.BLOOD_SPLASH,
                volume,
                MIN_SPLASH_PITCH,
                MAX_SPLASH_PITCH
            );
        }
    }
}