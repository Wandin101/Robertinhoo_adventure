package io.github.some_example_name.MapConfig.Generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class TreasureRoom {
    public static final int ROOM_WIDTH = 9;
    public static final int ROOM_HEIGHT = 9;

    private int[][] tiles;
    private Vector2 chestPosition; // posição do baú em coordenadas locais (tile)

    public TreasureRoom() {
        tiles = new int[ROOM_WIDTH][ROOM_HEIGHT];
        // Tenta carregar de uma imagem; se falhar, cria proceduralmente
        if (!loadFromImage("rooms/treasure_room.png")) {
            createDefaultRoom();
        }
        // Define o baú no centro da sala
        chestPosition = new Vector2(ROOM_WIDTH / 2, ROOM_HEIGHT / 2);
    }

    private boolean loadFromImage(String imagePath) {
        try {
            Pixmap pixmap = new Pixmap(Gdx.files.internal(imagePath));
            if (pixmap.getWidth() != ROOM_WIDTH || pixmap.getHeight() != ROOM_HEIGHT) {
                System.err.println("❌ Imagem da sala do tesouro com tamanho incorreto.");
                pixmap.dispose();
                return false;
            }

            for (int x = 0; x < ROOM_WIDTH; x++) {
                for (int y = 0; y < ROOM_HEIGHT; y++) {
                    int pixel = pixmap.getPixel(x, y);
                    // Usa a mesma detecção de cores da StartRoom
                    if (isWallColor(pixel)) {
                        tiles[x][y] = Mapa.PAREDE;
                    } else {
                        tiles[x][y] = Mapa.TILE;
                    }
                }
            }
            pixmap.dispose();
            System.out.println("✅ Sala TREASURE carregada de " + imagePath);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar imagem da sala TREASURE: " + e.getMessage());
            return false;
        }
    }

    private void createDefaultRoom() {
        // Sala procedural: bordas de parede, interior chão
        for (int x = 0; x < ROOM_WIDTH; x++) {
            for (int y = 0; y < ROOM_HEIGHT; y++) {
                if (x == 0 || y == 0 || x == ROOM_WIDTH - 1 || y == ROOM_HEIGHT - 1) {
                    tiles[x][y] = Mapa.PAREDE;
                } else {
                    tiles[x][y] = Mapa.TILE;
                }
            }
        }
        System.out.println("✅ Sala TREASURE criada proceduralmente.");
    }

    private boolean isWallColor(int pixel) {
        // Cor #df7126 (RGB: 223, 113, 38) – mesma da StartRoom
        int r = (pixel & 0xff000000) >>> 24;
        int g = (pixel & 0x00ff0000) >>> 16;
        int b = (pixel & 0x0000ff00) >>> 8;
        return Math.abs(r - 223) <= 10 && Math.abs(g - 113) <= 10 && Math.abs(b - 38) <= 10;
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
