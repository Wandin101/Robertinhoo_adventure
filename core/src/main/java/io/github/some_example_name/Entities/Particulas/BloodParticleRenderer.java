package io.github.some_example_name.Entities.Particulas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BloodParticleRenderer {
    private static final int TILE_SIZE = 64;

    public void render(SpriteBatch batch, BloodParticleSystem system,
            float offsetX, float offsetY) {
        TextureRegion[] sprites = system.getBloodSprites();

        // ✅ SALVA o estado atual do batch
        Color batchColor = batch.getColor();

        for (BloodParticle p : system.getActiveParticles()) {
            if (!p.isActive)
                continue;

            TextureRegion sprite = sprites[p.spriteIndex];

            // Converte: Unidades mundo → pixels na tela
            float screenX = offsetX + (p.position.x * TILE_SIZE);
            float screenY = offsetY + (p.position.y * TILE_SIZE);

            // Calcula tamanho
            float sizeInPixels = p.renderScale * TILE_SIZE;
            float scaleFactor = sizeInPixels / sprite.getRegionWidth();

            // Centraliza
            float originX = sprite.getRegionWidth() / 2f;
            float originY = sprite.getRegionHeight() / 2f;

            // ✅ Aplica alpha CORRETAMENTE (IMPORTANTE!)
            float alpha = p.getAlpha();
            batch.setColor(1f, 1f, 1f, alpha); // White com alpha

            // Desenha
            batch.draw(
                    sprite,
                    screenX, screenY,
                    originX, originY,
                    sprite.getRegionWidth(),
                    sprite.getRegionHeight(),
                    scaleFactor, scaleFactor,
                    p.rotation);
        }

        // ✅ RESTAURA a cor original do batch
        batch.setColor(batchColor);
    }
}