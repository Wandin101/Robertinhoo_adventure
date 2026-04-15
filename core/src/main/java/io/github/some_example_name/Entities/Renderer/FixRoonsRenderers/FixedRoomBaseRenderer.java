
package io.github.some_example_name.Entities.Renderer.FixRoonsRenderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.MapConfig.Mapa;

public class FixedRoomBaseRenderer {
    protected Mapa mapa;
    protected int tileSize;

    // Texturas comuns
    protected Texture floorSheet;
    protected TextureRegion[] floorTiles;
    protected Texture wallTopTexture;
    protected Texture wallFillTexture;

    public FixedRoomBaseRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;
        loadCommonTextures();
    }

    private void loadCommonTextures() {
        try {
            // Chão: sprite sheet 4x1 com tiles de 64x64
            floorSheet = new Texture(Gdx.files.internal("rooms/chão_salas_fixas.png"));
            floorTiles = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                floorTiles[i] = new TextureRegion(floorSheet, i * 64, 0, 64, 64);
            }

            // Paredes
            wallTopTexture = new Texture(Gdx.files.internal("rooms/parede.png"));
            wallFillTexture = new Texture(Gdx.files.internal("rooms/parede_full.png"));

            System.out.println("✅ Texturas base de salas fixas carregadas.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar texturas base: " + e.getMessage());
            createPlaceholderTextures();
        }
    }

    private void createPlaceholderTextures() {
        floorTiles = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            floorTiles[i] = new TextureRegion(createColorTexture(0.3f, 0.3f, 0.3f));
        }
        wallTopTexture = createColorTexture(0.8f, 0.1f, 0.1f);
        wallFillTexture = createColorTexture(0.5f, 0.3f, 0.1f);
    }

    private Texture createColorTexture(float r, float g, float b) {
        Pixmap pix = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888);
        pix.setColor(r, g, b, 1f);
        pix.fill();
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    /**
     * Renderiza o chão da sala fixa com o padrão de 4 tiles.
     */
    public void renderFloor(SpriteBatch batch, Rectangle bounds, float offsetX, float offsetY) {
        int startX = (int) bounds.x;
        int startY = (int) bounds.y;
        int width = (int) bounds.width;
        int height = (int) bounds.height;

        for (int wx = startX; wx < startX + width; wx++) {
            for (int wy = startY; wy < startY + height; wy++) {
                if (wx < 0 || wx >= mapa.mapWidth || wy < 0 || wy >= mapa.mapHeight)
                    continue;
                if (mapa.tiles[wx][wy] == Mapa.PAREDE)
                    continue;

                float baseScreenX = offsetX + wx * tileSize;
                float baseScreenY = offsetY + (mapa.mapHeight - 1 - wy) * tileSize;

                // Subdivide o tile 64x64 em 4x4 mini-tiles de 16x16 para mais detalhe
                int subDiv = 4;
                for (int subX = 0; subX < subDiv; subX++) {
                    for (int subY = 0; subY < subDiv; subY++) {
                        float screenX = baseScreenX + subX * (tileSize / (float) subDiv);
                        float screenY = baseScreenY + subY * (tileSize / (float) subDiv);

                        int globalSubX = wx * subDiv + subX;
                        int globalSubY = wy * subDiv + subY;
                        int hash = (globalSubX * 73856093) ^ (globalSubY * 19349663);
                        hash = hash & 0x7fffffff;
                        int index = hash % floorTiles.length;

                        batch.draw(floorTiles[index], screenX, screenY,
                                tileSize / (float) subDiv, tileSize / (float) subDiv);
                    }
                }
            }
        }
    }

    public void renderWalls(SpriteBatch batch, Rectangle bounds, float offsetX, float offsetY) {
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

            // Parede INFERIOR (y menor)
            int yBottom = startY;
            if (x >= 0 && x < mapa.mapWidth && yBottom >= 0 && yBottom < mapa.mapHeight) {
                if (mapa.tiles[x][yBottom] == Mapa.PAREDE) {
                    boolean isCorner = (x == startX || x == startX + width - 1);

                    if (isCorner) {
                        float screenX = offsetX + x * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - yBottom) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        renderWallTile(batch, x, yBottom, offsetX, offsetY, true);
                    }
                }
            }
        }

        // Paredes laterais
        for (int y = startY + 1; y < startY + height - 1; y++) {
            // Parede esquerda (oeste)
            int xLeft = startX;
            if (xLeft >= 0 && xLeft < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                if (mapa.tiles[xLeft][y] == Mapa.PAREDE) {
                    boolean isTopConnection = (y == startY + height - 2);

                    if (isTopConnection) {
                        float screenX = offsetX + xLeft * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        renderWallTile(batch, xLeft, y, offsetX, offsetY, false);
                    }
                }
            }

            // Parede direita (leste)
            int xRight = startX + width - 1;
            if (xRight >= 0 && xRight < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                if (mapa.tiles[xRight][y] == Mapa.PAREDE) {
                    boolean isTopConnection = (y == startY + height - 2);

                    if (isTopConnection) {
                        float screenX = offsetX + xRight * tileSize;
                        float screenY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                        batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                    } else {
                        renderWallTile(batch, xRight, y, offsetX, offsetY, false);
                    }
                }
            }
        }
    }

    private void renderWallTile(SpriteBatch batch, int tileX, int tileY, float offsetX, float offsetY, boolean isTop) {
        float screenX = offsetX + tileX * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tileY) * tileSize;
        if (isTop) {
            batch.draw(wallTopTexture, screenX, screenY, tileSize, tileSize);
        } else {
            batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
        }
    }

    public void dispose() {
        if (floorSheet != null)
            floorSheet.dispose();
        if (wallTopTexture != null)
            wallTopTexture.dispose();
        if (wallFillTexture != null)
            wallFillTexture.dispose();
    }
}