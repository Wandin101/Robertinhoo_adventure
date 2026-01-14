package io.github.some_example_name.MapConfig.Spawner;

import java.util.Random;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;

import io.github.some_example_name.Entities.Itens.CenarioItens.Grass;
import io.github.some_example_name.MapConfig.Mapa;

public class GrassSpawner {

    public static void spawnGrass(Mapa mapa, int grassCount) {
        Random rand = new Random();
        int grassSpawned = 0;

        for (Rectangle room : mapa.getRooms()) {
            if (!roomAllowsVegetation(mapa, room)) {
                continue;
            }
            
            for (int x = (int) room.x + 1; x < room.x + room.width - 1 && grassSpawned < grassCount; x++) {
                for (int y = (int) room.y + 1; y < room.y + room.height - 1 && grassSpawned < grassCount; y++) {

                    // Verifica se é um tile válido (chão)
                    if (mapa.tiles[x][y] == Mapa.TILE) {

                        // Chance de spawn (20% para não ficar muito denso)
                        if (rand.nextFloat() < 0.2f) {
                            Vector2 worldPos = mapa.tileToWorld(x, y);

                            // Cria a grama
                            Grass grass = new Grass(mapa, worldPos.x, worldPos.y);
                            mapa.getDestructibles().add(grass);

                            grassSpawned++;
                        }
                    }
                }
            }
        }
        
        Gdx.app.log("GrassSpawner", "✅ " + grassSpawned + " gramas spawnadas");
    }

    
   private static boolean roomAllowsVegetation(Mapa mapa, Rectangle room) {
        // Grama não é configurada no RoomConfiguration, mas podemos usar a mesma lógica
        // para sala de spawn não ter grama
        if (mapa.mapGenerator != null && mapa.mapGenerator.isSpawnRoomTile((int)room.x, (int)room.y)) {
            return false; // Sala de spawn não tem grama
        }
        return true; // Outras salas têm grama
    }
    // Método alternativo para spawn mais controlado
    public static void spawnGrassInArea(Mapa mapa, Rectangle area, int density) {
        Random rand = new Random();
        int grassInArea = 0;
        int maxGrass = (int) (area.width * area.height * density / 100f);

        for (int i = 0; i < maxGrass; i++) {
            float randomX = area.x + rand.nextFloat() * area.width;
            float randomY = area.y + rand.nextFloat() * area.height;

            int tileX = (int) randomX;
            int tileY = (int) randomY;

            // Verifica se está dentro dos limites do mapa e é tile válido
            if (tileX >= 0 && tileX < mapa.mapWidth &&
                    tileY >= 0 && tileY < mapa.mapHeight &&
                    mapa.tiles[tileX][tileY] == Mapa.TILE) {

                Vector2 worldPos = mapa.tileToWorld(tileX, tileY);
                Grass grass = new Grass(mapa, worldPos.x, worldPos.y);
                mapa.getDestructibles().add(grass);
                grassInArea++;
            }
        }

    }
}