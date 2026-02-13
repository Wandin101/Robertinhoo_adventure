package io.github.some_example_name.Entities.Particulas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Color;

// BloodPool.java - modifique a classe
public class BloodPool {
    public Vector2 position;
    public float size;
    public float currentSize;
    public float targetSize;
    public float growthProgress;

    // Visual
    public float alpha;
    public float rippleTimer;
    public float rippleIntensity;
    public Color color;
    public float dryness; // 0 = fresco, 1 = seco

    // Estado
    public float lifeTime;
    public float maxLifeTime;
    public boolean isActive;
    public int poolType;

    // Variação de cores
    private static final Color[] POOL_COLORS = {
            new Color(0.7f, 0.1f, 0.1f, 1f),
            new Color(0.65f, 0.12f, 0.12f, 1f),
            new Color(0.6f, 0.1f, 0.1f, 1f)
    };

    public BloodPool() {
        position = new Vector2();
        isActive = false;
        rippleTimer = 0f;
        dryness = 0f;
        color = new Color();
    }

    public void activate(float worldX, float worldY, float baseSize) {
        position.set(worldX, worldY);

        targetSize = baseSize * MathUtils.random(0.8f, 1.2f);
        currentSize = targetSize * 0.1f; // Começa pequeno
        growthProgress = 0f;

        // Tempo de vida
        lifeTime = 0f;
        maxLifeTime = MathUtils.random(25f, 40f);

        // Visual
        alpha = 1f;
        poolType = MathUtils.random(0, 2);
        color.set(POOL_COLORS[poolType % POOL_COLORS.length]);

        // Efeitos

        dryness = 0f;

        isActive = true;

        // Som de formação
        BloodSoundManager.playPoolSound(baseSize);
    }

    public void update(float delta, Vector2 cameraPosition, float distanceThreshold) {
        if (!isActive)
            return;

        lifeTime += delta;
        if (growthProgress < 1f) {
            growthProgress += delta * 3f;
            currentSize = targetSize * growthProgress;
        } else {
            currentSize = targetSize;
        }

        // Atualiza timer de ripple
        rippleTimer += delta * MathUtils.random(2f, 3f);

        // Progresso de secagem
        dryness = Math.min(lifeTime / maxLifeTime, 1f);

        // Calcula distância da câmera
        float distToCamera = position.dst(cameraPosition);
        float distanceAlpha = Math.min(1f, distanceThreshold / Math.max(1f, distToCamera));

        // Alpha baseado em tempo e distância
        float timeProgress = lifeTime / maxLifeTime;
        float fadeStart = 0.8f; // Começa a desaparecer nos últimos 20%

        if (timeProgress > fadeStart) {
            alpha = (1f - (timeProgress - fadeStart) / (1f - fadeStart)) * distanceAlpha;
        } else {
            alpha = distanceAlpha;
        }

        // Desativa quando quase invisível
        if (alpha < 0.05f || lifeTime >= maxLifeTime) {
            isActive = false;
        }
    }

    public void render(SpriteBatch batch, TextureRegion[] poolSprites,
            float offsetX, float offsetY, int tileSize) {
        if (!isActive || alpha < 0.05f)
            return;

        TextureRegion sprite = poolSprites[poolType % poolSprites.length];

        // Posição na tela
        float screenX = offsetX + (position.x * tileSize);
        float screenY = offsetY + (position.y * tileSize);

        // Tamanho atual - SEM ripple no tamanho!
        float renderSize = currentSize * tileSize;

        // Efeito de ripple (apenas posição Y para simular respingo)
        float rippleOffsetY = 0f;
        if (growthProgress < 1f) {
            // Durante o crescimento, leve efeito de "respingo"
            rippleOffsetY = (float) Math.sin(rippleTimer * 3f) * 1.5f;
        }

        // Cor com secagem (fica mais escura)
        float drynessFactor = 1f - dryness * 0.3f;
        float r = color.r * drynessFactor;
        float g = color.g * drynessFactor * 0.5f;
        float b = color.b * drynessFactor * 0.5f;

        // 1. Desenha sombra sutil (fixa, sem animação)
        batch.setColor(0.2f, 0.1f, 0.1f, alpha * 0.3f);
        batch.draw(
                sprite,
                screenX - renderSize / 2f + 2f,
                screenY - renderSize / 4f - 1f,
                renderSize * 0.9f, // SEM rippleScale
                renderSize * 0.45f);

        // 2. Desenha poça principal
        batch.setColor(r, g, b, alpha);
        batch.draw(
                sprite,
                screenX - renderSize / 2f,
                screenY - renderSize / 4f + rippleOffsetY,
                renderSize, // Tamanho fixo após crescimento
                renderSize * 0.5f);

        // 3. Efeito de borda úmida (apenas durante crescimento)
        if (growthProgress < 1f) {
            float wetAlpha = alpha * (1f - growthProgress) * 0.6f;
            batch.setColor(1f, 0.4f, 0.3f, wetAlpha);

            float wetSize = renderSize * 1.05f;
            batch.draw(
                    sprite,
                    screenX - wetSize / 2f,
                    screenY - wetSize / 4f,
                    wetSize,
                    wetSize * 0.55f);
        }

        // 4. Brilho para poças frescas (apenas por um tempo curto)
        if (dryness < 0.3f && alpha > 0.7f && lifeTime < 2f) {
            float glowAlpha = alpha * 0.4f * (1f - dryness) * (1f - growthProgress);
            batch.setColor(1f, 0.3f, 0.2f, glowAlpha);

            float glowSize = renderSize * 1.08f;
            // Efeito de pulsação suave apenas no brilho
            float pulse = 1f + (float) Math.sin(rippleTimer * 2f) * 0.03f;
            batch.draw(
                    sprite,
                    screenX - glowSize / 2f,
                    screenY - glowSize / 4f,
                    glowSize * pulse,
                    glowSize * 0.58f);
        }

        // Restaura cor
        batch.setColor(1f, 1f, 1f, 1f);
    }
}