package io.github.some_example_name.MapConfig.Rooms;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class Room0TileRenderer {
    private Mapa mapa;
    private Texture floorTexture;
    private int tileSize;
    
    public Room0TileRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;
        loadFloorTexture();
    }
    
   private void loadFloorTexture() {
        try {
            floorTexture = new Texture(Gdx.files.internal("sala_0/SOLO_125PX.png"));
            System.out.println("✅ Textura do chão da sala 0 carregada: " + 
                floorTexture.getWidth() + "x" + floorTexture.getHeight() +
                " | Tiles: " + mapa.mapWidth + "x" + mapa.mapHeight);
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar textura do chão da sala 0: " + e.getMessage());
            createPlaceholderTexture();
        }
    }
    
    private void createPlaceholderTexture() {
        // Cria um placeholder simples
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(125, 125, 
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.2f, 0.3f, 1f); // Azul escuro para diferenciar
        pixmap.fill();
        floorTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Renderiza APENAS o chão da sala 0 (sobrescrevendo o chão padrão)
     */
 public void renderFloor(SpriteBatch batch, float offsetX, float offsetY) {
        if (floorTexture == null) {
            System.err.println("❌ floorTexture é null!");
            return;
        }
        
        // Renderiza o chão especial em todos os tiles que são chão
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.TILE) {
                    Vector2 worldPos = mapa.tileToWorld(x, y);
                    float screenX = offsetX + worldPos.x * tileSize - tileSize/2;
                    float screenY = offsetY + worldPos.y * tileSize - tileSize/2;
                    
                    batch.draw(floorTexture, screenX, screenY, tileSize, tileSize);
                }
            }
        }
        
    }
    
    public void dispose() {
        if (floorTexture != null) {
            floorTexture.dispose();
        }
    }
}