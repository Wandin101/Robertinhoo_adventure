package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Inventory.Inventory;

public class InventoryGridRenderer {
    private ShapeRenderer shapeRenderer;
    private Vector2 position;
    private final Inventory inventory;
    private int cellSize;
    private Color gridLineColor = new Color(0.9f, 0.9f, 0.9f, 0.6f);

    public InventoryGridRenderer(Inventory inventory, Vector2 position, int cellSize) {
        this.inventory = inventory;
        this.position = position;
        this.cellSize = cellSize;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(gridLineColor);

        // Linhas horizontais
        for (int y = 0; y <= inventory.getGridRows(); y++) {
            float yPos = position.y + (y * cellSize);
            shapeRenderer.line(
                    position.x,
                    yPos,
                    position.x + (inventory.getGridCols() * cellSize),
                    yPos);
        }

        // Linhas verticais
        for (int x = 0; x <= inventory.getGridCols(); x++) {
            float xPos = position.x + (x * cellSize);
            shapeRenderer.line(
                    xPos,
                    position.y,
                    xPos,
                    position.y + (inventory.getGridRows() * cellSize));
        }

        shapeRenderer.end();
    }

    public void setGridLineColor(Color color) {
        this.gridLineColor = color;
    }

    public void updatePosition(Vector2 newPosition) {
        this.position.set(newPosition);
    }

    public void updateSize(Vector2 newPosition, int newCellSize) {
        this.position = newPosition;
        this.cellSize = newCellSize;
    }

    public void updateRenderers(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}