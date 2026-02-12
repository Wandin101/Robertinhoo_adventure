package io.github.some_example_name.Entities.Renderer;

import javax.swing.plaf.metal.MetalBorders.PaletteBorder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.PlayerWeaponSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.MapRenderer;

public class PlayerRenderer {
    private float animationTime = 0f;
    private final PlayerAnimations animations;
    private Animation<TextureRegion> currentAnimation;
    private final PlayerWeaponSystem weaponSystem;

    private boolean isPlayingDeathAnimation = false;
    private float deathAnimationTime = 0f;
    private boolean deathAnimationFinished = false;
    private Animation<TextureRegion> deathAnimation;

    public PlayerRenderer(PlayerWeaponSystem weaponSystem) {
        this.weaponSystem = weaponSystem;
        animations = new PlayerAnimations();
        currentAnimation = null;
    }

    public void startDeathAnimation() {
        System.out.println("🎬 [PlayerRenderer.startDeathAnimation] INICIANDO");
        System.out.println("   - Animação disponível: " + (animations.death != null));
        System.out.println("   - Animação deathAnimation: " + (animations.death.deathAnimation != null));

        isPlayingDeathAnimation = true;
        deathAnimationTime = 0f;
        deathAnimationFinished = false;
        deathAnimation = animations.death.deathAnimation;
        currentAnimation = deathAnimation;
        animationTime = 0f;

        if (deathAnimation != null) {
            System.out.println("   ✅ Animação configurada:");
            System.out.println("   - Duração total: " + deathAnimation.getAnimationDuration() + "s");
            System.out.println("   - Frames: " + deathAnimation.getKeyFrames().length);
            System.out.println("   - PlayMode: " + deathAnimation.getPlayMode());
        } else {
            System.err.println("❌ deathAnimation é null!");
        }
    }

    public boolean isDeathAnimationComplete() {
        return deathAnimationFinished;
    }

    public void resetDeathAnimation() {
        isPlayingDeathAnimation = false;
        deathAnimationTime = 0f;
        deathAnimationFinished = false;
        currentAnimation = null;
        animationTime = 0f;
    }

    private Animation<TextureRegion> selectAnimation(Robertinhoo player) {
        if (isPlayingDeathAnimation) {
            return animations.death.deathAnimation;
        }
        if (!player.hasArmor) {
            return selectNoArmorAnimation(player);
        }
        if (player.isTakingDamage) {
            return animations.damage.ritDamage;
        }

        if (player.state == Robertinhoo.MELEE_ATTACK) {
            return getMeleeAnimation(player);
        }

        Weapon equippedWeapon = player.getInventory().getEquippedWeapon();
        boolean isReloading = equippedWeapon != null &&
                equippedWeapon.getCurrentState() == Weapon.WeaponState.RELOADING;

        if (player.state == Robertinhoo.DASH) {
            return getDashAnimation(player);
        }

        if (isReloading) {
            if (player.dir != Robertinhoo.IDLE) {
                return animations.weapon.runDown; // Usar animação de movimento em reload
            } else {
                return animations.weapon.idleDown;
            }
        }

        // Se está mirando e tem arma equipada
        if (weaponSystem.isAiming() && equippedWeapon != null) {
            return getAimedAnimation(player, (player.dir != Robertinhoo.IDLE));
        }

        // Se está se movendo sem arma equipada
        if (player.dir != Robertinhoo.IDLE) {
            // Se não tem arma ou não está mirando, usar animações básicas com 8 direções
            if (equippedWeapon == null || !weaponSystem.isAiming()) {
                return getMovementAnimation(player);
            }
        }

        // Idle sem arma ou sem mirar
        return getIdleAnimation(player);
    }

    private Animation<TextureRegion> selectNoArmorAnimation(Robertinhoo player) {
        if (player.dir != Robertinhoo.IDLE) {
            return getNoArmorMovementAnimation(player);
        }
        return getNoArmorIdleAnimation(player);
    }

    private Animation<TextureRegion> getNoArmorIdleAnimation(Robertinhoo player) {
        switch (player.lastDir) {
            case Robertinhoo.UP:
                return animations.noArmor.idleUp;
            case Robertinhoo.DOWN:
                return animations.noArmor.idleDown;
            case Robertinhoo.LEFT:
                return animations.noArmor.idleLeft;
            case Robertinhoo.RIGHT:
                return animations.noArmor.idleRight;
            default:
                return animations.noArmor.idleDown;
        }
    }

