package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Interatibles.Interactable;

public class InteractableHandler implements ContactHandler {
    private Interactable currentInteractable = null;

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        // Verifica se um dos objetos é Interactable e o outro é o jogador
        if (isPlayerAndInteractable(dataA, dataB)) {
            Interactable interactable = (dataA instanceof Interactable) ? (Interactable) dataA : (Interactable) dataB;
            // Pode haver mais de um, mas vamos considerar o último que entrou em contato
            currentInteractable = interactable;
            return true; // se quiser parar a propagação
        }
        return false;
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        if (isPlayerAndInteractable(dataA, dataB)) {
            Interactable interactable = (dataA instanceof Interactable) ? (Interactable) dataA : (Interactable) dataB;
            if (currentInteractable == interactable) {
                currentInteractable = null;
            }
        }
    }

    private boolean isPlayerAndInteractable(Object a, Object b) {
        return (a instanceof Interactable && "PLAYER".equals(b)) ||
                (b instanceof Interactable && "PLAYER".equals(a));
    }

    public Interactable getCurrentInteractable() {
        return currentInteractable;
    }
}