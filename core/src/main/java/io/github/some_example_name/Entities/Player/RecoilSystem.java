package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Sistema de recuo gradual para armas.
 */
public class RecoilSystem {
    private Vector2 recoilForce;
    private float recoilTimer;
    private float duration;
    private boolean active;

    public RecoilSystem() {
        recoilForce = new Vector2();
        active = false;
        duration = 0.10f; // padrão
    }

    /**
     * Inicia um recuo com força e duração.
     * 
     * @param direction direção do tiro (normalizada)
     * @param strength  força inicial do recuo (ex: 10f)
     * @param duration  segundos que o recuo será aplicado
     */
    public void startRecoil(Vector2 direction, float strength, float duration) {
        this.duration = duration;
        recoilForce.set(direction).scl(-strength); // direção oposta ao tiro
        recoilTimer = duration;
        active = true;
    }

    /**
     * Aplica a força gradualmente ao corpo, deve ser chamado no update do jogador.
     * 
     * @param body  corpo do jogador
     * @param delta tempo desde o último frame
     */
    public void update(Body body, float delta) {
        if (!active)
            return;

        if (recoilTimer > 0) {
            // Aplica a força atual no centro de massa
            body.applyForceToCenter(recoilForce, true);
            recoilTimer -= delta;

            // Atenuação linear: a força diminui proporcionalmente ao tempo restante
            if (recoilTimer > 0) {
                float t = recoilTimer / duration;
                recoilForce.scl(t);
            } else {
                // Finaliza
                active = false;
                recoilForce.setZero();
            }
        } else {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void reset() {
        active = false;
        recoilForce.setZero();
        recoilTimer = 0;
    }
}