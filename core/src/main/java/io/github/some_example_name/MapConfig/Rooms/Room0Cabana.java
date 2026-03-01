package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.MapConfig.Mapa;

public class Room0Cabana {

    private Vector2 position;
    private Texture cabanaTexture; // Para NPCs
    private Texture cabanaWithArmorTexture;
    private Texture cabanaWithoutArmorTexture;
    private Texture groundTexture; // Solo da cabana (se houver)
    private Body body;
    private Mapa mapa;

    private boolean hasArmorStored = false;

    public enum CabanaType {
        PLAYER_HOUSE,
        NPC_HOUSE_1,
        NPC_HOUSE_2
    }

    private CabanaType type;

    public Room0Cabana(Mapa mapa, int tileX, int tileY, CabanaType type) {
        this.mapa = mapa;
        this.type = type;

        // Inversão do Y (mesmo da fogueira)
        this.position = new Vector2(tileX, mapa.mapHeight - 1 - tileY);

        System.out.println("🏠 Cabana criada em Tile: " + tileX + "," + tileY +
                " | Mundo invertido: " + position + " | Tipo: " + type);

        loadTextures(); // Agora carrega cabana e solo
        createPhysicsBody();
    }

    private void loadTextures() {
        try {
            switch (type) {
                case PLAYER_HOUSE:
                    cabanaWithArmorTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana robertinho.png"));
                    cabanaWithoutArmorTexture = new Texture(
                            Gdx.files.internal("sala_0/cabanas/cabana_robertinho_no_armor.png"));
                    // Tenta carregar o solo para a casa do jogador
                    try {
                        groundTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_robertinho_solo.png"));
                        groundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                        System.out.println("✅ Textura do solo da cabana carregada: " + groundTexture.getWidth() + "x"
                                + groundTexture.getHeight());
                    } catch (Exception e) {
                        System.err.println("⚠️ Solo da cabana do jogador não encontrado: " + e.getMessage());
                        groundTexture = null;
                    }
                    break;
                case NPC_HOUSE_1:
                    cabanaTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_npc1.png"));
                    // Se houver solo para NPC1, carregue aqui
                    groundTexture = null;
                    break;
                case NPC_HOUSE_2:
                    cabanaTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_npc2.png"));
                    groundTexture = null;
                    break;
                default:
                    cabanaTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_player.png"));
                    groundTexture = null;
            }

            // Aplica filtro linear para suavizar
            if (cabanaTexture != null)
                cabanaTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (cabanaWithArmorTexture != null)
                cabanaWithArmorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (cabanaWithoutArmorTexture != null)
                cabanaWithoutArmorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (groundTexture != null)
                groundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            System.out.println("✅ Textura da cabana carregada");

        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar textura da cabana: " + e.getMessage());
            createPlaceholderTexture();
        }
    }

    private void createPlaceholderTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(128, 128,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.55f, 0.27f, 0.07f, 1f);
        pixmap.fill();
        pixmap.setColor(0.75f, 0.37f, 0.10f, 1f);
        pixmap.fillRectangle(16, 16, 96, 64);
        cabanaTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        float bodyX = position.x + 0.5f;
        float bodyY = position.y + 0.5f;
        bodyDef.position.set(bodyX, bodyY);

        body = mapa.world.createBody(bodyDef);

        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(0.8f, 0.4f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = Constants.BIT_WALL;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_ENEMY;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        rectShape.dispose();

        body.setUserData(this);

        System.out.println("✅ Body da cabana criado em: " + bodyDef.position);
    }

    public void render(SpriteBatch batch, float screenX, float screenY) {
        // Primeiro desenha o solo (se existir)
        if (groundTexture != null) {
            float scale = 0.8f; // Ajuste conforme necessário (0.5, 0.6, 0.7...)
            float groundWidth = groundTexture.getWidth() * scale;
            float groundHeight = groundTexture.getHeight() * scale;
            float groundX = screenX - (groundWidth - 64) / 2f;
            float groundY = screenY - (groundHeight - 64) / 2f;
            batch.draw(groundTexture, groundX, groundY - 10, groundWidth, groundHeight);
        }
        // Depois desenha a cabana
        Texture currentTexture;
        if (type == CabanaType.PLAYER_HOUSE) {
            currentTexture = hasArmorStored ? cabanaWithArmorTexture : cabanaWithoutArmorTexture;
        } else {
            currentTexture = cabanaTexture;
        }

        if (currentTexture != null) {
            float renderSize = 92f; // Mantém o tamanho original da cabana
            float centeredX = screenX - (renderSize - 64) / 2f;
            float centeredY = screenY - (renderSize - 64) / 2f;
            batch.draw(currentTexture, centeredX, centeredY, renderSize, renderSize);
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setHasArmorStored(boolean hasArmor) {
        this.hasArmorStored = hasArmor;
        System.out.println("🏠 Cabana " + (hasArmor ? "COM" : "SEM") + " armadura guardada");
    }

    public boolean hasArmorStored() {
        return hasArmorStored;
    }

    public Vector2 getLayoutPosition() {
        return new Vector2(position.x, mapa.mapHeight - 1 - position.y);
    }

    public Body getBody() {
        return body;
    }

    public CabanaType getType() {
        return type;
    }

    public void dispose() {
        if (cabanaWithoutArmorTexture != null)
            cabanaWithoutArmorTexture.dispose();
        if (cabanaWithArmorTexture != null)
            cabanaWithArmorTexture.dispose();
        if (cabanaTexture != null)
            cabanaTexture.dispose();
        if (groundTexture != null)
            groundTexture.dispose();
    }
}