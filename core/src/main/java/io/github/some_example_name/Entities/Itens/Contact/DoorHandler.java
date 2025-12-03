package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.MapConfig.Rooms.Room0Door;
public class DoorHandler implements ContactHandler {

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        Room0Door door = null;
        short otherCategory = 0;

        // Identifica qual fixture é a porta
        if (userDataA instanceof Room0Door) {
            door = (Room0Door) userDataA;
            otherCategory = fixtureB.getFilterData().categoryBits;
        } else if (userDataB instanceof Room0Door) {
            door = (Room0Door) userDataB;
            otherCategory = fixtureA.getFilterData().categoryBits;
        }

        if (door != null) {
            // Verifica se é o jogador
            if ((otherCategory & Constants.BIT_PLAYER) != 0) {
                System.out.println("🚪 [BEGIN CONTACT] Porta detectou jogador - " +
                                 "Categoria Porta: " + fixtureA.getFilterData().categoryBits + " | " + 
                                 "Categoria Player: " + otherCategory +
                                 " | Estado: " + door.getCurrentState());
                door.onPlayerEnter();
                return true;
            } else {
                System.out.println("🚪 [BEGIN CONTACT] Porta detectou algo não-player - Categoria: " + otherCategory);
            }
        } else {
            // Debug: verifica se há BIT_DOOR envolvido
            short catA = fixtureA.getFilterData().categoryBits;
            short catB = fixtureB.getFilterData().categoryBits;
            if ((catA & Constants.BIT_DOOR) != 0 || (catB & Constants.BIT_DOOR) != 0) {
                System.out.println("🚪 [DEBUG] Contato com BIT_DOOR detectado mas não processado - " +
                                 "CatA: " + catA + " | CatB: " + catB +
                                 " | UserDataA: " + (userDataA != null ? userDataA.getClass().getSimpleName() : "null") +
                                 " | UserDataB: " + (userDataB != null ? userDataB.getClass().getSimpleName() : "null"));
            }
        }

        return false;
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        Room0Door door = null;
        short otherCategory = 0;

        if (userDataA instanceof Room0Door) {
            door = (Room0Door) userDataA;
            otherCategory = fixtureB.getFilterData().categoryBits;
        } else if (userDataB instanceof Room0Door) {
            door = (Room0Door) userDataB;
            otherCategory = fixtureA.getFilterData().categoryBits;
        }

        if (door != null) {
            if ((otherCategory & Constants.BIT_PLAYER) != 0) {
                System.out.println("🚪 [END CONTACT] Porta perdeu contato com jogador - Estado: " + door.getCurrentState());
                door.onPlayerExit();
            }
        }
    }
}