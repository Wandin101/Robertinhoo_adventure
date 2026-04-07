package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import java.util.ArrayList;
import java.util.List;

public class ItemDetailModal {
    private Window window;
    private final ShopModel model;
    private final ShopUI shopUI;
    private final Skin skin;

    private Texture currentIconTexture;

    private Texture panelTexture;
    private Texture panelInnerTexture;
    private Texture barBgTexture;
    private Texture barFillTexture;
    private Texture lineTexture;

    // Navegação interna
    private List<TextButton> actionButtons = new ArrayList<>();
    private int selectedButtonIndex = 0;
    private com.badlogic.gdx.scenes.scene2d.InputListener keyListener;

    public ItemDetailModal(Skin skin, ShopModel model, ShopUI shopUI) {
        this.skin = skin;
        this.model = model;
        this.shopUI = shopUI;
        ensureUiTextures();
    }

    public void show(ShopModel.ShopItem item, int row, int col) {
        close();

        window = new Window("", skin);
        window.setModal(true);
        window.setMovable(false);
        window.setResizable(false);
        window.setBackground(drawable(panelTexture));

        float width = 560;
        float height = 560; // altura reduzida (remoção das seções extras)
        window.setSize(width, height);
        window.setPosition(
                (Gdx.graphics.getWidth() - width) / 2f,
                (Gdx.graphics.getHeight() - height) / 2f);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(14);

        // ================= HEADER =================
        Table header = new Table();
        header.setBackground(drawable(panelInnerTexture));
        header.pad(8, 10, 8, 10);

        Label title = new Label(item.name.toUpperCase(), skin);
        title.setAlignment(Align.center);
        title.setFontScale(2.05f);
        title.setColor(Color.WHITE);

        TextButton closeBtn = new TextButton("X", skin);
        closeBtn.getLabel().setColor(Color.WHITE);
        closeBtn.pad(0);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                close();
            }
        });

        header.add(title).expandX().center();
        header.add(closeBtn).size(30, 30).right();
        root.add(header).growX().padBottom(10).row();

        // ================= BODY =================
        Table body = new Table();
        body.setBackground(drawable(panelInnerTexture));
        body.pad(16);
        body.defaults().space(8);

        // Ícone
        disposeIconTexture();
        currentIconTexture = new Texture(item.iconPath);
        currentIconTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        Image icon = new Image(new TextureRegionDrawable(new TextureRegion(currentIconTexture)));
        icon.setScaling(Scaling.fit);
        body.add(icon).size(128, 128).padBottom(6).row();

        // Stats
        if (item.stats != null) {
            Table stats = new Table();
            stats.defaults().growX().left().padBottom(8);
            stats.add(createStatRow("DANO", item.stats.getInt("damage"), 100f,
                    String.valueOf(item.stats.getInt("damage")))).row();
            stats.add(createStatRow("CADÊNCIA", item.stats.getFloat("fireRate"), 10f,
                    item.stats.getFloat("fireRate") + "/s")).row();
            stats.add(createStatRow("CAPACIDADE", item.stats.getInt("capacity"), 20f,
                    String.valueOf(item.stats.getInt("capacity")))).row();
            body.add(stats).growX().padTop(6).row();
        }

        // Linha divisória
        Image line = new Image(drawable(lineTexture));
        body.add(line).growX().height(2).padTop(4).padBottom(6).row();

        root.add(body).grow().fill().row();
        if (item.trait != null && !item.trait.isEmpty()) {
            Label trait = new Label(item.trait, skin);
            trait.setWrap(true);
            trait.setAlignment(Align.center);
            trait.setColor(new Color(0.9f, 0.9f, 0.92f, 1f));
            body.add(trait).width(width - 70).padTop(10).row();
        }

        // ================= FOOTER =================
        Table footer = new Table();
        footer.padTop(10);

        Table priceBox = new Table();
        priceBox.setBackground(drawable(panelInnerTexture));
        priceBox.pad(8, 14, 8, 14);
        Label priceLabel = new Label("₿", skin);
        priceLabel.setColor(Color.WHITE);
        Label priceValue = new Label(String.valueOf(item.price), skin);
        priceValue.setColor(Color.WHITE);
        priceValue.setFontScale(1.55f);
        priceBox.add(priceLabel).padRight(6);
        priceBox.add(priceValue);

        TextButton buyBtn = new TextButton("COMPRAR", skin);
        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (model.buyItem(item)) {
                    close();
                    shopUI.hide();
                } else {
                    close();
                    shopUI.onPurchaseFailed(row, col);
                }
            }
        });

        footer.add(priceBox).width(100).height(44).left();
        footer.add(buyBtn).expandX().fillX().height(44).padLeft(10);
        root.add(footer).growX().padTop(10).row();

        window.add(root).expand().fill();

        // ================= NAVEGAÇÃO POR TECLADO =================
        actionButtons.clear();
        actionButtons.add(buyBtn); // índice 0
        actionButtons.add(closeBtn); // índice 1
        selectedButtonIndex = 0;
        updateButtonHighlight();

        if (keyListener != null)
            window.removeListener(keyListener);
        keyListener = new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                switch (keycode) {
                    case com.badlogic.gdx.Input.Keys.UP:
                    case com.badlogic.gdx.Input.Keys.DOWN:
                        selectedButtonIndex = (selectedButtonIndex + 1) % actionButtons.size();
                        updateButtonHighlight();
                        return true;
                    case com.badlogic.gdx.Input.Keys.ENTER:
                    case com.badlogic.gdx.Input.Keys.SPACE:
                        actionButtons.get(selectedButtonIndex).fire(new ChangeEvent());
                        return true;
                    case com.badlogic.gdx.Input.Keys.ESCAPE:
                        close();
                        return true;
                    default:
                        return false;
                }
            }
        };
        window.addListener(keyListener);

        shopUI.getStage().addActor(window);
        shopUI.getStage().setKeyboardFocus(window);
    }

    private Table createStatRow(String label, float value, float max, String rightText) {
        Table row = new Table();

        Label left = new Label(label + ":", skin);
        left.setColor(new Color(0.85f, 0.85f, 0.9f, 1f));

        Table bar = new Table();
        bar.setBackground(drawable(barBgTexture));
        bar.pad(0);

        float ratio = MathUtils.clamp(value / max, 0f, 1f);
        Image fill = new Image(drawable(barFillTexture));
        bar.add(fill).left().width(170f * ratio).height(10f);

        Label right = new Label(rightText, skin);
        right.setColor(Color.WHITE);

        row.add(left).width(110).left();
        row.add(bar).width(170).height(10).padLeft(8).padRight(8);
        row.add(right).width(70).right();

        return row;
    }

    private void ensureUiTextures() {
        if (panelTexture != null)
            return;

        panelTexture = solid(new Color(0.09f, 0.09f, 0.12f, 0.98f));
        panelInnerTexture = solid(new Color(0.15f, 0.15f, 0.20f, 1f));
        barBgTexture = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
        barFillTexture = solid(new Color(0.85f, 0.85f, 0.92f, 1f));
        lineTexture = solid(new Color(1f, 1f, 1f, 0.12f));
    }

    private Texture solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }

    private Drawable drawable(Texture texture) {
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private void disposeIconTexture() {
        if (currentIconTexture != null) {
            currentIconTexture.dispose();
            currentIconTexture = null;
        }
    }

    public boolean isVisible() {
        return window != null && window.getStage() != null;
    }

    public void close() {
        if (window != null) {
            window.remove();
            window = null;
        }
        disposeIconTexture();
    }

    private void updateButtonHighlight() {
        for (int i = 0; i < actionButtons.size(); i++) {
            TextButton btn = actionButtons.get(i);
            btn.getLabel().setColor(i == selectedButtonIndex ? Color.YELLOW : Color.WHITE);
        }
    }

    public void dispose() {
        close();
        if (panelTexture != null)
            panelTexture.dispose();
        if (panelInnerTexture != null)
            panelInnerTexture.dispose();
        if (barBgTexture != null)
            barBgTexture.dispose();
        if (barFillTexture != null)
            barFillTexture.dispose();
        if (lineTexture != null)
            lineTexture.dispose();
        panelTexture = null;
        panelInnerTexture = null;
        barBgTexture = null;
        barFillTexture = null;
        lineTexture = null;
    }
}