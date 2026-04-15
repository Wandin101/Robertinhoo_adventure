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
    private FixedRoomBaseRenderer baseRenderer;

    public SpawnRoomRenderer(FixedRoom spawnRoom, Mapa mapa, int tileSize) {
        this.spawnRoom = spawnRoom;
        this.mapa = mapa;
        this.tileSize = tileSize;
        this.bounds = spawnRoom.getBounds();
        this.baseRenderer = new FixedRoomBaseRenderer(mapa, tileSize);
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
        if (bounds == null)
            return;
        baseRenderer.renderFloor(batch, bounds, offsetX, offsetY);
        baseRenderer.renderWalls(batch, bounds, offsetX, offsetY);
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
        baseRenderer.dispose();
    }
}