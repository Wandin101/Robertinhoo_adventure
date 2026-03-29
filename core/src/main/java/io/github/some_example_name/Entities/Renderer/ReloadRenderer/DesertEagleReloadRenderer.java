
package io.github.some_example_name.Entities.Renderer.ReloadRenderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.DesertEagle.DesertEagle;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

public class DesertEagleReloadRenderer implements IReloadRenderer {

    // Distribuição dos 13 frames da animação de recarga
    private static final int OPEN_FRAMES = 5; // frames 0-4
    private static final int INSERT_FRAMES = 4; // frames 5-8
    private static final int CLOSE_FRAMES = 4; // frames 9-12
    private static final int TOTAL_FRAMES = OPEN_FRAMES + INSERT_FRAMES + CLOSE_FRAMES; // 13

    @Override
    public TextureRegion getReloadFrame(Weapon weapon, WeaponAnimations animations, float animationTime) {
        if (!(weapon instanceof DesertEagle))
            return null;

        DesertEagle desert = (DesertEagle) weapon;
        int stage = desert.getReloadStage(); // 0 = abertura, 1 = inserção, 2 = fechamento
        float progress = desert.getStageProgress();

        Animation<TextureRegion> reloadAnim = animations.getAnimation(WeaponAnimations.WeaponDirection.S,
                Weapon.WeaponState.RELOADING);
        if (reloadAnim == null)
            return null;

        TextureRegion[] reloadFrames = reloadAnim.getKeyFrames();
        if (reloadFrames.length < TOTAL_FRAMES) {
            return reloadFrames.length > 0 ? reloadFrames[0] : null;
        }

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
        // Nada específico – o próprio sistema de recarga da arma já é atualizado
    }

    @Override
    public boolean isReloading() {
        // Não usado diretamente; a flag reloadTriggered no WeaponRenderer é usada
        return false;
    }

    @Override
    public void reset() {
        // Não necessário
    }
}
