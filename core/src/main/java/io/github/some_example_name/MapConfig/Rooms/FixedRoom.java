// FixedRoom.java
package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Generator.StartRoom;

public class FixedRoom extends StartRoom {
    private final RoomConfiguration config;
    private Rectangle bounds;
    private Vector2 spawnPoint;
    private int width;
    private int height;

    public FixedRoom(RoomConfiguration config) {
        super();
        this.config = config;

        // Inicializa com as dimensões padrão da StartRoom (serão sobrescritas se
        // necessário)
        this.width = super.getWidth();
        this.height = super.getHeight();

        if (config.getImagePath() != null && !config.getImagePath().isEmpty()) {
            loadRoomImage(config.getImagePath());
        }
    }

    // 🔥 GETTERS E SETTERS PARA DIMENSÕES
    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
        Vector2 relativeSpawn = getStartPosition();
        this.spawnPoint = new Vector2(
                bounds.x + relativeSpawn.x,
                bounds.y + relativeSpawn.y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getWorldSpawnPoint() {
        return spawnPoint;
    }

    public RoomConfiguration getConfiguration() {
        return config;
    }

    private void loadRoomImage(String imagePath) {
        try {
            System.out.println("🖼️ Carregando sala fixa: " + config.getType() + " -> " + imagePath);
            // Implementação de carregamento...
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar sala fixa: " + e.getMessage());
            createDefaultRoom();
        }
    }
}