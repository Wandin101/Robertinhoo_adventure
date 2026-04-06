package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class ItemDetailModal {
    private Window window;
    private ShopModel model;
    private ShopUI shopUI;
    private Skin skin;
    private Texture modalTexture; // para não recarregar toda vez

    public ItemDetailModal(Skin skin, ShopModel model, ShopUI shopUI) {
        this.skin = skin;
        this.model = model;
        this.shopUI = shopUI;
    }

    public void show(ShopModel.ShopItem item, int row, int col) {
        if (window != null)
            window.remove();

        window = new Window("", skin);
        window.setModal(true);
        window.setMovable(false);

        float width = 420;
        float height = 520;

        window.setSize(width, height);
        window.setPosition(
                (Gdx.graphics.getWidth() - width) / 2,
                (Gdx.graphics.getHeight() - height) / 2);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(15);

        // ================= HEADER =================
        Label title = new Label(item.name, skin);
        title.setAlignment(Align.center);

        root.add(title).expandX().fillX().padBottom(10).row();

        // ================= ICON =================
        Texture iconTex = new Texture(item.iconPath);
        Image icon = new Image(new TextureRegionDrawable(new TextureRegion(iconTex)));

        root.add(icon).size(96, 96).padBottom(15).row();

        // ================= STATS =================
        Table statsTable = new Table();

        if (item.stats != null) {
            statsTable.add(new Label("Dano:", skin)).left();
            statsTable.add(new Label(String.valueOf(item.stats.getInt("damage")), skin)).right().row();

            statsTable.add(new Label("Cadência:", skin)).left();
            statsTable.add(new Label(item.stats.getFloat("fireRate") + "/s", skin)).right().row();

            statsTable.add(new Label("Capacidade:", skin)).left();
            statsTable.add(new Label(String.valueOf(item.stats.getInt("capacity")), skin)).right().row();
        }

        root.add(statsTable).expandX().fillX().padBottom(15).row();

        // ================= TRAIT =================
        if (item.trait != null && !item.trait.isEmpty()) {
            Label trait = new Label(item.trait, skin);
            trait.setWrap(true);

            root.add(trait).width(width - 40).padBottom(10).row();
        }

        // ================= QUOTE =================
        if (item.esmeraldaQuote != null && !item.esmeraldaQuote.isEmpty()) {
            Label quote = new Label("\"" + item.esmeraldaQuote + "\"", skin);
            quote.setWrap(true);
            quote.setAlignment(Align.center);
            quote.setColor(Color.YELLOW);

            root.add(quote).width(width - 40).padBottom(20).row();
        }

        // ================= BOTÕES =================
        Table buttons = new Table();

        TextButton buyBtn = new TextButton("Comprar (" + item.price + ")", skin);
        TextButton closeBtn = new TextButton("Fechar", skin);

        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (model.buyItem(item)) {
                    window.remove();
                    shopUI.hide();
                } else {
                    window.remove();
                    shopUI.animateCardError(row, col);
                }
            }
        });

        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                window.remove();
            }
        });

        buttons.add(buyBtn).expandX().fillX().padRight(10);
        buttons.add(closeBtn).expandX().fillX();

        root.add(buttons).expandY().bottom().fillX();

        window.add(root).expand().fill();
        shopUI.getStage().addActor(window);
    }

    public boolean isVisible() {
        return window != null && window.getStage() != null;
    }

    public void close() {
        if (window != null)
            window.remove();
    }
}