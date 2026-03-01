package io.github.some_example_name.Entities.Particulas.MagicParticle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MagicParticleSystem {
    private Array<MagicParticle> particles;
    private TextureRegion particleTexture;
    private float emissionTimer = 0f;
    private static final float EMISSION_INTERVAL = 0.1f;
    private static final int PARTICLES_PER_EMISSION = 3;
    private static final float PARTICLE_LIFETIME = 3f;

    public MagicParticleSystem(TextureRegion texture) {
        this.particleTexture = texture;
        this.particles = new Array<>(false, 100);
        System.out.println("🔮 [MagicParticleSystem] Inicializado. Textura: " + (texture != null ? "OK" : "NULL"));
    }

    public void update(float delta, float left, float right, float bottom, float top) {
        emissionTimer += delta;
        while (emissionTimer >= EMISSION_INTERVAL) {
            emissionTimer -= EMISSION_INTERVAL;
            emitParticles(left, right, bottom, top);
        }

        for (int i = particles.size - 1; i >= 0; i--) {
            MagicParticle p = particles.get(i);
            p.update(delta);
            if (!p.isAlive()) {
                particles.removeIndex(i);
            }
        }
    }

    private void emitParticles(float left, float right, float bottom, float top) {
        for (int i = 0; i < PARTICLES_PER_EMISSION; i++) {
            float x = MathUtils.random(left, right);
            float y = MathUtils.random(bottom, top);
            MagicParticle particle = new MagicParticle(x, y, PARTICLE_LIFETIME);
            particles.add(particle);
        }
    }

    public void render(SpriteBatch batch) {
        for (MagicParticle p : particles) {
            p.render(batch, particleTexture);
        }
    }

    public int getParticleCount() {
        return particles.size;
    }

    public void clear() {
        particles.clear();
    }

    public void dispose() {
        if (particleTexture != null && particleTexture.getTexture() != null) {
            particleTexture.getTexture().dispose();
        }
    }
}