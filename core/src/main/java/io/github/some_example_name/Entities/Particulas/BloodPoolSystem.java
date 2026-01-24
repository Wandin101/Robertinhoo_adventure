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
    
    // Referência para câmera (para LOD)
    private Vector2 cameraPosition;
    
    public BloodPoolSystem(TextureRegion poolSpriteSheet, int cols) {
        activePools = new Array<>();
       poolPool = new Pool<BloodPool>() {
            @Override
            protected BloodPool newObject() {
                System.out.println("🔄 Pool criou novo objeto BloodPool");
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
                poolSpriteSheet.getRegionHeight()
            );
        }
                System.out.println("✅ BloodPoolSystem inicializado com " + poolSprites.length + " sprites");
    }
    
    // Chamado quando uma partícula morre
  public void createPoolFromParticle(float worldX, float worldY, float force) {
        System.out.println("🎯 CREATE POOL CALLED at world: " + worldX + ", " + worldY);
        
        if (activePools.size >= maxPools) {
            System.out.println("⚠️  Max pools reached (" + maxPools + "), trying merge...");
            if (!tryMergeWithExisting(worldX, worldY, force)) {
                System.out.println("❌ Merge failed - pool not created");
                return;
            }
        } else {
            BloodPool pool = poolPool.obtain();
            float baseSize = 0.15f + (force * 0.05f);
            pool.activate(worldX, worldY, baseSize);
            activePools.add(pool);
            System.out.println("✅ POOL CREATED! Total active: " + activePools.size);
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
        if (cameraPos == null) {
            System.out.println("⚠️  Camera position is NULL in pool update");
        }
        
        int poolsUpdated = 0;
        for (int i = activePools.size - 1; i >= 0; i--) {
            BloodPool pool = activePools.get(i);
            pool.update(delta, cameraPos, decayDistance);
            poolsUpdated++;
            
            if (!pool.isActive) {
                activePools.removeIndex(i);
                poolPool.free(pool);
                System.out.println("🗑️  Pool removed (inactive)");
            }
        }
        
        if (poolsUpdated > 0) {
            System.out.println("🔄 Updated " + poolsUpdated + " pools, remaining: " + activePools.size);
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