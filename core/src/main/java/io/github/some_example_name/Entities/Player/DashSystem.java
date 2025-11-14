package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import io.github.some_example_name.Entities.Itens.Contact.Constants;

public class DashSystem {
    private final Robertinhoo player;

    // Constantes (mantidas as mesmas)
    public static final float BASIC_ROLL_DURATION = 0.7f;
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
    private float currentDashDuration;
    
    // Para restaurar os filtros de colisão
    private short originalCategoryBits;
    private short originalMaskBits;

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
                    // Aplica a intangibilidade REAL quando começa a se mover
                    applyDashCollisionFilter();
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
        
        // Guarda os filtros originais ANTES de modificar
        saveOriginalCollisionFilter();
        
        dashDirectionCache = moveDir.cpy();

        currentDashDuration = BASIC_ROLL_DURATION;
        dashTime = currentDashDuration;
        dashCooldownTime = DASH_COOLDOWN;
        isDashing = true;
        isFreezing = true;

        player.body.setLinearVelocity(Vector2.Zero);
        player.getStaminaSystem().consumeStamina(DASH_STAMINA_COST);
    }

    private void saveOriginalCollisionFilter() {
        if (player.body.getFixtureList().size > 0) {
            Filter filter = player.body.getFixtureList().first().getFilterData();
            originalCategoryBits = filter.categoryBits;
            originalMaskBits = filter.maskBits;
        }
    }

    private void applyDashCollisionFilter() {
        if (player.body.getFixtureList().size > 0) {
            Filter filter = new Filter();
            filter.categoryBits = originalCategoryBits;
            filter.maskBits = (short) (Constants.BIT_GROUND | Constants.BIT_WALL | Constants.BIT_OBJECT);
            
            player.body.getFixtureList().first().setFilterData(filter);
        }
    }

    private void restoreOriginalCollisionFilter() {
        if (player.body.getFixtureList().size > 0) {
            Filter filter = new Filter();
            filter.categoryBits = originalCategoryBits;
            filter.maskBits = originalMaskBits;
            player.body.getFixtureList().first().setFilterData(filter);
        }
    }

    private void endDash() {
        player.state = Robertinhoo.IDLE;
        isDashing = false;

        // Restaura a colisão normal
        restoreOriginalCollisionFilter();

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

    // Remove o setInvulnerable e gerencia tudo via filtros de colisão
    public boolean isDashing() {
        return isDashing;
    }

    public boolean isApplyingPostDashImpulse() {
        return isApplyingPostDash;
    }

    public boolean isFreezing() {
        return isFreezing;
    }

    public float getDashProgress() {
        if (!isDashing)
            return 0f;
        return 1f - (dashTime / currentDashDuration);
    }
}