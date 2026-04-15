package io.github.some_example_name.MapConfig.Spawner;

import io.github.some_example_name.Entities.SoulShopSystem.Soul;
import io.github.some_example_name.MapConfig.Mapa;
import java.util.Random;

public class SoulSpawner {
    public static void spawnAroundChest(Mapa mapa, float chestTileX, float chestTileY, int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            double angle = i * 2 * Math.PI / count;
            int offsetX = (int) Math.round(Math.cos(angle) * 1.8);
            int offsetY = (int) Math.round(Math.sin(angle) * 1.8);
            Soul soul = new Soul(mapa, chestTileX + offsetX, chestTileY + offsetY);
            mapa.addSoul(soul);
        }
    }
}