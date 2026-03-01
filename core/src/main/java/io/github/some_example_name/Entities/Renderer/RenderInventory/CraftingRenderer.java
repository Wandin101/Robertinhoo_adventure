package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Fonts.FontsManager;

import java.util.List;
import java.util.Map;

public class CraftingRenderer {
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Vector2 inventoryPosition;
    private final OrthographicCamera hudCamera;

    private final SpriteBatch ownBatch;
    private final ShapeRenderer ownShapeRenderer;

    private static final float PADDING = 12f;
    private static final float RECIPE_SPACING = 40f; // espaço entre receitas
    private static final float ICON_SIZE = 24f;
    private static final float ARROW_WIDTH = 30f;
    private static final float MIN_MODAL_WIDTH = 500f; // largura mínima do modal

    public CraftingRenderer(Vector2 inventoryPosition, OrthographicCamera hudCamera) {
        this.inventoryPosition = inventoryPosition;
        this.hudCamera = hudCamera;
        this.ownBatch = new SpriteBatch();
        this.ownShapeRenderer = new ShapeRenderer();
        // Usar a fonte do inventário (já dimensionada)
        this.font = FontsManager.createInventoryFont(); // pode precisar de ajuste
        this.layout = new GlyphLayout();
    }

    public void render(List<CraftingRecipe> recipes, CraftingRecipe selected,
            int selectedGridX, int selectedGridY, Item selectedItem) {
        if (selectedItem == null || recipes == null || recipes.isEmpty())
            return;

        // Calcular dimensões do modal com base no conteúdo
        float maxRecipeWidth = 0f;
        for (CraftingRecipe recipe : recipes) {
            float recipeWidth = calculateRecipeWidth(recipe);
            if (recipeWidth > maxRecipeWidth)
                maxRecipeWidth = recipeWidth;
        }
        float modalWidth = Math.max(maxRecipeWidth + 2 * PADDING, MIN_MODAL_WIDTH);
        float modalHeight = PADDING * 2 + recipes.size() * RECIPE_SPACING + 30; // título + instruções

        // Posicionar o modal ao lado do inventário (ex: à direita)
        float modalX = inventoryPosition.x + 400; // ajuste conforme necessário
        float modalY = Gdx.graphics.getHeight() - modalHeight - 50; // fixo no topo

        // Desenhar fundo
        ownShapeRenderer.setProjectionMatrix(hudCamera.combined);
        ownBatch.setProjectionMatrix(hudCamera.combined);

        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ownShapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        ownShapeRenderer.rect(modalX, modalY, modalWidth, modalHeight);
        ownShapeRenderer.end();

        ownShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        ownShapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        ownShapeRenderer.rect(modalX, modalY, modalWidth, modalHeight);
        ownShapeRenderer.end();

        // Desenhar título
        ownBatch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "RECEITAS DISPONÍVEIS");
        float titleX = modalX + (modalWidth - layout.width) / 2;
        font.draw(ownBatch, layout, titleX, modalY + modalHeight - PADDING);

        // Desenhar receitas
        float yPos = modalY + modalHeight - PADDING - 30; // abaixo do título
        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe recipe = recipes.get(i);
            boolean isSelected = (recipe == selected);

