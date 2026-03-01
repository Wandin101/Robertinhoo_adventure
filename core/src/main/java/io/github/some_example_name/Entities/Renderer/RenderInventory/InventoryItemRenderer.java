package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventorySlot;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Fonts.FontsManager;

public class InventoryItemRenderer {
    private final SpriteBatch spriteBatch; // ✅ Batch próprio para ícones
    private final BitmapFont font;
    private final Inventory inventory;
    private Vector2 position;
    private int cellSize;

    public InventoryItemRenderer(Inventory inventory, Vector2 position, int cellSize) {
        this.inventory = inventory;
        this.position = position;
        this.cellSize = cellSize;
        this.spriteBatch = new SpriteBatch();
        this.font = FontsManager.createInventoryFont();
    }

    /** Renderiza todos os ícones dos itens (exceto o selecionado) */
    public void renderItemIcons(Item selectedItem, boolean validPlacement, int cursorGridX, int cursorGridY) {
        spriteBatch.begin();

        for (InventorySlot slot : inventory.getSlots()) {
            if (slot.item != null && slot.item != selectedItem) {
                renderItemIcon(slot.item, slot.x, slot.y, 1f, Color.WHITE);
                if (slot.item instanceof Ammo) {
                    renderAmmoQuantity((Ammo) slot.item, slot.x, slot.y);
                }
            }
        }

        if (selectedItem != null) {
            renderItemIcon(selectedItem, cursorGridX, cursorGridY, 0.5f,
                    validPlacement ? Color.GREEN : Color.RED);
        }

        spriteBatch.end();
    }

    /** Renderiza o ícone do item em modo de posicionamento (placement) */
    public void renderPlacementIcon(Item item, int gridX, int gridY, boolean isValid) {
        spriteBatch.begin();
        renderItemIcon(item, gridX, gridY, 0.5f,
                isValid ? new Color(1, 1, 1, 0.7f) : new Color(1, 0.5f, 0.5f, 0.7f));
        spriteBatch.end();
    }

    private void renderItemIcon(Item item, int gridX, int gridY, float alpha, Color tint) {
        TextureRegion icon = item.getIcon();
        int gridWidth = item.getGridWidth();
        int gridHeight = item.getGridHeight();
        float rotation = item.getRotation();

        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (gridHeight - 1) * cellSize;

        // Dimensões originais do ícone
        int iconWidth = icon.getRegionWidth();
        int iconHeight = icon.getRegionHeight();

        // Se o item está rotacionado, as dimensões efetivas trocam
        boolean isRotated = (rotation == 90 || rotation == 270);
        int effectiveIconWidth = isRotated ? iconHeight : iconWidth;
        int effectiveIconHeight = isRotated ? iconWidth : iconHeight;

        // Escala para caber na área do grid, mantendo a proporção
        float scale = Math.min(
                (gridWidth * cellSize) / effectiveIconWidth,
                (gridHeight * cellSize) / effectiveIconHeight);

        float scaledWidth = iconWidth * scale;
        float scaledHeight = iconHeight * scale;

        // Centro da área ocupada
        float centerX = baseX + (gridWidth * cellSize) / 2;
        float centerY = baseY + (gridHeight * cellSize) / 2;

        Color originalColor = spriteBatch.getColor();
        spriteBatch.setColor(tint.r, tint.g, tint.b, alpha);

        if (rotation != 0) {
            spriteBatch.draw(icon,
                    centerX - scaledWidth / 2, centerY - scaledHeight / 2,
                    scaledWidth / 2, scaledHeight / 2,
                    scaledWidth, scaledHeight,
                    1, 1,
                    rotation);
        } else {
            spriteBatch.draw(icon,
                    centerX - scaledWidth / 2, centerY - scaledHeight / 2,
                    scaledWidth, scaledHeight);
        }

        spriteBatch.setColor(originalColor);
    }

    private void renderAmmoQuantity(Ammo ammo, int gridX, int gridY) {
        int width = ammo.getGridWidth();
        int height = ammo.getGridHeight();

        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        int currentQuantity = ammo.getQuantity();
        float padding = 5;
        float textX = baseX + padding;
        float textY = baseY + padding + font.getCapHeight();

        font.setColor(Color.BLACK);
        font.draw(spriteBatch, String.valueOf(currentQuantity), textX + 1, textY - 1);
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, String.valueOf(currentQuantity), textX, textY);
    }

    public void updateSize(Vector2 newPosition, int newCellSize) {
        this.position = newPosition;
        this.cellSize = newCellSize;
    }

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
    }
}