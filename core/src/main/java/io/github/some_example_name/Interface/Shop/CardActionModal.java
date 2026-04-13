package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CardActionModal {
    private Window window;
    private Skin skin;
    private ShopModel model;
    private ShopUI shopUI;
    private ShopModel.ShopItem item;
    private int row, col;

    private Texture bgTexture;
    private TextButton verMaisBtn, comprarBtn;
    private int selectedIndex = 0;

    public CardActionModal(Skin skin, ShopModel model, ShopUI shopUI) {
        this.skin = skin;
        this.model = model;
        this.shopUI = shopUI;
        createBackgroundTexture();
    }

    private void createBackgroundTexture() {
        // Fundo semi-transparente escuro (opcional, pode ser null para sem fundo)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.85f));
        pixmap.fill();
        bgTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void show(ShopModel.ShopItem item, int row, int col, float cardX, float cardY, float cardWidth,
            float cardHeight) {
        this.item = item;
        this.row = row;
        this.col = col;
        if (window != null)
            close();

        window = new Window("", skin);
        window.setModal(true);
        window.setMovable(false);
        window.setResizable(false);
        window.pad(0);
        // Remover borda padrão da window
        window.setBackground((Drawable) null);

        // Container principal com fundo opcional
        Table content = new Table();
        content.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        content.pad(8, 12, 8, 12);
        content.defaults().pad(4).width(140).height(36);

        // Botão VER MAIS
        verMaisBtn = new TextButton("VER MAIS", skin);
        verMaisBtn.getLabel().setFontScale(0.5f);
        verMaisBtn.getLabel().setColor(Color.WHITE);
        verMaisBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectVerMais();
            }
        });

        // Botão COMPRAR
        comprarBtn = new TextButton("COMPRAR (" + item.price + ")", skin);
        comprarBtn.getLabel().setFontScale(0.5f);
        comprarBtn.getLabel().setColor(Color.WHITE);
        comprarBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectComprar();
            }
        });

        content.add(verMaisBtn).row();
        content.add(comprarBtn).row();

        window.add(content);
        window.pack();

        float posX = cardX + cardWidth + 8;
        float posY = cardY + (cardHeight / 2) - (window.getHeight() / 2);
        if (posX + window.getWidth() > Gdx.graphics.getWidth()) {
            posX = cardX - window.getWidth() - 8;
        }
        posY = Math.max(5, Math.min(posY, Gdx.graphics.getHeight() - window.getHeight() - 5));
        window.setPosition(posX, posY);

        shopUI.getStage().addActor(window);
        selectedIndex = 0;
        updateHighlight();
        shopUI.getStage().setKeyboardFocus(window);
    }

    private void updateHighlight() {
        verMaisBtn.getLabel().setColor(selectedIndex == 0 ? Color.YELLOW : Color.WHITE);
        comprarBtn.getLabel().setColor(selectedIndex == 1 ? Color.YELLOW : Color.WHITE); // comprar sempre amarelo
    }

    public void navigateUp() {
        selectedIndex = (selectedIndex - 1 + 2) % 2;
        updateHighlight();
    }

    public void navigateDown() {
        selectedIndex = (selectedIndex + 1) % 2;
        updateHighlight();
    }

    public void select() {
        if (selectedIndex == 0)
            selectVerMais();
        else
            selectComprar();
    }

    private void selectVerMais() {
        close();
        shopUI.showEsmeraldaOpinion(item);
        shopUI.showItemDetails(item, row, col);
    }

    private void selectComprar() {
        close();
        if (model.buyItem(item)) {
            shopUI.onPurchaseSuccess(row, col);
        } else {
            switch (model.getLastError()) {
                case INSUFFICIENT_SOULS:
                    shopUI.onPurchaseFailed(row, col);
                    break;
                case INVENTORY_FULL:
                    shopUI.onInventoryFull();
                    break;
                default:
                    shopUI.onPurchaseFailed(row, col);
                    break;
            }
        }
    }

    public void close() {
        if (window != null) {
            window.remove();
            window = null;
        }
    }

    public boolean isVisible() {
        return window != null && window.getStage() != null;
    }

    public void dispose() {
        close();
        if (bgTexture != null)
            bgTexture.dispose();
    }
}