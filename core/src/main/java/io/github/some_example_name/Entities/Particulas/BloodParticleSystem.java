package io.github.some_example_name.Entities.Particulas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.math.MathUtils;

public class BloodParticleSystem {
    private final Array<BloodParticle> activeParticles;
    private final Pool<BloodParticle> particlePool;
    private TextureRegion[] bloodSprites;
    private BloodPoolSystem bloodPoolSystem;
    
    
    public BloodParticleSystem(TextureRegion bloodSpriteSheet, int cols, BloodPoolSystem poolSystem) {
        activeParticles = new Array<>();
        particlePool = new Pool<BloodParticle>() {
            @Override
            protected BloodParticle newObject() {
                return new BloodParticle();
            }
        };
        
        // Divide sprite sheet
        bloodSprites = new TextureRegion[cols];
        int frameWidth = bloodSpriteSheet.getRegionWidth() / cols;
        for (int i = 0; i < cols; i++) {
            bloodSprites[i] = new TextureRegion(
                bloodSpriteSheet, 
                i * frameWidth, 
                0, 
                frameWidth, 
                bloodSpriteSheet.getRegionHeight()
            );
        }
        this.bloodPoolSystem = poolSystem;
    }
    
    // Método principal - sem parâmetro isCritical
    public void createBloodSplash(float worldX, float worldY, 
                                 Vector2 hitDirection, int count, float force) {
        
        for (int i = 0; i < count; i++) {
            BloodParticle p = particlePool.obtain();
            p.activate(worldX, worldY, hitDirection, force);
            activeParticles.add(p);
        }
        
        // Efeito de spray para golpes fortes
        if (force > 2.5f) {
            createSprayEffect(worldX, worldY, hitDirection, force);
        }
    }
    
    // Método de spray (sem crítico)
    private void createSprayEffect(float worldX, float worldY, 
                                  Vector2 direction, float force) {
        int sprayCount = (int)(force * 1.5f);
        
        for (int i = 0; i < sprayCount; i++) {
            BloodParticle p = particlePool.obtain();
            
            // Direção mais espalhada para spray
            Vector2 sprayDir = new Vector2(direction);
            float angle = sprayDir.angle() + MathUtils.random(-45f, 45f);
            sprayDir.setAngle(angle);
            
            // Força menor para spray
            float sprayForce = force * MathUtils.random(0.3f, 0.6f);
            
            p.activate(worldX, worldY, sprayDir, sprayForce);
            
            // Partículas de spray são menores
            p.renderScale *= 0.5f;
            
            activeParticles.add(p);
        }
    }
    
    // Sobrecarga para compatibilidade (força padrão)
    public void createBloodSplash(float worldX, float worldY, 
                                 Vector2 hitDirection, int count) {
        createBloodSplash(worldX, worldY, hitDirection, count, 2.5f);
    }
    
    public void update(float delta, Vector2 cameraPosition) {
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            BloodParticle p = activeParticles.get(i);
            p.update(delta, bloodPoolSystem, cameraPosition);
            
            if (!p.isActive) {
                activeParticles.removeIndex(i);
                particlePool.free(p);
            }
        }
        
        // Atualiza poças também
        if (bloodPoolSystem != null) {
            bloodPoolSystem.update(delta, cameraPosition);
        }
    }

    public void renderPools(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        if (bloodPoolSystem != null) {
            bloodPoolSystem.render(batch, offsetX, offsetY, tileSize);
        }
    }
    
    public Array<BloodParticle> getActiveParticles() {
        return activeParticles;
    }
    
    public TextureRegion[] getBloodSprites() {
        return bloodSprites;
    }
    
    public void clear() {
        activeParticles.clear();
    }
}