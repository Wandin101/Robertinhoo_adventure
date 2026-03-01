package io.github.some_example_name.Entities.Particulas.MagicParticle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;

public class MagicParticle {
    private Vector2 position; // em pixels (tela)
    private Vector2 velocity; // em pixels/segundo
    private float lifeTime;
    private float maxLifeTime;
    private float size;
    private Color color;
    private boolean alive = true;
    private float alpha;

    public MagicParticle(float pixelX, float pixelY, float maxLifeTime) {
        this.position = new Vector2(pixelX, pixelY);
        this.maxLifeTime = maxLifeTime;
        this.lifeTime = maxLifeTime;

        // Velocidade para baixo (pixels/segundo)
        this.velocity = new Vector2(
                MathUtils.random(-10f, 10f),
                MathUtils.random(-30f, -10f) // negativo = para baixo
        );

        this.size = MathUtils.random(10f, 18f);

        float greenVar = MathUtils.random(0.7f, 1f);
        this.color = new Color(0.2f, greenVar, 0.3f, 1f);
        this.alpha = MathUtils.random(0.3f, 0.7f);
    }

    public void update(float delta) {
        lifeTime -= delta;
        if (lifeTime <= 0) {
            alive = false;
            return;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Transparência diminui com o tempo
        float lifeFactor = lifeTime / maxLifeTime;
        alpha = lifeFactor * 0.7f;
        size *= 0.99f; // encolhe suavemente
    }

    public void render(SpriteBatch batch, TextureRegion texture) {
        if (!alive)
            return;

        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(texture,
                position.x - size / 2,
                position.y - size / 2,
                size, size);
        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() {
        return alive;
    }
}