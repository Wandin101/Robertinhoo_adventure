package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import com.badlogic.gdx.Gdx;

public class MeleeAttackHandler implements ContactHandler {
    private final Robertinhoo player;

    public MeleeAttackHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        if (checkMissileParry(contact, fixtureA, fixtureB)) {
            return true;
        }

        if (("MELEE_ATTACK".equals(dataA) && dataB instanceof Enemy)) {
            handleMeleeAttack((Enemy) dataB, fixtureA.getBody().getPosition());
        } else if ("MELEE_ATTACK".equals(dataB) && dataA instanceof Enemy) {
            handleMeleeAttack((Enemy) dataA, fixtureB.getBody().getPosition());
        }

        return false;
    }

    private boolean checkMissileParry(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        boolean isMeleeVsMissile = ("MELEE_ATTACK".equals(dataA) && dataB instanceof Missile) ||
                ("MELEE_ATTACK".equals(dataB) && dataA instanceof Missile);

        boolean isPlayerVsMissile = player.getMeleeAttackSystem().getParrySystem().isParryActive() &&
                (("PLAYER".equals(dataA) && dataB instanceof Missile) ||
                        ("PLAYER".equals(dataB) && dataA instanceof Missile));

        if (isMeleeVsMissile || isPlayerVsMissile) {
            Missile missile = (Missile) ((dataA instanceof Missile) ? dataA : dataB);

            if (player.getMeleeAttackSystem().getParrySystem().isParryActive()) {
                if (!missile.isReflected() && missile.getOwner() != null) {
                    Gdx.app.log("PARRY_SYNC", "Parry detectado - processando míssil");

                    // Marca o parry como sucesso e passa o míssil
                    player.getMeleeAttackSystem().getParrySystem().markParrySuccess(missile);

                    return true;
                } else {
                    Gdx.app.log("PARRY_SYNC", "Míssil não pode ser revertido");
                }
            }
        }
        return false;
    }

    private void handleMeleeAttack(Enemy enemy, Vector2 attackPosition) {
        if (enemy.isDead())
            return;

        enemy.takeDamage(15);
        float knockbackForce = 1f;

        if (enemy instanceof io.github.some_example_name.Entities.Enemies.Castor.Castor) {
            knockbackForce = 0.1f;
        }

        Vector2 direction = new Vector2(enemy.getBody().getPosition()).sub(attackPosition).nor();
        enemy.getBody().applyLinearImpulse(
                direction.scl(knockbackForce),
                enemy.getBody().getWorldCenter(),
                true);

        if (enemy.getHealth() <= 0) {

            enemy.die(Enemy.DeathType.MELEE);

        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {

    }
}