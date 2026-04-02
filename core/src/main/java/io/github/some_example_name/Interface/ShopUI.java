package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.Npcs.EsmeraldaDialogue;
import io.github.some_example_name.Interface.Npcs.NpcDialogue;
import java.util.ArrayList;
import java.util.List;

public class ShopUI {
    private Stage stage;
    private Skin skin;
    private List<ShopItem> items = new ArrayList<>();
    private List<TextButton> buttons = new ArrayList<>(); // botões de compra (para navegação)
    private List<Texture> iconTextures = new ArrayList<>(); // para liberar recursos depois
    private Robertinhoo player;
    private boolean visible = false;
    private TextButton.TextButtonStyle buttonStyle;
    private boolean purchaseMade = false;

    public ShopUI(Robertinhoo player) {
        this.player = player;
        this.skin = new Skin(Gdx.files.internal("UI/uiskin.json"));
        this.stage = new Stage(new ScreenViewport());
        loadItemsFromJson();
        createCustomButtonStyle(); // hover/foco amarelo
        createUI();
    }

    private void createCustomButtonStyle() {
        TextButton.TextButtonStyle defaultStyle = skin.get(TextButton.TextButtonStyle.class);
        buttonStyle = new TextButton.TextButtonStyle(defaultStyle);
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.focusedFontColor = Color.YELLOW;
    }

    private void loadItemsFromJson() {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("data/itens.json"));
            for (JsonValue entry : root) {
                String id = entry.getString("id");
                String name = entry.getString("name");
                int price = entry.getInt("price");
                String type = entry.getString("type");
                String iconPath = entry.getString("iconPath");
                items.add(new ShopItem(id, name, price, type, iconPath));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: se não achar o JSON, adiciona um item padrão
            items.add(new ShopItem("pistol", "Pistola", 100, "weapon",
                    "ITENS/Pistol/GUN_01_[square_frame]_01_V1.00.png"));
        }
    }

    private void createUI() {
        Table table = new Table();
        // Não usar fillParent para que a tabela não ocupe a tela toda
        table.top().padTop(50);

        Label title = new Label("Loja da Esmeralda", skin);
        title.setFontScale(1.5f);
        table.add(title).padBottom(20).row();

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);

            Texture iconTex = new Texture(item.iconPath);
            iconTextures.add(iconTex);
            Image icon = new Image(new TextureRegionDrawable(new TextureRegion(iconTex)));
            icon.setSize(48, 48);

            Label nameLabel = new Label(item.name, skin);
            nameLabel.setFontScale(1.2f);
            Label priceLabel = new Label(item.price + " almas", skin);
            priceLabel.setColor(Color.GOLD);

            Table infoTable = new Table();
            infoTable.add(nameLabel).left().row();
            infoTable.add(priceLabel).left();

            TextButton buyButton = new TextButton("Comprar", buttonStyle);
            final int idx = i;
            buyButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    buyItem(idx);
                }
            });

            table.add(icon).padRight(10).size(48, 48);
            table.add(infoTable).expandX().left(); // expandX para ocupar o espaço disponível
            table.add(buyButton).width(120).padLeft(20);
            table.row().padBottom(15);

            buttons.add(buyButton);
        }

        TextButton closeBtn = new TextButton("Sair", buttonStyle);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        table.add(closeBtn).colspan(3).padTop(20).width(200).row();
        buttons.add(closeBtn);

        // Ajusta o tamanho da tabela ao conteúdo
        table.pack();

        // Centraliza horizontalmente e mantém no topo com margem
        float screenWidth = Gdx.graphics.getWidth();
        table.setPosition((screenWidth - table.getWidth()) / 2, Gdx.graphics.getHeight() - table.getHeight() - 20);

        stage.addActor(table);
    }

    private void buyItem(int index) {
        ShopItem item = items.get(index);
        if (item.price > 0 && !player.getSoulManager().spendSouls(item.price)) {
            NpcDialogue current = NpcInteractionHUD.getInstance().getCurrentNpcDialogue();
            if (current instanceof EsmeraldaDialogue) {
                ((EsmeraldaDialogue) current).showInsufficientFundsMessage();
            }
            return;
        }
        purchaseMade = true;
        hide();
    }

    public void show() {
        visible = true;
        purchaseMade = false;
        Gdx.input.setInputProcessor(stage);
        if (!buttons.isEmpty()) {
            stage.setKeyboardFocus(buttons.get(0));
        }
        NpcDialogue current = NpcInteractionHUD.getInstance().getCurrentNpcDialogue();
        if (current instanceof EsmeraldaDialogue) {
            ((EsmeraldaDialogue) current).showShopOpenMessage();
        }
    }

    public void hide() {
        visible = false;
        Gdx.input.setInputProcessor(null);
        NpcDialogue current = NpcInteractionHUD.getInstance().getCurrentNpcDialogue();
        if (current instanceof EsmeraldaDialogue) {
            EsmeraldaDialogue ed = (EsmeraldaDialogue) current;
            if (purchaseMade) {
                ed.showShopResultMessage(true);
                purchaseMade = false;
            } else {
                ed.showShopResultMessage(false);
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void update(float delta) {
        if (!visible)
            return;
        stage.act(delta);

        // Navegação por teclado (setas, Enter)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            Actor focused = stage.getKeyboardFocus();
            if (focused instanceof TextButton) {
                int index = buttons.indexOf(focused);
                if (index > 0) {
                    stage.setKeyboardFocus(buttons.get(index - 1));
                } else {
                    stage.setKeyboardFocus(buttons.get(buttons.size() - 1));
                }
            } else if (!buttons.isEmpty()) {
                stage.setKeyboardFocus(buttons.get(0));
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            Actor focused = stage.getKeyboardFocus();
            if (focused instanceof TextButton) {
                int index = buttons.indexOf(focused);
                if (index < buttons.size() - 1) {
                    stage.setKeyboardFocus(buttons.get(index + 1));
                } else {
                    stage.setKeyboardFocus(buttons.get(0));
                }
            } else if (!buttons.isEmpty()) {
                stage.setKeyboardFocus(buttons.get(0));
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Actor focused = stage.getKeyboardFocus();
            if (focused instanceof TextButton) {
                ChangeEvent changeEvent = new ChangeEvent();
                focused.fire(changeEvent);
            }
        }
    }

    public void render() {
        if (visible)
            stage.draw();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        for (Texture tex : iconTextures)
            tex.dispose();
    }

    private static class ShopItem {
        String id, name, type, iconPath;
        int price;

        ShopItem(String id, String name, int price, String type, String iconPath) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.type = type;
            this.iconPath = iconPath;
        }
    }
}