    private Animation<TextureRegion> getNoArmorMovementAnimation(Robertinhoo player) {
        // Mapeia direções diagonais para as 4 direções principais
        int normalizedDir = player.dir;

        // Converte diagonais para direções cardinais
        if (normalizedDir == Robertinhoo.NORTH_EAST || normalizedDir == Robertinhoo.NORTH_WEST) {
            normalizedDir = Robertinhoo.UP;
        } else if (normalizedDir == Robertinhoo.SOUTH_EAST || normalizedDir == Robertinhoo.SOUTH_WEST) {
            normalizedDir = Robertinhoo.DOWN;
        }
        // LEFT e RIGHT permanecem os mesmos

        switch (normalizedDir) {
            case Robertinhoo.UP:
                return animations.noArmor.walkUp;
            case Robertinhoo.DOWN:
                return animations.noArmor.walkDown;
            case Robertinhoo.LEFT:
                return animations.noArmor.walkLeft;
            case Robertinhoo.RIGHT:
                return animations.noArmor.walkRight;
            default:
                return animations.noArmor.walkDown;
        }
    }

    private Animation<TextureRegion> getIdleAnimation(Robertinhoo player) {
        if (weaponSystem.isAiming() && player.getInventory().getEquippedWeapon() != null) {
            return getAimedAnimation(player, false);
        }
        return getDirectionalAnimation(player.lastDir, false, player);
    }

    // PlayerRenderer.java - Modificação no método getDashAnimation
    private Animation<TextureRegion> getDashAnimation(Robertinhoo player) {
        switch (player.dashDirection) {
            case Robertinhoo.UP:
                return animations.special.rollUp;

            case Robertinhoo.DOWN:
                return animations.special.rollDown;

            case Robertinhoo.LEFT:
                return animations.special.rollSide;

            case Robertinhoo.RIGHT:
                return animations.special.rollSide;

            case Robertinhoo.NORTH_EAST:
                return animations.special.rollUpRight;

            case Robertinhoo.NORTH_WEST:
                return animations.special.rollUpLeft;

            case Robertinhoo.SOUTH_EAST:
                return animations.special.rollDownRight;

            case Robertinhoo.SOUTH_WEST:
                return animations.special.rollDownLeft;

            default:
                return animations.special.rollSide;
        }
    }

    private Animation<TextureRegion> getDirectionalAnimation(int direction, boolean isMoving, Robertinhoo player) {
        boolean isOneHand = Robertinhoo.IsUsingOneHandWeapon;
        boolean hasWeapon = weaponSystem.isAiming() && player.getInventory().getEquippedWeapon() != null;
        boolean isShooting = hasWeapon && isWeaponShooting(player);
        if (hasWeapon && !isShooting) {
            direction = convertToCardinalDirection(direction);
        }
        if (isMoving) {
            switch (direction) {
                case Robertinhoo.RIGHT:
                    return isOneHand ? animations.weapon.runRight : animations.basic.walkRight;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.weapon.runLeft : animations.basic.walkLeft;
                case Robertinhoo.UP:
                    return isOneHand ? animations.weapon.runUp : animations.basic.walkUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.weapon.runDown : animations.basic.walkDown;
                case Robertinhoo.NORTH_WEST:
                    return isOneHand ? animations.weapon.runNW : animations.basic.walkNortWast;
                case Robertinhoo.NORTH_EAST:
                    return isOneHand ? animations.weapon.runNE : animations.basic.walkNortEast;
                case Robertinhoo.SOUTH_EAST:
                    return isOneHand ? animations.weapon.runSE : animations.basic.walkSE;
                case Robertinhoo.SOUTH_WEST:
                    return isOneHand ? animations.weapon.runSW : animations.basic.walkSW;
                default:
                    return animations.basic.idleDown;
            }
        } else {
            switch (direction) {
                case Robertinhoo.UP:
                    return isOneHand ? animations.weapon.idleUp : animations.basic.idleUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.weapon.idleDown : animations.basic.idleDown;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.weapon.idleLeft : animations.basic.idleLeft;
                case Robertinhoo.RIGHT:
                    return isOneHand ? animations.weapon.idleRight : animations.basic.idleRight;
                case Robertinhoo.NORTH_WEST:
                    return isOneHand ? animations.weapon.idleNW : animations.basic.idleNorthWest;
                case Robertinhoo.NORTH_EAST:
                    return isOneHand ? animations.weapon.idleNE : animations.basic.idleNorthEast;
                case Robertinhoo.SOUTH_WEST:
                    return isOneHand ? animations.weapon.idleSW : animations.basic.idleSouthWest;
                case Robertinhoo.SOUTH_EAST:
                    return isOneHand ? animations.weapon.idleSE : animations.basic.idleSouthEast;
                default:
                    return animations.basic.idleDown;
            }
        }
    }

    // Método auxiliar para converter direções diagonais em cardinais
    private int convertToCardinalDirection(int direction) {
        switch (direction) {
            case Robertinhoo.NORTH_EAST:
            case Robertinhoo.NORTH_WEST:
                return Robertinhoo.UP;
            case Robertinhoo.SOUTH_EAST:
            case Robertinhoo.SOUTH_WEST:
                return Robertinhoo.DOWN;
            case Robertinhoo.LEFT:
            case Robertinhoo.RIGHT:
            case Robertinhoo.UP:
            case Robertinhoo.DOWN:
            default:
                return direction;
        }
    }

