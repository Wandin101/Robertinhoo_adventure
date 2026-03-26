package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Inventory.Inventory;

public class InventoryGridRenderer {
    private Texture defaultBlockTexture;
    private Texture blockWithItemTexture;
    private Vector2 position;
    private final Inventory inventory;
    private int cellSize;

    public InventoryGridRenderer(Inventory inventory, Vector2 position, int cellSize) {
        this.inventory = inventory;
        this.position = position;
        this.cellSize = cellSize;

        // Carrega as texturas
        try {
            defaultBlockTexture = new Texture("UI/inventory_ui/BLOCO.png");
            blockWithItemTexture = new Texture("UI/inventory_ui/BLOCO_COM_ITEM.PNG");
        } catch (Exception e) {
            System.err.println("Erro ao carregar texturas do grid: " + e.getMessage());
        }
    }

    /**
     * Desenha o grid usando blocos (tiles). Se uma célula contiver algum item,
     * desenha o bloco especial, senão desenha o bloco padrão.
     * 
     * @param batch O SpriteBatch usado para desenhar as texturas.
     */
    public void render(SpriteBatch batch) {
        if (defaultBlockTexture == null)
            return;

        int cols = inventory.getGridCols();
        int rows = inventory.getGridRows();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // Inverte Y: célula (x, y) no inventário (y=0 = topo) deve desenhar na posição
                // Y correspondente à linha de baixo
                int drawYIndex = rows - 1 - y;
                float drawX = position.x + (x * cellSize);
                float drawY = position.y + (drawYIndex * cellSize);

                Texture textureToDraw = defaultBlockTexture;
                if (inventory.getItemAt(x, y) != null) { // itemAt usa as coordenadas do inventário (topo y=0)
                    textureToDraw = blockWithItemTexture != null ? blockWithItemTexture : defaultBlockTexture;
                }

                batch.draw(textureToDraw, drawX, drawY, cellSize, cellSize);
            }
        }
    }

    public void updatePosition(Vector2 newPosition) {
        this.position.set(newPosition);
    }

    public void updateSize(Vector2 newPosition, int newCellSize) {
        this.position = newPosition;
        this.cellSize = newCellSize;
    }

    public void dispose() {
        if (defaultBlockTexture != null)
            defaultBlockTexture.dispose();
        if (blockWithItemTexture != null)
            blockWithItemTexture.dispose();
    }
}