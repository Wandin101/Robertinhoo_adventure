package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.MapConfig.Mapa;

public class StaticItem {
    public enum ItemType {
        RUSTY_SWORD,
        // Adicione mais tipos aqui no futuro
        BROKEN_SHIELD,
        OLD_BOOK,
        BOULDER,
        BONE
    }

    private Vector2 position; // Posição em tiles (já invertida)
    private Texture texture; // Textura do item
    private Texture groundTexture; // Textura do solo (pode ser nula)
    private ItemType type;
    private Mapa mapa;
    private Body body;

    public StaticItem(Mapa mapa, float tileX, float tileY, ItemType type) {
        this.mapa = mapa;
        this.type = type;

        // Inversão de Y (mesmo da cabana)
        this.position = new Vector2(tileX, mapa.mapHeight - 1 - tileY);

        System.out.println("🗡️ Item estático criado: " + type + " em Tile: " + tileX + "," + tileY +
                " | Mundo invertido: " + position);

        loadTextures(); // Agora carrega item e solo
        createPhysicsBody();
    }

    private void loadTextures() {
        // Carrega a textura do item
        try {
            switch (type) {
                case RUSTY_SWORD:
                    texture = new Texture(Gdx.files.internal("sala_0/Pedaço_da_lore/espadaQuebrada.png"));
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de item não suportado: " + type);
            }
            System.out.println("✅ Textura do item carregada: " + texture.getWidth() + "x" + texture.getHeight());
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar textura do item " + type + ": " + e.getMessage());
            createPlaceholderItem();
        }

        // Carrega a textura do solo (se existir para este tipo)
        try {
            switch (type) {
                case RUSTY_SWORD:
                    groundTexture = new Texture(Gdx.files.internal("sala_0/Pedaço_da_lore/solo_espada_quebrada.png"));
                    System.out.println("✅ Textura do solo carregada: " + groundTexture.getWidth() + "x"
                            + groundTexture.getHeight());
                    break;
                // Para outros tipos, groundTexture permanece null (sem solo)
                default:
                    groundTexture = null;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Textura do solo não encontrada para " + type + ": " + e.getMessage());
            groundTexture = null; // Solo opcional, então não cria placeholder
        }
    }

    private void createPlaceholderItem() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        switch (type) {
            case RUSTY_SWORD:
                pixmap.setColor(0.55f, 0.27f, 0.07f, 1f);
                break;
            default:
                pixmap.setColor(com.badlogic.gdx.graphics.Color.GRAY);
        }

        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        float bodyX = position.x + 0.5f;
        float bodyY = position.y + 0.5f;

        bodyDef.position.set(bodyX, bodyY);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.3f); // Hitbox menor para itens

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = Constants.BIT_OBJECT;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData(this);

        System.out.println("✅ Body do item estático criado em: " + bodyDef.position +
                " (Tile invertido: " + position.x + ", " + position.y + ")");
    }

    public void update(float deltaTime) {
        // Itens estáticos não precisam de update
    }

    public void render(SpriteBatch batch, float screenX, float screenY) {
        // Primeiro desenha o solo (se existir), para que fique abaixo do item
        if (groundTexture != null) {
            float groundWidth = groundTexture.getWidth();
            float groundHeight = groundTexture.getHeight();
            // Centraliza o solo no tile (que tem 64x64)
            float groundX = screenX - (groundWidth - 64f) / 2f;
            float groundY = screenY - (groundHeight - 64f) / 2f;
            batch.draw(groundTexture, groundX, groundY, groundWidth, groundHeight);
        }

        // Depois desenha o item
        if (texture != null) {
            float itemWidth = texture.getWidth();
            float itemHeight = texture.getHeight();
            // Centraliza o item no tile
            float itemX = screenX - (itemWidth - 64f) / 2f;
            float itemY = screenY - (itemHeight - 64f) / 2f;
            batch.draw(texture, itemX, itemY, itemWidth, itemHeight);
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public ItemType getType() {
        return type;
    }

    public Body getBody() {
        return body;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            System.out.println("✅ Item estático descarregado: " + type);
        }
        if (groundTexture != null) {
            groundTexture.dispose();
            System.out.println("✅ Solo do item descarregado: " + type);
        }
    }
}