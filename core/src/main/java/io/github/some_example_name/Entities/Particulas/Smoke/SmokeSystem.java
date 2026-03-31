package io.github.some_example_name.Entities.Particulas.Smoke;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class SmokeSystem {
    private static SmokeSystem instance;
    private Array<SmokeParticle> activeSmoke;
    private Animation<TextureRegion> smokeAnimation;
    private float scale;

    private SmokeSystem() {
        activeSmoke = new Array<>();
        scale = 1.0f;
    }

    public static SmokeSystem getInstance() {
        if (instance == null)
            instance = new SmokeSystem();
        return instance;
    }

    /**
     * Inicializa o sistema com a animação carregada pelo SmokeAnimations.
     * 
     * @param animation animação da fumaça
     * @param scale     escala do sprite
     */
    public void init(Animation<TextureRegion> animation, float scale) {
        this.smokeAnimation = animation;
        this.scale = scale;
    }

    public void spawn(Vector2 position, float rotation) {
        if (smokeAnimation == null) {
            System.err.println("[SmokeSystem] Animação não inicializada!");
            return;
        }
        SmokeParticle particle = new SmokeParticle(smokeAnimation, scale);
        particle.init(position, rotation);
        activeSmoke.add(particle);
    }

    public void update(float delta) {
        for (int i = activeSmoke.size - 1; i >= 0; i--) {
            SmokeParticle p = activeSmoke.get(i);
            p.update(delta);
            if (!p.isAlive()) {
                activeSmoke.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        for (SmokeParticle p : activeSmoke) {
            p.render(batch, offsetX, offsetY, tileSize);
        }
    }

    public void dispose() {
        activeSmoke.clear();
    }
}