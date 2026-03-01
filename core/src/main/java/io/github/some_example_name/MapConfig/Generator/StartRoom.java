package io.github.some_example_name.MapConfig.Generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

import java.util.ArrayList;
import java.util.List;

public class StartRoom {

    public static final int ROOM_WIDTH = 9;
    public static final int ROOM_HEIGHT = 9;
    public static final int DOOR_TILE_X = 7;
    public static final int DOOR_TILE_Y = 0;

    // Cores para elementos especiais
    public static final int PILLAR_COLOR = 0x3b00ffff; // verde
    public static final int ENGRAVING_COLOR = 0x0dff6aff; // azul

    private int[][] tiles;
    private Vector2 startPosition;

    // Listas para armazenar posições dos elementos especiais
    private List<Vector2> pillarPositions = new ArrayList<>();
    private Vector2 engravingPosition = null;

    public StartRoom() {
        tiles = new int[ROOM_WIDTH][ROOM_HEIGHT];

        for (int x = 0; x < ROOM_WIDTH; x++) {
            for (int y = 0; y < ROOM_HEIGHT; y++) {
                tiles[x][y] = Mapa.TILE;
            }
        }

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
                    if (isWallColor(pixel)) {
                        tiles[x][y] = Mapa.PAREDE;
                    } else if (isStartColor(pixel)) {
                        tiles[x][y] = Mapa.START;
                        startPosition = new Vector2(x, y);
                    } else if (isPillarColor(pixel)) {
                        tiles[x][y] = Mapa.TILE;
                        pillarPositions.add(new Vector2(x, y));
                        System.out.println("🔹 Pilar detectado em: (" + x + "," + y + ")");
                    } else if (isEngravingColor(pixel)) {
                        tiles[x][y] = Mapa.TILE;
                        engravingPosition = new Vector2(x, y);
                        System.out.println("🔸 Gravura detectada em: (" + x + "," + y + ")");
                    } else {
                        tiles[x][y] = Mapa.TILE;
                    }
                }
            }

            pixmap.dispose();
            System.out.println("✅ Sala inicial carregada de " + imagePath);
            System.out.println("   Pilares encontrados: " + pillarPositions.size());
            System.out.println("   Gravura encontrada: " + (engravingPosition != null));

        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar sala inicial: " + e.getMessage());
            createDefaultRoom();
        }
    }

    public void createDefaultRoom() {
        // Sala padrão 9x9 com bordas de parede
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

        return Math.abs(r - 223) <= 10 &&
                Math.abs(g - 113) <= 10 &&
                Math.abs(b - 38) <= 10;
    }

    private boolean isStartColor(int pixel) {
        // START: vermelho #FF0000 = RGB(255, 0, 0)
        int r = (pixel & 0xff000000) >>> 24;
        int g = (pixel & 0x00ff0000) >>> 16;
        int b = (pixel & 0x0000ff00) >>> 8;
        return r == 255 && g == 0 && b == 0;
    }

    private boolean isPillarColor(int pixel) {
        int r = (pixel & 0xff000000) >>> 24;
        int g = (pixel & 0x00ff0000) >>> 16;
        int b = (pixel & 0x0000ff00) >>> 8;
        int a = pixel & 0x000000ff; // alpha
        // Se a constante for 0x3b00ffff, então esperamos r=0x3b, g=0x00, b=0xff, a=0xff
        return r == 0x3b && g == 0x00 && b == 0xff && a == 0xff;
    }

    private boolean isEngravingColor(int pixel) {
        int r = (pixel & 0xff000000) >>> 24;
        int g = (pixel & 0x00ff0000) >>> 16;
        int b = (pixel & 0x0000ff00) >>> 8;
        int a = pixel & 0x000000ff;
        // Cor esperada: R=0x0d (13), G=0xff (255), B=0x6a (106), A=0xff (255)
        return r == 0x0d && g == 0xff && b == 0x6a && a == 0xff;
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
        return new Vector2(
                startPosition.x + 0.5f,
                ROOM_HEIGHT - 1 - startPosition.y + 0.5f);
    }

    // Getters para os elementos especiais
    public List<Vector2> getPillarPositions() {
        return pillarPositions;
    }

    public Vector2 getEngravingPosition() {
        return engravingPosition;
    }
}