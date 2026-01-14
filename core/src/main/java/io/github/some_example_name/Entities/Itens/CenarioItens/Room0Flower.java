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
import com.badlogic.gdx.graphics.g2d.Animation;
public class Room0Flower extends BaseDestructible {

    private static Texture flowerTexture;
    private static Texture animationSheet; // NOVA: textura da animação

    private TextureRegion flowerStaticTexture;
    private Animation<TextureRegion> walkAnimation; // NOVA: animação
    private Body body;
    private final Mapa mapa;
    
    // NOVO: variáveis para controle de animação
    private boolean isAnimating = false;
    private float animationTime = 0f;

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
            // Textura estática
            if (flowerTexture == null) {
                flowerTexture = new Texture(Gdx.files.internal("sala_0/flores/florzinha.png"));
                flowerTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
            
            // NOVO: Textura de animação
            if (animationSheet == null) {
                animationSheet = new Texture(Gdx.files.internal("sala_0/flores/florzinha_animation.png"));
                animationSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        } catch (Exception e) {
            System.err.println("❌ Textura da flor da sala 0 não encontrada: " + e.getMessage());
            throw new RuntimeException("Textura da flor da sala 0 obrigatória não encontrada");
        }
        
        // Textura estática
        this.flowerStaticTexture = new TextureRegion(flowerTexture);
        this.intactTexture = flowerStaticTexture;
        
        // NOVO: Configurar animação
        setupAnimation();
    }

    private void setupAnimation() {
        // Assumindo que a spritesheet tem 4 frames em uma linha
        int columns = 4;
        int rows = 1;
        int frameWidth = animationSheet.getWidth() / columns;
        int frameHeight = animationSheet.getHeight() / rows;

        TextureRegion[] animationFrames = new TextureRegion[columns];
        for (int col = 0; col < columns; col++) {
            animationFrames[col] = new TextureRegion(
                animationSheet,
                col * frameWidth,
                0,
                frameWidth,
                frameHeight
            );
        }
        
        // Animação com loop (true) para que volte ao primeiro frame depois do último
        this.walkAnimation = new Animation<>(0.1f, animationFrames);
        System.out.println("🌸 Animação da flor configurada: " + columns + " frames");
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
        // Se está animando, retorna o frame atual da animação
        if (isAnimating) {
            TextureRegion frame = walkAnimation.getKeyFrame(animationTime, false);
            return frame != null ? frame : flowerStaticTexture;
        }
        // Senão, retorna a textura estática
        return flowerStaticTexture;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Atualiza o tempo da animação se estiver ativa
        if (isAnimating) {
            animationTime += delta;
            
            // Verifica se a animação terminou (não loop)
            if (walkAnimation.isAnimationFinished(animationTime)) {
                isAnimating = false;
                animationTime = 0f;
                System.out.println("🌸 Animação da flor terminou");
            }
        }
    }

    // NOVO: Método para ativar a animação
    public void triggerAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            animationTime = 0f;
            System.out.println("🌸 Animação da flor ativada!");
        }
    }

    @Override
    public void startDestructionAnimation() {
        // Não faz nada - flores da sala 0 não são destrutíveis
    }

    @Override
    public void destroy() {
        // Não faz nada - flores da sala 0 não são destrutíveis
    }

    @Override
    public boolean isAnimationFinished() {
        return !isAnimating;
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
        if (animationSheet != null) {
            animationSheet.dispose();
            animationSheet = null;
        }
    }
}