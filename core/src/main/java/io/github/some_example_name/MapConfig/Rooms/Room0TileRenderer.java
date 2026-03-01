package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;
import java.util.ArrayList;
import java.util.Random;

public class Room0TileRenderer {
    private Mapa mapa;
    private Texture[] floorTextures; // 4 texturas
    private Texture pathTexture;
    private int tileSize;
    private int[][] tileIndex; // matriz com o índice da textura para cada tile

    public Room0TileRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;
        loadFloorTextures();
        loadPathTexture();
        precomputeTileIndices();
    }

    private void loadFloorTextures() {
        floorTextures = new Texture[4];
        try {
            floorTextures[0] = new Texture(Gdx.files.internal("sala_0/room_0_solo1.png"));
            floorTextures[1] = new Texture(Gdx.files.internal("sala_0/room_0_solo2.png"));
            floorTextures[2] = new Texture(Gdx.files.internal("sala_0/rooom_0_solo3.png"));
            floorTextures[3] = new Texture(Gdx.files.internal("sala_0/room_0_solo4.png"));
            System.out.println("✅ 4 texturas de solo carregadas.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar texturas: " + e.getMessage());
            for (int i = 0; i < 4; i++) {
                floorTextures[i] = createPlaceholderTexture(i);
            }
        }
    }

    private void loadPathTexture() {
        try {
            pathTexture = new Texture(Gdx.files.internal("sala_0/solo_pedra.png"));
        } catch (Exception e) {
            pathTexture = createPathPlaceholderTexture();
        }
    }

    private Texture createPlaceholderTexture(int index) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(125, 125,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f + index * 0.15f, 0.2f, 0.3f, 1f);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createPathPlaceholderTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(125, 125,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.36f, 0.25f, 0.22f, 1f);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void precomputeTileIndices() {
        int width = mapa.mapWidth;
        int height = mapa.mapHeight;
        tileIndex = new int[width][height];

        // Inicialmente, todos os tiles de chão recebem solo4 (índice 3)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (mapa.tiles[x][y] == Mapa.TILE) {
                    tileIndex[x][y] = 3; // solo4
                } else {
                    tileIndex[x][y] = -1; // não chão
                }
            }
        }

        // Coletar todas as posições de chão
        java.util.ArrayList<Vector2> positions = new java.util.ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (mapa.tiles[x][y] == Mapa.TILE) {
                    positions.add(new Vector2(x, y));
                }
            }
        }

        int total = positions.size();
        int rareCount = (int) (total * 0.15f); // total de raros (solos 0,1,2)
        // Distribuir igualmente entre os 3 tipos
        int each = rareCount / 3;
        int remainder = rareCount % 3;
        int[] rarePerType = { each, each, each };
        for (int i = 0; i < remainder; i++) {
            rarePerType[i]++;
        }

        // Embaralhar posições deterministicamente
        java.util.Random rand = new java.util.Random(12345); // semente fixa
        for (int i = positions.size() - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            Vector2 temp = positions.get(i);
            positions.set(i, positions.get(j));
            positions.set(j, temp);
        }

        // Função para verificar se uma posição tem vizinho raro
        // Considera 4 direções (cima, baixo, esquerda, direita)
        java.util.function.BiPredicate<Integer, Integer> hasRareNeighbor = (px, py) -> {
            int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
            for (int[] d : dirs) {
                int nx = px + d[0];
                int ny = py + d[1];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (mapa.tiles[nx][ny] == Mapa.TILE && tileIndex[nx][ny] != 3) { // se não é solo4
                        return true;
                    }
                }
            }
            return false;
        };

        // Atribuir solos raros (tipos 0,1,2) de forma espaçada
        for (int type = 0; type < 3; type++) {
            int toPlace = rarePerType[type];
            int attempts = 0;
            int maxAttempts = positions.size() * 2; // evitar loop infinito

            while (toPlace > 0 && attempts < maxAttempts) {
                // Percorrer as posições embaralhadas em ordem
                for (int i = 0; i < positions.size() && toPlace > 0; i++) {
                    Vector2 pos = positions.get(i);
                    int x = (int) pos.x;
                    int y = (int) pos.y;

                    // Se já é solo4 e não tem vizinho raro
                    if (tileIndex[x][y] == 3 && !hasRareNeighbor.test(x, y)) {
                        tileIndex[x][y] = type;
                        toPlace--;
                    }
                }
                attempts++;
                // Se não conseguiu colocar todos, reembaralha e tenta novamente (opcional)
                if (toPlace > 0) {
                    // Embaralhar novamente para tentar outras combinações
                    for (int i = positions.size() - 1; i > 0; i--) {
                        int j = rand.nextInt(i + 1);
                        Vector2 temp = positions.get(i);
                        positions.set(i, positions.get(j));
                        positions.set(j, temp);
                    }
                }
            }

            // Se ainda restaram, coloca nos primeiros disponíveis sem verificar vizinhança
            if (toPlace > 0) {
                for (int i = 0; i < positions.size() && toPlace > 0; i++) {
                    Vector2 pos = positions.get(i);
                    int x = (int) pos.x;
                    int y = (int) pos.y;
                    if (tileIndex[x][y] == 3) {
                        tileIndex[x][y] = type;
                        toPlace--;
                    }
                }
            }
        }
    }

    public void renderFloor(SpriteBatch batch, float offsetX, float offsetY) {
        if (floorTextures == null || floorTextures[0] == null)
            return;

        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.TILE) {
                    int index = tileIndex[x][y];
                    if (index == -1) {
                        index = 3; // fallback seguro
                    }
                    float screenX = offsetX + x * tileSize;
                    float screenY = offsetY + y * tileSize;
                    batch.draw(floorTextures[index], screenX, screenY, tileSize, tileSize);
                }
            }
        }
    }

    public void dispose() {
        for (Texture tex : floorTextures) {
            if (tex != null)
                tex.dispose();
        }
        if (pathTexture != null)
            pathTexture.dispose();
    }
}