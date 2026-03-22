package io.github.some_example_name.Entities.Particulas.Shell;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;

public class Shell {
    private static final float GRAVITY = -7f;
    private static final float GROUND_FRICTION = 0.94f; // menos atrito para deslizar
    private static final float BOUNCE_DAMP = 0.65f; // quique mais energético
    private static final float ANGULAR_FRICTION = 0.96f; // atrito separado para rotação
    private static final float STOP_SPEED = 0.05f;

    private Vector2 position;
    private Vector2 velocity;
    private float rotation;
    private float angularVelocity;
    private boolean alive;
    private TextureRegion texture;
    private float width;
    private float height;
    private boolean onGround;
    private float groundY;
    private boolean soundPlayed;

    public Shell() {
        position = new Vector2();
        velocity = new Vector2();
        alive = true;
        soundPlayed = false;
    }

    public void init(Vector2 spawnPos, Vector2 direction, TextureRegion texture, float scale, float groundY) {
        this.position.set(spawnPos);
        this.groundY = groundY;

        this.velocity.set(direction).scl(-1.2f);
        this.velocity.x += MathUtils.random(-0.6f, 0.6f);
        this.velocity.y += MathUtils.random(1.2f, 2.0f);

        this.rotation = MathUtils.random(360f);
        this.angularVelocity = MathUtils.random(-400f, 400f);

        this.texture = texture;
        this.width = texture.getRegionWidth() * scale;
        this.height = texture.getRegionHeight() * scale;
        this.onGround = false;
        soundPlayed = false;
    }

    public void update(float delta) {
        if (!alive)
            return;

        if (!onGround) {
            velocity.y += GRAVITY * delta;
        }

        velocity.x *= 0.98f; // resistência do ar

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        rotation += angularVelocity * delta;
        rotation %= 360;

        // Colisão com o chão (altura dos pés)
        if (position.y <= groundY) {
            position.y = groundY;
            if (!onGround) {
                // primeiro toque: som e quique
                if (!soundPlayed) {
                    playRandomGroundSound();
                    soundPlayed = true;
                }
                velocity.y = -velocity.y * BOUNCE_DAMP;
                velocity.x *= 0.8f;
                angularVelocity *= 0.7f; // perde um pouco de giro
                if (Math.abs(velocity.y) < 0.2f) {
                    velocity.y = 0;
                    onGround = true;
                }
            } else {
                // no chão: desliza e perde rotação gradualmente
                velocity.x *= GROUND_FRICTION;
                if (Math.abs(velocity.x) < 0.05f)
                    velocity.x = 0;
                angularVelocity *= ANGULAR_FRICTION;
            }
        }

        // Para completamente se estiver muito lento e no chão
        if (onGround && velocity.len2() < STOP_SPEED * STOP_SPEED) {
            velocity.setZero();
            angularVelocity = 0;
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {

        float screenX = offsetX + (position.x * tileSize);
        float screenY = offsetY + (position.y * tileSize);
        float originX = width / 2f;
        float originY = height / 2f;

        batch.draw(texture, screenX - originX, screenY - originY, originX, originY,
                width, height, 1f, 1f, rotation);
    }

    public boolean isAlive() {
        return alive;
    }

    public void dispose() {
    }

    private void playRandomGroundSound() {
        int variant = MathUtils.random(1, 3);
        String soundName;
        switch (variant) {
            case 1:
                soundName = GameGameSoundsPaths.Sounds.BULLET_GROUND_1;
                break;
            case 2:
                soundName = GameGameSoundsPaths.Sounds.BULLET_GROUND_2;
                break;
            default:
                soundName = GameGameSoundsPaths.Sounds.BULLET_GROUND_3;
                break;
        }
        AudioManager.getInstance().playSound(soundName, 0.5f); // volume ajustável
    }
}