package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.Items_sala_0.CampFire;
public class Room0LayoutLoader {
    // Cores para identificar elementos no layout
    public static final Color COLOR_CAMPFIRE = new Color(0x99e550ff);
    public static final Color COLOR_PLAYER_SPAWN = new Color(0xff0000ff);

    public static void loadRoom0Specifics(Mapa mapa, String imagePath) {
        try {
            Pixmap pixmap = new Pixmap(Gdx.files.internal(imagePath));
            
            int layoutWidth = pixmap.getWidth();
            int layoutHeight = pixmap.getHeight();
            
            System.out.println("Layout da sala 0: " + layoutWidth + "x" + layoutHeight + " pixels");
            System.out.println("Tamanho da sala: " + mapa.mapWidth + "x" + mapa.mapHeight + " tiles");
            
            Vector2 playerSpawnPos = null;
            
            // Percorre cada pixel da imagem
            for (int x = 0; x < layoutWidth; x++) {
                for (int y = 0; y < layoutHeight; y++) {
                    Color pixelColor = new Color();
                    Color.rgba8888ToColor(pixelColor, pixmap.getPixel(x, y));
                    
                    // CORREÇÃO: Usa tileToWorld do mapa para coordenadas corretas
                    Vector2 worldPos = mapa.tileToWorld(x, y);
                    
                    // Verifica as cores
                    if (colorsMatch(pixelColor, COLOR_PLAYER_SPAWN)) {
                        playerSpawnPos = worldPos;
                        System.out.println("🎯 Spawn em pixel: " + x + "," + y + " -> mundo: " + worldPos);
                    } else if (colorsMatch(pixelColor, COLOR_CAMPFIRE)) {
                        addCampfire(mapa, worldPos.x, worldPos.y);
                        System.out.println("🔥 Fogueira em pixel: " + x + "," + y + " -> mundo: " + worldPos);
                    }
                }
            }
            
            // Posiciona o player
            if (playerSpawnPos != null && mapa.robertinhoo != null) {
                mapa.robertinhoo.body.setTransform(playerSpawnPos, 0);
                mapa.robertinhoo.pos.set(playerSpawnPos);
                System.out.println("✅ Player posicionado");
            }
            
            pixmap.dispose();
            System.out.println("✅ Elementos carregados!");
            
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }
    
    
    private static boolean colorsMatch(Color color1, Color color2) {
        return Math.abs(color1.r - color2.r) < 0.1f &&
               Math.abs(color1.g - color2.g) < 0.1f &&
               Math.abs(color1.b - color2.b) < 0.1f;
    }
    
    private static void addCampfire(Mapa mapa, float x, float y) {
        CampFire campfire = new CampFire( x, y);
        mapa.setCampFire(campfire);
        System.out.println("📍 Fogueira em mundo: " + x + ", " + y);
    }
}