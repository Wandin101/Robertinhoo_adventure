package io.github.some_example_name.Entities.Player;

import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Screens.ScreenEffects.ScreenFreezeSystem;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
public class ParrySystem {
    private final Robertinhoo player;
    private final Mapa mapa;
    private boolean isParryActive;
    private boolean parrySuccess = false;
    private float parryWindow = 0.15f;
    private float parryTimer = 0f;
    private float parrySuccessDuration = 0.616f;

    // Lista de míseis pendentes para reflexão
    private List<Missile> pendingMissiles = new ArrayList<>();

    public ParrySystem(Robertinhoo player) {
        this.player = player;
        this.mapa = player.getMap();
        this.isParryActive = false;
    }

    public void update(float deltaTime) {
        if (isParryActive) {
            parryTimer -= deltaTime;
            if (parryTimer <= 0) {
                isParryActive = false;
            }
        }

        if (parrySuccess) {
            parrySuccessDuration -= deltaTime;
            
            // Aplica a reflexão dos míseis quando a animação está quase terminando
            if (!pendingMissiles.isEmpty() && parrySuccessDuration <= 0.1f) {
                applyPendingReflections();
            }
            
            if (parrySuccessDuration <= 0) {
                completeParry();
            }
        }
    }

    public void activateParry() {
        isParryActive = true;
        parryTimer = parryWindow;
    }

    public void markParrySuccess(Missile missile) {
        if (isParryActive) {
            // Congela o míssil imediatamente
            missile.freezeForParry();
            
            // Calcula direção de reflexão
            Vector2 returnDirection = calculateReturnDirection(missile);
            missile.scheduleReflection(returnDirection);
            
            // Adiciona à lista de pendentes
            pendingMissiles.add(missile);
            
            triggerSatisfactionEffects();
            parrySuccess = true;
            parrySuccessDuration = 0.616f;
            player.getMeleeAttackSystem().extendAttackForParry();
            
            Gdx.app.log("PARRY_SYNC", "Parry bem-sucedido marcado. Míssis pendentes: " + pendingMissiles.size());
        }
    }

    private Vector2 calculateReturnDirection(Missile missile) {
        if (missile.getOwner() != null) {
            Vector2 ownerPos = missile.getOwner().getPosition();
            Vector2 missilePos = missile.getPosition();
            Vector2 direction = ownerPos.cpy().sub(missilePos).nor();
            Gdx.app.log("PARRY_CALC", "Direção calculada: " + direction + " (owner: " + ownerPos + ", missile: " + missilePos + ")");
            return direction;
        }
        // Fallback: reflete na direção oposta
        Gdx.app.log("PARRY_CALC", "Usando direção fallback");
        return new Vector2(-1, 0); 
    }

    public boolean isParrySuccess() {
        return parrySuccess;
    }

    public void resetParrySuccess() {
        parrySuccess = false;
        parrySuccessDuration = 0.616f;
        // Limpa míseis pendentes ao resetar
        for (Missile missile : pendingMissiles) {
            missile.unfreeze();
        }
        pendingMissiles.clear();
    }

    public boolean isParryActive() {
        return isParryActive;
    }

    public void deactivateParry() {
        isParryActive = false;
        parryTimer = 0f;
    }

    public float getParryWindow() {
        return parryWindow;
    }

    public void setParryWindow(float parryWindow) {
        this.parryWindow = parryWindow;
    }

    private void triggerSatisfactionEffects() {
        ScreenFreezeSystem.freeze(0.08f);

        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.PARRY_SUCCESS);

        Gdx.app.log("PARRY_EFFECTS", "Efeitos de parry ativados!");
    }

    private void applyPendingReflections() {
        Gdx.app.log("PARRY_SYNC", "Aplicando reflexões pendentes para " + pendingMissiles.size() + " míseis");
        
        for (Missile missile : pendingMissiles) {
            missile.applyScheduledReflection();
        }
        pendingMissiles.clear();
    }

    private void completeParry() {
        parrySuccess = false;
        parrySuccessDuration = 0.616f;
        
        if (!pendingMissiles.isEmpty()) {
            Gdx.app.log("PARRY_SYNC", "Ainda há " + pendingMissiles.size() + " míseis pendentes no final do parry");
            applyPendingReflections();
        }

        Gdx.app.log("PARRY_SYNC", "Animação de parry concluída");
    }
}