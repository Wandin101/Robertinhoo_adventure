package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class SistemaLuz {
    private Texture lightTexture;
    private SpriteBatch batch;
    private long startTime;
    private SistemaParticulasFogueira sistemaParticulas;
    
    // ✅ CORES CORRETAS - #1ef42b e #e2ff4a
   private static final Color FOGUEIRA_COR_PRINCIPAL = new Color(0.886f, 1.0f, 0.290f, 1f);   // #e2ff4a (mantido)
private static final Color FOGUEIRA_COR_SECUNDARIA = new Color(0.4f, 0.95f, 0.5f, 1f);    // #66ff80 - VERDE MAIS CLARO
private static final Color FOGUEIRA_COR_QUENTE = new Color(0.95f, 0.85f, 0.4f, 1f);  
       // Tom quente intermediário          // Núcleo quase branco
    
    public SistemaLuz() {
        createLightTexture();
        batch = new SpriteBatch();
        startTime = TimeUtils.millis();
        sistemaParticulas = new SistemaParticulasFogueira();
    }
    
    private void createLightTexture() {
        int size = 512;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        int center = size / 2;
        float maxDist = center * 0.9f; // Borda mais suave
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float dist = Vector2.dst(center, center, x, y);
                if (dist <= maxDist) {
                    // ✅ GRADIENTE MAIS SUAVE
                    float normalizedDist = dist / maxDist;
                    float alpha = (float) Math.pow(1.0f - normalizedDist, 2.0f);
                    
                    pixmap.setColor(1, 1, 1, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        
        lightTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    public void renderLight(float x, float y, float radius, Color color) {
        batch.setColor(color);
        batch.draw(lightTexture, 
            x - radius, y - radius, 
            radius * 2, radius * 2);
    }
    
    // ✅ FOGUEIRA SUAVE E NATURAL
  // ✅ FOGUEIRA SUAVE E NATURAL
public void renderFogueira(float x, float y, float baseRadius, float delta) {
    float elapsed = (TimeUtils.millis() - startTime) * 0.001f;
    
    // ✅ ATUALIZA PARTÍCULAS
    sistemaParticulas.update(delta, x, y - 10);
    
    // ✅ OSCILAÇÃO MUITO MAIS SUAVE - quase imperceptível
    float pulseLento = MathUtils.sin(elapsed * 2f) * 0.04f + 1f;        // Muito lento
    float pulseMedio = MathUtils.sin(elapsed * 3.5f + 1f) * 0.03f + 1f; // Suave
    float pulseSuave = MathUtils.sin(elapsed * 6f + 2f) * 0.02f + 1f;   // Quase imperceptível
    
    float combinedPulse = (pulseLento + pulseMedio + pulseSuave) / 3f;
    float currentRadius = baseRadius * combinedPulse;
    
    // ✅ VARIAÇÃO DE COR MUITO SUAVE
    float colorMix = (MathUtils.sin(elapsed * 1.2f) * 0.3f + 0.5f); // Transição bem lenta
    
    Color currentColor = new Color();
    
    // Interpolação suave entre as duas cores principais
    if (colorMix < 0.5f) {
        float t = colorMix * 2f;
        currentColor.r = MathUtils.lerp(FOGUEIRA_COR_PRINCIPAL.r, FOGUEIRA_COR_QUENTE.r, t);
        currentColor.g = MathUtils.lerp(FOGUEIRA_COR_PRINCIPAL.g, FOGUEIRA_COR_QUENTE.g, t);
        currentColor.b = MathUtils.lerp(FOGUEIRA_COR_PRINCIPAL.b, FOGUEIRA_COR_QUENTE.b, t);
    } else {
        float t = (colorMix - 0.5f) * 2f;
        currentColor.r = MathUtils.lerp(FOGUEIRA_COR_QUENTE.r, FOGUEIRA_COR_SECUNDARIA.r, t);
        currentColor.g = MathUtils.lerp(FOGUEIRA_COR_QUENTE.g, FOGUEIRA_COR_SECUNDARIA.g, t);
        currentColor.b = MathUtils.lerp(FOGUEIRA_COR_QUENTE.b, FOGUEIRA_COR_SECUNDARIA.b, t);
    }
    
    // ✅ INTENSIDADE QUASE CONSTANTE - sem piscar
    float alphaVariation = (MathUtils.sin(elapsed * 1.5f) * 0.05f + 0.95f); // Variação mínima
    currentColor.a = 0.85f * alphaVariation; // Alpha estável
    
    // ✅ MOVIMENTO MÍNIMO - quase estático
    float offsetX = MathUtils.sin(elapsed * 4f) * 0.8f; // Movimento quase imperceptível
    float offsetY = MathUtils.cos(elapsed * 3f) * 0.5f;
    
    // ✅ CAMADA 1: LUZ PRINCIPAL DA FOGUEIRA
    renderLight(x + offsetX, y + offsetY, currentRadius, currentColor);
    
    // ✅ CAMADA 2: NÚCLEO MAIS SUAVE E NATURAL
    // Usando uma cor mais próxima das cores da fogueira, não branco puro
    Color coreColor = new Color(
        MathUtils.lerp(FOGUEIRA_COR_PRINCIPAL.r, FOGUEIRA_COR_SECUNDARIA.r, 0.5f),
        MathUtils.lerp(FOGUEIRA_COR_PRINCIPAL.g, FOGUEIRA_COR_SECUNDARIA.g, 0.5f),
        MathUtils.lerp(FOGUEIRA_COR_PRINCIPAL.b, FOGUEIRA_COR_SECUNDARIA.b, 0.5f),
        1f
    );
    
    float corePulse = MathUtils.sin(elapsed * 6f) * 0.08f + 0.92f; // Variação mínima
    float coreRadius = baseRadius * 0.15f * corePulse; // Núcleo menor
    coreColor.a = 0.15f * alphaVariation; // Alpha bem reduzido
    renderLight(x + offsetX * 0.1f, y + offsetY * 0.1f, coreRadius, coreColor);
    
    // ✅ CAMADA 3: HALO EXTERNO (quase estático)
    Color haloColor = new Color(FOGUEIRA_COR_SECUNDARIA);
    float haloRadius = baseRadius * 1.2f;
    haloColor.a = 0.12f; // Alpha reduzido
    renderLight(x, y, haloRadius, haloColor);
    
    // ✅ RENDERIZA PARTÍCULAS
    sistemaParticulas.render(batch);
}
    
    public void begin() {
        batch.begin();
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, 
                              com.badlogic.gdx.graphics.GL20.GL_ONE);
    }
    
    public void end() {
        batch.end();
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, 
                              com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setProjectionMatrix(com.badlogic.gdx.math.Matrix4 matrix) {
        batch.setProjectionMatrix(matrix);
    }
    
    public void dispose() {
        if (lightTexture != null) {
            lightTexture.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (sistemaParticulas != null) {
            sistemaParticulas.dispose();
        }
    }
}