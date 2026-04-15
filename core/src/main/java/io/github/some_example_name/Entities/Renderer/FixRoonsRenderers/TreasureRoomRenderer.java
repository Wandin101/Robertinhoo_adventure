package io.github.some_example_name.Entities.Renderer.FixRoonsRenderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Interatibles.Chest;
import io.github.some_example_name.Luz.VignetteRenderer;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;
import io.github.some_example_name.MapConfig.Generator.TreasureRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreasureRoomRenderer {
    private FixedRoom treasureFixedRoom;
    private TreasureRoom treasureRoom;
    private Mapa mapa;
    private int tileSize;
    private Rectangle bounds;

    private Texture floorSheet; // sprite sheet 4x1
    private TextureRegion[] floorTiles = new TextureRegion[4];
    private Texture wallTopTexture;
    private Texture wallFillTexture;
    private Texture rugTexture;

    private Texture boneSheet;
    private Texture bloodSheet;
    private TextureRegion[] boneFrames;
    private TextureRegion[] bloodFrames;
    private Random decoRandom;
    private List<Decoration> decorations = new ArrayList<>();
    private VignetteRenderer vignette;
    private FixedRoomBaseRenderer baseRenderer;

    public TreasureRoomRenderer(FixedRoom treasureFixedRoom, TreasureRoom treasureRoom, Mapa mapa, int tileSize) {
        this.treasureFixedRoom = treasureFixedRoom;
        this.treasureRoom = treasureRoom;
        this.mapa = mapa;
        this.tileSize = tileSize;
        this.bounds = treasureFixedRoom.getBounds();

        loadTextures();
        loadDecorations();
        generateDecorations();
        vignette = new VignetteRenderer(0.6f, 1.8f, VignetteRenderer.Shape.ELLIPSE);
        this.baseRenderer = new FixedRoomBaseRenderer(mapa, tileSize);
    }

    private void loadTextures() {
        try {
            floorSheet = new Texture(Gdx.files.internal("rooms/chão_salas_fixas.png"));
            for (int i = 0; i < 4; i++) {
                floorTiles[i] = new TextureRegion(floorSheet, i * 64, 0, 64, 64);
            }

            wallTopTexture = new Texture(Gdx.files.internal("rooms/parede.png"));
            wallFillTexture = new Texture(Gdx.files.internal("rooms/parede_full.png"));
            rugTexture = new Texture(Gdx.files.internal("rooms/tapete_bau.png"));

            System.out.println("✅ Texturas da sala TREASURE carregadas.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar texturas: " + e.getMessage());
            createPlaceholderTextures();
        }
    }

    private void loadDecorations() {
        try {
            // Carrega sprite sheet de ossos (5 frames em uma linha, 32x32 cada)
            boneSheet = new Texture(Gdx.files.internal("rooms/ossos.png"));
            boneFrames = new TextureRegion[5];
            for (int i = 0; i < 5; i++) {
                boneFrames[i] = new TextureRegion(boneSheet, i * 32, 0, 32, 32);
            }

            // Carrega sprite sheet de sangue (3 frames em uma linha, 32x32 cada)
            bloodSheet = new Texture(Gdx.files.internal("ParticulasSangue/bloods.png"));
            bloodFrames = new TextureRegion[3];
            for (int i = 0; i < 3; i++) {
                bloodFrames[i] = new TextureRegion(bloodSheet, i * 32, 0, 32, 32);
            }

            System.out.println("✅ Decorações (ossos e sangue) carregadas via sprite sheet.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar decorações: " + e.getMessage());
        }
    }

    private static class Decoration {
        TextureRegion region;
        float x, y;

        Decoration(TextureRegion region, float x, float y) {
            this.region = region;
            this.x = x;
            this.y = y;
        }
    }

    private void createPlaceholderTextures() {

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
        baseRenderer.renderFloor(batch, bounds, offsetX, offsetY);
        renderRug(batch, offsetX, offsetY);
        renderDecorations(batch, offsetX, offsetY);
        baseRenderer.renderWalls(batch, bounds, offsetX, offsetY);
        renderChests(batch, offsetX, offsetY);
        vignette.render(batch, bounds, offsetX, offsetY, tileSize, mapa.mapHeight);
    }

    private void renderFloor(SpriteBatch batch, float offsetX, float offsetY) {
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

                // 4x4 subdivisões = 16 mini-tiles
                int subDiv = 4;
                for (int subX = 0; subX < subDiv; subX++) {
                    for (int subY = 0; subY < subDiv; subY++) {
                        float screenX = baseScreenX + subX * (tileSize / (float) subDiv);
                        float screenY = baseScreenY + subY * (tileSize / (float) subDiv);

                        // Coordenadas globais do subtile (escala 4)
                        int globalSubX = wx * subDiv + subX;
                        int globalSubY = wy * subDiv + subY;

                        int hash = (globalSubX * 73856093) ^ (globalSubY * 19349663);
                        hash = hash & 0x7fffffff;
                        int index = hash % floorTiles.length;

                        // Desenha com metade do tamanho do tile (16px se tileSize=64)
                        batch.draw(floorTiles[index], screenX, screenY, tileSize / (float) subDiv,
                                tileSize / (float) subDiv);
                    }
                }
            }
        }
    }

    private void renderDecorations(SpriteBatch batch, float offsetX, float offsetY) {
        Vector2 localChest = treasureRoom.getChestPosition();
        float tileCenterX = bounds.x + localChest.x + 0.5f;
        float tileCenterY = bounds.y + localChest.y + 0.5f;

        float screenCenterX = offsetX + tileCenterX * tileSize;
        float screenCenterY = offsetY + (mapa.mapHeight - 1 - tileCenterY) * tileSize;

        for (Decoration dec : decorations) {
            float x = screenCenterX + dec.x;
            float y = screenCenterY + dec.y;
            batch.draw(dec.region, x, y + 55f, 32, 32);
        }
    }

    private void renderChests(SpriteBatch batch, float offsetX, float offsetY) {
        for (Chest chest : mapa.getChests()) {
            Vector2 worldPos = chest.getPosition();
            Vector2 tilePos = mapa.worldToTile(worldPos);
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

        float scale = 1.5f;
        float rugWidth = 166 * scale;
        float rugHeight = 145 * scale;

        float verticalOffset = -tileSize * 0.8f;

        float drawX = screenX - rugWidth / 2f;
        float drawY = screenY - rugHeight / 2f - verticalOffset;

        batch.draw(rugTexture, drawX, drawY, rugWidth, rugHeight);
    }

    private void generateDecorations() {
        Vector2 localChest = treasureRoom.getChestPosition();
        float tileCenterX = bounds.x + localChest.x + 0.5f;
        float tileCenterY = bounds.y + localChest.y + 0.5f;

        // Semente fixa para consistência entre execuções
        long seed = (long) (bounds.x * 1000 + bounds.y);
        Random rand = new Random(seed);

        float[] boneAngles = new float[5];
        for (int i = 0; i < 5; i++) {
            boneAngles[i] = (float) (i * 2 * Math.PI / 5);
        }

        shuffleArray(boneAngles, rand);

        float boneDistance = 85f;

        for (int i = 0; i < 5; i++) {
            // Cada frame usado exatamente uma vez
            TextureRegion frame = boneFrames[i];
            float angle = boneAngles[i];
            float x = (float) Math.cos(angle) * boneDistance;
            float y = (float) Math.sin(angle) * boneDistance;
            // Ajuste para centralizar o sprite (32x32)
            decorations.add(new Decoration(frame, x - 16, y - 16));
        }

        float[] bloodAngles = new float[3];
        for (int i = 0; i < 3; i++) {
            bloodAngles[i] = (float) (i * 2 * Math.PI / 3);
        }
        shuffleArray(bloodAngles, rand);

        float bloodDistance = 50f;

        for (int i = 0; i < 3; i++) {
            TextureRegion frame = bloodFrames[i];
            float angle = bloodAngles[i];
            float x = (float) Math.cos(angle) * bloodDistance;
            float y = (float) Math.sin(angle) * bloodDistance;
            decorations.add(new Decoration(frame, x - 16, y - 16));
        }

        System.out.println("✅ Decorações geradas: " + decorations.size() + " itens.");
    }

    private void shuffleArray(float[] array, Random rand) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            float temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    public void dispose() {
        if (floorSheet != null)
            floorSheet.dispose();
        if (wallTopTexture != null)
            wallTopTexture.dispose();
        if (wallFillTexture != null)
            wallFillTexture.dispose();
        if (rugTexture != null)
            rugTexture.dispose();
        if (boneSheet != null)
            boneSheet.dispose();
        if (bloodSheet != null)
            bloodSheet.dispose();
        if (vignette != null)
            vignette.dispose();
        baseRenderer.dispose();
    }
}