package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Fonts.FontsManager;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.NpcInteractionHUD;
import io.github.some_example_name.Interface.Npcs.EsmeraldaDialogue;
import io.github.some_example_name.Interface.Npcs.NpcDialogue;
import java.util.ArrayList;
import java.util.List;

public class ShopUI {
    private Stage stage;
    private Skin skin;
    private List<CardButton> cardButtons = new ArrayList<>();
    private List<Texture> iconTextures = new ArrayList<>();
    private List<TextButton> categoryButtons = new ArrayList<>();
    private boolean visible = false;

    private ShopModel model;
    private ShopController controller;

    // Texturas
    private Texture moldeTexture, cardTexture, botaoTexture;
    private Texture modalTexture;

    // Dimensões
    private static final float SCALE = 5.8f;
    private static final float ORIG_MODAL_W = 124, ORIG_MODAL_H = 128;
    private static final float ORIG_CARD_W = 24, ORIG_CARD_H = 34;
    private static final float ORIG_BTN_W = 30, ORIG_BTN_H = 16;
    private static final float ORIG_LEFT_MENU_W = 30;
    private static final float PADDING = 1;

    private static final float MODAL_WIDTH = ORIG_MODAL_W * SCALE;
    private static final float MODAL_HEIGHT = ORIG_MODAL_H * SCALE;
    private static final float CARD_WIDTH = ORIG_CARD_W * SCALE;
    private static final float CARD_HEIGHT = ORIG_CARD_H * SCALE;
    private static final float BUTTON_WIDTH = ORIG_BTN_W * SCALE;
    private static final float BUTTON_HEIGHT = ORIG_BTN_H * SCALE;
    private static final float LEFT_MENU_WIDTH = ORIG_LEFT_MENU_W * SCALE;
    private static final float CARDS_AREA_WIDTH = 3 * CARD_WIDTH + 2 * (PADDING * SCALE);
    private static final float CARDS_AREA_HEIGHT = 2 * CARD_HEIGHT + 1 * (PADDING * SCALE);

    private static final int COLS = 3, ROWS = 2;

    private Robertinhoo player;
    private ItemDetailModal detailModal;

    public ShopUI(Robertinhoo player) {
        this.player = player;
        this.model = new ShopModel(player);
        this.controller = new ShopController(this, model);
        loadCustomTextures();
        createSkin();
        this.stage = new Stage(new ScreenViewport());
        this.detailModal = new ItemDetailModal(skin, model, this);
        createUI();
    }

    private void loadCustomTextures() {
        moldeTexture = new Texture("HUD/loja_molde.png");
        cardTexture = new Texture("HUD/card_shop.png");
        botaoTexture = new Texture("HUD/botao_loja.png");
        modalTexture = new Texture("HUD/detailModal.png");
    }

