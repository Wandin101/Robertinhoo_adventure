package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Calibre12.Calibre12;
import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;

public class WeaponRenderer {
    private float animationTime = 0f;
    private WeaponAnimations animations;
    private Weapon.WeaponState currentState;
    private WeaponDirection currentDirection;
    private boolean animationCompleted = true;
    private boolean shotTriggered = false;
    private boolean reloadTriggered = false;
    private Weapon weapon;
    private String weaponType;

    // Variáveis específicas para recarga da espingarda
    private boolean isShotgunReloading = false;
    private int currentReloadStage = 0;
    private int currentShellInserted = 0;
    private float reloadAnimationTime = 0f;

    public void loadWeaponAnimations(Weapon weapon) {
        this.weapon = weapon;
        this.weaponType = weapon.getClass().getSimpleName();
        Gdx.app.log("WEAPON_RENDERER", "Carregando animações para: " + weaponType);
        this.animations = new WeaponAnimations(weaponType);
        this.currentState = Weapon.WeaponState.IDLE;
    }

    public void update(float delta, Vector2 aimDirection, Weapon.WeaponState state,
            boolean shotJustFired, boolean reloadJustTriggered) {

        boolean isShotgun = weaponType.equals("Calibre12");

        if (isShotgun && weapon instanceof Calibre12) {
            Calibre12 shotgun = (Calibre12) weapon;

            if (shotgun.getCurrentState() == Weapon.WeaponState.IDLE &&
                    (isShotgunReloading || reloadTriggered)) {
                isShotgunReloading = false;
                reloadTriggered = false;
                currentState = Weapon.WeaponState.IDLE;
                animationCompleted = true;
            }
        }
        if (reloadJustTriggered) {
            reloadTriggered = true;
            animationTime = 0f;
            animationCompleted = false;
            currentState = Weapon.WeaponState.RELOADING;

            if (isShotgun) {
                isShotgunReloading = true;
                reloadAnimationTime = 0f;
                currentReloadStage = 0;
                currentShellInserted = 0;
            }
        }
        if (shotJustFired) {
            shotTriggered = true;
            animationTime = 0f;
            animationCompleted = false;

            if (isShotgun && reloadTriggered) {
                reloadTriggered = false;
                isShotgunReloading = false;
            }
        }
        if (!reloadTriggered && !shotTriggered) {
            currentState = state;
        }

        currentDirection = getDirectionFromAngle(aimDirection.angleDeg());
        if (currentState == Weapon.WeaponState.IDLE && !shotTriggered && !reloadTriggered) {
            currentDirection = convertToCardinalDirection(currentDirection);
        }
        if (!animationCompleted) {
            animationTime += delta;
            if (isShotgun && isShotgunReloading && weapon instanceof Calibre12) {
                Calibre12 shotgun = (Calibre12) weapon;
                reloadAnimationTime += delta;
                currentReloadStage = shotgun.getReloadStage();
                currentShellInserted = shotgun.getShellsInserted();
                if (currentReloadStage == 2 && shotgun.getStageProgress() >= 0.95f) {
                    isShotgunReloading = false;
                    reloadTriggered = false;
                    currentState = Weapon.WeaponState.IDLE;
                    animationCompleted = true;
                }
            }
            Weapon.WeaponState animState = getCurrentAnimationState();
            Animation<TextureRegion> anim = animations.getAnimation(currentDirection, animState);

            if (anim != null && anim.isAnimationFinished(animationTime)) {
                animationCompleted = true;
                shotTriggered = false;
                if (!isShotgun && reloadTriggered) {
                    reloadTriggered = false;
                }
            }
        }
    }

    private WeaponDirection convertToCardinalDirection(WeaponDirection direction) {
        switch (direction) {
            case NE:
            case NW:
                return WeaponDirection.N;
            case SE:
            case SW:
                return WeaponDirection.S;
            default:
                return direction;
        }
    }

    private Weapon.WeaponState getCurrentAnimationState() {
        if (weaponType.equals("Calibre12") && isShotgunReloading) {
            return Weapon.WeaponState.RELOADING;
        } else if (reloadTriggered) {
            return Weapon.WeaponState.RELOADING;
        } else if (shotTriggered) {
            return Weapon.WeaponState.SHOOTING;
        } else {
            return currentState;
        }
    }

    private WeaponDirection getDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360;

