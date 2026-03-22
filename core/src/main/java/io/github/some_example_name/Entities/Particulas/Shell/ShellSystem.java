package io.github.some_example_name.Entities.Particulas.Shell;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ShellSystem {
    private static ShellSystem instance;
    private Array<Shell> activeShells;
    private float scale;
    private static final int MAX_SHELLS = 50;

    private ShellSystem() {
        activeShells = new Array<>();
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

    public void spawn(Vector2 position, Vector2 direction, TextureRegion texture, float groundY) {
        Shell shell = new Shell();
        shell.init(position, direction, texture, scale, groundY);
        activeShells.add(shell);

        // FIFO: remove as mais antigas se exceder o limite
        while (activeShells.size > MAX_SHELLS) {
            Shell oldest = activeShells.first();
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
        activeShells.clear();
    }
}