            // Destacar se selecionada
            if (isSelected) {
                ownBatch.end(); // pausar batch para shape
                ownShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                ownShapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f);
                ownShapeRenderer.rect(modalX + 2, yPos - RECIPE_SPACING + 5,
                        modalWidth - 4, RECIPE_SPACING - 10);
                ownShapeRenderer.end();
                ownBatch.begin();
            }

            renderRecipe(recipe, modalX + PADDING, yPos, isSelected, modalWidth - 2 * PADDING);
            yPos -= RECIPE_SPACING;
        }

        // Instruções na parte inferior
        String instructions = "↑↓: Navegar   ENTER: Craftar   ESC: Sair";
        layout.setText(font, instructions);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(ownBatch, layout,
                modalX + (modalWidth - layout.width) / 2,
                modalY + PADDING + layout.height);

        ownBatch.end();
    }

    /** Calcula a largura necessária para desenhar uma receita em uma linha */
    private float calculateRecipeWidth(CraftingRecipe recipe) {
        float width = 0f;

        // Ingredientes
        for (Map.Entry<Class<? extends Item>, Integer> entry : recipe.getIngredients().entrySet()) {
            if (width > 0)
                width += 20; // espaço para o "+"
            String qty = entry.getValue() + "x";
            layout.setText(font, qty);
            width += layout.width + 5; // espaço após quantidade

            width += ICON_SIZE + 5;

            String itemName = getItemName(entry.getKey());
            layout.setText(font, itemName);
            width += layout.width + 15; // espaço após nome
        }

        // Seta
        layout.setText(font, "-->");
        width += layout.width + 15;

        // Resultado
        String resQty = recipe.getResultQuantity() + "x";
        layout.setText(font, resQty);
        width += layout.width + 5;

        width += ICON_SIZE + 5;

        String resName = recipe.getResult().getName();
        layout.setText(font, resName);
        width += layout.width;

        return width;
    }

    /** Renderiza uma única receita em uma posição (x,y) com largura disponível */
    private void renderRecipe(CraftingRecipe recipe, float x, float y, boolean isSelected, float availableWidth) {
        float startX = x;
        float currentX = startX;

        font.setColor(isSelected ? Color.YELLOW : Color.WHITE);

        // Ingredientes
        boolean first = true;
        for (Map.Entry<Class<? extends Item>, Integer> entry : recipe.getIngredients().entrySet()) {
            if (!first) {
                // Desenha '+'
                layout.setText(font, "+");
                font.draw(ownBatch, layout, currentX, y);
                currentX += layout.width + 8;
            }
            first = false;

            // Quantidade
            String qty = entry.getValue() + "x";
            layout.setText(font, qty);
            font.draw(ownBatch, layout, currentX, y);
            currentX += layout.width + 5;

            // Ícone
            TextureRegion icon = recipe.getIngredientIcon(entry.getKey());
            if (icon != null) {
                ownBatch.draw(icon, currentX, y - ICON_SIZE / 2 - 2, ICON_SIZE, ICON_SIZE);
            }
            currentX += ICON_SIZE + 5;

            // Nome do item
            String itemName = getItemName(entry.getKey());
            layout.setText(font, itemName);
            font.draw(ownBatch, layout, currentX, y);
            currentX += layout.width + 15; // espaço extra antes do próximo
        }

        // Seta
        layout.setText(font, "-->");
        font.draw(ownBatch, layout, currentX, y);
        currentX += layout.width + 15;

        // Resultado
        String resQty = recipe.getResultQuantity() + "x";
        layout.setText(font, resQty);
        font.draw(ownBatch, layout, currentX, y);
        currentX += layout.width + 5;

        TextureRegion resultIcon = recipe.getResultIcon();
        if (resultIcon != null) {
            ownBatch.draw(resultIcon, currentX, y - ICON_SIZE / 2 - 2, ICON_SIZE, ICON_SIZE);
        }
        currentX += ICON_SIZE + 5;

        String resName = recipe.getResult().getName();
        layout.setText(font, resName);
        font.setColor(isSelected ? Color.GREEN : Color.WHITE);
        font.draw(ownBatch, layout, currentX, y);
    }

    private String getItemName(Class<? extends Item> itemClass) {
        if (itemClass == PolvoraBruta.class)
            return "Pólvora Bruta";
        if (itemClass == Ammo9mm.class)
            return "Munição 9mm";
        // Se for genérico, usar nome simples da classe
        return itemClass.getSimpleName().replace("Ammo", "").replace("Item", "");
    }

    public void dispose() {
        ownBatch.dispose();
        ownShapeRenderer.dispose();
        font.dispose();
    }
}