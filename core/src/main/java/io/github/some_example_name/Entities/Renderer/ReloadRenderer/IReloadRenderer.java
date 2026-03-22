package io.github.some_example_name.Entities.Renderer.ReloadRenderer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

public interface IReloadRenderer {
    TextureRegion getReloadFrame(Weapon weapon, WeaponAnimations animations, float animationTime);

    void update(float delta, Weapon weapon);

    boolean isReloading();

    void reset();
}