package io.github.some_example_name.Entities.Renderer.FixRoonsRenderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;

public class SpawnRoomRenderer {
    private FixedRoom spawnRoom;
    private Mapa mapa;
    private Texture floorTexture;
    private Texture wallTopTexture;
    private Texture wallFillTexture;
    private int tileSize;
    private Rectangle bounds;

    public SpawnRoomRenderer(FixedRoom spawnRoom, Mapa mapa, int tileSize) {
        this.spawnRoom = spawnRoom;
        this.mapa = mapa;
        this.tileSize = tileSize;
        this.bounds = spawnRoom.getBounds();

        loadSpawnRoomTextures();
        debugTileContents();
    }

    private void loadSpawnRoomTextures() {
        try {
            floorTexture = new Texture(Gdx.files.internal("rooms/textura_chão_start_room.png"));

            floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            floorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            wallTopTexture = new Texture(Gdx.files.internal("rooms/parede.png"));
            wallFillTexture = new Texture(Gdx.files.internal("rooms/parede_full.png"));
            System.out.println("✅ Texturas da sala SPAWN carregadas");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar texturas da sala SPAWN: " + e.getMessage());
            createPlaceholderTextures();
        }
    }

    private void createPlaceholderTextures() {
        // Placeholder para chão (verde claro)
        com.badlogic.gdx.graphics.Pixmap pixmapFloor = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapFloor.setColor(0.3f, 0.8f, 0.3f, 1f); // Verde
        pixmapFloor.fill();
        floorTexture = new Texture(pixmapFloor);
        pixmapFloor.dispose();

        // Placeholder para parede topo (vermelho escuro)
        com.badlogic.gdx.graphics.Pixmap pixmapTop = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapTop.setColor(0.8f, 0.1f, 0.1f, 1f); // Vermelho
        pixmapTop.fill();
        wallTopTexture = new Texture(pixmapTop);
        pixmapTop.dispose();

        // Placeholder para parede fill (marrom)
        com.badlogic.gdx.graphics.Pixmap pixmapFill = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapFill.setColor(0.5f, 0.3f, 0.1f, 1f); // Marrom
        pixmapFill.fill();
        wallFillTexture = new Texture(pixmapFill);
        pixmapFill.dispose();
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (bounds == null) {
            System.err.println("❌ SpawnRoomRenderer: bounds é null!");
            return;
        }

        if (floorTexture == null || wallTopTexture == null || wallFillTexture == null) {
            System.err.println("❌ Texturas não carregadas");
            return;
        }

        // 1. Renderiza o chão da sala de spawn
        renderFloor(batch, offsetX, offsetY);

        // 2. Renderiza as paredes da sala de spawn
        renderWalls(batch, offsetX, offsetY);
    }

    private void renderFloor(SpriteBatch batch, float offsetX, float offsetY) {
        int startX = (int) bounds.x;
        int startY = (int) bounds.y;
        int width = (int) bounds.width;
        int height = (int) bounds.height;

        // Posição na tela
        float screenX = offsetX + startX * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - (startY + height - 1)) * tileSize;

        // Tamanho em pixels (considerando que cada tile tem tileSize)
        float roomWidthPixels = width * tileSize;
        float roomHeightPixels = height * tileSize;

        // ✅ AJUSTE: Se a textura é 128px e tileSize é 64px, precisamos de uma escala
        // diferente
        // A textura 128px deve se repetir MENOS vezes

        // Tamanho da textura original
        int texWidth = 100; // 128
        int texHeight = 100; // 128

        float scaleFactor = texWidth / (float) tileSize; // 128 / 64 = 2.0

        // A sala tem 'width' tiles horizontalmente.
        // Se cada textura cobre 2 tiles, então precisamos de width/2 repetições
        float repeatX = width / scaleFactor; // ex: 16 / 2 = 8 repetições
        float repeatY = height / scaleFactor; // ex: 16 / 2 = 8 repetições
        // Cria uma região da textura com as dimensões calculadas
        TextureRegion floorRegion = new TextureRegion(floorTexture);

        // Define a região da textura para repetição
        // Nota: floorTexture.setWrap já foi configurado para Repeat no
        // loadSpawnRoomTextures
        floorRegion.setRegion(0, 0,
                (int) (texWidth * repeatX),
                (int) (texHeight * repeatY));

