package io.github.some_example_name.Entities.Renderer.FixRoonsRenderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Interatibles.Chest;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;
import io.github.some_example_name.MapConfig.Generator.TreasureRoom;
import java.util.List;

public class TreasureRoomRenderer {
    private FixedRoom treasureFixedRoom;
    private TreasureRoom treasureRoom;
    private Mapa mapa;
    private int tileSize;
    private Rectangle bounds;

    private Texture floorTexture;
    private Texture wallTopTexture;
    private Texture wallFillTexture;
    private Texture rugTexture;

    public TreasureRoomRenderer(FixedRoom treasureFixedRoom, TreasureRoom treasureRoom, Mapa mapa, int tileSize) {
        this.treasureFixedRoom = treasureFixedRoom;
        this.treasureRoom = treasureRoom;
        this.mapa = mapa;
        this.tileSize = tileSize;
        this.bounds = treasureFixedRoom.getBounds();

        loadTextures();
    }

    private void loadTextures() {
        try {
            floorTexture = new Texture(Gdx.files.internal("rooms/textura_chão_start_room.png"));
            floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            floorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            wallTopTexture = new Texture(Gdx.files.internal("rooms/parede.png"));
            wallFillTexture = new Texture(Gdx.files.internal("rooms/parede_full.png"));
            rugTexture = new Texture(Gdx.files.internal("rooms/tapete_bau.png"));

            System.out.println("✅ Texturas da sala TREASURE carregadas.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar texturas: " + e.getMessage());
            createPlaceholderTextures();
        }
    }

    private void createPlaceholderTextures() {
        floorTexture = createColorTexture(0.3f, 0.8f, 0.3f);
        wallTopTexture = createColorTexture(0.8f, 0.1f, 0.1f);
        wallFillTexture = createColorTexture(0.5f, 0.3f, 0.1f);
    }

    private Texture createColorTexture(float r, float g, float b) {
        com.badlogic.gdx.graphics.Pixmap pix = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pix.setColor(r, g, b, 1f);
        pix.fill();
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (bounds == null)
            return;

        renderFloor(batch, offsetX, offsetY);
        renderRug(batch, offsetX, offsetY);
        renderWalls(batch, offsetX, offsetY);
        renderChests(batch, offsetX, offsetY);
    }

    private void renderFloor(SpriteBatch batch, float offsetX, float offsetY) {
        int startX = (int) bounds.x;
        int startY = (int) bounds.y;
        int width = (int) bounds.width;
        int height = (int) bounds.height;

        float screenX = offsetX + startX * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - (startY + height - 1)) * tileSize;

        float roomWidthPixels = width * tileSize;
        float roomHeightPixels = height * tileSize;

        int texWidth = 100;
        int texHeight = 100;
        float scaleFactor = texWidth / (float) tileSize;
        float repeatX = width / scaleFactor;
        float repeatY = height / scaleFactor;

        TextureRegion floorRegion = new TextureRegion(floorTexture);
        floorRegion.setRegion(0, 0, (int) (texWidth * repeatX), (int) (texHeight * repeatY));
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
        float screenX = offsetX + tileX * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tileY) * tileSize;
        if (isTop) {
            batch.draw(wallTopTexture, screenX, screenY, tileSize, tileSize);
        } else {
            batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
        }
    }

    private void renderChests(SpriteBatch batch, float offsetX, float offsetY) {
        for (Chest chest : mapa.getChests()) {
            Vector2 worldPos = chest.getPosition(); // posição do corpo no mundo
            Vector2 tilePos = mapa.worldToTile(worldPos); // converte para tile
            if (bounds.contains(tilePos.x, tilePos.y)) {
                chest.render(batch, offsetX, offsetY, tileSize);
            }
        }
    }

    private void renderRug(SpriteBatch batch, float offsetX, float offsetY) {
        if (rugTexture == null)
            return;

        Vector2 localChest = treasureRoom.getChestPosition();
        float tileCenterX = bounds.x + localChest.x + 0.5f;
        float tileCenterY = bounds.y + localChest.y + 0.5f;

        float screenX = offsetX + tileCenterX * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tileCenterY) * tileSize;

        float scale = 1.2f;
        float rugWidth = 166 * scale;
        float rugHeight = 145 * scale;

        float verticalOffset = -tileSize * 0.8f;

        float drawX = screenX - rugWidth / 2f;
        float drawY = screenY - rugHeight / 2f - verticalOffset;

        batch.draw(rugTexture, drawX, drawY, rugWidth, rugHeight);
    }

    public void dispose() {
        if (floorTexture != null)
            floorTexture.dispose();
        if (wallTopTexture != null)
            wallTopTexture.dispose();
        if (wallFillTexture != null)
            wallFillTexture.dispose();
        if (rugTexture != null)
            rugTexture.dispose();
    }
}