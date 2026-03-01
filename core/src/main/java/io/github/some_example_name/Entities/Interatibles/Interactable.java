package io.github.some_example_name.Entities.Interatibles;

import com.badlogic.gdx.math.Vector2;

public interface Interactable {
    Vector2 getPosition(); // posição do corpo (para verificação de distância)

    void onInteract(); // chamado quando o jogador interage

    String getInteractionPrompt(); // texto do prompt (opcional)

    boolean isActive(); // se ainda pode ser interagido
}