package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

public class InventoryContextMenu {

    public interface Listener {
        void onDrop(Item item);

        void onMove(Item item);

        void onCraft(Item item);

        void onEquip(Item item); // nova ação

        void onUnequip(Item item); // nova ação
    }

    private static final float MENU_WIDTH = 130f;
    private static final float PADDING = 8f;
    private static final float OPTION_HEIGHT = 28f;

    private final Listener listener;
    private final RenderInventory renderInventory;
    private final OrthographicCamera hudCamera;

    private Item item;
    private boolean visible = false;
    private int gridX, gridY;
    private int itemWidth;
    private int hoverIndex = -1;

    private final SpriteBatch ownBatch;
    private final ShapeRenderer ownShapeRenderer;
    private static Texture whitePixel;

    public InventoryContextMenu(Listener listener, RenderInventory renderInventory, OrthographicCamera hudCamera) {
        this.listener = listener;
        this.renderInventory = renderInventory;
        this.hudCamera = hudCamera;
        this.ownBatch = new SpriteBatch();
        this.ownShapeRenderer = new ShapeRenderer();
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    public void show(Item item, int gridX, int gridY, int itemWidth) {
        this.item = item;
        this.gridX = gridX;
        this.gridY = gridY;
        this.itemWidth = itemWidth;
        this.visible = true;
        this.hoverIndex = -1;
    }

    public void hide() {
        visible = false;
        item = null;
        hoverIndex = -1;
    }

    public boolean isVisible() {
        return visible;
    }

    /** Retorna a lista de opções dinâmica para o item atual */
    private List<String> getOptionsForItem() {
        List<String> opts = new ArrayList<>();
        opts.add("Descartar");
        opts.add("Mover");

        if (item instanceof Weapon) {
            boolean isEquipped = renderInventory.inventory.isEquipped((Weapon) item);
            if (isEquipped) {
                opts.add("Desequipar");
            } else {
                opts.add("Equipar");
            }
        }

        opts.add("Craft");
        return opts;
    }

    private float[] getCurrentMenuPosition() {
        float invPosX = renderInventory.position.x;
        float invPosY = renderInventory.position.y;
        float cell = renderInventory.cellSize;
        int rows = renderInventory.inventory.getGridRows();

        float itemX = invPosX + (gridX * cell);
        float itemY = invPosY + ((rows - 1 - gridY) * cell);
        itemY -= (item.getGridHeight() - 1) * cell;

        float menuX = itemX + (itemWidth * cell) + 5f;
        float menuY = itemY + (item.getGridHeight() * cell / 2f);
        float menuHeight = PADDING * 2f + getOptionsForItem().size() * OPTION_HEIGHT;
        float menuBottomY = menuY - menuHeight / 2f;

        menuX = MathUtils.clamp(menuX, 5f, Gdx.graphics.getWidth() - MENU_WIDTH - 5f);
        menuBottomY = MathUtils.clamp(menuBottomY, 5f, Gdx.graphics.getHeight() - menuHeight - 5f);

        return new float[] { menuX, menuBottomY, menuHeight };
    }

    public void render() {
        if (!visible || item == null)
            return;

        List<String> options = getOptionsForItem();
        float[] pos = getCurrentMenuPosition();
        float menuX = pos[0];
        float menuBottomY = pos[1];
        float menuHeight = pos[2];

        ownShapeRenderer.setProjectionMatrix(hudCamera.combined);
        ownBatch.setProjectionMatrix(hudCamera.combined);

        // Hover detection
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        hoverIndex = -1;
        if (mouseX >= menuX && mouseX <= menuX + MENU_WIDTH &&
                mouseY >= menuBottomY && mouseY <= menuBottomY + menuHeight) {
            float relativeY = mouseY - menuBottomY;
            float invertedY = menuHeight - relativeY;
            int index = (int) ((invertedY - PADDING) / OPTION_HEIGHT);
            if (index >= 0 && index < options.size()) {
                hoverIndex = index;
            }
        }

        // Fundo
        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ownShapeRenderer.setColor(0f, 0f, 0f, 0.85f);
        ownShapeRenderer.rect(menuX, menuBottomY, MENU_WIDTH, menuHeight);
        ownShapeRenderer.end();

        // Borda
        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        ownShapeRenderer.setColor(1f, 1f, 1f, 0.15f);
        ownShapeRenderer.rect(menuX, menuBottomY, MENU_WIDTH, menuHeight);
        ownShapeRenderer.end();

        // Destaque do hover
        if (hoverIndex != -1) {
            ownShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            ownShapeRenderer.setColor(1f, 1f, 1f, 0.06f);
            float optionTopY = menuBottomY + menuHeight - PADDING - hoverIndex * OPTION_HEIGHT;
            float highlightY = optionTopY - OPTION_HEIGHT;
            ownShapeRenderer.rect(menuX, highlightY, MENU_WIDTH, OPTION_HEIGHT);
            ownShapeRenderer.end();
        }

        // Texto
        ownBatch.begin();
        BitmapFont font = new BitmapFont(); // Idealmente use uma fonte compartilhada
        font.setColor(Color.WHITE);
        float textX = menuX + PADDING;
        for (int i = 0; i < options.size(); i++) {
            float optionTopY = menuBottomY + menuHeight - PADDING - i * OPTION_HEIGHT;
            float baseline = optionTopY - (OPTION_HEIGHT - font.getCapHeight()) / 2f - 2f;
            baseline = Math.min(baseline, Gdx.graphics.getHeight() - 10);
            font.setColor(i == hoverIndex ? Color.YELLOW : Color.WHITE);
            font.draw(ownBatch, options.get(i), textX, baseline);
        }
        ownBatch.end();
    }

    public boolean handleClick(float screenX, float screenY) {
        if (!visible)
            return false;
        List<String> options = getOptionsForItem();
        float[] pos = getCurrentMenuPosition();
        float menuX = pos[0];
        float menuBottomY = pos[1];
        float menuHeight = pos[2];

        if (screenX < menuX || screenX > menuX + MENU_WIDTH ||
                screenY < menuBottomY || screenY > menuBottomY + menuHeight) {
            hide();
            return false;
        }
        float relativeY = screenY - menuBottomY;
        float invertedY = menuHeight - relativeY;
        int index = (int) ((invertedY - PADDING) / OPTION_HEIGHT);
        index = MathUtils.clamp(index, 0, options.size() - 1);
        triggerOption(options.get(index));
        hide();
        return true;
    }

    private void triggerOption(String option) {
        if (listener == null || item == null)
            return;
        switch (option) {
            case "Descartar":
                listener.onDrop(item);
                break;
            case "Mover":
                listener.onMove(item);
                break;
            case "Craft":
                listener.onCraft(item);
                break;
            case "Equipar":
                listener.onEquip(item);
                break;
            case "Desequipar":
                listener.onUnequip(item);
                break;
        }
    }

    public void dispose() {
        ownBatch.dispose();
        ownShapeRenderer.dispose();
    }
}