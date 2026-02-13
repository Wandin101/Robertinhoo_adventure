package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;

import java.util.List;
import java.util.Map;

public class CraftingRenderer {
    private final BitmapFont font;
    private final Vector2 inventoryPosition;
    private final OrthographicCamera hudCamera; // 🔥 Câmera HUD recebida do MapRenderer

    private final SpriteBatch ownBatch; // 🔥 Batch próprio
    private final ShapeRenderer ownShapeRenderer; // 🔥 ShapeRenderer próprio

    private final float recipeSpacing = 35f;
    private final float iconSize = 24f;
    private final float padding = 8f;
    private final float arrowWidth = 20f;

    public CraftingRenderer(Vector2 inventoryPosition, OrthographicCamera hudCamera) {
        this.inventoryPosition = inventoryPosition;
        this.hudCamera = hudCamera;
        this.ownBatch = new SpriteBatch();
        this.ownShapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont(); // Fonte padrão (pode ser substituída por uma recebida)
    }

    public void render(List<CraftingRecipe> recipes, CraftingRecipe selected,
            int selectedGridX, int selectedGridY, Item selectedItem) {
        if (selectedItem == null || recipes == null || recipes.isEmpty())
            return;

        float menuX = inventoryPosition.x;
        float menuY = inventoryPosition.y + 150;
        float menuWidth = 320f;
        float menuHeight = 200f;

        // ===== PROJEÇÃO COM A HUD CAMERA =====
        ownShapeRenderer.setProjectionMatrix(hudCamera.combined);
        ownBatch.setProjectionMatrix(hudCamera.combined);

        // ===== FUNDO E BORDA =====
        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ownShapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.85f);
        ownShapeRenderer.rect(menuX, menuY, menuWidth, menuHeight);
        ownShapeRenderer.end();

        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        ownShapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.5f);
        ownShapeRenderer.rect(menuX, menuY, menuWidth, menuHeight);
        ownShapeRenderer.end();

        renderSelectionHighlights(recipes, selected, menuX + padding, menuY + padding, menuWidth - 2 * padding);

        // ===== TEXTO E ÍCONES =====
        ownBatch.begin();
        renderRecipeList(recipes, selected, menuX + padding, menuY + padding, menuWidth - 2 * padding);
        renderInstructions(menuX + padding, menuY);
        ownBatch.end();
    }

    private void renderSelectionHighlights(List<CraftingRecipe> recipes, CraftingRecipe selected,
            float startX, float startY, float width) {
        float yPos = startY + 140;
        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ownShapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.7f);
        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe recipe = recipes.get(i);
            if (recipe == selected) {
                ownShapeRenderer.rect(startX - 5, yPos - 25, width, recipeSpacing);
            }
            yPos -= recipeSpacing;
        }
        ownShapeRenderer.end();
    }

    private void renderRecipeList(List<CraftingRecipe> recipes, CraftingRecipe selected,
            float startX, float startY, float width) {
        font.setColor(Color.WHITE);
        font.draw(ownBatch, "RECEITAS DISPONÍVEIS:", startX, startY + 170);

        float yPos = startY + 140;
        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe recipe = recipes.get(i);
            renderRecipe(recipe, startX, yPos, recipe == selected);
            yPos -= recipeSpacing;
        }
    }

    private void renderRecipe(CraftingRecipe recipe, float x, float y, boolean isSelected) {
        float currentX = x;
        font.setColor(isSelected ? Color.YELLOW : Color.LIGHT_GRAY);

        // Ingredientes
        boolean first = true;
        for (Map.Entry<Class<? extends Item>, Integer> entry : recipe.getIngredients().entrySet()) {
            if (!first) {
                font.draw(ownBatch, "+", currentX, y);
                currentX += 15;
            }
            first = false;

            font.draw(ownBatch, entry.getValue() + "x", currentX, y);
            currentX += 25;

            TextureRegion icon = recipe.getIngredientIcon(entry.getKey());
            if (icon != null) {
                ownBatch.draw(icon, currentX, y - iconSize / 2 - 5, iconSize, iconSize);
            }
            currentX += iconSize + 5;

            font.draw(ownBatch, getItemName(entry.getKey()), currentX, y);
            currentX += getItemName(entry.getKey()).length() * 7 + 10;
        }

        // Seta
        font.draw(ownBatch, "-->", currentX, y);
        currentX += arrowWidth;

        // Resultado
        font.draw(ownBatch, recipe.getResultQuantity() + "x", currentX, y);
        currentX += 25;

        TextureRegion resultIcon = recipe.getResultIcon();
        if (resultIcon != null) {
            ownBatch.draw(resultIcon, currentX, y - iconSize / 2 - 5, iconSize, iconSize);
        }
        currentX += iconSize + 5;

        font.setColor(isSelected ? Color.GREEN : Color.WHITE);
        font.draw(ownBatch, recipe.getResult().getName(), currentX, y);
    }

    private void renderInstructions(float x, float y) {
        font.setColor(Color.LIGHT_GRAY);
        font.draw(ownBatch, "↑↓: Navegar", x, y);
        font.draw(ownBatch, "ENTER: Craftar", x + 120, y);
        font.draw(ownBatch, "ESC: Sair", x + 240, y);
    }

    private String getItemName(Class<? extends Item> itemClass) {
        if (itemClass == PolvoraBruta.class)
            return "Pólvora Bruta";
        if (itemClass == Ammo9mm.class)
            return "Munição 9mm";
        return "Item";
    }

    public void dispose() {
        ownBatch.dispose();
        ownShapeRenderer.dispose();
        font.dispose();
    }
}