package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Gdx;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
public class MeleeAttackSystem {
    private final Robertinhoo player;
    private Body meleeHitboxBody;
    private boolean attackInProgress;
    private float attackDuration;
    private final ParrySystem parrySystem;
    private Timer.Task endAttackTask;
    private boolean isParryExtended = false;
    private boolean hitboxCreated = false;

    public MeleeAttackSystem(Robertinhoo player) {
        this.player = player;
        this.attackInProgress = false;
        this.attackDuration = player.getMeleeAttackDuration();
        this.parrySystem = new ParrySystem(player);
    }
    
    // MÉTODO AUXILIAR para obter o World atual
    private World getCurrentWorld() {
        if (player != null && player.getMap() != null) {
            return player.getMap().world;
        }
        return null;
    }

    public void startAttack(int direction) {
        if (attackInProgress) {
            return;
        }

        // VERIFICAÇÃO DE SEGURANÇA
        World currentWorld = getCurrentWorld();
        if (currentWorld == null) {
            System.err.println("❌ ERRO: Não há World válido para criar ataque!");
            return;
        }

        attackInProgress = true;
        isParryExtended = false;
        hitboxCreated = false;
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
        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.PARRY_SUCCESS);
    }

    private void createMeleeHitbox(int direction) {
        // Obter o World atual
        World world = getCurrentWorld();
        if (world == null) {
            System.err.println("❌ ERRO: World é null ao criar hitbox de ataque!");
            return;
        }
        
        // Verificar se o body do jogador é válido
        if (player.body == null || player.body.getWorld() == null) {
            System.err.println("❌ ERRO: Body do jogador inválido para criar hitbox!");
            return;
        }

        System.out.println("⚔️ Criando hitbox de ataque no mundo atual...");
        
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
        System.out.println("⚔️ Finalizando ataque...");
        
        parrySystem.resetParrySuccess();
        
        if (meleeHitboxBody != null) {
            World world = meleeHitboxBody.getWorld();
            if (world != null) {
                try {
                    world.destroyBody(meleeHitboxBody);
                    System.out.println("✅ Hitbox destruída");
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao destruir hitbox: " + e.getMessage());
                }
            }
            meleeHitboxBody = null;
        }
        attackInProgress = false;
        isParryExtended = false;
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


        public void extendAttackForParry() {
        if (attackInProgress && !isParryExtended) {
            isParryExtended = true;
            if (hitboxCreated) {
                scheduleEndAttackTimer();
            }
        }
    }


}