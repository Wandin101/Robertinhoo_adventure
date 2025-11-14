package io.github.some_example_name.MapConfig.Rooms.Items_sala_0;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class CampFire {
    private Vector2 position; // Apenas guarda a posição em TILES
    private Texture spriteSheet;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private float frameDuration = 0.1f;

    // REMOVEMOS toda a lógica de luz daqui!

    public CampFire(float tileX, float tileY) {
        this.position = new Vector2(tileX, tileY); // Apenas coordenadas de tile
        this.stateTime = 0f;

        System.out.println("🔥 Fogueira SIMPLES criada em Tile: " + position);

        loadSpriteSheet();
        // NÃO cria luz aqui!
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = new Texture(Gdx.files.internal("sala_0/fogueiraPrincipal.png"));

            int frameWidth = spriteSheet.getWidth() / 8;
            int frameHeight = spriteSheet.getHeight();

            TextureRegion[] frames = new TextureRegion[8];
            for (int i = 0; i < 8; i++) {
                frames[i] = new TextureRegion(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
            }

            animation = new Animation<TextureRegion>(frameDuration, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);

        } catch (Exception e) {
            System.err.println("❌ Erro no sprite sheet: " + e.getMessage());
            createPlaceholder();
        }
    }

    private void createPlaceholder() {
        // Placeholder simples
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.ORANGE);
        pixmap.fill();
        spriteSheet = new Texture(pixmap);
        pixmap.dispose();

        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spriteSheet);
        animation = new Animation<TextureRegion>(frameDuration, frames);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        // Nada relacionado a luz
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            
            float screenX = offsetX + position.x * 64;
            float screenY = offsetY + position.y * 64;
            float renderSize = 333;
            
            batch.draw(currentFrame, screenX, screenY, renderSize, renderSize);
        }
    }

    public Vector2 getPosition() {
        return position; // Retorna posição em TILES
    }

    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
        // Nada para remover de luz
    }
}