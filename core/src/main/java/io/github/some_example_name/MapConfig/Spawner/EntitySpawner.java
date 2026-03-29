package io.github.some_example_name.MapConfig.Spawner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraReforcada;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Itens.Weapon.Calibre12.Calibre12;
import io.github.some_example_name.Entities.Itens.Weapon.DesertEagle.DesertEagle;
import io.github.some_example_name.Entities.Itens.Weapon.Revolver.Revolver;
import io.github.some_example_name.MapConfig.Spawner.BarrelSpawner;
import io.github.some_example_name.MapConfig.Spawner.GrassSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;

/**
 * Responsável por gerar todas as entidades dinâmicas do mapa:
 * inimigos, itens, barris, grama, etc.
 */
public class EntitySpawner {

    private final Mapa mapa;
    private final Random random = new Random();

    public EntitySpawner(Mapa mapa) {
        this.mapa = mapa;
    }

    /**
     * Método principal: decide o que spawnar baseado no tipo de mapa.
     */
    public void spawnAll() {
        if (mapa.isRoom0) {
            spawnRoom0TestEntities();
        } else {
            spawnRandomEntities();
        }
    }

    // ----------------------------------------------------------------------
    // SPAWN PARA MAPAS PROCEDURAIS (SALAS ALEATÓRIAS)
    // ----------------------------------------------------------------------

    private void spawnRandomEntities() {
        List<Vector2> validPositions = collectValidTilePositions();

        // Embaralha para distribuição aleatória
        java.util.Collections.shuffle(validPositions, random);

        spawnItems(validPositions);
        spawnRats(validPositions);
        spawnCastors(validPositions);

        // Spawn de barris e grama (já são classes separadas)
        BarrelSpawner.spawnBarrels(mapa, 10);
        GrassSpawner.spawnGrass(mapa, 80);
    }

    private List<Vector2> collectValidTilePositions() {
        List<Vector2> positions = new ArrayList<>();
        for (Rectangle room : mapa.rooms) {
            for (int x = (int) room.x + 1; x < room.x + room.width - 1; x++) {
                for (int y = (int) room.y + 1; y < room.y + room.height - 1; y++) {
                    if (mapa.tiles[x][y] == Mapa.TILE) {
                        // Não spawna na posição inicial do jogador
                        if (x != (int) mapa.startPosition.x || y != (int) mapa.startPosition.y) {
                            positions.add(new Vector2(x, y));
                        }
                    }
                }
            }
        }
        return positions;
    }

    private void spawnItems(List<Vector2> validPositions) {
        int itemsSpawned = 0;
        for (int i = 0; i < validPositions.size() && itemsSpawned < 3; i++) {
            Vector2 tilePos = validPositions.get(i);
            Rectangle room = mapa.findRoomContainingTile(tilePos);
            if (room != null && roomAllowsItems(room)) {
                Vector2 worldPos = mapa.tileToWorld((int) tilePos.x, (int) tilePos.y);
                if (random.nextBoolean()) {
                    mapa.weapons.add(new Pistol(mapa, worldPos.x, worldPos.y, mapa.robertinhoo.getInventory()));
                    mapa.weapons
                            .add(new Calibre12(mapa, worldPos.x + 1.2f, worldPos.y, mapa.robertinhoo.getInventory()));
                } else {
                    mapa.ammo.add(new Ammo9mm(mapa, worldPos.x, worldPos.y));
                }
                itemsSpawned++;
            }
        }
    }

    private void spawnRats(List<Vector2> validPositions) {
        int ratsAdded = 0;
        for (int i = 0; i < validPositions.size() && ratsAdded < 14; i++) {
            Vector2 tilePos = validPositions.get(i);
            Rectangle room = mapa.findRoomContainingTile(tilePos);
            if (room != null && roomAllowsEnemies(room)) {
                Vector2 worldPos = mapa.tileToWorld((int) tilePos.x, (int) tilePos.y);
                mapa.enemies.add(new Ratinho(mapa, worldPos.x, worldPos.y, mapa.robertinhoo, room));
                ratsAdded++;
            }
        }
    }

    private void spawnCastors(List<Vector2> validPositions) {
        int castoresAdded = 0;
        for (int i = 0; i < validPositions.size() && castoresAdded < 4; i++) {
            Vector2 tilePos = validPositions.get(i);
            Rectangle room = mapa.findRoomContainingTile(tilePos);
            if (room != null && roomAllowsEnemies(room)) {
                Vector2 worldPos = mapa.tileToWorld((int) tilePos.x, (int) tilePos.y);
                mapa.enemies.add(new Castor(mapa, worldPos.x, worldPos.y, mapa.robertinhoo));
                castoresAdded++;
            }
        }
    }

