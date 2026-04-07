package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.some_example_name.Interface.NpcInteractionHUD;
import io.github.some_example_name.Interface.Npcs.EsmeraldaDialogue;
import io.github.some_example_name.Interface.Npcs.NpcDialogue;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;

public class ShopController {
    private ShopUI ui;
    private ShopModel model;
    private boolean visible = false;

    private enum FocusArea {
        CATEGORIES, GRID
    }

    private FocusArea currentArea = FocusArea.CATEGORIES;
    private int selectedCategoryIndex = 0;
    private int selectedRow = 0, selectedCol = 0;

    private static final int CATEGORIES_COUNT = 4;
    private static final int GRID_COLS = 3, GRID_ROWS = 2;

    public ShopController(ShopUI ui, ShopModel model) {
        this.ui = ui;
        this.model = model;
    }

    public void update(float delta) {
        if (!visible)
            return;

        if (ui.isActionModalVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START, 0.5f);
                ui.getActionModal().navigateUp(); // ou navigateDown, tanto faz
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                ui.getActionModal().select();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                ui.getActionModal().close();
            }
            return; // não processa navegação normal
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START, 0.5f);
            if (currentArea == FocusArea.CATEGORIES) {
                selectedCategoryIndex = (selectedCategoryIndex - 1 + CATEGORIES_COUNT) % CATEGORIES_COUNT;
                ui.highlightCategory(selectedCategoryIndex);
            } else { // GRID
                selectedRow = (selectedRow - 1 + GRID_ROWS) % GRID_ROWS;
                ui.highlightCard(selectedRow, selectedCol);
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START, 0.5f);
            if (currentArea == FocusArea.CATEGORIES) {
                selectedCategoryIndex = (selectedCategoryIndex + 1) % CATEGORIES_COUNT;
                ui.highlightCategory(selectedCategoryIndex);
            } else {
                selectedRow = (selectedRow + 1) % GRID_ROWS;
                ui.highlightCard(selectedRow, selectedCol);
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START, 0.5f);
            if (currentArea == FocusArea.GRID && selectedCol == 0) {
                // move para categorias
                currentArea = FocusArea.CATEGORIES;
                ui.highlightCategory(selectedCategoryIndex);
                ui.clearCardHighlight();
            } else if (currentArea == FocusArea.GRID && selectedCol > 0) {
                selectedCol--;
                ui.highlightCard(selectedRow, selectedCol);
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.ITEM_DRAG_START, 0.5f);
            if (currentArea == FocusArea.CATEGORIES) {
                // move para grade, primeiro card
                currentArea = FocusArea.GRID;
                selectedRow = 0;
                selectedCol = 0;
                ui.highlightCard(selectedRow, selectedCol);
            } else if (currentArea == FocusArea.GRID && selectedCol < GRID_COLS - 1) {
                selectedCol++;
                ui.highlightCard(selectedRow, selectedCol);
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (currentArea == FocusArea.CATEGORIES) {
                if (selectedCategoryIndex == 3) { // Sair
                    ui.hide();
                } else {
                    String[] categories = { "weapon", "ammo", "other" };
                    model.setCurrentCategory(categories[selectedCategoryIndex]);
                    ui.updateCards();
                    currentArea = FocusArea.GRID;
                    selectedRow = 0;
                    selectedCol = 0;
                    ui.highlightCard(selectedRow, selectedCol);
                }
            } else { // GRID
                int index = selectedRow * GRID_COLS + selectedCol;
                ShopModel.ShopItem item = model.getItemAt(index);
                if (item != null) {
                    // Exibe o modal de ações (Ver mais / Comprar)
                    ui.showCardActions(item, selectedRow, selectedCol);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            if (currentArea == FocusArea.GRID) {
                int index = selectedRow * GRID_COLS + selectedCol;
                ShopModel.ShopItem item = model.getItemAt(index);
                if (item != null) {
                    ui.showItemDetails(item, selectedRow, selectedCol);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            hide();
        }
    }

    public void show() {
        visible = true;
        currentArea = FocusArea.CATEGORIES;
        selectedCategoryIndex = 0;
        selectedRow = 0;
        selectedCol = 0;
        ui.highlightCategory(0);
        ui.clearCardHighlight();
        model.filterItemsByCategory();
        ui.updateCards();
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void notifyCategoryChanged() {
        // chamado quando usuário clica em categoria com mouse
        currentArea = FocusArea.GRID;
        selectedRow = 0;
        selectedCol = 0;
        ui.highlightCard(0, 0);
        ui.clearCardHighlight(); // garante que só o card fique destacado
        ui.highlightCard(0, 0);
    }
}