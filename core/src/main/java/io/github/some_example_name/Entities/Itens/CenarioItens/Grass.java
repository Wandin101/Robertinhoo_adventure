// Grass.java
package io.github.some_example_name.Entities.Itens.CenarioItens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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

public class Grass extends BaseDestructible {

    private static Texture staticTexture;
    private static Texture destructionSheet;

    private TextureRegion grassTexture;
    private Animation<TextureRegion> destructionAnimation;
    private Animation<TextureRegion> walkAnimation; // Nova animação para quando passam por cima
    private Body body;
    private final Mapa mapa;
    private boolean bodyMarkedForDestruction = false;
    private boolean isWalkingAnimation = false;
    private float walkAnimationTime = 0f;

    public Grass(Mapa mapa, float x, float y) {
        super(x, y, null, null);
        this.mapa = mapa;
        this.destroyed = false;
        this.isAnimating = false;
        loadAssets();
        createPhysicsBody();
    }

    @Override
    public void loadAssets() {
        // Carrega textura estática da grama intacta
        if (staticTexture == null) {
            staticTexture = new Texture(Gdx.files.internal("ITENS/DestrutiveItens/Grama_fase_1/Grama.png"));
            staticTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // Carrega spritesheet de destruição
        if (destructionSheet == null) {
            destructionSheet = new Texture(
                    Gdx.files.internal("ITENS/DestrutiveItens/Grama_fase_1/Grama_animation.png"));
            destructionSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // Textura estática da grama intacta
        this.grassTexture = new TextureRegion(staticTexture);
        this.intactTexture = grassTexture;

        // Configura spritesheet (4 colunas, 3 linhas)
        int totalColumns = 4;
        int totalRows = 3;
        int frameWidth = destructionSheet.getWidth() / totalColumns;
        int frameHeight = destructionSheet.getHeight() / totalRows;

        // ANIMAÇÃO DE CAMINHADA (Linha 0 - 4 frames)
        TextureRegion[] walkFrames = new TextureRegion[4];
        for (int col = 0; col < 4; col++) {
            walkFrames[col] = new TextureRegion(
                    destructionSheet,
                    col * frameWidth,
                    0 * frameHeight, // Primeira linha
                    frameWidth,
                    frameHeight);
        }
        this.walkAnimation = new Animation<>(0.1f, walkFrames);

        // ANIMAÇÃO DE DESTRUIÇÃO (Linhas 1 e 2 - 5 frames)
        TextureRegion[] destructionFrames = new TextureRegion[5];
        int frameIndex = 0;

        // Linha 1 (4 frames)
        for (int col = 0; col < 4; col++) {
            destructionFrames[frameIndex] = new TextureRegion(
                    destructionSheet,
                    col * frameWidth,
                    1 * frameHeight, // Segunda linha
                    frameWidth,
                    frameHeight);
            frameIndex++;
        }

        // Linha 2 (1 frame)
        destructionFrames[frameIndex] = new TextureRegion(
                destructionSheet,
                0, // Primeira coluna
                2 * frameHeight, // Terceira linha
                frameWidth,
                frameHeight);

        this.destructionAnimation = new Animation<>(0.08f, destructionFrames);
        this.destroyedTexture = destructionFrames[4];
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.4f); // Hitbox um pouco maior para detectar passagem

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_OBJECT;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER_ATTACK | Constants.BIT_PLAYER | Constants.BIT_ENEMY;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        body.setUserData(this);
    }

    @Override
    public TextureRegion getTexture() {
        // Prioridade: Animação de destruição > Animação de caminhada > Textura estática
        if (isAnimating) {
            TextureRegion frame = destructionAnimation.getKeyFrame(animationTime, false);
            return frame != null ? frame : grassTexture;
        }

        if (isWalkingAnimation) {
            TextureRegion frame = walkAnimation.getKeyFrame(walkAnimationTime, false);
            return frame != null ? frame : grassTexture;
        }

        if (destroyed) {
            return destroyedTexture != null ? destroyedTexture : grassTexture;
        }

        return grassTexture;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Atualiza animação de caminhada
        if (isWalkingAnimation) {
            walkAnimationTime += delta;
            if (walkAnimation.isAnimationFinished(walkAnimationTime)) {
                // Reseta a animação de caminhada quando termina
                isWalkingAnimation = false;
                walkAnimationTime = 0f;
            }
        }

        // Verifica fim de animação de destruição
        if (isAnimating && destructionAnimation.isAnimationFinished(animationTime)) {
            bodyMarkedForDestruction = true;
            Gdx.app.log("Grass", "Animação de destruição finalizada.");
        }
    }

    public void takeDamage(int damage) {
        if (destroyed || isAnimating) {
            return;
        }

        Gdx.app.log("Grass", "Grama atingida por ataque corpo a corpo!");
        startDestructionAnimation();
        destroy();
    }

    // Novo método para ativar animação de passagem
    public void triggerWalkAnimation() {
        if (!destroyed && !isAnimating) {
            isWalkingAnimation = true;
            walkAnimationTime = 0f;
        }
    }

    @Override
    public void startDestructionAnimation() {
        isAnimating = true;
        animationTime = 0f;
    }

    @Override
    public void destroy() {
        if (!destroyed) {
            destroyed = true;
            Gdx.app.log("Grass", "Grama destruída!");
        }
    }

    public void destroyBody() {
        if (body != null) {
            mapa.world.destroyBody(body);
            body = null;
        }
    }

    public boolean isBodyMarkedForDestruction() {
        return bodyMarkedForDestruction;
    }

    public void setBodyMarkedForDestruction(boolean bodyMarkedForDestruction) {
        this.bodyMarkedForDestruction = bodyMarkedForDestruction;
    }

    @Override
    public boolean isAnimationFinished() {
        return isAnimating && destructionAnimation.isAnimationFinished(animationTime);
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
        if (destructionSheet != null) {
            destructionSheet.dispose();
            destructionSheet = null;
        }
    }
}