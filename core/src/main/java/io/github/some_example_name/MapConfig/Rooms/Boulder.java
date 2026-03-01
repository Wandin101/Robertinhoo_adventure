package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class Boulder {
    private Sprite sprite;
    private Vector2 tilePosition; // posição em tiles (coordenadas do grid)

    public Boulder(Mapa mapa, int tileX, int tileY) {
        this.tilePosition = new Vector2(tileX, tileY); // armazena como float, mas são inteiros
        Texture texture = new Texture("sala_0/passarela.png"); // use a textura correta
        sprite = new Sprite(texture);
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        float screenX = offsetX + tilePosition.x * tileSize;
        float screenY = offsetY + tilePosition.y * tileSize;
        batch.draw(sprite, screenX, screenY, tileSize, tileSize);
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }

    public Vector2 getTilePosition() {
        return tilePosition;
    }
}