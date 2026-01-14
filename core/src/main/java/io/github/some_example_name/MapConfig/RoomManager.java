package io.github.some_example_name.MapConfig;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Player.Robertinhoo;

public class RoomManager {
    private static RoomManager instance;
    private Robertinhoo playerInstance;

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public Mapa createOrResetRoom0(Robertinhoo existingPlayer) {
        System.out.println("🏠 Criando/Resetando Sala 0...");

        Mapa room0 = new Mapa(true);
        float cabanaX = 4.5f;
        float cabanaY = 2.5f;

        if (existingPlayer != null) {
            System.out.println("👤 Reutilizando jogador existente para Sala 0");
            playerInstance = existingPlayer;

            Vector2 spawnPos = new Vector2(cabanaX, cabanaY);
            System.out.println("📍 Spawnando na cabana (mundo): " + spawnPos);

            playerInstance.resetForNewRoom(spawnPos, room0, 0.7f);
            room0.robertinhoo = playerInstance;

            if (room0.cabanaInteraction != null) {
                room0.cabanaInteraction.setPlayer(playerInstance);
            }
        } else {
            playerInstance = room0.robertinhoo;

            if (playerInstance != null && playerInstance.getBody() != null) {
                playerInstance.getBody().setTransform(cabanaX, cabanaY, 0);
                playerInstance.pos.set(cabanaX, cabanaY);
                System.out.println("👤 Jogador novo posicionado na cabana: (" + cabanaX + ", " + cabanaY + ")");
            }
        }

        return room0;
    }

    public Robertinhoo getPlayerInstance() {
        return playerInstance;
    }

    public void setPlayerInstance(Robertinhoo player) {
        this.playerInstance = player;
    }
}