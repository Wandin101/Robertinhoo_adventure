package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.some_example_name.Entities.Itens.CenarioItens.Room0Grass;
import io.github.some_example_name.Entities.Itens.CenarioItens.Room0Flower;

public class Room0PlantHandler implements ContactHandler {

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        // Identifica qual fixture é a planta da sala 0
        Room0Grass room0Grass = null;
        Room0Flower room0Flower = null;

        // Verifica se é Room0Grass
        if (userDataA instanceof Room0Grass) {
            room0Grass = (Room0Grass) userDataA;
        } else if (userDataB instanceof Room0Grass) {
            room0Grass = (Room0Grass) userDataB;
        }

        // Verifica se é Room0Flower
        if (userDataA instanceof Room0Flower) {
            room0Flower = (Room0Flower) userDataA;
        } else if (userDataB instanceof Room0Flower) {
            room0Flower = (Room0Flower) userDataB;
        }

        // Aciona a animação específica para cada tipo
        if (room0Grass != null) {
            System.out.println("🌿 Room0Grass: Animação de caminhada ativada");
            room0Grass.triggerWalkAnimation();
            return true;
        }

        if (room0Flower != null) {
            System.out.println("🌸 Room0Flower: Contato detectado (sem animação)");
            room0Flower.triggerAnimation();
            return true;
        }

        return false;
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // Podemos adicionar lógica se necessário quando o contato termina
        // Ex: parar animação ou resetar estado
    }
}