    private boolean roomAllowsEnemies(Rectangle room) {
        if (mapa.mapGenerator == null)
            return true;
        if (mapa.mapGenerator.isSpawnRoomTile((int) room.x, (int) room.y)) {
            FixedRoom spawnRoom = mapa.mapGenerator.getSpawnRoom();
            return spawnRoom != null && spawnRoom.getConfiguration().hasEnemies();
        }
        return true;
    }

    private boolean roomAllowsItems(Rectangle room) {
        if (mapa.mapGenerator == null)
            return true;
        if (mapa.mapGenerator.isSpawnRoomTile((int) room.x, (int) room.y)) {
            return false; // sala do spawn não tem itens
        }
        return true;
    }

    // ----------------------------------------------------------------------
    // SPAWN PARA SALA 0 (TESTES / FIXO)
    // ----------------------------------------------------------------------

    private void spawnRoom0TestEntities() {
        Gdx.app.log("EntitySpawner", "Adicionando entidades de teste na Sala 0");

        spawnTestWeapons();
        // spawnTestEnemies();
        spawnTestCraftItems();
    }

    private void spawnTestWeapons() {
        Vector2 pistolPos = mapa.tileToWorld(3, 3);
        Vector2 calibre12Pos = mapa.tileToWorld(6, 3);
        Vector2 revolverPos = mapa.tileToWorld(9, 3);
        Vector2 desertEaglePos = mapa.tileToWorld(12, 3);

        mapa.weapons.add(new Pistol(mapa, pistolPos.x, pistolPos.y, mapa.robertinhoo.getInventory()));
        mapa.weapons.add(new Calibre12(mapa, calibre12Pos.x, calibre12Pos.y, mapa.robertinhoo.getInventory()));
        mapa.weapons.add(new Revolver(mapa, revolverPos.x, revolverPos.y, mapa.robertinhoo.getInventory()));
        mapa.weapons.add(new DesertEagle(mapa, desertEaglePos.x, desertEaglePos.y, mapa.robertinhoo.getInventory()));
        // Munição
        mapa.ammo.add(new Ammo9mm(mapa, mapa.tileToWorld(3, 5).x, mapa.tileToWorld(3, 5).y));
        mapa.ammo.add(new Ammo9mm(mapa, mapa.tileToWorld(6, 5).x, mapa.tileToWorld(6, 5).y));
    }

    private void spawnTestCraftItems() {
        // 4 Polvoras Reforçadas
        for (int i = 0; i < 4; i++) {
            Vector2 pos = mapa.tileToWorld(4 + i, 7);
            PolvoraReforcada p = new PolvoraReforcada(mapa.world, pos.x, pos.y);
            p.createBody(pos);
            mapa.addCraftItem(p);
        }
        // 4 Polvoras Brutas
        for (int i = 0; i < 4; i++) {
            Vector2 pos = mapa.tileToWorld(4 + i, 8);
            PolvoraBruta p = new PolvoraBruta(mapa.world, pos.x, pos.y);
            p.createBody(pos);
            mapa.addCraftItem(p);
        }
    }

    private void spawnTestEnemies() {
        Rectangle room0Rect = new Rectangle(1, 1, mapa.mapWidth - 2, mapa.mapHeight - 2);

        // Ratos
        float[][] ratPositions = {
                { mapa.startPosition.x + 3, mapa.startPosition.y + 2 },
                { mapa.startPosition.x - 3, mapa.startPosition.y - 2 },
                { mapa.startPosition.x + 2, mapa.startPosition.y - 3 },
                { mapa.startPosition.x - 2, mapa.startPosition.y + 3 }
        };
        for (float[] pos : ratPositions) {
            int tx = (int) pos[0], ty = (int) pos[1];
            if (isValidTile(tx, ty)) {
                Vector2 wp = mapa.tileToWorld(tx, ty);
                mapa.enemies.add(new Ratinho(mapa, wp.x, wp.y, mapa.robertinhoo, room0Rect));
            }
        }

        // Castores
        float[][] castorPositions = {
                { mapa.startPosition.x + 5, mapa.startPosition.y + 1 },
                { mapa.startPosition.x - 5, mapa.startPosition.y - 1 },
                { mapa.startPosition.x + 1, mapa.startPosition.y - 5 },
                { mapa.startPosition.x - 1, mapa.startPosition.y + 5 }
        };
        for (float[] pos : castorPositions) {
            int tx = (int) pos[0], ty = (int) pos[1];
            if (isValidTile(tx, ty)) {
                Vector2 wp = mapa.tileToWorld(tx, ty);
                mapa.enemies.add(new Castor(mapa, wp.x, wp.y, mapa.robertinhoo));
            }
        }
    }

    private boolean isValidTile(int tileX, int tileY) {
        return tileX > 0 && tileX < mapa.mapWidth - 1 &&
                tileY > 0 && tileY < mapa.mapHeight - 1 &&
                mapa.tiles[tileX][tileY] == Mapa.TILE;
    }
}
