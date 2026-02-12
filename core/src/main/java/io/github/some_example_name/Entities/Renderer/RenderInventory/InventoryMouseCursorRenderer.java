package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;

public class InventoryMouseCursorRenderer {
    private final ShapeRenderer shapeRenderer;
    private final InventoryController inventoryController;
    private final Inventory inventory;

    private Vector2 inventoryPosition; // posição atual do inventário (canto inferior esquerdo)
    private int cellSize;
    private boolean freeCursorMode = false;

    public InventoryMouseCursorRenderer(InventoryController inventoryController,
            Vector2 inventoryPosition,
            int cellSize,
            Inventory inventory) {
        this.inventoryController = inventoryController;
        this.inventoryPosition = new Vector2(inventoryPosition);
        this.cellSize = cellSize;
        this.inventory = inventory;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void renderMouseCursor(SpriteBatch uiBatch) {
        if (!inventoryController.isInventoryOpen())
            return;

        // 🎯 Configura o ShapeRenderer para usar a MESMA projeção do batch (coordenadas
        // de tela HUD)
        shapeRenderer.setProjectionMatrix(uiBatch.getProjectionMatrix());

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        if (freeCursorMode) {
            // ✅ Conversão correta: tela (Y invertido) → mundo (Y para cima)
            float worldX = mouseX;
            float worldY = Gdx.graphics.getHeight() - mouseY;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.circle(worldX, worldY, 4);
            shapeRenderer.end();
        } else {
            // Obtém a célula sob o mouse (o controller já deve ter posição/cellSize
            // atualizados)
            Vector2 gridPos = inventoryController.getMouseController().screenToGrid(mouseX, mouseY);
            if (gridPos != null) {
                int gridX = (int) gridPos.x;
                int gridY = (int) gridPos.y;

                float baseX = inventoryPosition.x + (gridX * cellSize);
                float baseY = inventoryPosition.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);

                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.rect(baseX, baseY, cellSize, cellSize);
                shapeRenderer.end();
            }
        }
    }

    public InventoryController getInventoryController() {
        return inventoryController;
    }

    public void setFreeCursorMode(boolean freeMode) {
        this.freeCursorMode = freeMode;
    }

    public void updateSize(Vector2 newPosition, int newCellSize) {
        this.inventoryPosition.set(newPosition);
        this.cellSize = newCellSize;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}