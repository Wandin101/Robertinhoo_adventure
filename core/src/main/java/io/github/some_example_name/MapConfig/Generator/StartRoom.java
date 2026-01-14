package io.github.some_example_name.MapConfig.Generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class StartRoom {

    public static final int ROOM_WIDTH = 9;
    public static final int ROOM_HEIGHT = 9;

    private int[][] tiles;
    private Vector2 startPosition;

    public StartRoom() {
        tiles = new int[ROOM_WIDTH][ROOM_HEIGHT];
        loadFromImage("rooms/sala_1_start_room.png");
    }

    private void loadFromImage(String imagePath) {
        try {
            Pixmap pixmap = new Pixmap(Gdx.files.internal(imagePath));

            // Verifica se a imagem tem o tamanho correto
            if (pixmap.getWidth() != ROOM_WIDTH || pixmap.getHeight() != ROOM_HEIGHT) {
                System.err.println("❌ Imagem da sala inicial tem tamanho errado: " +
                        pixmap.getWidth() + "x" + pixmap.getHeight() +
                        " (esperado: " + ROOM_WIDTH + "x" + ROOM_HEIGHT + ")");
                createDefaultRoom();
                pixmap.dispose();
                return;
            }

            // Carrega os tiles da imagem
            for (int x = 0; x < ROOM_WIDTH; x++) {
                for (int y = 0; y < ROOM_HEIGHT; y++) {
                    int pixel = pixmap.getPixel(x, y);

                    // Mapeia cores para tiles
                    if (isWallColor(pixel)) {
                        tiles[x][y] = Mapa.PAREDE;
                    } else if (isStartColor(pixel)) {
                        tiles[x][y] = Mapa.START;
                        startPosition = new Vector2(x, y);
                    } else {
                        tiles[x][y] = Mapa.TILE;
                    }
                }
            }

            pixmap.dispose();
            System.out.println("✅ Sala inicial carregada de " + imagePath);

        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar sala inicial: " + e.getMessage());
            createDefaultRoom();
        }
    }

    public void createDefaultRoom() {
        // Sala padrão 16x16 com bordas de parede
        for (int x = 0; x < ROOM_WIDTH; x++) {
            for (int y = 0; y < ROOM_HEIGHT; y++) {
                if (x == 0 || y == 0 || x == ROOM_WIDTH - 1 || y == ROOM_HEIGHT - 1) {
                    tiles[x][y] = Mapa.PAREDE;
                } else {
                    tiles[x][y] = Mapa.TILE;
                }
            }
        }

        // Posição inicial no centro
        startPosition = new Vector2(ROOM_WIDTH / 2, ROOM_HEIGHT / 2);
        tiles[(int) startPosition.x][(int) startPosition.y] = Mapa.START;

        System.out.println("✅ Sala padrão criada");
    }

    private boolean isWallColor(int pixel) {
        // PAREDE: #df7126 = RGB(223, 113, 38)
        int r = (pixel & 0xff000000) >>> 24;
        int g = (pixel & 0x00ff0000) >>> 16;
        int b = (pixel & 0x0000ff00) >>> 8;

        // Cor laranja #df7126 = RGB(223, 113, 38)
        // Usando uma margem de tolerância de 10
        return Math.abs(r - 223) <= 10 &&
                Math.abs(g - 113) <= 10 &&
                Math.abs(b - 38) <= 10;
    }

    private boolean isStartColor(int pixel) {
        // Verifica se é ponto de início (vermelho #FF0000)
        int r = (pixel & 0xff000000) >>> 24;
        int g = (pixel & 0x00ff0000) >>> 16;
        int b = (pixel & 0x0000ff00) >>> 8;

        // #FF0000 = RGB(255, 0, 0)
        return r == 255 && g == 0 && b == 0;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public int getWidth() {
        return ROOM_WIDTH;
    }

    public int getHeight() {
        return ROOM_HEIGHT;
    }

    public Vector2 getWorldStartPosition() {
        // Converte coordenadas de tile para mundo
        return new Vector2(
                startPosition.x + 0.5f,
                ROOM_HEIGHT - 1 - startPosition.y + 0.5f);
    }
}