package io.github.some_example_name.Entities.Particulas.Shell;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.MapConfig.Mapa;

public class ShellSystem {
    private static ShellSystem instance;

    private Array<Shell> activeShells;
    private Array<Shell> pool;
    private TextureRegion shellTexture;
    private Mapa mapa;
    private float scale;

    private ShellSystem() {
        activeShells = new Array<>();
        pool = new Array<>();
        scale = 1.0f;
    }

    public static ShellSystem getInstance() {
        if (instance == null) {
            instance = new ShellSystem();
        }
        return instance;
    }

    public void init(Mapa mapa, TextureRegion shellTexture, float scale) {
        this.mapa = mapa;
        this.shellTexture = shellTexture;
        this.scale = scale;
    }

    public void spawn(Vector2 position, Vector2 shootDirection) {
        Shell shell = obtainShell();
        shell.init(position, shootDirection, shellTexture, scale);
        activeShells.add(shell);
    }

    private Shell obtainShell() {
        if (pool.size > 0) {
            return pool.pop();
        } else {
            return new Shell();
        }
    }

    private void freeShell(Shell shell) {
        pool.add(shell);
    }

    public void update(float delta) {
        for (int i = activeShells.size - 1; i >= 0; i--) {
            Shell shell = activeShells.get(i);
            shell.update(delta, mapa);
            if (!shell.isAlive()) {
                activeShells.removeIndex(i);
                freeShell(shell);
            }
        }
    }

    /**
     * Renderiza todas as cápsulas ativas.
     * 
     * @param batch    SpriteBatch (já deve estar com begin() chamado)
     * @param offsetX  offset da câmera em X (pixels)
     * @param offsetY  offset da câmera em Y (pixels)
     * @param tileSize tamanho do tile (ex: 64)
     */
    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        for (Shell shell : activeShells) {
            shell.render(batch, offsetX, offsetY, tileSize);
        }
    }

    public void dispose() {
        activeShells.clear();
        pool.clear();
    }
}