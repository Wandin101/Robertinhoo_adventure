package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.MapConfig.Mapa;
public class Room0Cabana {
    
    private Vector2 position; // Posição em tiles (igual à fogueira)
    private Texture cabanaTexture;
    private Body body;
    private Mapa mapa;

    private boolean hasArmorStored = false;
    private Texture cabanaWithArmorTexture;
    private Texture cabanaWithoutArmorTexture;
    
    // Tipos de cabana
    public enum CabanaType {
        PLAYER_HOUSE,
        NPC_HOUSE_1,
        NPC_HOUSE_2
    }
    
    private CabanaType type;
    
    public Room0Cabana(Mapa mapa, int tileX, int tileY, CabanaType type) {
        this.mapa = mapa;
        this.type = type;
        
        // ✅ INVERTE O Y para corresponder ao layout
        // No layout, Y=0 é o topo, mas no mundo Y=0 é a base
        this.position = new Vector2(tileX, mapa.mapHeight - 1 - tileY);

        System.out.println("🏠 Cabana criada em Tile: " + tileX + "," + tileY + 
                         " | Mundo invertido: " + position + " | Tipo: " + type);

        loadTexture();
        createPhysicsBody();
    }
    
    private void loadTexture() {
        try {
            // Carrega textura baseada no tipo de cabana
            switch (type) {
                case PLAYER_HOUSE:
                    cabanaWithArmorTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana robertinho.png"));
                    cabanaWithoutArmorTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_robertinho_no_armor.png"));
                    break;
                case NPC_HOUSE_1:
                    cabanaTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_npc1.png"));
                    break;
                case NPC_HOUSE_2:
                    cabanaTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_npc2.png"));
                    break;
                default:
                    cabanaTexture = new Texture(Gdx.files.internal("sala_0/cabanas/cabana_player.png"));
            }
            
            cabanaTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            System.out.println("✅ Textura da cabana carregada: " + cabanaTexture.getWidth() + "x" + cabanaTexture.getHeight());
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar textura da cabana: " + e.getMessage());
            createPlaceholderTexture();
        }
    }
    
    private void createPlaceholderTexture() {
        // Placeholder simples (igual à fogueira)
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(128, 128,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.55f, 0.27f, 0.07f, 1f); // Marrom
        pixmap.fill();
        pixmap.setColor(0.75f, 0.37f, 0.10f, 1f); // Marrom claro para detalhes
        pixmap.fillRectangle(16, 16, 96, 64);
        cabanaTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        // ✅ USA A POSIÇÃO JÁ INVERTIDA
        float bodyX = position.x + 0.5f;
        float bodyY = position.y + 0.5f;
        bodyDef.position.set(bodyX, bodyY);

        body = mapa.world.createBody(bodyDef);

        PolygonShape rectShape = new PolygonShape();
        // Hitbox um pouco menor que a visual para permitir passagem próxima
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
        Texture currentTexture = hasArmorStored ? cabanaWithArmorTexture : cabanaWithoutArmorTexture;
        
        if (currentTexture != null) {
            float renderSize = 92f;
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
    
    // ✅ NOVO MÉTODO: Retorna a posição original do layout (sem inversão)
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
        if (cabanaWithoutArmorTexture != null) {
            cabanaWithoutArmorTexture.dispose();
        }
        if (cabanaWithArmorTexture != null) {
            cabanaWithArmorTexture.dispose();
        }
    }}