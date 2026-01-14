package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;

public class SistemaParticulasFogueira {
    private Array<Particle> particles;
    private long startTime;
    private Texture particleTexture;
    
    public SistemaParticulasFogueira() {
        particles = new Array<>();
        startTime = TimeUtils.millis();
        createParticleTexture();
    }
    
    private void createParticleTexture() {
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        
        // Cria textura circular para partículas
        int center = 4;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                float dist = Vector2.dst(center, center, x, y);
                if (dist <= 4) {
                    float alpha = 1f - (dist / 4f);
                    pixmap.setColor(1, 1, 1, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        
        particleTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    public void update(float delta, float fogueiraX, float fogueiraY) {
        float elapsed = (TimeUtils.millis() - startTime) * 0.001f;
        
        // Spawn de novas partículas
        if (MathUtils.random() < 0.6f) { // 60% de chance por frame
            spawnParticle(fogueiraX, fogueiraY, elapsed);
        }
        
        // Update partículas existentes
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            
            if (p.life <= 0) {
                particles.removeIndex(i);
            }
        }
    }
    
    private void spawnParticle(float x, float y, float time) {
        Particle p = new Particle();
        p.position.set(
            x + MathUtils.random(-8f, 15f),
            y + MathUtils.random(-5f, 15f)
        );
        
        p.velocity.set(
            MathUtils.random(-15f, 15f),
            MathUtils.random(40f, 100f)
        );
        
        p.life = p.maxLife = MathUtils.random(0.8f, 1.5f);
        float colorChoice = MathUtils.random();
       if (colorChoice < 0.7f) {
        p.color.set(0.886f, 1.0f, 0.290f, 1f); // #e2ff4a predominante
    } else {
        p.color.set(0.118f, 0.956f, 0.169f, 1f); // #1ef42b
    }
        p.size = MathUtils.random(2f, 6f);        
        particles.add(p);
    }
    
    public void render(SpriteBatch batch) {
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, 
                              com.badlogic.gdx.graphics.GL20.GL_ONE);
        
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            batch.setColor(p.color.r, p.color.g, p.color.b, alpha);
            batch.draw(particleTexture, 
                p.position.x - p.size/2, p.position.y - p.size/2,
                p.size, p.size);
        }
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, 
                              com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public void dispose() {
        if (particleTexture != null) {
            particleTexture.dispose();
        }
    }
    
    private static class Particle {
        Vector2 position = new Vector2();
        Vector2 velocity = new Vector2();
        Color color = new Color();
        float life;
        float maxLife;
        float size;
        
        public void update(float delta) {
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            
            // Reduz velocidade horizontal (resistência do ar)
            velocity.x *= 0.95f;
            
            // Gravidade leve
            velocity.y -= 20f * delta;
            
            // Reduz vida
            life -= delta;
            
            // Reduz tamanho ao longo da vida
            size *= 0.98f;
        }
    }
}