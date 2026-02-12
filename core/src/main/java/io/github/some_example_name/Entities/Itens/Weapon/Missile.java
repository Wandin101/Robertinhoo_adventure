package io.github.some_example_name.Entities.Itens.Weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.MapConfig.Mapa;

public class Missile extends Projectile {
    private static final float MISSILE_SPEED = 5f;
    private static final float MISSILE_DAMAGE = 25f;
    private static final float MISSILE_LIFESPAN = 4f;
    private Castor owner;
    private boolean isReflected = false;
    private boolean isFrozen = false;
    private Vector2 frozenVelocity = new Vector2();
    private Vector2 pendingReflectionDirection = null;

    public Missile(Mapa mapa, Vector2 position, Vector2 direction, Castor owner) {
        super(mapa, position, direction.scl(MISSILE_SPEED), MISSILE_DAMAGE, "Missile");
        this.owner = owner;
        this.lifespan = MISSILE_LIFESPAN;
    }

    @Override
    public void update(float delta) {
        // Se estiver congelado, não atualiza
        if (isFrozen) {
            return;
        }

        // Se há reflexão pendente, aplica
        if (pendingReflectionDirection != null) {
            applyReflection();
        }

        super.update(delta);

        if (body != null && !body.getLinearVelocity().isZero(0.1f)) {
            Vector2 velocity = body.getLinearVelocity();
            float angle = velocity.angleDeg() - 90f;
            setAngle(angle);
        }
    }

    public void freezeForParry() {
        if (body != null && !isFrozen) {
            isFrozen = true;
            frozenVelocity.set(body.getLinearVelocity());
            body.setLinearVelocity(0, 0);
            Gdx.app.log("MISSILE", "Míssil congelado para parry");
        }
    }

    public void scheduleReflection(Vector2 newDirection) {
        this.pendingReflectionDirection = newDirection;
        Gdx.app.log("MISSILE", "Reflexão do míssil agendada: " + newDirection);
    }

    private void applyReflection() {
        if (body != null && pendingReflectionDirection != null) {
            isFrozen = false; // Descongela antes de refletir

            float newSpeed = MISSILE_SPEED * 1.5f;
            body.setLinearVelocity(pendingReflectionDirection.scl(newSpeed));
            isReflected = true;
            pendingReflectionDirection = null;

            Gdx.app.log("MISSILE", "Míssil refletido! Nova velocidade: " + newSpeed);
        }
    }

    public void applyScheduledReflection() {
        Gdx.app.log("MISSILE", "Aplicando reflexão agendada");
        applyReflection();
    }

    public void unfreeze() {
        isFrozen = false;
        if (body != null && !isReflected) {
            // Restaura velocidade original se não foi refletido
            body.setLinearVelocity(frozenVelocity);
            Gdx.app.log("MISSILE", "Míssil descongelado sem reflexão");
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public Castor getOwner() {
        return owner;
    }

    public Vector2 getPosition() {
        if (body != null) {
            return body.getPosition();
        }
        return new Vector2(0, 0);
    }

    public boolean isReflected() {
        return isReflected;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.6f;
    }
}