    private void createSkin() {
        skin = new Skin();
        int normalSize = (int) (2 * SCALE);
        int smallSize = (int) (2 * SCALE);
        int buttonSize = (int) (4 * SCALE);

        BitmapFont normalFont = FontsManager.getInstance().getMenuFont(normalSize, Color.WHITE);
        BitmapFont smallFont = FontsManager.getInstance().getMenuFont(smallSize, Color.WHITE);
        BitmapFont buttonFont = FontsManager.getInstance().getMenuFont(buttonSize, Color.WHITE);

        skin.add("default-font", normalFont);
        skin.add("small-font", smallFont);
        skin.add("button-font", buttonFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture whiteTex = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable whiteDrawable = new TextureRegionDrawable(new TextureRegion(whiteTex));
        TextureRegionDrawable botaoDrawable = new TextureRegionDrawable(new TextureRegion(botaoTexture));

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = normalFont;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        Label.LabelStyle smallLabelStyle = new Label.LabelStyle();
        smallLabelStyle.font = smallFont;
        smallLabelStyle.fontColor = Color.WHITE;
        skin.add("small", smallLabelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = botaoDrawable;
        buttonStyle.down = botaoDrawable;
        buttonStyle.over = botaoDrawable;
        skin.add("default", buttonStyle);

        TextButton.TextButtonStyle buyButtonStyle = new TextButton.TextButtonStyle();
        buyButtonStyle.font = smallFont;
        buyButtonStyle.fontColor = Color.YELLOW;
        buyButtonStyle.up = botaoDrawable;
        buyButtonStyle.down = botaoDrawable;
        buyButtonStyle.over = botaoDrawable;
        skin.add("buy", buyButtonStyle);

        TextureRegionDrawable modalBackground = new TextureRegionDrawable(new TextureRegion(modalTexture));
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = normalFont;
        windowStyle.titleFontColor = Color.YELLOW;
        windowStyle.background = modalBackground;
        skin.add("default", windowStyle);
    }

    private void createUI() {
        Image background = new Image(new TextureRegionDrawable(new TextureRegion(moldeTexture)));
        background.setSize(MODAL_WIDTH, MODAL_HEIGHT);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float bgX = (screenWidth - MODAL_WIDTH) / 2f;
        float bgY = screenHeight - MODAL_HEIGHT - 20;
        background.setPosition(bgX, bgY);
        stage.addActor(background);

        Table mainTable = new Table();
        mainTable.setSize(MODAL_WIDTH, MODAL_HEIGHT);
        mainTable.setPosition(background.getX(), background.getY());
        mainTable.padTop(15 * SCALE);

        Table leftMenu = new Table();
        leftMenu.top().pad(5 * SCALE);

        // Botões de categoria
        addCategoryButton(leftMenu, "Armas", "weapon");
        addCategoryButton(leftMenu, "Munições", "ammo");
        addCategoryButton(leftMenu, "Outros", "other");

        // Botão Sair (adicional)
        TextButton btnSair = new TextButton("Sair", skin);
        btnSair.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        leftMenu.add(btnSair).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(10 * SCALE).row();
        categoryButtons.add(btnSair);

        Table rightGrid = new Table();
        rightGrid.top().pad(5 * SCALE);
        rightGrid.setSize(CARDS_AREA_WIDTH, CARDS_AREA_HEIGHT);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                CardButton card = new CardButton();
                cardButtons.add(card);
                rightGrid.add(card).size(CARD_WIDTH, CARD_HEIGHT).pad(PADDING * SCALE);
                if (col == COLS - 1)
                    rightGrid.row();
            }
        }

        mainTable.add(leftMenu).width(LEFT_MENU_WIDTH + 30f).expandY().top();
        mainTable.add(rightGrid).width(CARDS_AREA_WIDTH).height(CARDS_AREA_HEIGHT).top();
        stage.addActor(mainTable);

        updateCards();
    }

