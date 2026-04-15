package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import java.util.ArrayList;
import java.util.List;

public class GameContactListener implements ContactListener {
    private Robertinhoo player;
    private final List<ContactHandler> handlers = new ArrayList<>();

    public GameContactListener(Robertinhoo player) {
        System.out.println("🎯 GameContactListener CRIADO");
        System.out.println("   - Jogador fornecido: " + (player != null));
        System.out.println("   - Hash do jogador: " + (player != null ? System.identityHashCode(player) : "null"));
        this.player = player;
        initializeHandlers();
    }

    public Robertinhoo getPlayer() {
        return player;
    }

    public void updatePlayerReference(Robertinhoo newPlayer) {
        System.out.println("🔄 [GameContactListener] ATUALIZANDO referência do jogador");

        // Limpar TODOS os handlers antes de recriar
        for (ContactHandler handler : handlers) {
            if (handler instanceof PlayerItemHandler) {
                PlayerItemHandler itemHandler = (PlayerItemHandler) handler;
                System.out.println("🧹 Limpando PlayerItemHandler antigo...");
                itemHandler.clearAllItems(); // Vamos criar este método
            }
        }

        handlers.clear();
        this.player = newPlayer;
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.clear();
        handlers.add(new MeleeAttackHandler(player));
        handlers.add(new EnemyHandler(player));
        handlers.add(new PlayerItemHandler(player));
        handlers.add(new ProjectileHandler(player));
        handlers.add(new BarrelHandler());
        handlers.add(new GrassHandler());
        handlers.add(new DoorHandler());
        handlers.add(new Room0PlantHandler());
        handlers.add(new InteractableHandler());
        handlers.add(new SoulHandler(player));
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        for (ContactHandler handler : handlers) {
            if (handler.handleBeginContact(contact, fixtureA, fixtureB)) {
                break;
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        for (ContactHandler handler : handlers) {
            handler.handleEndContact(contact, fixtureA, fixtureB);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    public PlayerItemHandler getPlayerItemHandler() {
        for (ContactHandler handler : handlers) {
            if (handler instanceof PlayerItemHandler) {
                return (PlayerItemHandler) handler;
            }
        }
        return null;
    }

    public InteractableHandler getInteractableHandler() {
        for (ContactHandler handler : handlers) {
            if (handler instanceof InteractableHandler) {
                return (InteractableHandler) handler;
            }
        }
        return null;
    }
}