package io.github.some_example_name.MapConfig;

public class RoomManager {
    private static RoomManager instance;
    
    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }
    
    public Mapa createRoom0() {
        Mapa room0 = new Mapa(true);
        room0.clearEnemies();
        room0.clearItems();
        
        return room0;
    }
}