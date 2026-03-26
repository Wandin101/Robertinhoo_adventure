package io.github.some_example_name.Entities.Inventory;

import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.MapConfig.Mapa;

public class InventoryMapUpdater {

    /**
     * Atualiza a referência do mapa no InventoryController e em todos os itens
     * do inventário que possuem uma referência a Mapa.
     *
     * @param inventoryController controlador do inventário
     * @param newMap              novo mapa (sala) carregada
     */
    public static void updateInventoryForNewMap(InventoryController inventoryController, Mapa newMap) {
        if (inventoryController == null) {
            System.err.println("⚠️ InventoryMapUpdater: inventoryController é null");
            return;
        }

        // 1. Atualiza a referência no próprio controller
        inventoryController.mapa = newMap;
        System.out.println("✅ InventoryController.mapa atualizado");

        // 2. Obtém o inventário e seus slots
        Inventory inventory = inventoryController.inventory;
        if (inventory == null) {
            System.err.println("⚠️ InventoryMapUpdater: inventory é null");
            return;
        }

        // 3. Percorre todos os slots e atualiza os itens que precisam do mapa
        for (InventorySlot slot : inventory.getSlots()) {
            if (slot.item == null)
                continue;

            if (slot.item instanceof Weapon) {
                Weapon weapon = (Weapon) slot.item;
                weapon.setMapa(newMap);
                System.out.println("   🔫 Arma atualizada: " + weapon.getName());
            } else if (slot.item instanceof Ammo) {
                Ammo ammo = (Ammo) slot.item;
                ammo.setMapa(newMap);
                System.out.println("   💥 Munição atualizada: " + ammo.getName());
            }
            // Se houver outros tipos de item que guardam referência a Mapa,
            // adicione aqui (ex.: CraftItem)
        }

        System.out.println("✅ Todos os itens do inventário foram atualizados para o novo mapa.");
    }
}