    private Animation<TextureRegion> getMovementAnimation(Robertinhoo player) {
        return getDirectionalAnimation(player.dir, true, player);
    }

    private Animation<TextureRegion> getAimedAnimation(Robertinhoo player, boolean isMoving) {
        float aimAngle = player.applyAimRotation();
        boolean shooting = isWeaponShooting(player);
        int direction;
        if (shooting) {
            direction = getDirectionFromAngle(aimAngle);
        } else {
            direction = getCardinalDirectionFromAngle(aimAngle);
        }

        return getDirectionalAnimation(direction, isMoving, player);
    }

    // Adicionar este método para converter para direções cardinais
    private int getCardinalDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360;
        if (angle >= 22.5f && angle < 157.5f) {
            return Robertinhoo.UP;
        } else if (angle >= 202.5f && angle < 337.5f) {
            return Robertinhoo.DOWN;
        } else if (angle >= 157.5f && angle < 202.5f) {
            return Robertinhoo.LEFT;
        } else {
            return Robertinhoo.RIGHT;
        }
    }

    private int getDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360;
        if (angle >= 22.5 && angle < 67.5)
            return Robertinhoo.NORTH_EAST;
        if (angle >= 67.5 && angle < 112.5)
            return Robertinhoo.UP;
        if (angle >= 112.5 && angle < 157.5)
            return Robertinhoo.NORTH_WEST;
        if (angle >= 157.5 && angle < 202.5)
            return Robertinhoo.LEFT;
        if (angle >= 202.5 && angle < 247.5)
            return Robertinhoo.SOUTH_WEST;
        if (angle >= 247.5 && angle < 292.5)
            return Robertinhoo.DOWN;
        if (angle >= 292.5 && angle < 337.5)
            return Robertinhoo.SOUTH_EAST;
        return Robertinhoo.RIGHT;
    }

    private boolean areOpposite(int dir1, int dir2) {
        return (dir1 == Robertinhoo.UP && dir2 == Robertinhoo.DOWN) ||
                (dir1 == Robertinhoo.DOWN && dir2 == Robertinhoo.UP) ||
                (dir1 == Robertinhoo.LEFT && dir2 == Robertinhoo.RIGHT) ||
                (dir1 == Robertinhoo.RIGHT && dir2 == Robertinhoo.LEFT);

    }

    private Animation<TextureRegion> getMeleeAnimation(Robertinhoo player) {
        boolean isParrySuccess = player.getMeleeAttackSystem().isParrySuccess();
        if (isParrySuccess) {
            switch (player.meleeDirection) {
                case Robertinhoo.RIGHT:
                    return animations.special.parryRight;
                case Robertinhoo.LEFT:
                    return animations.special.parryLeft;
                case Robertinhoo.UP:
                    return animations.special.parryUp;
                case Robertinhoo.DOWN:
                default:
                    return animations.special.parryDown;
            }
        } else {
            switch (player.meleeDirection) {
                case Robertinhoo.RIGHT:
                    return animations.special.meleeAttackRight;
                case Robertinhoo.LEFT:
                    return animations.special.meleeAttackLeft;
                case Robertinhoo.UP:
                    return animations.special.meleeAttackUp;
                case Robertinhoo.DOWN:
                default:
                    return animations.special.meleeAttackDown;
            }
        }
    }

    private boolean shouldReverseAnimation(Robertinhoo player) {

        if (player.state == Robertinhoo.DASH || player.state == Robertinhoo.MELEE_ATTACK) {
            return false;
        }

        if (player.dir == Robertinhoo.IDLE || !weaponSystem.isAiming()
                || player.getInventory().getEquippedWeapon() == null) {
            return false;
        }
        int movementDir = player.dir;
        int aimingDir = getDirectionFromAngle(player.applyAimRotation());
        return areOpposite(movementDir, aimingDir);
    }

    public void render(SpriteBatch batch, Robertinhoo player, float delta, float offsetX, float offsetY) {

        if (isPlayingDeathAnimation) {
            deathAnimationTime += delta;
            // Verifica se a animação terminou
            if (animations.death.deathAnimation.isAnimationFinished(deathAnimationTime)) {
                deathAnimationFinished = true;
            }
        }

        animationTime += delta;

        Animation<TextureRegion> selectedAnimation = selectAnimation(player);
        if (selectedAnimation != currentAnimation) {
            animationTime = 0f;
            currentAnimation = selectedAnimation;
        }

        float effectiveTime = animationTime;
        if (shouldReverseAnimation(player)) {
            float totalDuration = currentAnimation.getAnimationDuration();
            effectiveTime = totalDuration - (animationTime % totalDuration);
        }

        TextureRegion frame = currentAnimation.getKeyFrame(effectiveTime, player.isTakingDamage ? false : true);

        float originalWidth = player.bounds.width * MapRenderer.TILE_SIZE;
        float originalHeight = player.bounds.height * MapRenderer.TILE_SIZE;

        float scale;
        if (player.isTakingDamage) {
            scale = 1.1f;
            float pulse = (float) Math.sin(animationTime * 10f) * 0.1f;
            scale += pulse;
        } else if (!player.hasArmor) {
            scale = 0.9f;
            if (currentAnimation == animations.noArmor.walkDown ||
                    currentAnimation == animations.noArmor.idleDown ||
                    currentAnimation == animations.noArmor.walkUp ||
                    currentAnimation == animations.noArmor.idleUp) {
                scale = 0.9f;
            }
        } else {
            scale = 1.4f;
            if (currentAnimation == animations.basic.walkDown ||
                    currentAnimation == animations.basic.idleDown ||
                    currentAnimation == animations.basic.walkUp ||
                    currentAnimation == animations.basic.idleUp) {
                scale = 1.2f;
            }
        }

        float scaledWidth = originalWidth * scale;
        float scaledHeight = originalHeight * scale;

        float x = offsetX + (player.bounds.x * MapRenderer.TILE_SIZE) - (scaledWidth - originalWidth) / 2;
        float y = offsetY + (player.bounds.y * MapRenderer.TILE_SIZE) - (scaledHeight - originalHeight) / 2;

        // Durante dano, aplica efeito visual de flash vermelho
        if (player.isTakingDamage) {
            float flashAlpha = 0.7f + (float) Math.sin(animationTime * 10f) * 0.3f;
            batch.setColor(1.0f, 0.3f, 0.3f, flashAlpha); // Tom vermelho
        }

        boolean shouldFlip = false;
        if (currentAnimation == animations.basic.walkLeft ||
                (currentAnimation == animations.special.rollSide && player.dashDirection == Robertinhoo.LEFT)) {
            shouldFlip = true;
        } else if (currentAnimation == animations.weapon.idleDown) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle > 90 && aimAngle < 270);
        } else if (currentAnimation == animations.weapon.idleUp) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        } else if (currentAnimation == animations.weapon.runDown) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        } else if (currentAnimation == animations.weapon.runUp) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        }

        this.currentFlipState = shouldFlip;
        batch.draw(frame,
                shouldFlip ? x + scaledWidth : x,
                y,
                shouldFlip ? -scaledWidth : scaledWidth,
                scaledHeight);

        if (player.isTakingDamage) {
            batch.setColor(1, 1, 1, 1);
        }
    }

    private boolean currentFlipState;

    public boolean getCurrentFlipState() {
        return currentFlipState;
    }

    public float getRenderScale(Robertinhoo player) {
        if (currentAnimation == null)
            return player.hasArmor ? 1.4f : 0.9f; // ✅ Diferente para sem armadura

        if (!player.hasArmor) {
            // Escalas para sem armadura
            if (currentAnimation == animations.noArmor.walkDown ||
                    currentAnimation == animations.noArmor.idleDown ||
                    currentAnimation == animations.noArmor.walkUp ||
                    currentAnimation == animations.noArmor.idleUp) {
                return 0.9f;
            }
            return 0.9f;
        } else {
            // Escalas para com armadura (original)
            if (currentAnimation == animations.basic.walkDown ||
                    currentAnimation == animations.basic.idleDown ||
                    currentAnimation == animations.basic.walkUp ||
                    currentAnimation == animations.basic.idleUp) {
                return 1.2f;
            }
            return 1.4f;
        }
    }

    // Adicione este método na classe PlayerRenderer
    private boolean isWeaponShooting(Robertinhoo player) {
        Weapon equippedWeapon = player.getInventory().getEquippedWeapon();
        if (equippedWeapon == null) {
            return false;
        }

        boolean isShooting = equippedWeapon.getCurrentState() == Weapon.WeaponState.SHOOTING;

        // Log para debug
        if (isShooting) {
            Gdx.app.log("PlayerRenderer", "✅ ARMA ATIRANDO - Estado: SHOOTING");
        }

        return isShooting;
    }

    private boolean isWeaponReloading(Robertinhoo player) {
        Weapon equippedWeapon = player.getInventory().getEquippedWeapon();
        if (equippedWeapon == null)
            return false;
        return equippedWeapon.getCurrentState() == Weapon.WeaponState.RELOADING;
    }

    public float getDeathAnimationProgress() {
        if (deathAnimation == null)
            return 0f;
        return Math.min(1.0f, deathAnimationTime / deathAnimation.getAnimationDuration());
    }

    public void dispose() {
        animations.dispose();
    }
}