package io.github.some_example_name.Entities.Particulas.Shell;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ShellSystem {
    private static ShellSystem instance;
    private Array<Shell> activeShells;
    private Array<Shell> pool;
    private float scale;
    private static final float SHELL_LIFE = 0.8f; // tempo de vida das cápsulas

    private ShellSystem() {
        activeShells = new Array<>();
        pool = new Array<>();
        scale = 1.0f;
    }

    public static ShellSystem getInstance() {
        if (instance == null)
            instance = new ShellSystem();
        return instance;
    }

    public void init(float scale) {
        this.scale = scale;
    }

    public void spawn(Vector2 position, Vector2 direction, TextureRegion texture) {
        Shell shell = obtainShell();
        shell.init(position, direction, texture, scale, SHELL_LIFE);
        activeShells.add(shell);
    }

    private Shell obtainShell() {
        if (pool.size > 0)
            return pool.pop();
        return new Shell();
    }

    private void freeShell(Shell shell) {
        pool.add(shell);
    }

    public void update(float delta) {
        for (int i = activeShells.size - 1; i >= 0; i--) {
            Shell shell = activeShells.get(i);
            shell.update(delta);
            if (!shell.isAlive()) {
                activeShells.removeIndex(i);
                freeShell(shell);
            }
        }
    }

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