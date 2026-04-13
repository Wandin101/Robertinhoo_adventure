package io.github.some_example_name.Entities.Renderer.InteractionHudRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Interatibles.Interactable;
import io.github.some_example_name.MapConfig.Mapa;

public class InteractionHighlightRenderer {
    private final Mapa mapa;
    private final int tileSize;
    private final Texture eKeyTexture;
    private final TextureRegion whitePixel; // para fallback visível

    public InteractionHighlightRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;
        eKeyTexture = new Texture(Gdx.files.internal("HUD/e_icon.png"));
        // Cria um pixel branco para desenhar retângulo colorido (fallback)
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        whitePixel = new TextureRegion(new Texture(pix));
        pix.dispose();
    }

    public void render(SpriteBatch batch, ShapeRenderer shape, Interactable current, float offsetX, float offsetY) {
        if (current == null || !current.isActive())
            return;

        Vector2 tilePos = current.getTilePosition();
        float screenX = offsetX + tilePos.x * tileSize;
        float screenY = offsetY + (mapa.mapHeight - 1 - tilePos.y) * tileSize;

        // Centro do tile (onde o baú é efetivamente desenhado)
        float centerX = screenX + tileSize / 2f;
        float centerY = screenY + tileSize / 2f;

        // Tamanho do baú (igual ao Chest.render)
        float chestSize = tileSize * 0.8f;

        // --- ÍCONE "E" (acima do baú, centralizado) ---
        float iconSize = 32;
        float iconX = centerX - iconSize / 2;
        float iconY = centerY + chestSize / 2 + 5; // 5 pixels de folga

        batch.begin();
        batch.draw(eKeyTexture, iconX, iconY, iconSize, iconSize);
        batch.end();

    }

    public void dispose() {
        eKeyTexture.dispose();
    }
}