// FootstepSystem.java - VERSÃO COM MUSIC (PARADA PRECISA)
package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;

public class FootstepSystem {
    private final Robertinhoo player;
    private final AudioManager audioManager;
    
    private float stepTimer = 0f;
    private final float stepInterval = 0.4f;
    private boolean isMoving = false;
    private boolean wasMoving = false;
    
    public enum SurfaceType {
        GRASS,
        STONE,
        WOOD,
        SAND
    }
    
    private SurfaceType currentSurface = SurfaceType.GRASS;
    
    public FootstepSystem(Robertinhoo player) {
        this.player = player;
        this.audioManager = AudioManager.getInstance();
    }
    
    public void update(float deltaTime) {
        boolean wPressed = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean sPressed = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean aPressed = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.D);
        
        boolean movingNow = wPressed || sPressed || aPressed || dPressed;
        
        // PARADA IMEDIATA
        if (!movingNow && wasMoving) {
            stopFootstepMusic();
            stepTimer = 0f;
            Gdx.app.log("FOOTSTEP", "Stopped moving - music stopped");
        }
        
        // INÍCIO IMEDIATO
        if (movingNow && !wasMoving) {
            startFootstepMusic();
            stepTimer = 0f;
            Gdx.app.log("FOOTSTEP", "Started moving - music started");
        }
        
        // Timer para efeito visual/debug (não afeta o Music que já está em loop)
        if (movingNow) {
            stepTimer += deltaTime;
        }
        
        wasMoving = movingNow;
    }
    
    private void startFootstepMusic() {
        switch (currentSurface) {
            case GRASS:
                audioManager.playMusic(GameGameSoundsPaths.Music.FOOTSTEP_GRASS);
                break;
            case STONE:
                // audioManager.playMusic(GameGameSoundsPaths.Music.FOOTSTEP_STONE);
                break;
            case WOOD:
                // audioManager.playMusic(GameGameSoundsPaths.Music.FOOTSTEP_WOOD);
                break;
            case SAND:
                // audioManager.playMusic(GameGameSoundsPaths.Music.FOOTSTEP_SAND);
                break;
        }
    }
    
    private void stopFootstepMusic() {
        switch (currentSurface) {
            case GRASS:
                audioManager.stopMusic(GameGameSoundsPaths.Music.FOOTSTEP_GRASS);
                break;
            case STONE:
                // audioManager.stopMusic(GameGameSoundsPaths.Music.FOOTSTEP_STONE);
                break;
            case WOOD:
                // audioManager.stopMusic(GameGameSoundsPaths.Music.FOOTSTEP_WOOD);
                break;
            case SAND:
                // audioManager.stopMusic(GameGameSoundsPaths.Music.FOOTSTEP_SAND);
                break;
        }
    }
    
    public void setSurfaceType(SurfaceType surface) {
        this.currentSurface = surface;
    }
    
    public SurfaceType getCurrentSurface() {
        return currentSurface;
    }
}