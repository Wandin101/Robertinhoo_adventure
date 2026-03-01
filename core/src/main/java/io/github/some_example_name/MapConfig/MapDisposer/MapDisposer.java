package io.github.some_example_name.MapConfig.MapDisposer;

import com.badlogic.gdx.Gdx;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.MapConfig.Mapa;

/**
 * Responsável por realizar a limpeza completa dos recursos de um mapa
 * quando ele for descartado (ex: ao trocar de sala).
 */
public class MapDisposer {

    public static void disposeSafely(Mapa mapa) {
        System.out.println("🧹 [MapDisposer] disposeSafely iniciado para mapa " + mapa.mapaId);

        // 1. Limpar referências de listener
        mapa.roomTransitionListener = null;

        // 2. Parar todos os sons
        AudioManager.getInstance().stopAllAmbientSounds();

        // 3. Destruir corpos de todos os itens ANTES de destruir o mundo
        System.out.println("💥 Destruindo corpos de todos os itens...");

        // CraftItems
        for (Item item : mapa.craftItems) {
            if (item.getBody() != null) {
                try {
                    item.destroyBody();
                } catch (Exception e) {
                    System.err.println("Erro ao destruir corpo de craftItem: " + e.getMessage());
                }
            }
        }

        // Weapons
        for (Weapon weapon : mapa.weapons) {
            if (weapon.getBody() != null) {
                try {
                    weapon.destroyBody();
                } catch (Exception e) {
                    System.err.println("Erro ao destruir corpo de weapon: " + e.getMessage());
                }
            }
        }

        // Ammo
        for (Ammo ammo : mapa.ammo) {
            if (ammo.getBody() != null) {
                try {
                    ammo.destroyBody();
                } catch (Exception e) {
                    System.err.println("Erro ao destruir corpo de ammo: " + e.getMessage());
                }
            }
        }

        // 4. Destruir RayHandler
        if (mapa.rayHandler != null) {
            try {
                mapa.rayHandler.dispose();
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao dispor rayHandler: " + e.getMessage());
            }
            mapa.rayHandler = null;
        }

        // 5. Destruir o World (isso destrói quaisquer corpos restantes)
        if (mapa.world != null) {
            System.out.println("💥 Destruindo World...");
            try {
                mapa.world.dispose();
                System.out.println("✅ World destruído");
            } catch (Exception e) {
                System.err.println("❌ ERRO ao dispor world: " + e.getMessage());
            }
            mapa.world = null;
        }

        // 6. Limpar todas as listas
        clearAllLists(mapa);

        System.out.println("✅ [MapDisposer] disposeSafely completo");
    }

    private static void clearAllLists(Mapa mapa) {
        // CampFire
        if (mapa.campFire != null) {
            mapa.campFire.dispose();
            mapa.campFire = null;
        }

        // Enemies
        if (mapa.enemies != null) {
            for (Enemy enemy : mapa.enemies) {
                if (enemy.getBody() != null) {
                    enemy.getBody().setUserData(null);
                }
            }
            mapa.enemies.clear();
        }

        // Weapons
        if (mapa.weapons != null) {
            for (Weapon weapon : mapa.weapons) {
                if (weapon.getBody() != null) {
                    weapon.getBody().setUserData(null);
                }
            }
            mapa.weapons.clear();
        }

        // Ammo
        if (mapa.ammo != null) {
            for (Ammo ammoItem : mapa.ammo) {
                if (ammoItem.getBody() != null) {
                    ammoItem.getBody().setUserData(null);
                }
            }
            mapa.ammo.clear();
        }

        // Projectiles
        if (mapa.projectiles != null) {
            for (Projectile projectile : mapa.projectiles) {
                if (projectile.getBody() != null) {
                    projectile.getBody().setUserData(null);
                }
            }
            mapa.projectiles.clear();
        }

        // Destructibles
        if (mapa.destructibles != null) {
            for (Destructible destructible : mapa.destructibles) {
                if (destructible.getBody() != null) {
                    destructible.getBody().setUserData(null);
                }
            }
            mapa.destructibles.clear();
        }

        // Outras listas
        if (mapa.polvoras != null)
            mapa.polvoras.clear();
        if (mapa.craftItems != null)
            mapa.craftItems.clear();
        if (mapa.pendingActions != null)
            mapa.pendingActions.clear();
        if (mapa.rooms != null)
            mapa.rooms.clear();
        if (mapa.cabanas != null)
            mapa.cabanas.clear();
        if (mapa.staticItems != null)
            mapa.staticItems.clear();
        if (mapa.wallPositions != null)
            mapa.wallPositions.clear();
    }
}