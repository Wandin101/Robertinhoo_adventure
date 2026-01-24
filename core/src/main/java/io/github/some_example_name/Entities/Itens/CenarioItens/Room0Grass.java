package io.github.some_example_name.Entities.Itens.CenarioItens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.BaseDestructible;
import io.github.some_example_name.MapConfig.Mapa;

public class Room0Grass extends BaseDestructible {

    private static Texture staticTexture;
    private static Texture walkSheet;

    private TextureRegion grassTexture;
    private Animation<TextureRegion> walkAnimation;
    private Body body;
    private final Mapa mapa;
    private boolean isWalkingAnimation = false;
    private float walkAnimationTime = 0f;

    public Room0Grass(Mapa mapa, float x, float y) {
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
            if (staticTexture == null) {
                staticTexture = new Texture(Gdx.files.internal("sala_0/grama/grama_sala_0.png"));
                staticTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            if (walkSheet == null) {
                walkSheet = new Texture(Gdx.files.internal("sala_0/grama/animation_grama.png"));
                walkSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        } catch (Exception e) {
            System.err.println("❌ Texturas da sala 0 não encontradas: " + e.getMessage());
            throw new RuntimeException("Texturas da sala 0 obrigatórias não encontradas");
        }

        setupAnimations();
    }

    private void setupAnimations() {
        this.grassTexture = new TextureRegion(staticTexture);
        this.intactTexture = grassTexture;

        int columns = 3;
        int rows = 1;
        int frameWidth = walkSheet.getWidth() / columns;
        int frameHeight = walkSheet.getHeight() / rows;

        TextureRegion[] walkFrames = new TextureRegion[columns];
        for (int col = 0; col < columns; col++) {
            walkFrames[col] = new TextureRegion(
                    walkSheet,
                    col * frameWidth,
                    0,
                    frameWidth,
                    frameHeight);
        }

        this.walkAnimation = new Animation<>(0.1f, walkFrames);

        // Sala 0 não tem animação de destruição
        this.destructionAnimation = null;
        this.destroyedTexture = null;
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.1f, 0.1f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_ROOM0_PLANT; 
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        body.setUserData(this);
    }

    @Override
    public TextureRegion getTexture() {
        if (isWalkingAnimation) {
            TextureRegion frame = walkAnimation.getKeyFrame(walkAnimationTime, false);
            return frame != null ? frame : grassTexture;
        }
        return grassTexture;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isWalkingAnimation) {
            walkAnimationTime += delta;
            if (walkAnimation.isAnimationFinished(walkAnimationTime)) {
                isWalkingAnimation = false;
                walkAnimationTime = 0f;
            }
        }
    }

    public void triggerWalkAnimation() {
        if (!destroyed && !isAnimating) {
            isWalkingAnimation = true;
            walkAnimationTime = 0f;
        }
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
        if (staticTexture != null) {
            staticTexture.dispose();
            staticTexture = null;
        }
        if (walkSheet != null) {
            walkSheet.dispose();
            walkSheet = null;
        }
    }
}