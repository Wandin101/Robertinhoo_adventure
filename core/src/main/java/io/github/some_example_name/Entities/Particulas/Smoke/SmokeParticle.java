package io.github.some_example_name.Entities.Particulas.Smoke;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class SmokeParticle {
    private Vector2 position;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private boolean alive;
    private float scale;
    private float rotation; // rotação opcional

    public SmokeParticle(Animation<TextureRegion> animation, float scale) {
        this.animation = animation;
        this.scale = scale;
        this.position = new Vector2();
        this.stateTime = 0;
        this.alive = false;
        this.rotation = 0;
    }

    public void init(Vector2 pos, float rotation) {
        this.position.set(pos);
        this.rotation = rotation;
        this.stateTime = 0;
        this.alive = true;
    }

    public void update(float delta) {
        if (!alive)
            return;
        stateTime += delta;
        if (animation.isAnimationFinished(stateTime)) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        if (!alive)
            return;
        TextureRegion frame = animation.getKeyFrame(stateTime);
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;
        // Desloca a fumaça para baixo (pés) e para trás na direção da rotação
        float offsetFeet = -0.25f * tileSize;
        float offsetBack = +0.3f * tileSize;
        float angleRad = (float) Math.toRadians(rotation);
        float backX = (float) (Math.cos(angleRad) * offsetBack);
        float backY = (float) (Math.sin(angleRad) * offsetBack);
        float x = offsetX + (position.x * tileSize) - width / 2 + backX;
        float y = offsetY + (position.y * tileSize) - height / 2 + offsetFeet + backY;
        batch.draw(frame, x, y, width / 2, height / 2, width, height, 1, 1, rotation);
    }

    public boolean isAlive() {
        return alive;
    }
}