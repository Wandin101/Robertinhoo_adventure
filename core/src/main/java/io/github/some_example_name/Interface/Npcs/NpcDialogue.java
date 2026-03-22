package io.github.some_example_name.Interface.Npcs;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface NpcDialogue {
    /**
     * Retorna a animação facial atual do NPC.
     * Pode variar conforme a expressão.
     */
    Animation<TextureRegion> getFaceAnimation();

    /**
     * Retorna o texto atual da fala.
     */
    String getCurrentText();

    /**
     * Avança para a próxima fala (se houver).
     * 
     * @return true se ainda há falas, false se acabou.
     */
    boolean next();

    /**
     * Reinicia o diálogo para a primeira fala.
     */
    void reset();

    /**
     * Libera recursos (texturas, etc).
     */
    void dispose();

    default void setTalking(boolean talking) {
    }
}