    private void addCategoryButton(Table table, String label, String category) {
        TextButton button = new TextButton(label, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.setCurrentCategory(category);
                updateCards();
                controller.notifyCategoryChanged();
            }
        });
        table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(10 * SCALE).row();
        categoryButtons.add(button);
    }

    public void updateCards() {
        List<ShopModel.ShopItem> items = model.getFilteredItems();
        for (int i = 0; i < cardButtons.size(); i++) {
            ShopModel.ShopItem item = (i < items.size()) ? items.get(i) : null;
            cardButtons.get(i).setItem(item);
        }
    }

    public void highlightCategory(int index) {
        for (int i = 0; i < categoryButtons.size(); i++) {
            categoryButtons.get(i).setColor(i == index ? Color.YELLOW : Color.WHITE);
        }
    }

    public void highlightCard(int row, int col) {
        int idx = row * COLS + col;
        for (int i = 0; i < cardButtons.size(); i++) {
            cardButtons.get(i).setSelected(i == idx);
        }
    }

    public void clearCardHighlight() {
        for (CardButton card : cardButtons) {
            card.setSelected(false);
        }
    }

    // Classe interna do card
    private class CardButton extends Table {
        private ShopModel.ShopItem item;
        private Image iconImage;
        private Label nameLabel, priceLabel;
        private TextButton buyButton;
        private boolean selected = false;

        public CardButton() {
            setBackground(new TextureRegionDrawable(new TextureRegion(cardTexture)));
            float iconSize = CARD_WIDTH * 0.4f;
            iconImage = new Image();
            iconImage.setSize(iconSize, iconSize);

            nameLabel = new Label("", skin, "small");
            nameLabel.setAlignment(Align.center);
            priceLabel = new Label("", skin, "small");
            priceLabel.setAlignment(Align.center);

            buyButton = new TextButton("Comprar", skin, "buy");
            buyButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (item != null) {
                        boolean success = model.buyItem(item);
                        if (success) {
                            hide();
                        } else {
                            NpcDialogue current = NpcInteractionHUD.getInstance().getCurrentNpcDialogue();
                            if (current instanceof EsmeraldaDialogue) {
                                ((EsmeraldaDialogue) current).showInsufficientFundsMessage();
                            }
                        }
                    }
                }
            });

            add(iconImage).size(iconSize, iconSize).padTop(2 * SCALE).row();
            add(nameLabel).padTop(1 * SCALE).width(CARD_WIDTH - 6 * SCALE).row();
            add(priceLabel).padTop(0).row();
            add(buyButton).size(CARD_WIDTH * 0.6f, BUTTON_HEIGHT * 0.5f).padBottom(2 * SCALE);
        }

        public void setItem(ShopModel.ShopItem item) {
            this.item = item;
            if (item != null) {
                Texture iconTex = new Texture(item.iconPath);
                iconTextures.add(iconTex);
                iconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(iconTex)));
                String shortName = item.name.length() > 12 ? item.name.substring(0, 10) + ".." : item.name;
                nameLabel.setText(shortName);
                priceLabel.setText(item.price + "al");
                buyButton.setVisible(true);
            } else {
                iconImage.setDrawable(null);
                nameLabel.setText("");
                priceLabel.setText("");
                buyButton.setVisible(false);
            }
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            setColor(selected ? Color.YELLOW : Color.WHITE);
        }

        public boolean isSelected() {
            return selected;
        }
    }

    public void update(float delta) {
        if (!visible)
            return;
        stage.act(delta);
        controller.update(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (detailModal.isVisible()) {
                detailModal.close();
            } else {
                hide();
            }
        }
    }

    public void render() {
        if (visible)
            stage.draw();
    }

    public void show() {
        visible = true;
        Gdx.input.setInputProcessor(stage);
        controller.show();
        NpcDialogue current = NpcInteractionHUD.getInstance().getCurrentNpcDialogue();
        if (current instanceof EsmeraldaDialogue) {
            ((EsmeraldaDialogue) current).showShopOpenMessage();
        }
    }

    public void hide() {
        visible = false;
        Gdx.input.setInputProcessor(null);
        controller.hide();
        NpcDialogue current = NpcInteractionHUD.getInstance().getCurrentNpcDialogue();
        if (current instanceof EsmeraldaDialogue) {
            EsmeraldaDialogue ed = (EsmeraldaDialogue) current;
            ed.showShopResultMessage(model.wasPurchaseMade());
            model.resetPurchaseFlag();
        }
    }

    public void animateCardError(int row, int col) {
        int index = row * COLS + col;
        if (index >= 0 && index < cardButtons.size()) {
            CardButton card = cardButtons.get(index);
            card.addAction(
                    Actions.sequence(
                            Actions.color(Color.RED, 0.1f),
                            Actions.color(Color.WHITE, 0.1f),
                            Actions.color(Color.RED, 0.1f),
                            Actions.color(Color.WHITE, 0.1f),
                            Actions.run(() -> card.setSelected(card.isSelected()))));
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public Stage getStage() {
        return stage;
    }

    public void showItemDetails(ShopModel.ShopItem item, int row, int col) {
        if (item != null) {
            detailModal.show(item, row, col);
        }
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        moldeTexture.dispose();
        cardTexture.dispose();
        botaoTexture.dispose();
        modalTexture.dispose();
        for (Texture tex : iconTextures)
            tex.dispose();
    }
}