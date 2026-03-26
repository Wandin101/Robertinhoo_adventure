package io.github.some_example_name.Entities.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Crafting.CraftingManager;
import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;

public class Inventory {

    private List<Item> items = new ArrayList<>();
    private Weapon equippedWeapon;
    private Robertinhoo robertinhoo;
    public int gridCols = 5;
    public int gridRows = 5;
    private List<InventorySlot> slots = new ArrayList<>();
    private boolean[][] grid;
    private CraftingManager craftingManager;

    public int getGridCols() {
        return gridCols;
    }

    public int getGridRows() {
        return gridRows;
    }

    private HashMap<String, Integer> ammoStock = new HashMap<>();

    public List<InventorySlot> getSlots() {
        return slots;
    }

    public Inventory(Robertinhoo player) {
        this.robertinhoo = player;
        this.grid = new boolean[gridRows][gridCols];
        this.craftingManager = new CraftingManager();
    }

    public boolean moveItem(Item item, int newX, int newY) {
        boolean wasEquipped = (item instanceof Weapon) && (equippedWeapon == item);
        if (!canPlaceAt(newX, newY, item)) {
            return false;
        }

        removeItem(item);
        boolean success = placeItem(item, newX, newY);

        if (success && wasEquipped) {
            equipWeapon((Weapon) item);
        }

        return success;
    }

    public void unequipWeapon() {
        if (this.equippedWeapon != null) {
            this.equippedWeapon = null;
            if (robertinhoo != null) {
                robertinhoo.unequipWeapon();
            }
        }
    }

    public Weapon getWeaponAt(int gridX, int gridY) {
        for (InventorySlot slot : slots) {
            if (slot.item instanceof Weapon) {
                Weapon weapon = (Weapon) slot.item;
                for (Vector2 cell : weapon.getOccupiedCells()) {
                    int slotX = slot.x + (int) cell.x;
                    int slotY = slot.y + (int) cell.y;
                    if (slotX == gridX && slotY == gridY) {
                        return weapon;
                    }
                }
            }
        }
        return null;
    }

    public Ammo getAmmoAt(int gridX, int gridY) {
        for (InventorySlot slot : slots) {
            if (slot.item instanceof Ammo) {
                Ammo ammo = (Ammo) slot.item;
                for (Vector2 cell : ammo.getOccupiedCells()) {
                    int slotX = slot.x + (int) cell.x;
                    int slotY = slot.y + (int) cell.y;
                    if (slotX == gridX && slotY == gridY) {
                        return ammo;
                    }
                }
            }
        }
        return null;
    }

    public Item getItemAt(int gridX, int gridY) {
        for (InventorySlot slot : slots) {
            if (isItemAtPosition(slot.item, slot.x, slot.y, gridX, gridY)) {
                return slot.item;
            }
        }
        return null;
    }

    private boolean isItemAtPosition(Item item, int slotX, int slotY, int targetX, int targetY) {
        for (Vector2 cell : item.getOccupiedCells()) {
            int checkX = slotX + (int) cell.x;
            int checkY = slotY + (int) cell.y;
            if (checkX == targetX && checkY == targetY)
                return true;
        }
        return false;
    }

    public boolean isEquipped(Weapon weapon) {
        return equippedWeapon == weapon;
    }

    public void markGrid(int startX, int startY, Object item, boolean value) {

        for (InventorySlot slot : slots) {
            if (slot.item == item) {
                Vector2[] cells = slot.item.getOccupiedCells();
                for (Vector2 cell : cells) {
                    int x = slot.x + (int) cell.x;
                    int y = slot.y + (int) cell.y;
                    if (x >= 0 && x < gridCols && y >= 0 && y < gridRows) {
                        grid[gridRows - 1 - y][x] = value;
                    }
                }
            }
        }
        if (item instanceof Item) {
            Item i = (Item) item;
            Vector2[] cells = i.getOccupiedCells();

            for (Vector2 cell : cells) {
                int x = startX + (int) cell.x;
                int y = startY + (int) cell.y;
                if (x >= 0 && x < gridCols && y >= 0 && y < gridRows) {
                    grid[gridRows - 1 - y][x] = value;
                }
            }
        }
    }

    public Boolean addWeapon(Weapon weapon) {
        int[] position = findAvailablePosition(weapon);
        if (position != null) {
            markGrid(position[0], position[1], weapon, true);
            slots.add(new InventorySlot(position[0], position[1], weapon));

            if (equippedWeapon == null) {
                equipWeapon(weapon);
            }
            return true;
        }
        return false;
    }

