package io.github.some_example_name.Entities.Renderer.RenderInventory;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.InventoryMouseController;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Fonts.FontsManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class RenderInventory {
    public final Inventory inventory;
    public int cellSize;
    public final Vector2 position;

    private final InventoryGridRenderer gridRenderer;
    private final InventoryCursorRenderer cursorRenderer;
    private InventoryItemRenderer itemRenderer; // recriado no resize
    private CraftingRenderer craftingRenderer; // recriado no resize
    private BitmapFont inventoryFont;
    public final InventoryMouseCursorRenderer mouseCursorRenderer;

    private final InventoryController inventoryController;

    private Item selectedItem;
    private int originalGridX;
    private int originalGridY;
    private int cursorGridX;
    private int cursorGridY;

    private Color backgroundColor = new Color(0.15f, 0.15f, 0.15f, 1);

    private Color validColor = new Color(0, 1, 0, 0.4f);
    private Color invalidColor = new Color(1, 0, 0, 0.4f);

    private int baseCellSize;
    private float scaleFactor = 1.0f;

    private InventoryContextMenu contextMenu;
    private final InventoryContextMenu.Listener contextMenuListener;

    private Color selectionColor = new Color(1f, 0.8f, 0.3f, 1f);
    private Color hoverColor = new Color(0.6f, 0.8f, 1f, 0.8f);

    private boolean placementValid;

    public RenderInventory(Inventory inventory, int cellSize, Vector2 startPosition,
            InventoryController inventoryController) {
        System.out.println("=== RENDER INVENTORY CONSTRUTOR ===");
        System.out.println("Tela atual: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());

        this.inventory = inventory;
        this.inventoryController = inventoryController;
        this.baseCellSize = cellSize;

        this.scaleFactor = calculateScaleFactor(64);
        this.cellSize = (int) (baseCellSize * scaleFactor);

        System.out.println("Scale factor: " + scaleFactor);
        System.out.println("Cell size ajustado: " + this.cellSize);

        this.position = new Vector2(startPosition);
        inventoryController.setInventoryPosition(position.x, position.y);

        System.out.println("Posição central calculada: " + position);

        this.inventoryFont = FontsManager.getInstance().getDefaultDialogueFont(calculateFontSize());
        this.selectedItem = null;
        this.originalGridX = 0;
        this.originalGridY = 0;
        this.cursorGridX = 0;
        this.cursorGridY = 0;

        this.gridRenderer = new InventoryGridRenderer(inventory, position, cellSize);
        this.itemRenderer = new InventoryItemRenderer(inventory, position, cellSize);
        this.cursorRenderer = new InventoryCursorRenderer(inventory, position, cellSize);

        this.mouseCursorRenderer = new InventoryMouseCursorRenderer(
                inventoryController,
                position,
                cellSize,
                inventory);

        this.contextMenuListener = new InventoryContextMenu.Listener() {
            @Override
            public void onDrop(Item item) {
                System.out.println("Drop -> " + item.getName());
                inventoryController.dropItem(item);
            }

            @Override
            public void onMove(Item item) {
                System.out.println("Move -> " + item.getName());
            }

            @Override
            public void onCraft(Item item) {
                System.out.println("Craft -> " + item.getName());
                inventoryController.craftingMode = true;
                inventoryController.selectedItem = item;
                inventoryController.availableRecipes = inventoryController.inventory.getAvailableRecipes();
                inventoryController.selectedRecipe = inventoryController.availableRecipes.isEmpty() ? null
                        : inventoryController.availableRecipes.get(0);
            }

            @Override
            public void onEquip(Item item) {
                if (item instanceof Weapon) {
                    inventoryController.equipWeapon((Weapon) item);
                }
            }

            @Override
            public void onUnequip(Item item) {
                // Apenas desequipa a arma atual (não precisa do item)
                inventoryController.unequipWeapon();
            }
        };

        this.contextMenu = null; // será criado depois com a hudCamera
    }

    /** Deve ser chamado após a criação, com a câmera HUD do MapRenderer */
    public void setHudCamera(OrthographicCamera hudCamera) {

        if (contextMenu != null)
            contextMenu.dispose();
        this.contextMenu = new InventoryContextMenu(contextMenuListener, this, hudCamera);

        if (craftingRenderer != null)
            craftingRenderer.dispose();
        this.craftingRenderer = new CraftingRenderer(position, hudCamera);

        inventoryController.setContextMenu(this.contextMenu);
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch uiBatch,
            Item placementItem,
            int placementX,
            int placementY,
            boolean isValid,
            Item selectedItem,
            int originalGridX,
            int originalGridY,
            int cursorGridX,
            int cursorGridY,
            List<CraftingRecipe> availableRecipes,
            CraftingRecipe selectedRecipe) {

        // Atualiza estado interno
        this.selectedItem = selectedItem;
        this.originalGridX = originalGridX;
        this.originalGridY = originalGridY;
        this.cursorGridX = cursorGridX;
        this.cursorGridY = cursorGridY;

        this.placementValid = isValid;

        // ========== SHAPE RENDERER (COMPARTILHADO) ==========
        drawBackground(shapeRenderer);
        uiBatch.begin();
        gridRenderer.render(uiBatch);
        uiBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (placementItem != null) {
            drawPlacementLines(shapeRenderer, placementItem, placementX, placementY, isValid);
        }
        mouseCursorRenderer.setFreeCursorMode(contextMenu != null && contextMenu.isVisible());
        drawSelection(shapeRenderer);
        shapeRenderer.end();

        // ========== ÍCONES DOS ITENS (BATCH PRÓPRIO) ==========
        itemRenderer.updateSize(position, cellSize);
        boolean valid = (selectedItem == null) ? false : inventory.canPlaceAt(cursorGridX, cursorGridY, selectedItem);
        itemRenderer.renderItemIcons(selectedItem, valid, cursorGridX, cursorGridY);

        if (placementItem != null) {
            itemRenderer.renderPlacementIcon(placementItem, placementX, placementY, placementValid);
        }

        // ========== UI / HUD ==========
        uiBatch.begin();

        checkRightClick();
        mouseCursorRenderer.renderMouseCursor(uiBatch);

        uiBatch.end();

        // ========== MENU DE CONTEXTO ==========
        if (contextMenu != null) {
            contextMenu.render();
        }

        if (mouseCursorRenderer.getInventoryController().craftingMode && selectedItem != null) {
            craftingRenderer.render(availableRecipes, selectedRecipe,
                    cursorGridX, cursorGridY, selectedItem);
        }
    }

    // ---------- MÉTODOS AUXILIARES (drawBackground, drawSelection, etc.)
    // ----------
    private void drawBackground(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);
        float totalWidth = inventory.getGridCols() * cellSize;
        float totalHeight = inventory.getGridRows() * cellSize;
        shapeRenderer.rect(position.x - 2, position.y - 2, totalWidth + 4, totalHeight + 4);
        shapeRenderer.end();
    }

    private void drawSelection(ShapeRenderer shapeRenderer) {
        Object hoveredItem = inventory.getItemAt(cursorGridX, cursorGridY);
        if (hoveredItem != null && hoveredItem != selectedItem) {
            drawItemHoverEffect(shapeRenderer, hoveredItem, cursorGridX, cursorGridY);
        }
        drawCursor(shapeRenderer, hoveredItem);
        if (selectedItem != null) {
            drawSelectedItem(shapeRenderer);
        }
        cursorRenderer.render(shapeRenderer, selectedItem, originalGridX, originalGridY, cursorGridX, cursorGridY);
    }

    private void drawCursor(ShapeRenderer shapeRenderer, Object hoveredItem) {
        int width = 1;
        int height = 1;
        if (hoveredItem instanceof Item) {
            Item item = (Item) hoveredItem;
            width = item.getGridWidth();
            height = item.getGridHeight();
        }
        float baseX = position.x + (cursorGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - cursorGridY) * cellSize);
        baseY -= (height - 1) * cellSize;
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7);
        shapeRenderer.setColor(selectionColor.r, selectionColor.g, selectionColor.b, pulse);
        shapeRenderer.rectLine(baseX - 2, baseY + height * cellSize + 2,
                baseX + width * cellSize + 2, baseY + height * cellSize + 2, 3);
        shapeRenderer.rectLine(baseX - 2, baseY - 2,
                baseX + width * cellSize + 2, baseY - 2, 3);
    }

    private void drawItemHoverEffect(ShapeRenderer shapeRenderer, Object item, int gridX, int gridY) {
        int width = item instanceof Weapon ? ((Weapon) item).getGridWidth() : 1;
        int height = item instanceof Weapon ? ((Weapon) item).getGridHeight() : 1;
        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (height - 1) * cellSize;
        shapeRenderer.setColor(hoverColor);
        shapeRenderer.rectLine(baseX, baseY + height * cellSize,
                baseX + width * cellSize, baseY + height * cellSize, 2);
        shapeRenderer.rectLine(baseX, baseY,
                baseX + width * cellSize, baseY, 2);
    }

    private void drawSelectedItem(ShapeRenderer shapeRenderer) {
        int width = selectedItem instanceof Weapon ? ((Weapon) selectedItem).getGridWidth() : 1;
        int height = selectedItem instanceof Weapon ? ((Weapon) selectedItem).getGridHeight() : 1;
        float baseX = position.x + (originalGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - originalGridY) * cellSize);
        baseY -= (height - 1) * cellSize;
        shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
        float pulse = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.005));
        shapeRenderer.rectLine(baseX - 2, baseY + height * cellSize + 2,
                baseX + width * cellSize + 2, baseY + height * cellSize + 2, 3 * pulse);
        shapeRenderer.rectLine(baseX - 2, baseY - 2,
                baseX + width * cellSize + 2, baseY - 2, 3 * pulse);
    }

    private void drawPlacementLines(ShapeRenderer shapeRenderer,
            Item item, int x, int y, boolean isValid) {
        int renderY = inventory.getGridRows() - 1 - y - (item.getGridHeight() - 1);
        shapeRenderer.setColor(isValid ? validColor : invalidColor);
        shapeRenderer.rectLine(position.x + x * cellSize,
                position.y + renderY * cellSize + item.getGridHeight() * cellSize,
                position.x + (x + item.getGridWidth()) * cellSize,
                position.y + renderY * cellSize + item.getGridHeight() * cellSize, 3);
        shapeRenderer.rectLine(position.x + x * cellSize,
                position.y + renderY * cellSize,
                position.x + (x + item.getGridWidth()) * cellSize,
                position.y + renderY * cellSize, 3);
    }

    public void setValidColor(Color color) {
        this.validColor = color;
    }

    public void setInvalidColor(Color color) {
        this.invalidColor = color;
    }

    private void checkRightClick() {
        if (contextMenu == null)
            return;
        InventoryMouseController mouseController = mouseCursorRenderer
                .getInventoryController()
                .getMouseController();
        if (mouseController.rightClickTriggered) {
            mouseController.rightClickTriggered = false;
            handleRightClick(
                    mouseController.rightClickGridX,
                    mouseController.rightClickGridY,
                    mouseController.rightClickScreenX,
                    mouseController.rightClickScreenY);
        }
    }

    public void handleRightClick(int gridX, int gridY, float screenX, float screenY) {
        if (contextMenu == null)
            return;
        Item clickedItem = inventory.getItemAt(gridX, gridY);
        if (clickedItem != null) {
            contextMenu.show(clickedItem, gridX, gridY, clickedItem.getGridWidth());
        } else {
            contextMenu.hide();
        }
    }

    public InventoryContextMenu getContextMenu() {
        return contextMenu;
    }

    private float calculateScaleFactor(float currentWidth) {
        float baseWidth = 800f;
        float scale = currentWidth / baseWidth;
        scale = Math.max(0.7f, Math.min(scale, 2.0f));
        return scale;
    }

    public void updateScreenSize(float width, float height) {
        this.scaleFactor = calculateScaleFactor(width);
        this.cellSize = (int) (baseCellSize * scaleFactor);

        // Recalcula a posição central
        float totalWidth = inventory.getGridCols() * this.cellSize;
        float totalHeight = inventory.getGridRows() * this.cellSize;
        float centerX = width / 2f - totalWidth / 2f;
        float centerY = height / 2f - totalHeight / 2f;
        this.position.set(centerX, centerY);

        updateAllRenderers();

        InventoryController controller = mouseCursorRenderer.getInventoryController();
        controller.setInventoryPosition(position.x, position.y);
        controller.setCellSize(cellSize);

        // ATUALIZA A FONTE SEM DESCARREGAR (o FontsManager cuida do cache)
        int newFontSize = Math.max(12, Math.min(48, (int) (cellSize * 0.5f)));

        FontsManager.getInstance().getDefaultMenuFont(newFontSize);

        // Recria os renderers que dependem do tamanho da célula (se necessário)
        this.itemRenderer = new InventoryItemRenderer(inventory, position, cellSize);
        // Se craftingRenderer também usa a fonte, recrie-o também

    }

    private void updateAllRenderers() {
        if (gridRenderer != null)
            gridRenderer.updateSize(position, cellSize);
        if (itemRenderer != null)
            itemRenderer.updateSize(position, cellSize);
        if (cursorRenderer != null)
            cursorRenderer.updateSize(position, cellSize);
        if (mouseCursorRenderer != null)
            mouseCursorRenderer.updateSize(position, cellSize);
    }

    public void setPosition(Vector2 newPosition) {
        this.position.set(newPosition);
        updateAllRenderers();
        mouseCursorRenderer.getInventoryController().setInventoryPosition(newPosition.x, newPosition.y);
    }

    private int calculateFontSize() {
        // Ajuste o fator conforme necessário (ex.: cellSize * 0.4f)
        return Math.max(12, Math.min(48, (int) (cellSize * 0.5f)));
    }

    public void dispose() {
        gridRenderer.dispose();
        itemRenderer.dispose();
        cursorRenderer.dispose();
        mouseCursorRenderer.dispose();
        if (contextMenu != null)
            contextMenu.dispose();
        if (craftingRenderer != null)
            craftingRenderer.dispose();

    }
}