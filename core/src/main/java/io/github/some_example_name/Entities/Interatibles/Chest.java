package io.github.some_example_name.Entities.Interatibles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Itens.Weapon.Revolver.Revolver;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Entities.Itens.Contact.Constants;

import java.util.Random;

public class Chest implements Interactable {
    private static final float ANIMATION_SPEED = 0.1f;
    private static final int FRAME_COLS = 7;

    private final Mapa mapa;
    private final Vector2 position; // tile position
    private final Body body;
    private final Texture spriteSheet;
    private final TextureRegion[] frames;
    private final Animation<TextureRegion> animation;

    private boolean opened = false;
    private boolean opening = false;
    private float stateTime = 0f;
    private TextureRegion currentFrame;
    private boolean dropped = false;
    private final Random random = new Random();

    // Referência ao jogador para verificar distância
    private Robertinhoo player;

    public Chest(Mapa mapa, int tileX, int tileY, String texturePath) {
        this.mapa = mapa;
        this.position = new Vector2(tileX, tileY);
        this.player = mapa.robertinhoo; // obtém jogador do mapa

        spriteSheet = new Texture(Gdx.files.internal(texturePath));
        int frameWidth = spriteSheet.getWidth() / FRAME_COLS;
        int frameHeight = spriteSheet.getHeight();
        frames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            frames[i] = new TextureRegion(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }
        animation = new Animation<>(ANIMATION_SPEED, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        currentFrame = frames[0];
        Gdx.app.log("Chest", "Textura: " + texturePath + " " + spriteSheet.getWidth() + "x" + spriteSheet.getHeight());
        Gdx.app.log("Chest", "Frame 0: " + frames[0].getRegionWidth() + "x" + frames[0].getRegionHeight());
        // Corpo estático com sensor para interação
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Vector2 worldPos = mapa.tileToWorld(tileX, tileY);
        bodyDef.position.set(worldPos);
        body = mapa.world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.3f);// tamanho do sensor

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        fixtureDef.filter.categoryBits = Constants.BIT_INTERACTABLE;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void update(float delta) {
        if (opening && !opened) {
            stateTime += delta;
            currentFrame = animation.getKeyFrame(stateTime, false);
            if (animation.isAnimationFinished(stateTime)) {
                opened = true;
                opening = false;
                currentFrame = frames[FRAME_COLS - 1];
                dropLoot();
            }
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        float screenX = offsetX + position.x * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - position.y) * tileSize;

        float chestSize = tileSize * 0.8f;
        float drawX = screenX + (tileSize - chestSize) / 2f;
        float drawY = screenY + (tileSize - chestSize) / 2f;

        batch.draw(currentFrame, drawX, drawY, chestSize, chestSize);
    }

    // Implementação da interface Interactable
    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void onInteract() {
        if (opened || opening) {
            return;
        }
        // Verificar distância com o jogador antes de abrir
        if (player != null) {
            Vector2 playerPos = player.getPosition();
            float distance = body.getPosition().dst(playerPos);
            if (distance > 1.5f) { // distância em unidades do mundo
                Gdx.app.log("Chest", "Muito longe para interagir.");
                return;
            }
        }
        opening = true;
        stateTime = 0f;
        Gdx.app.log("Chest", "Baú sendo aberto!");
    }

    @Override
    public String getInteractionPrompt() {
        if (opened)
            return "Vazio";
        if (opening)
            return "Abrindo...";
        return "Abrir (E)";
    }

    @Override
    public boolean isActive() {
        return !opened; // se já abriu, não está mais ativo para interação
    }

    public Body getBody() {
        return body;
    }

    private void dropLoot() {
        if (dropped)
            return;
        dropped = true;

        Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
        Weapon arma;
        if (random.nextBoolean()) {
            arma = new Pistol(mapa, worldPos.x, worldPos.y, mapa.robertinhoo.getInventory());
        } else {
            arma = new Revolver(mapa, worldPos.x, worldPos.y, mapa.robertinhoo.getInventory());
        }
        mapa.weapons.add(arma);

        Vector2 targetPos = worldPos.cpy().add(
                (random.nextFloat() - 0.5f) * 1.5f,
                (random.nextFloat() - 0.5f) * 1.5f);
        arma.startDropAnimation(worldPos, targetPos);
    }

    public void dispose() {
        spriteSheet.dispose();
    }

    @Override
    public Vector2 getTilePosition() {
        return position; // já é a posição em tiles
    }
}