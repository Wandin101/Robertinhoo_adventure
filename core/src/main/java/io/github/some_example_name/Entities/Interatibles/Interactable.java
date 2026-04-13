package io.github.some_example_name.Entities.Interatibles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public interface Interactable {
    Vector2 getPosition();

    void onInteract();

    String getInteractionPrompt();

    boolean isActive();

    /**
     * Retorna a textura de contorno para este objeto.
     * Retorne null para usar o fallback (retângulo).
     */
    default TextureRegion getOutlineTexture() {
        return null;
    }

    /**
     * Raio máximo de interação (em unidades do mundo). Padrão 1.5f.
     */
    default float getInteractionRadius() {
        return 1.5f;
    }

    Vector2 getTilePosition();
}