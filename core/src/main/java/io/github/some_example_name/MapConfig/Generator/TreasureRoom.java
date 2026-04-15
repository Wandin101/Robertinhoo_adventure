// TreasureRoom.java
package io.github.some_example_name.MapConfig.Generator;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class TreasureRoom {
    public static final int ROOM_WIDTH = 7;
    public static final int ROOM_HEIGHT = 7;

    private int[][] tiles;
    private Vector2 chestPosition;

    public TreasureRoom() {
        tiles = new int[ROOM_WIDTH][ROOM_HEIGHT];
        createDefaultRoom();
        // Baú no centro
        chestPosition = new Vector2(ROOM_WIDTH / 2, ROOM_HEIGHT / 2);
    }

    private void createDefaultRoom() {
        for (int x = 0; x < ROOM_WIDTH; x++) {
            for (int y = 0; y < ROOM_HEIGHT; y++) {
                if (x == 0 || y == 0 || x == ROOM_WIDTH - 1 || y == ROOM_HEIGHT - 1) {
                    tiles[x][y] = Mapa.PAREDE;
                } else {
                    tiles[x][y] = Mapa.TILE;
                }
            }
        }
        System.out.println("✅ Sala TREASURE criada proceduralmente (7x7).");
    }

    public int[][] getTiles() {
        return tiles;
    }

    public Vector2 getChestPosition() {
        return chestPosition;
    }

    public int getWidth() {
        return ROOM_WIDTH;
    }

    public int getHeight() {
        return ROOM_HEIGHT;
    }
}