package io.github.some_example_name.Entities.Itens.CenarioItens;

import com.badlogic.gdx.math.Vector2;

public interface Obstacle {
    boolean blocksPath();

    Vector2 getTilePosition();
}