package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Luz.SistemaSombras;
import io.github.some_example_name.MapConfig.Mapa;
public class Room0WallRenderer {
    private Mapa mapa;
    private Texture wallTopTexture;
    private Texture wallFillTexture;
    private int tileSize;
    private SistemaSombras sistemaSombras;

    public Room0WallRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;
        this.sistemaSombras = new SistemaSombras();
        loadWallTextures();
        debugMapState();
    }

    private void loadWallTextures() {
        try {
            wallTopTexture = new Texture(Gdx.files.internal("sala_0/wall_top.png"));
            wallFillTexture = new Texture(Gdx.files.internal("sala_0/wall_fill.png"));
            System.out.println("✅ Texturas das paredes da sala 0 carregadas");
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar texturas das paredes: " + e.getMessage());
            createPlaceholderTextures();
        }
    }

    private void createPlaceholderTextures() {
        // Placeholder para wall_top (vermelho)
        com.badlogic.gdx.graphics.Pixmap pixmapTop = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapTop.setColor(0.8f, 0.1f, 0.1f, 1f);
        pixmapTop.fill();
        wallTopTexture = new Texture(pixmapTop);
        pixmapTop.dispose();

        // Placeholder para wall_fill (azul)
        com.badlogic.gdx.graphics.Pixmap pixmapFill = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapFill.setColor(0.1f, 0.1f, 0.8f, 1f);
        pixmapFill.fill();
        wallFillTexture = new Texture(pixmapFill);
        pixmapFill.dispose();
    }

    /**
     * Renderiza todas as paredes da sala 0 incluindo camadas externas
     */
    public void renderWalls(SpriteBatch batch, float offsetX, float offsetY) {
        if (wallTopTexture == null || wallFillTexture == null) {
            System.err.println("❌ Texturas das paredes não carregadas");
            return;
        }

        // 1. Primeiro renderiza as paredes internas do mapa
        renderInternalWalls(batch, offsetX, offsetY);

        // 2. Depois renderiza as paredes externas (camada de fundo)
        renderExternalWalls(batch, offsetX, offsetY);
    }

    /**
     * Renderiza as paredes internas (dentro dos limites do mapa)
     */
    private void renderInternalWalls(SpriteBatch batch, float offsetX, float offsetY) {
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    renderWallTile(batch, x, y, offsetX, offsetY, false);
                }
            }
        }
    }

    /**
     * Renderiza as paredes externas (fora dos limites do mapa)
     */
    private void renderExternalWalls(SpriteBatch batch, float offsetX, float offsetY) {
        // Define quantas camadas externas queremos renderizar
        int externalLayers = 2;

        // Renderiza camadas externas ao redor do mapa
        for (int layer = 1; layer <= externalLayers; layer++) {
            // TOPO EXTERNO (acima do topo visual)
            for (int x = -layer; x < mapa.mapWidth + layer; x++) {
                int y = -layer; // Acima do topo visual (y=0)
                renderWallTile(batch, x, y, offsetX, offsetY, true);
            }

            // BASE EXTERNA (abaixo da base visual)
            for (int x = -layer; x < mapa.mapWidth + layer; x++) {
                int y = mapa.mapHeight - 1 + layer; // Abaixo da base visual
                renderWallTile(batch, x, y, offsetX, offsetY, true);
            }

            // LATERAIS EXTERNAS (esquerda e direita)
            for (int y = -layer + 1; y < mapa.mapHeight + layer - 1; y++) {
                // Esquerda externa
                int xLeft = -layer;
                renderWallTile(batch, xLeft, y, offsetX, offsetY, true);

                // Direita externa
                int xRight = mapa.mapWidth - 1 + layer;
                renderWallTile(batch, xRight, y, offsetX, offsetY, true);
            }
        }
    }

    /**
     * Renderiza um tile de parede individual
     */
    private void renderWallTile(SpriteBatch batch, int tileX, int tileY, float offsetX, float offsetY,
            boolean isExternal) {
        Vector2 worldPos = mapa.tileToWorld(tileX, tileY);
        float screenX = offsetX + worldPos.x * tileSize - tileSize / 2;
        float screenY = offsetY + worldPos.y * tileSize - tileSize / 2;

        // Para paredes externas, sempre usa wall_fill
        if (isExternal) {
            renderExternalWallTile(batch, tileX, tileY, screenX, screenY);
        } else {
            // Para paredes internas, usa a lógica normal
            renderInternalWallTile(batch, tileX, tileY, screenX, screenY);
        }
    }

    /**
     * Renderiza uma parede interna (com lógica de bordas)
     */
    private void renderInternalWallTile(SpriteBatch batch, int tileX, int tileY, float screenX, float screenY) {
        WallType wallType = getWallType(tileX, tileY);

        switch (wallType) {
            case TOP:
                // Verifica se é um tile do topo que precisa ser rotacionado (extremidade)
                if (isTopEdgeTile(tileX, tileY)) {
                    // Se for extremidade, usa wall_fill rotacionado apropriadamente
                    renderTopEdgeTile(batch, tileX, tileY, screenX, screenY);
                } else {
                    // Tile normal do topo
                    batch.draw(wallTopTexture, screenX, screenY, tileSize, tileSize);
                }
                break;

            case LEFT_SIDE:
                TextureRegion leftRegion = new TextureRegion(wallFillTexture);
                batch.draw(leftRegion, screenX, screenY,
                        tileSize / 2, tileSize / 2,
                        tileSize, tileSize,
                        1, 1,
                        90);
                break;

            case RIGHT_SIDE:
                TextureRegion rightRegion = new TextureRegion(wallFillTexture);
                batch.draw(rightRegion, screenX, screenY,
                        tileSize / 2, tileSize / 2,
                        tileSize, tileSize,
                        1, 1,
                        -90);
                break;

            case BOTTOM:
            case INTERNAL:
            default:
                batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
                break;
        }
    }

    /**
     * NOVO: Verifica se um tile do topo é uma extremidade que precisa de tratamento especial
     */
    private boolean isTopEdgeTile(int tileX, int tileY) {
        // Só aplica para tiles do topo
        if (tileY != 0) return false;
        
        // Verifica se é o primeiro tile do topo (canto esquerdo superior)
        if (tileX == 0) return true;
        
        // Verifica se é o último tile do topo (canto direito superior)
        if (tileX == mapa.mapWidth - 1) return true;
        
        // Verifica se tem tile do topo adjacente (se não tiver, é uma extremidade)
        boolean hasLeftNeighbor = (tileX > 0) && (mapa.tiles[tileX - 1][tileY] == Mapa.PAREDE);
        boolean hasRightNeighbor = (tileX < mapa.mapWidth - 1) && (mapa.tiles[tileX + 1][tileY] == Mapa.PAREDE);
        
        // Se não tiver ambos os vizinhos, é uma extremidade
        return !(hasLeftNeighbor && hasRightNeighbor);
    }

    /**
     * NOVO: Renderiza um tile do topo que está na extremidade
     */
    private void renderTopEdgeTile(SpriteBatch batch, int tileX, int tileY, float screenX, float screenY) {
        TextureRegion region = new TextureRegion(wallFillTexture);
        
        // Determina a rotação baseada na posição
        if (tileX == 0) {
            // Canto esquerdo superior - rotaciona 90 graus
            batch.draw(region, screenX, screenY,
                    tileSize / 2, tileSize / 2,
                    tileSize, tileSize,
                    1, 1,
                    90);
        } else if (tileX == mapa.mapWidth - 1) {
            // Canto direito superior - rotaciona -90 graus
            batch.draw(region, screenX, screenY,
                    tileSize / 2, tileSize / 2,
                    tileSize, tileSize,
                    1, 1,
                    -90);
        } else {
            // Tile do topo isolado (sem vizinhos em ambos os lados)
            // Usa uma rotação que faça sentido visualmente
            // Pode ser 90 ou -90 dependendo do contexto do seu jogo
            // Vou usar 90 como padrão, mas você pode ajustar
            batch.draw(region, screenX, screenY,
                    tileSize / 2, tileSize / 2,
                    tileSize, tileSize,
                    1, 1,
                    90);
        }
    }

    /**
     * Renderiza uma parede externa (sempre wall_fill com orientação apropriada)
     */
    private void renderExternalWallTile(SpriteBatch batch, int tileX, int tileY, float screenX, float screenY) {
        // Determina a orientação baseado na posição relativa ao mapa
        boolean isTopExternal = tileY < 0;
        boolean isBottomExternal = tileY >= mapa.mapHeight;
        boolean isLeftExternal = tileX < 0;
        boolean isRightExternal = tileX >= mapa.mapWidth;

        if (isLeftExternal) {
            // LATERAL ESQUERDA EXTERNA: wall_fill rotacionado 90°
            TextureRegion leftRegion = new TextureRegion(wallFillTexture);
            batch.draw(leftRegion, screenX, screenY,
                    tileSize / 2, tileSize / 2,
                    tileSize, tileSize,
                    1, 1,
                    90);
        } else if (isRightExternal) {
            // LATERAL DIREITA EXTERNA: wall_fill rotacionado -90°
            TextureRegion rightRegion = new TextureRegion(wallFillTexture);
            batch.draw(rightRegion, screenX, screenY,
                    tileSize / 2, tileSize / 2,
                    tileSize, tileSize,
                    1, 1,
                    -90);
        } else if (isTopExternal || isBottomExternal) {
            // TOPO/BASE EXTERNA: wall_fill normal (sem rotação)
            batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
        } else {
            // Caso padrão (não deveria acontecer para paredes externas)
            batch.draw(wallFillTexture, screenX, screenY, tileSize, tileSize);
        }
    }

    /**
     * Define os tipos de parede baseado na posição (apenas para paredes internas)
     */
    private WallType getWallType(int tileX, int tileY) {
        // TOPO VISUAL: borda superior (y = 0)
        if (tileY == 0) {
            return WallType.TOP;
        }

        // FUNDO/BASE VISUAL: borda inferior (y = mapa.mapHeight - 1)
        if (tileY == mapa.mapHeight - 1) {
            return WallType.BOTTOM;
        }

        // LATERAIS: bordas esquerda/direita
        if (tileX == 0) {
            return WallType.LEFT_SIDE;
        }
        if (tileX == mapa.mapWidth - 1) {
            return WallType.RIGHT_SIDE;
        }

        // INTERNO: qualquer outra parede
        return WallType.INTERNAL;
    }

    /**
     * Enum para tipos de parede
     */
    private enum WallType {
        TOP,        // Topo visual (y=0) - wall_top
        LEFT_SIDE,  // Lateral esquerda (rotacionada)
        RIGHT_SIDE, // Lateral direita (rotacionada)
        BOTTOM,     // Fundo/base visual (y=max) - wall_fill normal
        INTERNAL    // Interno (fill normal)
    }

    /**
     * Método de debug para verificar o estado do mapa
     */
    public void debugMapState() {
        int wallCount = 0;
        int topCount = 0, leftCount = 0, rightCount = 0, bottomCount = 0, internalCount = 0;
        int topEdgeCount = 0;

        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    wallCount++;
                    WallType type = getWallType(x, y);
                    
                    switch (type) {
                        case TOP:
                            topCount++;
                            if (isTopEdgeTile(x, y)) {
                                topEdgeCount++;
                            }
                            break;
                        case LEFT_SIDE:
                            leftCount++;
                            break;
                        case RIGHT_SIDE:
                            rightCount++;
                            break;
                        case BOTTOM:
                            bottomCount++;
                            break;
                        case INTERNAL:
                            internalCount++;
                            break;
                    }
                }
            }
        }

        System.out.println("=== DEBUG ROOM 0 WALLS (SISTEMA VISUAL) ===");
        System.out.println("🗺️ Tamanho do Mapa: " + mapa.mapWidth + "x" + mapa.mapHeight);
        System.out.println("🧱 Total de tiles de parede internos: " + wallCount);
        System.out.println("🔺 Topo VISUAL (y=0): " + topCount);
        System.out.println("🔺🔺 Topo Edge (extremidades): " + topEdgeCount);
        System.out.println("⬇️ Base/Fundo VISUAL (y=" + (mapa.mapHeight - 1) + "): " + bottomCount);
        System.out.println("⬅️ Lateral Esquerda (x=0): " + leftCount);
        System.out.println("➡️ Lateral Direita (x=" + (mapa.mapWidth - 1) + "): " + rightCount);
        System.out.println("🔲 Interno: " + internalCount);
        System.out.println("🎨 Renderizando 2 camadas externas de paredes de fundo");
        System.out.println("===========================================");
    }

    public void dispose() {
        if (wallTopTexture != null) {
            wallTopTexture.dispose();
        }
        if (wallFillTexture != null) {
            wallFillTexture.dispose();
        }
        if (sistemaSombras != null) {
            sistemaSombras.dispose();
        }
    }
}