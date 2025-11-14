package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Gdx;
import io.github.some_example_name.Entities.Itens.Contact.Constants;

public class MeleeAttackSystem {
    private final Robertinhoo player;
    private Body meleeHitboxBody;
    private boolean attackInProgress;
    private float attackDuration;
    private final World world;
    private final ParrySystem parrySystem;
    private Timer.Task endAttackTask;
    private boolean isParryExtended = false;
     private boolean hitboxCreated = false;

    public MeleeAttackSystem(Robertinhoo player) {
        this.player = player;
        this.world = player.getMap().world;
        this.attackInProgress = false;
        this.attackDuration = player.getMeleeAttackDuration();
        this.parrySystem = new ParrySystem(player);
    }

     public void startAttack(int direction) {
        if (attackInProgress) {
            return;
        }

        attackInProgress = true;
        isParryExtended = false;
        hitboxCreated = false; // RESET
        parrySystem.activateParry();
        cancelExistingTimers();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                createMeleeHitbox(direction);
                hitboxCreated = true;
                scheduleEndAttackTimer();
            }
        }, 0.1f);
    }
       public void extendAttackForParry() {
        if (attackInProgress && !isParryExtended) {
            isParryExtended = true;
            if (hitboxCreated) {
                scheduleEndAttackTimer();
            }
        }
    }

    private void createMeleeHitbox(int direction) {
        // Criar definição do corpo
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(player.body.getPosition());
        bodyDef.fixedRotation = true;

        // Criar o corpo no mundo
        meleeHitboxBody = world.createBody(bodyDef);
        meleeHitboxBody.setUserData("MELEE_ATTACK");

        // Criar a forma do hitbox
        PolygonShape shape = new PolygonShape();
        float halfW = 0.6f / 2f;
        float halfH = 0.3f / 2f;
        Vector2 localCenter = new Vector2(0, 0);

        switch (direction) {
            case Robertinhoo.RIGHT:
                localCenter.set(0.5f + halfW, 0);
                break;
            case Robertinhoo.LEFT:
                localCenter.set(-0.5f - halfW, 0);
                break;
            case Robertinhoo.UP:
                localCenter.set(0, 0.5f + halfH);
                break;
            case Robertinhoo.DOWN:
                localCenter.set(0, -0.5f - halfH);
                break;
        }

        shape.setAsBox(halfW, halfH, localCenter, 0f);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = true;
        fd.filter.categoryBits = Constants.BIT_PLAYER_ATTACK;
        fd.filter.maskBits = Constants.BIT_ENEMY | Constants.BIT_OBJECT;
        meleeHitboxBody.createFixture(fd);
        shape.dispose();
    }

private void endAttack() {
    Gdx.app.log("MELEE_DEBUG", "=== FINALIZANDO ATAQUE ===");
    
    parrySystem.resetParrySuccess();
    
    if (meleeHitboxBody != null) {
        world.destroyBody(meleeHitboxBody);
        meleeHitboxBody = null;
        Gdx.app.log("MELEE_DEBUG", "Hitbox destruída");
    }
    attackInProgress = false;
    isParryExtended = false;
    
    Gdx.app.log("MELEE_DEBUG", "Ataque finalizado. attackInProgress: " + attackInProgress);
}

    public boolean isAttacking() {
        return attackInProgress;
    }

    public Body getMeleeHitboxBody() {
        return meleeHitboxBody;
    }

    public ParrySystem getParrySystem() {
        return parrySystem;
    }

    public boolean isParrySuccess() {
        boolean success = parrySystem.isParrySuccess();
        return success;
    }
        private void cancelExistingTimers() {
        if (endAttackTask != null) {
            endAttackTask.cancel();
            endAttackTask = null;
        }
    }

        private void scheduleEndAttackTimer() {
        cancelExistingTimers();
        
        float duration;
        if (parrySystem.isParrySuccess()) {
            duration = 0.616f;
        } else {
            duration = attackDuration;
        }

        endAttackTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                endAttack();
            }
        }, duration);
    }

}