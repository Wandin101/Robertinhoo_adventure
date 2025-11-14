// ScreenFreezeSystem.java
package io.github.some_example_name.Screens.ScreenEffects;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;

import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import java.util.List;

public class ScreenFreezeSystem {
    private static float freezeTime = 0f;
    private static float currentFreezeTime = 0f;
    private static boolean isFrozen = false;

    private static List<Missile> frozenMissiles = new ArrayList<>();

    public static void freeze(float duration) {
        freezeTime = duration;
        currentFreezeTime = 0f;
        isFrozen = true;
        Gdx.app.log("FREEZE", "Jogo congelado por: " + duration + "s (exceto player)");
    }

public static void update(float delta) {
        if (isFrozen) {
            currentFreezeTime += delta;
            if (currentFreezeTime >= freezeTime) {
                isFrozen = false;
                Gdx.app.log("FREEZE", "Lógica do jogo descongelada");
            }
        }
        
        frozenMissiles.removeIf(missile -> !missile.isFrozen());
    }

    public static boolean isFrozen() {
        return isFrozen;
    }

public static float getGameplayDelta() {
    if (isFrozen) {
        return 0.0001f; 
    }
    return Gdx.graphics.getDeltaTime();
}


    public static float getPlayerDelta() {
        return Gdx.graphics.getDeltaTime(); 
    }

    public static float getAnimationDelta() {
        return Gdx.graphics.getDeltaTime();
    }


    
}