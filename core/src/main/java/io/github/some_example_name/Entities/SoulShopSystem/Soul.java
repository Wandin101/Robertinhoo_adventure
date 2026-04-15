
package io.github.some_example_name.Entities.SoulShopSystem;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Luz.SistemaLuz;
import io.github.some_example_name.MapConfig.Mapa;

public class Soul {
    private static final float ANIMATION_SPEED = 0.1f;
    private static final int FRAME_COLS = 10;
    private static final float FLOAT_SPEED = 2.5f;
    private static final float FLOAT_AMPLITUDE = 0.15f;
    private static final float LIGHT_PULSE_SPEED = 4f;
    private static final Color LIGHT_COLOR = new Color(1f, 1f, 0.9f, 1f);
    private static final float LIGHT_BASE_RADIUS = 40f;

    private final Mapa mapa;
    private final Vector2 tilePosition; // posição em tiles
    private final Body body;
    private final Texture spriteSheet;
    private final TextureRegion[] frames;
    private final Animation<TextureRegion> animation;
    private final int value = 10;

    private float stateTime = 0f;
    private TextureRegion currentFrame;
    private float floatTime = 0f;
    private float lightTime = 0f;
    private boolean markedForRemoval = false;

    public Soul(Mapa mapa, float tileX, float tileY) {
        this.mapa = mapa;
        this.tilePosition = new Vector2(tileX, tileY);

        spriteSheet = new Texture(Gdx.files.internal("Almas/alma.png"));
        int frameWidth = spriteSheet.getWidth() / FRAME_COLS;
        int frameHeight = spriteSheet.getHeight();
        frames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            frames[i] = new TextureRegion(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }
        animation = new Animation<>(ANIMATION_SPEED, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        currentFrame = frames[0];

        // Corpo sensor
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Vector2 worldPos = mapa.tileToWorld((int) tileX, (int) tileY);
        bodyDef.position.set(worldPos);
        body = mapa.world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.25f, 0.25f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_SOUL;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();
        this.floatTime = new Random().nextFloat() * 2 * (float) Math.PI;
    }

    public void update(float delta) {
        stateTime += delta;
        currentFrame = animation.getKeyFrame(stateTime, true);
        floatTime += delta * FLOAT_SPEED;
        lightTime += delta;
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        float floatOffset = (float) Math.sin(floatTime) * FLOAT_AMPLITUDE;
        float screenX = offsetX + tilePosition.x * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tilePosition.y) * tileSize + floatOffset * tileSize;

        float soulSize = tileSize * 0.3f;
        float drawX = screenX + (tileSize - soulSize) / 2f;
        float drawY = screenY + (tileSize - soulSize) / 2f;

        batch.draw(currentFrame, drawX, drawY, soulSize, soulSize);
    }

    public void renderLight(SistemaLuz sistemaLuz, float offsetX, float offsetY, int tileSize) {
        float floatOffset = (float) Math.sin(floatTime) * FLOAT_AMPLITUDE;
        float centerX = offsetX + tilePosition.x * tileSize + tileSize / 2f;
        float centerY = offsetY + (mapa.mapHeight - 1 - tilePosition.y) * tileSize + tileSize / 2f
                + floatOffset * tileSize;

        float pulse = 0.6f + (float) Math.sin(lightTime * LIGHT_PULSE_SPEED) * 0.3f;
        Color color = new Color(LIGHT_COLOR.r, LIGHT_COLOR.g, LIGHT_COLOR.b, 0.35f * pulse);
        float radius = LIGHT_BASE_RADIUS * pulse;

        sistemaLuz.renderLight(centerX, centerY, radius, color);
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public Body getBody() {
        return body;
    }

    public int getValue() {
        return value;
    }

    public void markForRemoval() {
        markedForRemoval = true;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void dispose() {
        spriteSheet.dispose();
        if (body != null) {
            mapa.world.destroyBody(body);
        }
    }
}