        if (angle >= 337.5 || angle < 22.5) {
            return WeaponDirection.E;
        } else if (angle >= 22.5 && angle < 67.5) {
            return WeaponDirection.NE;
        } else if (angle >= 67.5 && angle < 112.5) {
            return WeaponDirection.N;
        } else if (angle >= 112.5 && angle < 157.5) {
            return WeaponDirection.NW;
        } else if (angle >= 157.5 && angle < 202.5) {
            return WeaponDirection.W;
        } else if (angle >= 202.5 && angle < 247.5) {
            return WeaponDirection.SW;
        } else if (angle >= 247.5 && angle < 292.5) {
            return WeaponDirection.S;
        } else {
            return WeaponDirection.SE;
        }
    }

    public void render(SpriteBatch batch, Vector2 position, float offsetX, float offsetY) {
        if (animations == null || weapon == null) {
            return;
        }

        TextureRegion frame = null;
        Animation<TextureRegion> anim = null;
        Weapon.WeaponState animState = getCurrentAnimationState();
        boolean isShotgunReload = weaponType.equals("Calibre12") &&
                animState == Weapon.WeaponState.RELOADING &&
                isShotgunReloading;

        if (isShotgunReload) {
            frame = getShotgunReloadFrame();
        } else {
            WeaponDirection renderDirection = currentDirection;

            if (animState == Weapon.WeaponState.IDLE && !shotTriggered) {
                renderDirection = convertToCardinalDirection(currentDirection);
            }

            anim = animations.getAnimation(renderDirection, animState);

            if (anim != null) {
                if (animState == Weapon.WeaponState.IDLE) {
                    // Para IDLE, usar apenas o primeiro frame
                    frame = anim.getKeyFrame(0);
                } else {
                    // Para animações de shooting ou reloading (de outras armas)
                    frame = anim.getKeyFrame(animationTime, false);
                }

                if (animState == Weapon.WeaponState.SHOOTING && animations.needsFlip(renderDirection)) {
                    TextureRegion flipped = new TextureRegion(frame);
                    flipped.flip(true, false);
                    frame = flipped;
                }
            }
        }

        if (frame == null) {
            anim = animations.getAnimation(WeaponDirection.S, Weapon.WeaponState.IDLE);
            if (anim != null) {
                frame = anim.getKeyFrame(0);
            }
        }

        if (frame == null) {
            return;
        }

        WeaponDirection renderDirection = currentDirection;
        if (isShotgunReload) {
            renderDirection = WeaponDirection.S;
        } else if (animState == Weapon.WeaponState.IDLE && !shotTriggered) {
            renderDirection = convertToCardinalDirection(currentDirection);
        }

        Vector2 weaponOffset = weapon.getRenderOffset(renderDirection);

        float scale = 1.4f;
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        float adjustedX = position.x - (width * 0.57f) + weaponOffset.x;
        float adjustedY = position.y - (height * 0.52f) + weaponOffset.y;

        batch.draw(frame, adjustedX, adjustedY, width, height);
    }

    private TextureRegion getShotgunReloadFrame() {
        if (!(weapon instanceof Calibre12)) {
            return null;
        }

        Calibre12 shotgun = (Calibre12) weapon;
        Animation<TextureRegion> reloadAnim = animations.getAnimation(WeaponDirection.S, Weapon.WeaponState.RELOADING);

        if (reloadAnim == null) {
            return null;
        }

        TextureRegion[] reloadFrames = reloadAnim.getKeyFrames();
        if (reloadFrames.length < 10) {
            return reloadFrames.length > 0 ? reloadFrames[0] : null;
        }

        int stage = shotgun.getReloadStage();
        float progress = shotgun.getStageProgress();

        if (stage == 0) {
            // Inclinação: frames 0-2
            int frameIndex = (int) (progress * 3);
            frameIndex = Math.min(frameIndex, 2);
            return reloadFrames[frameIndex];

        } else if (stage == 1) {
            // Inserção das cápsulas: frames 3-9
            int frameIndex = 3 + (int) (progress * 7);
            frameIndex = Math.min(frameIndex, 9);
            return reloadFrames[frameIndex];

        } else if (stage == 2) {
            // Finalização: inclinação reversa (primeira metade) + COCKING (segunda metade)
            if (progress < 0.5f) {
                // 0.0 a 0.5: inclinação reversa (frames 2→0)
                float reverseProgress = progress * 2; // 0 a 1
                int frameIndex = 2 - (int) (reverseProgress * 3);
                frameIndex = Math.max(frameIndex, 0);
                return reloadFrames[frameIndex];
            } else {
                // 0.5 a 1.0: COCKING – frames 5,6,7 da animação de TIRO na direção SUL
                Animation<TextureRegion> shootS = animations.getAnimation(WeaponDirection.S,
                        Weapon.WeaponState.SHOOTING);
                if (shootS != null) {
                    TextureRegion[] shootFrames = shootS.getKeyFrames();
                    if (shootFrames.length >= 8) {
                        float cockingProgress = (progress - 0.5f) * 2; // 0 a 1
                        int cockingIndex;
                        if (cockingProgress < 0.33f) {
                            cockingIndex = 5; // primeiro frame do cocking
                        } else if (cockingProgress < 0.66f) {
                            cockingIndex = 6; // segundo frame
                        } else {
                            cockingIndex = 7; // terceiro frame
                        }
                        return shootFrames[cockingIndex];
                    }
                }
                // Fallback: se não conseguir os frames de tiro, retorna frame 0 do reload
                return reloadFrames[0];
            }
        }

        return reloadFrames[0];
    }

    public void dispose() {
        if (animations != null) {
            animations.dispose();
        }
    }

    public String getDebugInfo() {
        return "Weapon: " + weaponType +
                " | State: " + currentState +
                " | Direction: " + currentDirection +
                " | ShotTriggered: " + shotTriggered +
                " | ReloadTriggered: " + reloadTriggered +
                " | ShotgunReloading: " + isShotgunReloading;
    }
}