    public Boolean addAmmo(Ammo ammo) {
        int[] position = findAvailablePosition(ammo);
        if (position != null) {
            markGrid(position[0], position[1], ammo, true);
            slots.add(new InventorySlot(position[0], position[1], ammo));
            ammoStock.put(ammo.getCaliber(),
                    ammoStock.getOrDefault(ammo.getCaliber(), 0) + ammo.getQuantity());

            return true;
        }
        return false;
    }

    public boolean addItem(Item item) {
        if (item instanceof Weapon) {
            return addWeapon((Weapon) item);
        } else if (item instanceof Ammo) {
            return addAmmo((Ammo) item);
        } else {
            int[] position = findAvailablePosition(item);
            if (position != null) {
                markGrid(position[0], position[1], item, true);
                slots.add(new InventorySlot(position[0], position[1], item));
                return true;
            }
            return false;
        }
    }

    private int[] findAvailablePosition(Item item) {
        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                if (canPlaceAt(x, y, item)) {
                    return new int[] { x, y };
                }
            }
        }
        return null;
    }

    public boolean canPlaceAt(int startX, int startY, Item item) {
        Vector2[] cells = item.getOccupiedCells();

        for (Vector2 cell : cells) {
            int x = startX + (int) cell.x;
            int y = startY + (int) cell.y;

            if (x < 0 || x >= gridCols || y < 0 || y >= gridRows) {
                return false;
            }

            if (grid[gridRows - 1 - y][x]) {
                return false;
            }
        }
        return true;
    }

    public void equipWeapon(Weapon weapon) {
        if (items.contains(weapon)) { // Verifica se a arma está no inventário
            this.equippedWeapon = weapon;
            robertinhoo.equipWeapon(weapon);
        }
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public boolean removeItem(Item item) {
        for (InventorySlot slot : new ArrayList<>(slots)) {
            if (slot.item == item) {
                markGrid(slot.x, slot.y, item, false);
                slots.remove(slot);
                items.remove(item);

                if (equippedWeapon == item) {
                    unequipWeapon();
                }
                return true;
            }
        }
        return false;
    }

    public boolean placeItem(Item item, int newX, int newY) {
        System.out.println("\n=== 📦 INVENTORY PLACE ITEM (INICIO) ===");
        System.out.println("   - Item: " + item);
        System.out.println("   - HashCode: " + System.identityHashCode(item));
        System.out.println("   - Posição: [" + newX + ", " + newY + "]");
        System.out.println("   - Item já no inventário? " + items.contains(item));

        if (items.contains(item)) {
            System.out.println("🔄 Item já está no inventário, removendo primeiro...");
            removeItem(item);
        }

        if (!canPlaceAt(newX, newY, item)) {
            System.out.println("❌ Não pode colocar nesta posição!");
            System.out.println("=== ❌ INVENTORY PLACE ITEM (FALHA) ===\n");
            return false;
        }

        markGrid(newX, newY, item, true);
        slots.add(new InventorySlot(newX, newY, item));
        items.add(item);

        System.out.println("✅ Item colocado no inventário!");
        System.out.println("   - Slots count: " + slots.size());
        System.out.println("   - Items count: " + items.size());

        if (item instanceof Ammo) {
            Ammo ammo = (Ammo) item;
            ammoStock.put(ammo.getCaliber(),
                    ammoStock.getOrDefault(ammo.getCaliber(), 0) + ammo.getQuantity());
            System.out.println(
                    "   - Ammo stock atualizado: " + ammo.getCaliber() + " = " + ammoStock.get(ammo.getCaliber()));
        }

        if (item instanceof Weapon && equippedWeapon == null) {
            equipWeapon((Weapon) item);
            System.out.println("   - Arma equipada automaticamente");
        }

        System.out.println("=== ✅ INVENTORY PLACE ITEM (SUCESSO) ===\n");
        return true;
    }

    public List<Item> getItems() {
        return items;
    }

    public int getAmmoCount(String ammoType) {
        return ammoStock.getOrDefault(ammoType, 0);
    }

    public boolean consumeAmmoOneByOne(String type, int maxAmount) {
        if (maxAmount <= 0 || !ammoStock.containsKey(type) || ammoStock.get(type) <= 0) {
            return false;
        }
        for (InventorySlot slot : slots) {
            if (slot.item instanceof Ammo) {
                Ammo ammo = (Ammo) slot.item;
                if (ammo.getCaliber().equals(type) && ammo.getQuantity() > 0) {
                    ammo.reduceQuantity(1);
                    int currentStock = ammoStock.getOrDefault(type, 0);
                    ammoStock.put(type, Math.max(0, currentStock - 1));
                    if (ammo.getQuantity() <= 0) {
                        markGrid(slot.x, slot.y, ammo, false);
                        slots.remove(slot);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public void consumeAmmo(String type, int amount) {
        List<InventorySlot> slotsToRemove = new ArrayList<>();
        int remaining = amount;

        // Itera pelos slots na ordem inversa para evitar
        // ConcurrentModificationException
        for (int i = slots.size() - 1; i >= 0; i--) {
            InventorySlot slot = slots.get(i);
            if (slot.item instanceof Ammo) {
                Ammo ammo = (Ammo) slot.item;
                if (ammo.getCaliber().equals(type)) {
                    int available = ammo.getQuantity();

                    if (available > 0) {
                        int taken = Math.min(available, remaining);
                        ammo.reduceQuantity(taken);
                        remaining -= taken;

                        // Remove slot se a munição acabou
                        if (ammo.getQuantity() <= 0) {
                            markGrid(slot.x, slot.y, ammo, false);
                            slotsToRemove.add(slot);
                        }

                        if (remaining <= 0)
                            break;
                    }
                }
            }
        }

        // Remove slots vazios
        slots.removeAll(slotsToRemove);

        // Atualiza o estoque total
        int currentStock = ammoStock.getOrDefault(type, 0);
        ammoStock.put(type, Math.max(0, currentStock - amount));
    }

    public void cleanupAmmoSlots(String type) {

        List<InventorySlot> toRemove = new ArrayList<>();
        for (InventorySlot slot : slots) {
            if (slot.item instanceof Ammo) {
                Ammo ammo = (Ammo) slot.item;
                if (ammo.getAmmoType().equals(type) && ammo.getQuantity() <= 0) {
                    markGrid(slot.x, slot.y, ammo, false);
                    toRemove.add(slot);
                }
            }
        }
        slots.removeAll(toRemove);
    }

    public void updateAmmoSlots(String caliber) {
        List<InventorySlot> toRemove = new ArrayList<>();

        for (InventorySlot slot : slots) {
            if (slot.item instanceof Ammo) {
                Ammo ammo = (Ammo) slot.item;
                if (ammo.getCaliber().equals(caliber)) {
                    // Atualize a quantidade do slot com o estoque restante
                    int stock = ammoStock.getOrDefault(caliber, 0);
                    ammo.setQuantity(stock);

                    if (stock <= 0) {
                        toRemove.add(slot);
                    }
                }
            }
        }

        // Remova slots vazios
        slots.removeAll(toRemove);
    }

    public int getItemCount(Class<? extends Item> itemClass) {
        int count = 0;
        for (InventorySlot slot : slots) {
            if (slot.item != null && itemClass.isInstance(slot.item)) {
                count++;
            }
        }
        return count;
    }

    public boolean removeItems(Class<? extends Item> itemClass, int quantity) {
        if (getItemCount(itemClass) < quantity)
            return false;

        int removed = 0;
        List<InventorySlot> toRemove = new ArrayList<>();

        for (InventorySlot slot : slots) {
            if (slot.item != null && itemClass.isInstance(slot.item)) {
                toRemove.add(slot);
                removed++;
                if (removed >= quantity)
                    break;
            }
        }

        for (InventorySlot slot : toRemove) {
            removeItem(slot.item);
        }

        return true;
    }

    public List<CraftingRecipe> getAvailableRecipes() {
        return craftingManager.getAvailableRecipes(this);
    }

    // Na classe Inventory, atualize o método craftRecipe
    public boolean craftRecipe(CraftingRecipe recipe) {
        return craftingManager.craftRecipe(recipe, this, robertinhoo.getInventoryController());
    }

    public Robertinhoo getPlayer() {
        return robertinhoo;
    }

    public void debugPrintGrid() {
        System.out.println("=== INVENTORY GRID ===");
        for (int y = gridRows - 1; y >= 0; y--) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < gridCols; x++) {
                boolean occupied = grid[gridRows - 1 - y][x];
                if (occupied) {
                    Item item = getItemAt(x, y);
                    if (item != null) {
                        line.append(String.format("[%s]", item.getName().substring(0, 1)));
                    } else {
                        line.append("[X]");
                    }
                } else {
                    line.append("[ ]");
                }
            }
            System.out.println("y=" + y + ": " + line);
        }
        System.out.println("======================");
    }

    public boolean isCellOccupied(int x, int y) {
        if (x < 0 || x >= gridCols || y < 0 || y >= gridRows)
            return true;
        return grid[gridRows - 1 - y][x];
    }

    public InventoryController getInventoryController() {
        return robertinhoo.getInventoryController();
    }

    public Vector2 getItemPosition(Item item) {
        for (InventorySlot slot : slots) {
            if (slot.item == item) {
                return new Vector2(slot.x, slot.y);
            }
        }
        return null;
    }

}