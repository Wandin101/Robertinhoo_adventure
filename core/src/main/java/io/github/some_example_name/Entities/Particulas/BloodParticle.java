package io.github.some_example_name.Entities.Particulas;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

public class BloodParticle {
    // COORDENADAS EM UNIDADES DE MUNDO
    public final Vector2 position;
    public final Vector2 velocity;
    public final float gravity = 12f;

    public int spriteIndex;
    public float renderScale;
    public float rotation;
    public boolean isActive;
    public boolean hasCreatedPool = false;

    // Propriedades visuais
    public Color color;
    public float initialScale;
    public float angularVelocity;
    public boolean hasSplattered = false;

    private float drag = 0.95f;
    private float lifeTime;
    private float maxLifeTime;

    // Cores de sangue (sem crítico)
    private static final Color[] BLOOD_COLORS = {
            new Color(0.8f, 0.1f, 0.1f, 1f), // Vermelho vivo
            new Color(0.7f, 0.15f, 0.15f, 1f), // Vermelho escuro
            new Color(0.6f, 0.1f, 0.1f, 1f) // Vermelho quase marrom
    };

    public BloodParticle() {
        position = new Vector2();
        velocity = new Vector2();
        isActive = false;
        color = new Color(0.8f, 0.1f, 0.1f, 1f);
        angularVelocity = 0;
    }

    public void activate(float x, float y, Vector2 direction, float force) {
        position.set(x, y);

        // Força com variação
        float forceVariation = MathUtils.random(0.7f, 1.3f);
        velocity.set(direction).nor().scl(force * forceVariation);

        // Aleatoriedade mais orgânica
        float spread = force * 0.3f;
        velocity.x += MathUtils.random(-spread, spread);
        velocity.y += MathUtils.random(-spread * 0.5f, spread);

        // Escala variada
        initialScale = MathUtils.random(0.04f, 0.1f);
        renderScale = initialScale;

        // Rotação e velocidade angular
        rotation = MathUtils.random(0f, 360f);
        angularVelocity = MathUtils.random(-180f, 180f);

        // Cor aleatória
        color.set(BLOOD_COLORS[MathUtils.random(0, BLOOD_COLORS.length - 1)]);

        // Tempo de vida variado
        maxLifeTime = MathUtils.random(0.8f, 1.5f);

        lifeTime = 0;
        isActive = true;
        hasCreatedPool = false;
        hasSplattered = false;

        // Tocar som de splash (intensidade baseada na força)
        float soundIntensity = Math.min(force / 5f, 1f);
        BloodSoundManager.playSplashSound(soundIntensity);
    }

    public void update(float delta, BloodPoolSystem poolSystem, Vector2 cameraPos) {
        if (!isActive)
            return;

        // Aplica resistência do ar
        velocity.x *= drag;
        velocity.y *= drag;

        // Gravidade
        velocity.y -= gravity * delta * 0.5f;

        // Movimento
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Rotação
        rotation += angularVelocity * delta;

        // Efeito de compressão ao cair
        if (!hasSplattered && velocity.y < -2f) {
            float squash = Math.abs(velocity.y) * 0.1f;
            renderScale = initialScale * (1f + squash * 0.2f);
        }

        lifeTime += delta;

        // Efeito de "splat" quando atinge o chão
        if (!hasSplattered && position.y <= 0.05f && velocity.y < -1f) {
            createSplatEffect(poolSystem);
            hasSplattered = true;
            velocity.scl(0.3f);
            angularVelocity *= 0.5f;
        }

        // Cria poça quando velocidade é baixa
        float speed = velocity.len();
        if (!hasCreatedPool && speed < 2f && lifeTime > 0.3f) {
            createBloodPool(poolSystem);
        }

        // Remove se tempo esgotou
        if (lifeTime > maxLifeTime) {
            if (!hasCreatedPool && poolSystem != null) {
                poolSystem.createPoolFromParticle(position.x, position.y, 0.1f);
            }
            isActive = false;
        }
    }

    private void createSplatEffect(BloodPoolSystem poolSystem) {
        if (poolSystem != null) {
            // Cria pequenas poças de respingo
            for (int i = 0; i < MathUtils.random(1, 3); i++) {
                float offsetX = position.x + MathUtils.random(-0.1f, 0.1f);
                float offsetY = Math.max(0f, position.y + MathUtils.random(0f, 0.05f));
                poolSystem.createPoolFromParticle(offsetX, offsetY, 0.05f);
            }
        }
    }

    private void createBloodPool(BloodPoolSystem poolSystem) {
        if (poolSystem != null) {
            float poolSize = renderScale * 6f;
            poolSystem.createPoolFromParticle(position.x, Math.max(0f, position.y), poolSize);
            hasCreatedPool = true;
        }
    }

    public float getAlpha() {
        float progress = lifeTime / maxLifeTime;

        // Fade suave no final
        if (progress > 0.7f) {
            return 1f - ((progress - 0.7f) / 0.3f);
        }

        return 1f;
    }
}