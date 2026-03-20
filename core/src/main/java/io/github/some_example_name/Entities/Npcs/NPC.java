package io.github.some_example_name.Entities.Npcs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public abstract class NPC {
    protected Mapa mapa;
    protected Vector2 position; // posição em tiles (invertida)
    protected float stateTime;

    public NPC(Mapa mapa, int tileX, int tileY) {
        this.mapa = mapa;
        // Converte tile para posição de mundo (inversão de Y igual à cabana)
        this.position = new Vector2(tileX, mapa.mapHeight - 1 - tileY);
        this.stateTime = 0f;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public abstract void render(SpriteBatch batch, float offsetX, float offsetY);

    public abstract void dispose();
}