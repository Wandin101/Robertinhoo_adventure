
package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class EscurecedorAmbiente {
    private ShapeRenderer shapeRenderer;
    private float intensidadeEscuro;
    
    public EscurecedorAmbiente() {
        shapeRenderer = new ShapeRenderer();
        intensidadeEscuro = 0.20f; // 0 = claro, 1 = totalmente escuro
    }
    
   public void aplicarEscurecimento(Matrix4 projectionMatrix, float viewportWidth, float viewportHeight) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // ✅ CAMADA ESCURA SEMI-TRANSPARENTE
        Color corEscura = new Color(0f, 0f, 0f, intensidadeEscuro);
        shapeRenderer.setColor(corEscura);
        shapeRenderer.rect(0, 0, viewportWidth, viewportHeight);
        
        shapeRenderer.end();
    }
    public void setIntensidade(float intensidade) {
        this.intensidadeEscuro = Math.max(0f, Math.min(1f, intensidade));
    }
    
    public float getIntensidade() {
        return intensidadeEscuro;
    }

    
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}