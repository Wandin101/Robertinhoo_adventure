package io.github.some_example_name.Entities.Itens.CenarioItens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.BaseDestructible;
import io.github.some_example_name.MapConfig.Mapa;

public class Room0Flower extends BaseDestructible {

    private static Texture flowerTexture;
    private Body body;
    private final Mapa mapa;

    public Room0Flower(Mapa mapa, float x, float y) {
        super(x, y, null, null);
        this.mapa = mapa;
        this.destroyed = false;
        this.isAnimating = false;
        
        loadAssets();
        createPhysicsBody();
    }

    @Override
    public void loadAssets() {
        try {
            if (flowerTexture == null) {
                flowerTexture = new Texture(Gdx.files.internal("sala_0/flores/florzinha.png"));
                flowerTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        } catch (Exception e) {
            System.err.println("❌ Textura da flor da sala 0 não encontrada: " + e.getMessage());
            throw new RuntimeException("Textura da flor da sala 0 obrigatória não encontrada");
        }
        
        this.intactTexture = new TextureRegion(flowerTexture);
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.3f); // Hitbox menor para flores

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_OBJECT;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_ENEMY; // Sem BIT_PLAYER_ATTACK

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        body.setUserData(this);
    }

    @Override
    public TextureRegion getTexture() {
        return intactTexture;
    }

    @Override
    public void update(float delta) {
        // Flores não têm animação
    }



    @Override
    public void startDestructionAnimation() {
        // Não faz nada
    }

    @Override
    public void destroy() {
        // Não faz nada
    }

    @Override
    public boolean isAnimationFinished() {
        return true;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public void dispose() {
        if (body != null) {
            mapa.world.destroyBody(body);
        }
    }

    public static void disposeAll() {
        if (flowerTexture != null) {
            flowerTexture.dispose();
            flowerTexture = null;
        }
    }
}