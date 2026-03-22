package io.github.some_example_name.Entities.Renderer.ReloadRenderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Revolver.Revolver;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

public class RevolverReloadRenderer implements IReloadRenderer {
    private static final int OPEN_FRAMES = 9;
    private static final int INSERT_FRAMES = 4;
    private static final int CLOSE_FRAMES = 4;
    private static final int TOTAL_FRAMES = OPEN_FRAMES + INSERT_FRAMES + CLOSE_FRAMES; // 17

    @Override
    public TextureRegion getReloadFrame(Weapon weapon, WeaponAnimations animations, float animationTime) {
        if (!(weapon instanceof Revolver))
            return null;

        Revolver revolver = (Revolver) weapon;
        int stage = revolver.getReloadStage();
        float progress = revolver.getStageProgress();

        Animation<TextureRegion> reloadAnim = animations.getAnimation(WeaponAnimations.WeaponDirection.S,
                Weapon.WeaponState.RELOADING);
        if (reloadAnim == null)
            return null;
        TextureRegion[] reloadFrames = reloadAnim.getKeyFrames();
        if (reloadFrames.length < TOTAL_FRAMES)
            return reloadFrames.length > 0 ? reloadFrames[0] : null;

        int frameIndex;
        if (stage == 0) {
            frameIndex = (int) (progress * OPEN_FRAMES);
            frameIndex = Math.min(frameIndex, OPEN_FRAMES - 1);
        } else if (stage == 1) {
            frameIndex = OPEN_FRAMES + (int) (progress * INSERT_FRAMES);
            frameIndex = Math.min(frameIndex, OPEN_FRAMES + INSERT_FRAMES - 1);
        } else { // stage == 2
            frameIndex = OPEN_FRAMES + INSERT_FRAMES + (int) (progress * CLOSE_FRAMES);
            frameIndex = Math.min(frameIndex, TOTAL_FRAMES - 1);
        }
        return reloadFrames[frameIndex];
    }

    @Override
    public void update(float delta, Weapon weapon) {
        // Se a arma tiver um sistema de recarga, poderíamos chamá-lo aqui, mas já é
        // feito no próprio Revolver.update
        // Esse método pode ser útil se quisermos controlar algo independente.
    }

    @Override
    public boolean isReloading() {
        // Delegado para a arma; como não temos acesso direto aqui, podemos retornar
        // false e usar a flag do WeaponRenderer
        // Mas para simplificar, podemos não usar esse método e manter a verificação de
        // reloadTriggered no renderer.
        return false;
    }

    @Override
    public void reset() {
        // Se necessário, resetar alguma variável local (nada por enquanto)
    }
}