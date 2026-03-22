package io.github.some_example_name.Entities.Npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.MapConfig.Mapa;

public class EsmeraldaNPC extends NPC {
    private Texture idleSheet;
    private Animation<TextureRegion> idleAnimation;

    private static final float OFFSET_X = 25;
    private static final float OFFSET_Y = 73; // sobe um pouco para ficar atrás do balcão

    public EsmeraldaNPC(Mapa mapa, int tileX, int tileY) {
        super(mapa, tileX, tileY);

        // Carrega a sprite sheet idle (ajuste o caminho conforme seu asset)
        idleSheet = new Texture("npcs/Esmeralda/EsmeraldaShop.png"); // exemplo
        int frameCols = 6; // supondo 4 frames de idle (ajuste conforme sua imagem)
        int frameRows = 1;
        TextureRegion[][] tmp = TextureRegion.split(idleSheet,
                idleSheet.getWidth() / frameCols,
                idleSheet.getHeight() / frameRows);
        TextureRegion[] frames = new TextureRegion[frameCols];
        System.arraycopy(tmp[0], 0, frames, 0, frameCols);
        idleAnimation = new Animation<>(0.2f, frames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        // Converte posição do tile para coordenadas de tela
        float screenX = offsetX + (position.x * 64);
        float screenY = offsetY + (position.y * 64);

        // Aplica offset fino
        screenX += OFFSET_X;
        screenY += OFFSET_Y;

        // Tamanho de renderização (ajuste conforme o tamanho do sprite)
        float renderWidth = 64; // exemplo: ocupa um tile
        float renderHeight = 64;

        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, screenX, screenY, renderWidth, renderHeight);
    }

    @Override
    public void dispose() {
        if (idleSheet != null)
            idleSheet.dispose();
    }
}