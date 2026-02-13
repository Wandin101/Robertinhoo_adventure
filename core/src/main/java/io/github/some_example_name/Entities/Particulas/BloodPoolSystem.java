package io.github.some_example_name.Entities.Particulas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class BloodPoolSystem {
    // Arrays principais
    private final Array<BloodPool> activePools;
    private final Pool<BloodPool> poolPool; // Pool de pools (meta!)

    // Sprites
    private TextureRegion[] poolSprites;

    // Configuração
    private int maxPools = 100; // Máximo de poças ativas
    private float mergeDistance = 1.5f; // Distância para fusão (em unidades mundo)
    private float decayDistance = 20f; // Distância para LOD

    public BloodPoolSystem(TextureRegion poolSpriteSheet, int cols) {
        activePools = new Array<>();
        poolPool = new Pool<BloodPool>() {
            @Override
            protected BloodPool newObject() {
                return new BloodPool();
            }
        };

        // Divide sprite sheet
        poolSprites = new TextureRegion[cols];
        int frameWidth = poolSpriteSheet.getRegionWidth() / cols;
        for (int i = 0; i < cols; i++) {
            poolSprites[i] = new TextureRegion(
                    poolSpriteSheet,
                    i * frameWidth,
                    0,
                    frameWidth,
                    poolSpriteSheet.getRegionHeight());
        }
    }

    // Chamado quando uma partícula morre
    public void createPoolFromParticle(float worldX, float worldY, float force) {
        if (activePools.size >= maxPools) {
            if (!tryMergeWithExisting(worldX, worldY, force)) {
                return;
            }
        } else {
            BloodPool pool = poolPool.obtain();
            float baseSize = 0.15f + (force * 0.05f);
            pool.activate(worldX, worldY, baseSize);
            activePools.add(pool);
        }
    }

    private boolean tryMergeWithExisting(float worldX, float worldY, float force) {
        BloodPool closestPool = null;
        float closestDist = Float.MAX_VALUE;

        // Encontra poça mais próxima
        for (BloodPool pool : activePools) {
            float dist = pool.position.dst(worldX, worldY);
            if (dist < closestDist) {
                closestDist = dist;
                closestPool = pool;
            }
        }

        // Se está perto o suficiente, funde
        if (closestPool != null && closestDist < mergeDistance) {
            // Aumenta a poça existente
            closestPool.size += 0.05f * force;
            closestPool.lifeTime = Math.max(0f, closestPool.lifeTime - 10f); // "Renova" a poça
            return true;
        }

        return false;
    }

    public void update(float delta, Vector2 cameraPos) {
        for (int i = activePools.size - 1; i >= 0; i--) {
            BloodPool pool = activePools.get(i);
            pool.update(delta, cameraPos, decayDistance);

            if (!pool.isActive) {
                activePools.removeIndex(i);
                poolPool.free(pool);
            }
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        if (batch == null) {
            return;
        }
        for (BloodPool pool : activePools) {
            if (pool.isActive && pool.alpha > 0.05f) {
                pool.render(batch, poolSprites, offsetX, offsetY, tileSize);
            }
        }
    }

    public void clear() {
        activePools.clear();
    }

    // Para debug: número de poças ativas
    public int getActivePoolCount() {
        return activePools.size;
    }
}