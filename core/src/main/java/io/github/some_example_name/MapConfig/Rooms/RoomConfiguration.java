

package io.github.some_example_name.MapConfig.Rooms;

import java.util.HashSet;
import java.util.Set;

public class RoomConfiguration {
    private final RoomType type;
    private final String imagePath;
    private final boolean hasEnemies;
    private final boolean hasBarrels;
    private final boolean hasChests;
    private final boolean hasNPCs;
    private final boolean hasTraps;
    private final boolean hasPuzzles;
    private final int minItems;
    private final int maxItems;
    
    private RoomConfiguration(Builder builder) {
        this.type = builder.type;
        this.imagePath = builder.imagePath;
        this.hasEnemies = builder.hasEnemies;
        this.hasBarrels = builder.hasBarrels;
        this.hasChests = builder.hasChests;
        this.hasNPCs = builder.hasNPCs;
        this.hasTraps = builder.hasTraps;
        this.hasPuzzles = builder.hasPuzzles;
        this.minItems = builder.minItems;
        this.maxItems = builder.maxItems;
    }
    
    // Builder Pattern para fácil criação
    public static class Builder {
        private final RoomType type;
        private String imagePath = "";
        private boolean hasEnemies = false;
        private boolean hasBarrels = false;
        private boolean hasChests = false;
        private boolean hasNPCs = false;
        private boolean hasTraps = false;
        private boolean hasPuzzles = false;
        private int minItems = 0;
        private int maxItems = 0;
        
        public Builder(RoomType type) {
            this.type = type;
            setDefaults(type);
        }
        
        private void setDefaults(RoomType type) {
            switch (type) {
                case SPAWN:
                    imagePath = "rooms/sala_1_start_room.png";
                    hasEnemies = false;
                    hasBarrels = false;
                    hasChests = false;
                    hasNPCs = false;
                    hasTraps = false;
                    hasPuzzles = false;
                    minItems = 0;
                    maxItems = 0;
                    break;
                    
                case MERCHANT:
                    imagePath = "rooms/merchant_room.png";
                    hasEnemies = false;
                    hasBarrels = false;
                    hasChests = true;
                    hasNPCs = true;
                    hasTraps = false;
                    hasPuzzles = false;
                    minItems = 3;
                    maxItems = 6;
                    break;
                    
                case TREASURE:
                    imagePath = "rooms/treasure_room.png";
                    hasEnemies = true; // Guardiões
                    hasBarrels = false;
                    hasChests = true;
                    hasNPCs = false;
                    hasTraps = true;
                    hasPuzzles = false;
                    minItems = 1;
                    maxItems = 3;
                    break;
                    
                case PROCEDURAL:
                    imagePath = ""; // Gerado proceduralmente
                    hasEnemies = true;
                    hasBarrels = true;
                    hasChests = true;
                    hasNPCs = false;
                    hasTraps = false;
                    hasPuzzles = false;
                    minItems = 2;
                    maxItems = 5;
                    break;
                    
                // ... outros tipos
            }
        }
        
        public Builder imagePath(String path) {
            this.imagePath = path;
            return this;
        }
        
        public Builder hasEnemies(boolean value) {
            this.hasEnemies = value;
            return this;
        }
        
        public Builder hasBarrels(boolean value) {
            this.hasBarrels = value;
            return this;
        }
        public Builder hasChests(boolean value) {
            this.hasChests = value;
            return this;
        }
        
        
        public RoomConfiguration build() {
            return new RoomConfiguration(this);
        }
    }
    
    // Getters
    public RoomType getType() { return type; }
    public String getImagePath() { return imagePath; }
    public boolean hasEnemies() { return hasEnemies; }
    public boolean hasBarrels() { return hasBarrels; }
    public boolean hasChests() { return hasChests; }
    public boolean hasNPCs() { return hasNPCs; }
    public boolean hasTraps() { return hasTraps; }
    public boolean hasPuzzles() { return hasPuzzles; }
    public int getMinItems() { return minItems; }
    public int getMaxItems() { return maxItems; }
}