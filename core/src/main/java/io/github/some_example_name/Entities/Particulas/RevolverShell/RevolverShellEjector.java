package io.github.some_example_name.Entities.Particulas.RevolverShell;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.Entities.Particulas.Shell.Shell;

public class RevolverShellEjector {
    private static RevolverShellEjector instance;
    private Array<Shell> activeShells;
    private float scale;
    private static final int MAX_SHELLS = 50; // máximo de cápsulas ativas

    private RevolverShellEjector() {
        activeShells = new Array<>();
        scale = 1.0f;
    }

    public static RevolverShellEjector getInstance() {
        if (instance == null)
            instance = new RevolverShellEjector();
        return instance;
    }

    public void init(float scale) {
        this.scale = scale;
    }

    /**
     * Ejecta várias cápsulas de uma vez.
     * 
     * @param playerPos posição do jogador (centro)
     * @param direction direção do tambor (pode ser a mira ou a direção do corpo)
     * @param count     número de cápsulas a ejetar
     * @param texture   textura da cápsula
     */
    public void ejectMultiple(Vector2 playerPos, Vector2 direction, int count, TextureRegion texture) {
        if (texture == null)
            return;

        Vector2 baseDir = direction.cpy().nor();
        float groundY = playerPos.y - 0.25f;

        for (int i = 0; i < count; i++) {
            float angleOffset = MathUtils.random(-22f, 22f);
            Vector2 ejectDir = baseDir.cpy().rotateDeg(angleOffset);
            float speed = MathUtils.random(1.0f, 1.6f);
            ejectDir.scl(speed);

            Vector2 spawnPos = playerPos.cpy().add(baseDir.scl(0.45f));

            // Gera rotação e velocidade angular aleatórias
            float rotation = MathUtils.random(360f);
            float angularVelocity = MathUtils.random(-400f, 400f);

            Shell shell = new Shell();
            // Ajuste os parâmetros conforme a assinatura atual do Shell.init
            // Se forem 8 parâmetros, adicione um último valor (ex: 0f)
            shell.init(spawnPos, ejectDir, texture, scale, groundY, rotation, angularVelocity, 0f);
            activeShells.add(shell);
        }

        while (activeShells.size > MAX_SHELLS) {
            Shell oldest = activeShells.first();
            oldest.dispose();
            activeShells.removeIndex(0);
        }
    }

    public void update(float delta) {
        for (Shell shell : activeShells) {
            shell.update(delta);
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        for (Shell shell : activeShells) {
            shell.render(batch, offsetX, offsetY, tileSize);
        }
    }

    public void dispose() {
        for (Shell shell : activeShells)
            shell.dispose();
        activeShells.clear();
    }
}