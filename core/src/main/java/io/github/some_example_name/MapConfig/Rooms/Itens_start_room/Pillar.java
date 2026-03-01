package io.github.some_example_name.MapConfig.Rooms.Itens_start_room;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.g2d.Animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Luz.SistemaLuz;

public class Pillar {
    private Vector2 tilePos;
    private Texture spriteSheet;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private float frameDuration = 0.1f; // ajuste conforme necessário
    private Mapa mapa;

    // Parâmetros da luz
    private static final float LIGHT_RADIUS = 120f; // pixels
    private static final float LIGHT_INTENSITY = 0.7f;
    private com.badlogic.gdx.graphics.Color lightColor;

    public Pillar(Mapa mapa, int tileX, int tileY, String texturePath) {
        this.mapa = mapa;
        this.tilePos = new Vector2(tileX, tileY);
        this.stateTime = 0f;

        // Define a cor da luz (verde-azulado)
        lightColor = new com.badlogic.gdx.graphics.Color(0.3f, 0.9f, 0.6f, 1f);

        loadSpriteSheet(texturePath);
    }

    private void loadSpriteSheet(String path) {
        try {
            spriteSheet = new Texture(Gdx.files.internal(path));
            int frameWidth = spriteSheet.getWidth() / 10; // 10 colunas
            int frameHeight = spriteSheet.getHeight(); // 1 linha
            TextureRegion[][] temp = TextureRegion.split(spriteSheet, frameWidth, frameHeight);
            TextureRegion[] frames = temp[0]; // primeira linha
            animation = new Animation<>(frameDuration, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);
            System.out.println("✅ Spritesheet do pilar carregada: " + path);
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar spritesheet do pilar: " + e.getMessage());
            createPlaceholder();
        }
    }

    private void createPlaceholder() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        pixmap.fill();
        spriteSheet = new Texture(pixmap);
        pixmap.dispose();

        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spriteSheet);
        animation = new Animation<>(frameDuration, frames);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        if (animation == null)
            return;

        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        float screenX = offsetX + tilePos.x * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tilePos.y) * tileSize;

        // Ajuste visual: pilar um pouco maior que o tile
        float renderSize = tileSize * 1.2f;
        float centeredX = screenX - (renderSize - tileSize) / 2f;
        float centeredY = screenY - (renderSize - tileSize) / 2f;

        batch.draw(currentFrame, centeredX, centeredY, renderSize, renderSize);
    }

    public void renderLight(SistemaLuz sistemaLuz, float offsetX, float offsetY, int tileSize) {
        // Centro do pilar em pixels
        float screenX = offsetX + (tilePos.x + 0.5f) * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tilePos.y + 0.5f) * tileSize;

        // Pequena pulsação na luz
        float pulse = (float) Math.sin(stateTime * 3) * 0.1f + 0.9f;
        float radius = LIGHT_RADIUS * pulse;

        com.badlogic.gdx.graphics.Color color = lightColor.cpy();
        color.a = LIGHT_INTENSITY * pulse;
        sistemaLuz.renderLight(screenX, screenY, radius, color);
    }

    public void dispose() {
        if (spriteSheet != null)
            spriteSheet.dispose();
    }
}