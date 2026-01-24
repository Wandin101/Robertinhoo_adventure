package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class SistemaSombras {
    private ShapeRenderer shapeRenderer;
    private Vector2 posicaoLuz;
    private float intensidadeSombra;
    
    public SistemaSombras() {
        shapeRenderer = new ShapeRenderer();
        posicaoLuz = new Vector2();
        intensidadeSombra = 0.6f;
    }
    
    /**
     * Projeta sombra de um objeto retangular
     */
    public void projetarSombraRetangulo(SpriteBatch batch, float objX, float objY, 
                                      float objLargura, float objAltura, 
                                      Vector2 posicaoLuz, float alturaObjeto) {
        
        // Calcular direção da luz para o objeto
        Vector2 centroObjeto = new Vector2(objX + objLargura/2, objY + objAltura/2);
        Vector2 direcaoLuz = centroObjeto.sub(posicaoLuz).nor();
        
        // Calcular fatores de projeção baseados na altura do objeto
        float fatorProjecao = alturaObjeto * 0.8f;
        float comprimentoSombra = MathUtils.clamp(fatorProjecao, 10f, 50f);
        
        // Calcular vértices da sombra projetada
        Vector2[] verticesSombra = calcularVerticesSombra(
            objX, objY, objLargura, objAltura, 
            direcaoLuz, comprimentoSombra
        );
        
        // Renderizar sombra
        renderizarSombraPoligono(batch, verticesSombra);
    }
    
    /**
     * Calcula os vértices da sombra projetada
     */
    private Vector2[] calcularVerticesSombra(float x, float y, float largura, float altura,
                                           Vector2 direcaoLuz, float comprimento) {
        
        Vector2[] vertices = new Vector2[8]; // 4 pontos base + 4 pontos projetados
        
        // Pontos base do objeto (chão)
        vertices[0] = new Vector2(x, y);                    // inferior esquerdo
        vertices[1] = new Vector2(x + largura, y);          // inferior direito  
        vertices[2] = new Vector2(x + largura, y + altura); // superior direito
        vertices[3] = new Vector2(x, y + altura);           // superior esquerdo
        
        // Pontos projetados (sombras)
        for (int i = 0; i < 4; i++) {
            vertices[i + 4] = new Vector2(
                vertices[i].x + direcaoLuz.x * comprimento,
                vertices[i].y + direcaoLuz.y * comprimento
            );
        }
        
        return vertices;
    }
    
    /**
     * Renderiza a sombra como um polígono
     */
    private void renderizarSombraPoligono(SpriteBatch batch, Vector2[] vertices) {
        batch.end(); // Pausar SpriteBatch
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Cor da sombra (preto semi-transparente)
        shapeRenderer.setColor(0f, 0f, 0f, intensidadeSombra * 0.7f);
        
        // Desenhar polígono da sombra (forma trapezoidal)
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            
            // Triângulo formado por: ponto base -> ponto projetado -> próximo ponto base
            shapeRenderer.triangle(
                vertices[i].x, vertices[i].y,
                vertices[i + 4].x, vertices[i + 4].y,
                vertices[next].x, vertices[next].y
            );
            
            // Triângulo formado por: ponto projetado -> próximo ponto projetado -> próximo ponto base
            shapeRenderer.triangle(
                vertices[i + 4].x, vertices[i + 4].y,
                vertices[next + 4].x, vertices[next + 4].y,
                vertices[next].x, vertices[next].y
            );
        }
        
        shapeRenderer.end();
        batch.begin(); // Retomar SpriteBatch
    }
    
    /**
     * Sombra simplificada para personagens (silhueta alongada)
     */
    public void projetarSombraPlayer(SpriteBatch batch, float playerX, float playerY, 
                                   float playerLargura, float playerAltura,
                                   Vector2 posicaoLuz) {
        
        Vector2 centroPlayer = new Vector2(playerX + playerLargura/2, playerY);
        Vector2 direcaoLuz = centroPlayer.sub(posicaoLuz).nor();
        
        // Calcular tamanho da sombra baseado na distância da luz
        float distancia = centroPlayer.dst(posicaoLuz);
        float escalaSombra = MathUtils.clamp(100f / distancia, 0.5f, 2f);
        
        float sombraLargura = playerLargura * 1.2f * escalaSombra;
        float sombraAltura = playerAltura * 0.3f * escalaSombra;
        float comprimentoSombra = 15f * escalaSombra;
        
        batch.end(); // Pausar SpriteBatch
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Sombra elíptica alongada na direção da luz
        shapeRenderer.setColor(0f, 0f, 0f, intensidadeSombra * 0.5f);
        
        float sombraX = playerX + playerLargura/2 - sombraLargura/2 + direcaoLuz.x * comprimentoSombra;
        float sombraY = playerY - 5f; // Levemente abaixo do player
        
        shapeRenderer.ellipse(sombraX, sombraY, sombraLargura, sombraAltura);
        
        shapeRenderer.end();
        batch.begin(); // Retomar SpriteBatch
    }
    
    /**
     * Sombra dinâmica para a fogueira (halo de luz com sombra)
     */
    public void renderizarHaloLuz(SpriteBatch batch, Vector2 posicaoFogueira, float raioLuz) {
        batch.end(); // Pausar SpriteBatch
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Halo de luz (gradiente circular)
        int segments = 32;
        float[] vertices = new float[2 * (segments + 2)];
        int idx = 0;
        
        // Centro do halo
        vertices[idx++] = posicaoFogueira.x;
        vertices[idx++] = posicaoFogueira.y;
        
        // Criar vértices do círculo com alpha gradient
        for (int i = 0; i <= segments; i++) {
            float angle = 2 * MathUtils.PI * i / segments;
            float x = posicaoFogueira.x + raioLuz * MathUtils.cos(angle);
            float y = posicaoFogueira.y + raioLuz * MathUtils.sin(angle);
            
            vertices[idx++] = x;
            vertices[idx++] = y;
        }
        
        shapeRenderer.end();
        batch.begin(); // Retomar SpriteBatch
    }
    
    public void setPosicaoLuz(float x, float y) {
        this.posicaoLuz.set(x, y);
    }
    
    public void setIntensidadeSombra(float intensidade) {
        this.intensidadeSombra = MathUtils.clamp(intensidade, 0f, 1f);
    }
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}