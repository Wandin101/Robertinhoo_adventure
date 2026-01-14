package io.github.some_example_name.MapConfig.Rooms;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Generator.StartRoom;


public class FixedRoom extends StartRoom {
    private final RoomConfiguration config;
    private Rectangle bounds;
    private Vector2 spawnPoint;
    
    public FixedRoom(RoomConfiguration config) {
        super();
        this.config = config;
        
        // Carrega a imagem específica para este tipo de sala
        if (config.getImagePath() != null && !config.getImagePath().isEmpty()) {
            loadRoomImage(config.getImagePath());
        }
    }
    
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
        // Calcula o spawn point relativo dentro da sala
        Vector2 relativeSpawn = getStartPosition();
        this.spawnPoint = new Vector2(
            bounds.x + relativeSpawn.x,
            bounds.y + relativeSpawn.y
        );
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
        // Sobrescreve o método de carregamento padrão
        // Implementação similar à StartRoom, mas com caminho customizado
        try {
            System.out.println("🖼️ Carregando sala fixa: " + config.getType() + " -> " + imagePath);
            // ... implementação de carregamento
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar sala fixa: " + e.getMessage());
            createDefaultRoom();
        }
    }
}