        // Desenha
        batch.draw(floorRegion, screenX, screenY, roomWidthPixels, roomHeightPixels);
    }

    private void renderWalls(SpriteBatch batch, float offsetX, float offsetY) {
        int startX = (int) bounds.x;
        int startY = (int) bounds.y;
        int width = (int) bounds.width;
        int height = (int) bounds.height;

        for (int x = startX; x < startX + width; x++) {
            int yTop = startY + height - 1;
            if (x >= 0 && x < mapa.mapWidth && yTop >= 0 && yTop < mapa.mapHeight) {
                if (mapa.tiles[x][yTop] == Mapa.PAREDE) {
                    boolean isCorner = (x == startX || x == startX + width - 1);

                    if (isCorner) {
                        float screenX = offsetX + x * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - yTop) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        renderWallTile(batch, x, yTop, offsetX, offsetY, false);
                    }
                }
            }

            // Parede INFERIOR (y menor) → usa textura de TOPO
            int yBottom = startY;
            if (x >= 0 && x < mapa.mapWidth && yBottom >= 0 && yBottom < mapa.mapHeight) {
                if (mapa.tiles[x][yBottom] == Mapa.PAREDE) {
                    // ✅ VERIFICA SE É CANTO (extremidade)
                    boolean isCorner = (x == startX || x == startX + width - 1);

                    if (isCorner) {
                        // ✅ CANTO DO FUNDO: usa wallFillTexture em vez de wallTopTexture
                        float screenX = offsetX + x * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - yBottom) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        // ✅ MEIO DO FUNDO: mantém a lógica original (wallTopTexture)
                        renderWallTile(batch, x, yBottom, offsetX, offsetY, true);
                    }
                }
            }
        }

        // Paredes laterais permanecem iguais
        for (int y = startY + 1; y < startY + height - 1; y++) {
            // Parede esquerda (oeste)
            int xLeft = startX;
            if (xLeft >= 0 && xLeft < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                if (mapa.tiles[xLeft][y] == Mapa.PAREDE) {
                    // ✅ Para paredes laterais, verifica se está conectando com o topo
                    boolean isTopConnection = (y == startY + height - 2); // Logo abaixo do topo

                    if (isTopConnection) {
                        // Paredes laterais próximas ao topo: usa wallFillTexture normal
                        float screenX = offsetX + xLeft * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        // Outras paredes laterais: mantém a rotação original
                        renderWallTile(batch, xLeft, y, offsetX, offsetY, false);
                    }
                }
            }

            // Parede direita (leste)
            int xRight = startX + width - 1;
            if (xRight >= 0 && xRight < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                if (mapa.tiles[xRight][y] == Mapa.PAREDE) {
                    // ✅ Para paredes laterais, verifica se está conectando com o topo
                    boolean isTopConnection = (y == startY + height - 2); // Logo abaixo do topo

                    if (isTopConnection) {
                        // Paredes laterais próximas ao topo: usa wallFillTexture normal
                        float screenX = offsetX + xRight * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        // Outras paredes laterais: mantém a rotação original
                        renderWallTile(batch, xRight, y, offsetX, offsetY, false); // Usa fill
                    }
                }
            }
        }
    }

    private void renderWallTile(SpriteBatch batch, int tileX, int tileY, float offsetX, float offsetY, boolean isTop) {
        // MESMA LÓGICA DO TILE_RENDERER!
        float screenX = offsetX + tileX * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tileY) * tileSize;

        if (isTop) {
            batch.draw(wallTopTexture, screenX, screenY, tileSize, tileSize);
        } else {
            batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
        }
    }

    private void debugTileContents() {
        if (bounds == null)
            return;

        System.out.println("=== DEBUG TILE CONTENTS IN SPAWN ROOM ===");
        System.out.println("Mapa.PAREDE = " + Mapa.PAREDE);
        System.out.println("Mapa.TILE = " + Mapa.TILE);
        System.out.println("Mapa.START = " + Mapa.START);

        int startX = (int) bounds.x;
        int startY = (int) bounds.y;
        int width = (int) bounds.width;
        int height = (int) bounds.height;

        // Imprime as primeiras e últimas linhas
        System.out.println("Primeira linha (y = " + startY + "):");
        for (int x = startX; x < startX + width; x++) {
            int y = startY;
            if (x >= 0 && x < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                System.out.print(mapa.tiles[x][y] + " ");
            }
        }
        System.out.println();

        System.out.println("Última linha (y = " + (startY + height - 1) + "):");
        for (int x = startX; x < startX + width; x++) {
            int y = startY + height - 1;
            if (x >= 0 && x < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                System.out.print(mapa.tiles[x][y] + " ");
            }
        }
        System.out.println();

        // Verifica o tile START também
        System.out.println("Procurando tile START...");
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x >= 0 && x < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                    if (mapa.tiles[x][y] == Mapa.START) {
                        System.out.println("✅ Tile START encontrado em: (" + x + ", " + y + ")");
                        System.out.println("   Relativo à sala: x=" + (x - startX) +
                                ", y=" + (y - startY) + "/" + (height - 1));
                    }
                }
            }
        }
        System.out.println("=== END DEBUG ===");
    }

    public void dispose() {
        if (floorTexture != null) {
            floorTexture.dispose();
        }
        if (wallTopTexture != null) {
            wallTopTexture.dispose();
        }
        if (wallFillTexture != null) {
            wallFillTexture.dispose();
        }
    }
}