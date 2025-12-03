// package io.github.some_example_name.Entities.Itens.CenarioItens;

// import com.badlogic.gdx.graphics.Texture;
// import com.badlogic.gdx.graphics.g2d.SpriteBatch;
// import com.badlogic.gdx.math.Vector2;

// import io.github.some_example_name.Entities.Renderer.ItensRenderer.BaseDestructible;
// import io.github.some_example_name.MapConfig.Mapa;

// public class Flower extends BaseDestructible {
//     private Texture flowerTexture;
    
//     public Flower(Mapa mapa, float x, float y) {
//         super(mapa, x, y);
//         loadTexture();
//     }
    
//     private void loadTexture() {
//         try {
//             flowerTexture = new Texture("sala_0/flower_texture.png"); // Sua textura de flor
//         } catch (Exception e) {
//             System.err.println("❌ Erro ao carregar textura da flor: " + e.getMessage());
//         }
//     }
    
//     @Override
//     public void render(SpriteBatch batch) {
//         if (flowerTexture != null) {
//             Vector2 screenPos = getScreenPosition();
//             batch.draw(flowerTexture, screenPos.x, screenPos.y, getWidth(), getHeight());
//         }
//     }
    
//     @Override
//     public void dispose() {
//         if (flowerTexture != null) {
//             flowerTexture.dispose();
//         }
//     }
// }