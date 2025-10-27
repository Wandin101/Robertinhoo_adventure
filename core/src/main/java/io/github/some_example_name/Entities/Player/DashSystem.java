package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;

public class DashSystem {
    private final Robertinhoo player;

    // Constantes AJUSTADAS para sincronizar com animações
    public static final float BASIC_ROLL_DURATION = 0.7f; // Para rolls básicos
    public static final float DASH_COOLDOWN = 1f;
    public static final float DASH_SPEED = 2.5f;
    public static final float DASH_STAMINA_COST = 30f;
    public static final float FREEZE_DURATION = 0.10f;
    public static final float POST_DASH_IMPULSE = 1.2f;
    public static final float POST_DASH_DURATION = 0.2f;

    // Estado do dash
    private float dashTime = 0;
    private float dashCooldownTime = 0;
    private float postDashTime = 0;
    private boolean dashKeyWasPressed = false;
    private boolean isDashing = false;
    private boolean isFreezing = false;
    private boolean isApplyingPostDash = false;
    private Vector2 dashDirectionCache;
    private Vector2 postDashImpulse;
    private float currentDashDuration; // Duração atual baseada na direção

    public DashSystem(Robertinhoo player) {
        this.player = player;
    }

    public void update(float deltaTime) {
        if (dashCooldownTime > 0) {
            dashCooldownTime -= deltaTime;
        }

        if (isDashing) {
            dashTime -= deltaTime;

            if (isFreezing) {
                if (dashTime <= currentDashDuration - FREEZE_DURATION) {
                    isFreezing = false;
                    player.body.setLinearVelocity(dashDirectionCache.cpy().scl(DASH_SPEED));
                }
            } else if (dashTime <= 0) {
                endDash();
            }
        }

        if (isApplyingPostDash) {
            postDashTime -= deltaTime;
            if (postDashTime <= 0) {
                isApplyingPostDash = false;
                player.body.setLinearVelocity(0, 0);
            }
        }
    }

    public void handleDashInput(boolean spacePressed, boolean spaceJustPressed, Vector2 moveDir) {
        if (canDash(spaceJustPressed, spacePressed, moveDir)) {
            activateDash(moveDir);
        }

        dashKeyWasPressed = spacePressed;
    }

    private boolean canDash(boolean spaceJustPressed, boolean spacePressed, Vector2 moveDir) {
        return (spaceJustPressed || (!dashKeyWasPressed && spacePressed)) &&
                dashCooldownTime <= 0 &&
                !isDashing &&
                !isApplyingPostDash &&
                !moveDir.isZero() &&
                player.getStaminaSystem().hasStamina(DASH_STAMINA_COST);
    }

    private void activateDash(Vector2 moveDir) {
        moveDir.nor();
        player.state = Robertinhoo.DASH;
        player.dashDirection = player.dir;
        player.setInvulnerable(true);
        dashDirectionCache = moveDir.cpy();

        // Todos os rolls têm a mesma duração agora
        currentDashDuration = BASIC_ROLL_DURATION;
        dashTime = currentDashDuration;
        dashCooldownTime = DASH_COOLDOWN;
        isDashing = true;
        isFreezing = true;

        player.body.setLinearVelocity(Vector2.Zero);
        player.getStaminaSystem().consumeStamina(DASH_STAMINA_COST);
    }

    private void endDash() {
        player.state = Robertinhoo.IDLE;
        player.setInvulnerable(false);
        isDashing = false;

        postDashImpulse = dashDirectionCache.cpy().scl(DASH_SPEED * POST_DASH_IMPULSE);
    }

    public boolean shouldApplyPostDashImpulse() {
        return postDashImpulse != null;
    }

    public void applyPostDashImpulse() {
        if (postDashImpulse != null) {
            player.body.setLinearVelocity(postDashImpulse);
            postDashImpulse = null;
            isApplyingPostDash = true;
            postDashTime = POST_DASH_DURATION;
        }
    }

    public boolean isDashing() {
        return isDashing;
    }

    public boolean isApplyingPostDashImpulse() {
        return isApplyingPostDash;
    }

    public boolean isFreezing() {
        return isFreezing;
    }

    // Novo método para obter o progresso do dash (útil para sincronização)
    public float getDashProgress() {
        if (!isDashing)
            return 0f;
        return 1f - (dashTime / currentDashDuration);
    }
}