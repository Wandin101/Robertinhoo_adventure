// GrassHandler.java
package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.some_example_name.Entities.Itens.CenarioItens.Grass;
import io.github.some_example_name.Entities.Itens.CenarioItens.Room0Grass;
import io.github.some_example_name.Entities.Itens.CenarioItens.Room0Flower;

public class GrassHandler implements ContactHandler {

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        short categoryA = fixtureA.getFilterData().categoryBits;
        short categoryB = fixtureB.getFilterData().categoryBits;

        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        Grass grass = null;
        short otherCategory = 0;

        // Identifica qual fixture é a grama
        if (userDataA instanceof Grass) {
            grass = (Grass) userDataA;
            otherCategory = categoryB;
        } else if (userDataB instanceof Grass) {
            grass = (Grass) userDataB;
            otherCategory = categoryA;
        }

        if (grass != null) {
            // Verifica se é um ataque corpo a corpo
            if ((otherCategory & Constants.BIT_PLAYER_ATTACK) != 0) {
                grass.takeDamage(1);
                return true;
            }
            // Verifica se é player ou inimigo
            else if ((otherCategory & (Constants.BIT_PLAYER | Constants.BIT_ENEMY)) != 0) {
                grass.triggerWalkAnimation();
                return true;
            }
        }

        return false;
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // Nada necessário para grama no end contact
        // A animação de caminhada termina automaticamente
    }
}