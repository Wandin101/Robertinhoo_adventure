package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.SoulShopSystem.Soul;

public class SoulHandler implements ContactHandler {
    private Robertinhoo player;

    public SoulHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        Soul soul = null;
        if (dataA instanceof Soul && "PLAYER".equals(dataB)) {
            soul = (Soul) dataA;
        } else if (dataB instanceof Soul && "PLAYER".equals(dataA)) {
            soul = (Soul) dataB;
        }

        if (soul != null && !soul.isMarkedForRemoval()) {
            player.getSoulManager().addSouls(soul.getValue());
            soul.markForRemoval();
            Gdx.app.log("SoulHandler", "Alma coletada! +" + soul.getValue());
            return true;
        }
        return false;
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // nada
    }
}