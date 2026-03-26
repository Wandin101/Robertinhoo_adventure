package io.github.some_example_name.Entities.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.RenderInventory.InventoryContextMenu;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.Entities.Renderer.RenderInventory.InventoryContextMenu;

import java.util.ArrayList;
import java.util.List;

public class InventoryController {
    private final Robertinhoo player;
    public final Inventory inventory;
    public Mapa mapa;
    private InventoryMouseController mouseController;

    private boolean isOpen = false;
    private int selectedSlot = 0;
    private boolean placementMode = false;
    private Item currentPlacementItem;
    private int placementGridX = 0;
    private int placementGridY = 0;
    private boolean validPlacement = false;

    private int lastPlacementX = 0;
    private int lastPlacementY = 0;

    private boolean itemSelected = false;
    private int cursorGridX = 0;
    private int cursorGridY = 0;
    private int originalGridX, originalGridY;
    public Item selectedItem = null;

    public boolean craftingMode = false;
    public CraftingRecipe selectedRecipe;
    public List<CraftingRecipe> availableRecipes;
    private InventoryContextMenu inventoryContextMenu;
    private int cellSize = 40;

    private Vector2 inventoryPosition;
    private float originalRotation;

    public InventoryController(Robertinhoo player, Inventory inventory, Mapa mapa) {
        this.player = player;
        this.inventory = inventory;
        this.mapa = mapa;
        this.mouseController = new InventoryMouseController(this, inventory);

        this.inventoryPosition = new Vector2(50, 50);

    }

    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
            toggleInventory();
        }
        if (placementMode) {
            updatePlacementMode();
            return;
        }
        if (isOpen) {

            if (Gdx.input.isKeyJustPressed(Keys.C)) {
                toggleCraftingMode();
            }
            if (craftingMode) {
                handleCraftingMode();
            } else {
                handleInventorySelection();
            }
        }
    }

    private void toggleCraftingMode() {

        craftingMode = true;

        if (craftingMode) {
            availableRecipes = inventory.getAvailableRecipes();

            selectedRecipe = availableRecipes.isEmpty() ? null : availableRecipes.get(0);
        }
    }

    private void handleCraftingMode() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectPreviousRecipe();
        } else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectNextRecipe();
        } else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            executeCraft();
        } else if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            craftingMode = false;
        } else {
        }
    }

    private void selectNextRecipe() {
        if (availableRecipes.isEmpty())
            return;
        int index = availableRecipes.indexOf(selectedRecipe);
        index = (index + 1) % availableRecipes.size();
        selectedRecipe = availableRecipes.get(index);
    }

    private void selectPreviousRecipe() {
        if (availableRecipes.isEmpty())
            return;
        int index = availableRecipes.indexOf(selectedRecipe);
        index = (index - 1 + availableRecipes.size()) % availableRecipes.size();
        selectedRecipe = availableRecipes.get(index);
    }

    private void executeCraft() {
        if (selectedRecipe != null && inventory.craftRecipe(selectedRecipe)) {
            System.out.println("Item craftado com sucesso!");
            craftingMode = false;
        }
    }

    private void handleInventorySelection() {
        boolean isVisible = getContextMenu().isVisible();
        if (isVisible)
            return;

        int gridCols = inventory.gridCols;
        int gridRows = inventory.gridRows;

        // Limites do cursor
        int maxX = gridCols - 1;
        int maxY = gridRows - 1;
        if (selectedItem != null) {
            maxX = gridCols - selectedItem.getGridWidth();
            maxY = gridRows - selectedItem.getGridHeight();
        }

        // Movimento do cursor com som de "arrastar" se houver item selecionado
        boolean moved = false;
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            cursorGridX = Math.max(0, cursorGridX - 1);
            moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            cursorGridX = Math.min(maxX, cursorGridX + 1);
            moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            cursorGridY = Math.max(0, cursorGridY - 1);
            moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            cursorGridY = Math.min(maxY, cursorGridY + 1);
            moved = true;
        }
        // Toca som de arrastar quando move o cursor com item selecionado
        if (moved && selectedItem != null) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START);
            Gdx.app.log("INVENTORY", "ITEM_DRAG_START triggered (cursor move)");
        }

        // ENTER: seleciona ou coloca
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (selectedItem == null) {
                Item item = inventory.getItemAt(cursorGridX, cursorGridY);
                if (item != null) {
                    selectItem(item, cursorGridX, cursorGridY);
                }
            } else {
                attemptToPlaceSelectedItem(cursorGridX, cursorGridY);
            }
        }

        // Tecla R - rotação (só com item selecionado)
        if (Gdx.input.isKeyJustPressed(Keys.R) && selectedItem != null) {
            inventory.debugPrintGrid();
            selectedItem.rotate();
            System.out.println("[DEBUG] Item rotacionado. Novas dimensões: " + selectedItem.getGridWidth() + "x"
                    + selectedItem.getGridHeight());
            int newMaxX = inventory.gridCols - selectedItem.getGridWidth();
            int newMaxY = inventory.gridRows - selectedItem.getGridHeight();
            cursorGridX = Math.min(cursorGridX, newMaxX);
            cursorGridY = Math.min(cursorGridY, newMaxY);
            inventory.debugPrintGrid();
        }

        // Tecla ESC - cancela seleção
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            cancelSelection();
        }

        // Tecla Q - descartar
        if (Gdx.input.isKeyJustPressed(Keys.Q)) {
            if (selectedItem != null) {
                dropItem(selectedItem);
                selectedItem = null;
            }
        }
    }

    private void rotateItem() {
        if (currentPlacementItem == null)
            return;

        currentPlacementItem.rotate();

        int newX = placementGridX;
        int newY = placementGridY;
        if (newX + currentPlacementItem.getGridWidth() > inventory.gridCols) {
            newX = inventory.gridCols - currentPlacementItem.getGridWidth();
        }
        if (newY + currentPlacementItem.getGridHeight() > inventory.gridRows) {
            newY = inventory.gridRows - currentPlacementItem.getGridHeight();
        }
        newX = Math.max(0, newX);
        newY = Math.max(0, newY);

        placementGridX = newX;
        placementGridY = newY;

        updatePlacementValidity();

        System.out.println("Item rotacionado para " +
                currentPlacementItem.getGridWidth() + "x" + currentPlacementItem.getGridHeight() +
                " na posição (" + placementGridX + "," + placementGridY + ")");
    }

    private InputProcessor previousInputProcessor;

    private void toggleInventory() {
        if (placementMode)
            return;

        isOpen = !isOpen;

        if (isOpen) {
            player.state = Robertinhoo.IDLE;
            player.body.setLinearVelocity(Vector2.Zero);
            previousInputProcessor = Gdx.input.getInputProcessor();
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(mouseController);
            if (previousInputProcessor != null) {
                multiplexer.addProcessor(previousInputProcessor);
            }

            Gdx.input.setInputProcessor(multiplexer);
            selectedItem = null;
            cursorGridX = 0;
            cursorGridY = 0;
            craftingMode = false;
        } else {

            Gdx.input.setInputProcessor(previousInputProcessor);

            if (selectedItem != null) {
                inventory.placeItem(selectedItem, originalGridX, originalGridY);
                selectedItem = null;
                System.out.println("[DEBUG] Inventário fechado, item recolocado na posição original");
            }
            craftingMode = false;
            selectedItem = null;
        }
    }

    private void updatePlacementMode() {
        // Cancelamento
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            if (currentPlacementItem != null) {
                Vector2 dropPosition = player.getPosition().cpy();
                if (currentPlacementItem instanceof Weapon) {
                    Weapon weapon = (Weapon) currentPlacementItem;
                    weapon.createBody(dropPosition);
                    mapa.getWeapons().add(weapon);
                } else if (currentPlacementItem instanceof Ammo) {
                    Ammo ammo = (Ammo) currentPlacementItem;
                    ammo.setMapa(mapa);
                    ammo.createBody(dropPosition);
                    mapa.getAmmo().add(ammo);
                } else {
                    currentPlacementItem.createBody(dropPosition);
                    mapa.getCraftItems().add(currentPlacementItem);
                }
            }
            exitPlacementMode(false);
            return;
        }

        // Rotação
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            rotateItem();
        }

        // Movimento
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            placementGridX = Math.max(0, placementGridX - 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            placementGridX = Math.min(inventory.gridCols - currentPlacementItem.getGridWidth(), placementGridX + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            placementGridY = Math.min(inventory.gridRows - currentPlacementItem.getGridHeight(), placementGridY + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            placementGridY = Math.max(0, placementGridY - 1);
            updatePlacementValidity();
        }

        // Confirmação
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (validPlacement &&
                    placementGridX >= 0 && placementGridY >= 0 &&
                    placementGridX + currentPlacementItem.getGridWidth() <= inventory.gridCols &&
                    placementGridY + currentPlacementItem.getGridHeight() <= inventory.gridRows) {

                if (itemSelected) {
                    inventory.removeItem(selectedItem);
                    itemSelected = false;
                }

                if (inventory.placeItem(currentPlacementItem, placementGridX, placementGridY)) {
                    AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_PLACE_SUCCESS);
                    if (currentPlacementItem instanceof Weapon) {
                        ((Weapon) currentPlacementItem).destroyBody();
                        mapa.getWeapons().remove(currentPlacementItem);
                    } else if (currentPlacementItem instanceof Ammo) {
                        Ammo ammo = (Ammo) currentPlacementItem;
                        ammo.destroyBody();
                        mapa.getAmmo().remove(ammo);
                    }
                    exitPlacementMode(true);
                } else {
                    Gdx.app.log("INVENTORY", "ITEM_PLACE_ERROR (placement mode placeItem falhou)");
                    AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_PLACE_ERROR);
                }
            }
        }
    }

    public void dropItem(Item item) {

        if (inventory.removeItem(item)) {
            player.IsUsingOneHandWeapon = false;
            if (item instanceof Weapon && inventory.getEquippedWeapon() == item) {
                inventory.unequipWeapon();
            }
            Vector2 dropPosition = player.getPosition().cpy();

            if (item instanceof Ammo) {
                ((Ammo) item).setMapa(mapa);
            }
            item.setPosition(dropPosition);
            if (item instanceof Weapon) {
                Weapon weapon = (Weapon) item;
                weapon.createBody(dropPosition);
                mapa.getWeapons().add(weapon);
            } else if (item instanceof Ammo) {
                Ammo ammo = (Ammo) item;
                ammo.createBody(dropPosition);
                mapa.getAmmo().add(ammo);
            } else {
                item.createBody(dropPosition);
                mapa.getCraftItems().add(item);
            }
        }
    }

    private void updatePlacementValidity() {
        validPlacement = inventory.canPlaceAt(placementGridX, placementGridY, currentPlacementItem);
    }

    private void exitPlacementMode(boolean success) {
        placementMode = false;
        if (success) {
            if (player.weaponToPickup != null) {
                player.weaponToPickup.destroyBody();
                mapa.getWeapons().remove(player.weaponToPickup);
                player.clearWeaponToPickup();
                lastPlacementX = placementGridX;
                lastPlacementY = placementGridY;
            }
            if (currentPlacementItem != null && currentPlacementItem instanceof Weapon) {
                inventory.equipWeapon((Weapon) currentPlacementItem);
            }
            selectedItem = null;
        }
        currentPlacementItem = null;

    }

    public void enterPlacementMode(Item item) {

        if (item != null) {
            placementMode = true;
            currentPlacementItem = item;

            if (item instanceof Weapon) {
                mapa.getWeapons().remove(item);
            } else if (item instanceof Ammo) {
                mapa.getAmmo().remove(item);
            } else {
                mapa.getCraftItems().remove(item);
            }

            placementGridX = lastPlacementX;
            placementGridY = lastPlacementY;
            updatePlacementValidity();
        }
    }

    public boolean GetIsOpen() {
        return isOpen;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public boolean isInPlacementMode() {
        return placementMode;
    }

    public Item getCurrentPlacementItem() {
        return currentPlacementItem;
    }

    public int getPlacementGridX() {
        return placementGridX;
    }

    public int getPlacementGridY() {
        return placementGridY;
    }

    public boolean isValidPlacement() {
        return validPlacement;
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public int getOriginalGridX() {
        return originalGridX;
    }

    public int getOriginalGridY() {
        return originalGridY;
    }

    public int getCursorGridX() {
        return cursorGridX;
    }

    public int getCursorGridY() {
        return cursorGridY;
    }

    public List<CraftingRecipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public CraftingRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setCursorGridPosition(int x, int y) {
        this.cursorGridX = x;
        this.cursorGridY = y;
    }

    public void setSelectedItem(Item item, int gridX, int gridY) {
        this.selectedItem = item;
        this.originalGridX = gridX;
        this.originalGridY = gridY;
    }

    public void moveSelectedItem(int gridX, int gridY) {
        if (inventory.moveItem(selectedItem, gridX, gridY)) {
            selectedItem = null;
        }
    }

    // InventoryController.java
    public float getInventoryStartX() {
        return inventoryPosition.x;
    }

    public float getInventoryStartY() {
        return inventoryPosition.y;
    }

    public float getCellSize() {
        return this.cellSize; // ← retorna o valor atualizado, não fixo!
    }

    public boolean isInventoryOpen() {
        return isOpen;
    }

    public void setCursorPosition(int x, int y) {
        // Garante que o cursor fique dentro dos limites
        this.cursorGridX = MathUtils.clamp(x, 0, inventory.gridCols - 1);
        this.cursorGridY = MathUtils.clamp(y, 0, inventory.gridRows - 1);
    }

    public void selectItemAtCursor() {
        if (selectedItem == null) {
            Item item = inventory.getItemAt(cursorGridX, cursorGridY);
            if (item != null) {
                selectItem(item, cursorGridX, cursorGridY);
                AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.WEAPON_SELECTED);
            }
        } else {
            if (inventory.moveItem(selectedItem, cursorGridX, cursorGridY)) {
                selectedItem = null;
            }
        }
    }

    public void startDragging(Item item, int gridX, int gridY) {
        this.selectedItem = item;
        this.originalGridX = gridX;
        this.originalGridY = gridY;
        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START);
        Gdx.app.log("INVENTORY", "ITEM_DRAG_START triggered");
    }

    public void completeDrag(int gridX, int gridY) {
        if (selectedItem != null) {
            if (inventory.moveItem(selectedItem, gridX, gridY)) {
                selectedItem = null;
            }
        }
    }

    public Vector2 getInventoryPosition() {
        return inventoryPosition;
    }

    public void setInventoryPosition(float x, float y) {
        this.inventoryPosition.set(x, y);
    }

    public InventoryMouseController getMouseController() {
        return mouseController;
    }

    public InventoryContextMenu getContextMenu() {
        return inventoryContextMenu;
    }

    public void setContextMenu(InventoryContextMenu contextMenu) {
        this.inventoryContextMenu = contextMenu;
    }

    // valor inicial (será atualizado pelo render)

    // Métodos get/set:
    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    public void equipWeapon(Weapon weapon) {
        inventory.equipWeapon(weapon);
    }

    public void unequipWeapon() {
        inventory.unequipWeapon();
    }

    public void selectItem(Item item, int clickGridX, int clickGridY) {
        Vector2 realPos = inventory.getItemPosition(item);
        if (realPos == null) {
            Gdx.app.log("INVENTORY", "Item não encontrado nos slots do inventário!");
            return;
        }
        int realX = (int) realPos.x;
        int realY = (int) realPos.y;

        if (inventory.removeItem(item)) {
            this.selectedItem = item;
            this.originalGridX = realX;
            this.originalGridY = realY;
            this.originalRotation = item.getRotation();
            this.cursorGridX = clickGridX; // posição do clique (para o cursor)
            this.cursorGridY = clickGridY;
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.WEAPON_SELECTED);
        } else {
            Gdx.app.log("INVENTORY", "Falha ao remover item do inventário em selectItem");
        }
    }

    public void tryPlaceSelectedItem(int gridX, int gridY) {
        attemptToPlaceSelectedItem(gridX, gridY);
    }

    private boolean attemptToPlaceSelectedItem(int targetX, int targetY) {
        if (selectedItem == null)
            return false;

        // Tenta colocar na posição alvo
        if (inventory.placeItem(selectedItem, targetX, targetY)) {
            selectedItem = null;
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_PLACE_SUCCESS);
            inventory.debugPrintGrid();
            return true;
        }

        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_PLACE_ERROR);

        if (inventory.placeItem(selectedItem, originalGridX, originalGridY)) {
            selectedItem = null;
            inventory.debugPrintGrid();
            return true; // recolocado, mas sem som adicional
        }

        while (selectedItem.getRotation() != originalRotation) {
            selectedItem.rotate();
        }
        if (inventory.placeItem(selectedItem, originalGridX, originalGridY)) {
            selectedItem = null;
            inventory.debugPrintGrid();
            return true;
        }

        // Falha total: solta item no chão
        dropItem(selectedItem);
        selectedItem = null;
        inventory.debugPrintGrid();
        return false;
    }

    public void cancelSelection() {
        if (selectedItem != null) {
            inventory.placeItem(selectedItem, originalGridX, originalGridY);
            selectedItem = null;
            System.out.println("[DEBUG] Seleção cancelada");
        }
    }
}
