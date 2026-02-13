package io.github.some_example_name.Entities.Particulas.Shell;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class Shell {
    // 🎯 GRAVIDADE LEVE – para um pequeno arco
    private static final float GRAVITY = -5f; // negativa = puxa para baixo
    private static final float GROUND_Y = 0.2f; // altura do "chão" (mesma do jogador)
    private static final float GROUND_FRICTION = 0.9f; // atrito no chão
    private static final float STOP_SPEED = 0.2f; // velocidade para considerar parado

    private Vector2 position;
    private Vector2 velocity;
    private float rotation;
    private float angularVelocity;
    private float lifeTime;
    private float maxLife;
    private boolean alive;
    private TextureRegion texture;
    private float width;
    private float height;
    private boolean onGround; // se já está no chão

    public Shell() {
        position = new Vector2();
        velocity = new Vector2();
        alive = false;
        onGround = false;
    }

    public void init(Vector2 spawnPos, Vector2 direction, TextureRegion texture, float scale) {
        this.position.set(spawnPos);

        // 💥 VELOCIDADE INICIAL – para trás, lateral e PULO
        this.velocity.set(direction).scl(-1.4f); // recuo um pouco mais forte
        this.velocity.x += MathUtils.random(-0.6f, 0.6f);
        this.velocity.y += MathUtils.random(0.8f, 1.8f); // pulo médio (sobe)

        // 🌀 Rotação
        this.rotation = MathUtils.random(360f);
        this.angularVelocity = MathUtils.random(-350f, 350f);

        this.lifeTime = 0f;
        this.maxLife = 1.5f; // vive um pouco mais
        this.alive = true;
        this.texture = texture;
        this.width = texture.getRegionWidth() * scale;
        this.height = texture.getRegionHeight() * scale;
        this.onGround = false;
    }

    public void update(float delta, Mapa mapa) {
        if (!alive)
            return;

        // 🎯 GRAVIDADE – só age se não estiver no chão
        if (!onGround) {
            velocity.y += GRAVITY * delta;
        }

        velocity.x *= 0.98f;

        // 📍 Atualiza posição
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // 🔄 Rotação
        rotation += angularVelocity * delta;
        rotation %= 360;

        // 🌍 Colisão com o chão (altura fixa)
        if (position.y <= GROUND_Y) {
            position.y = GROUND_Y;
            if (!onGround) {
                // Quique mínimo (quase nada)
                velocity.y = -velocity.y * 0.2f;
                velocity.x *= 0.7f;
                angularVelocity *= 0.7f;
                if (Math.abs(velocity.y) < 0.3f) {
                    velocity.y = 0;
                    onGround = true;
                }
            } else {
                // Atrito no chão – para rápido
                velocity.x *= GROUND_FRICTION;
                if (Math.abs(velocity.x) < 0.1f)
                    velocity.x = 0;
                angularVelocity *= GROUND_FRICTION;
            }
        }

        // 🛑 Para completamente se estiver muito lento
        if (onGround && velocity.len2() < STOP_SPEED * STOP_SPEED) {
            velocity.setZero();
            angularVelocity = 0;
        }

        // ⏳ Tempo de vida
        lifeTime += delta;
        if (lifeTime > maxLife || (onGround && velocity.isZero(0.05f) && lifeTime > 0.8f)) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        if (!alive || texture == null)
            return;

        float screenX = offsetX + (position.x * tileSize);
        float screenY = offsetY + (position.y * tileSize);

        float originX = width / 2f;
        float originY = height / 2f;

        // ✨ Fade-out suave no final
        float alpha = 1f;
        if (lifeTime > maxLife * 0.7f) {
            alpha = 1f - ((lifeTime - maxLife * 0.7f) / (maxLife * 0.3f));
        }

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture,
                screenX - originX, screenY - originY,
                originX, originY,
                width, height,
                1f, 1f,
                rotation);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public boolean isAlive() {
        return alive;
    }

    public void dispose() {
    }
}