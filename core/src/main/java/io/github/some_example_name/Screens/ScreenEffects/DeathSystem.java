package io.github.some_example_name.Screens.ScreenEffects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Fonts.FontsManager; // <-- Import do gerenciador de fontes
import io.github.some_example_name.Interface.RobertinhoFaceHUD;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Screens.GameScreen;

public class DeathSystem {

    private enum DeathState {
        ALIVE,
        PLAYING_DEATH_ANIMATION,
        DYING,
        DEATH_SCREEN,
        RESPAWNING
    }

    private DeathState currentState = DeathState.ALIVE;
    private float deathTimer = 0f;
    private float screenAlpha = 0f;
    private float textPulse = 0f;
    private boolean canRespawn = false;
    private RobertinhoFaceHUD faceHUD;

    private BitmapFont deathFont;
    private BitmapFont promptFont;
    private Texture skullTexture;

    private Robertinhoo player;
    private Mapa currentMap;
    private GameScreen gameScreen;
    private PlayerRenderer playerRenderer;

    private static final float DEATH_ANIMATION_DURATION = 3.5f;
    private static final float DYING_DURATION = 0f;
    private static final float DEATH_SCREEN_DURATION = 2.0f;

    public DeathSystem(GameScreen gameScreen, PlayerRenderer playerRenderer, RobertinhoFaceHUD faceHUD) {
        this.gameScreen = gameScreen;
        this.playerRenderer = playerRenderer;
        this.faceHUD = faceHUD;
        loadAssets();
    }

    private void loadAssets() {
        // Usa o FontsManager centralizado para obter as fontes
        deathFont = FontsManager.getInstance().getDefaultMenuFont(48);
        promptFont = FontsManager.getInstance().getDefaultMenuFont(24);

        // Carrega a textura da caveira (com fallback)
        try {
            skullTexture = new Texture(Gdx.files.internal("HUD/DEATH ICON.png"));
        } catch (Exception e) {
            createFallbackTexture();
        }
    }

    // Fallback apenas para a textura (as fontes são gerenciadas pelo FontsManager)
    private void createFallbackTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(32, 32, 30);
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(22, 40, 6);
        pixmap.fillCircle(42, 40, 6);
        pixmap.fillRectangle(20, 20, 24, 8);
        skullTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta, Robertinhoo player, Mapa map) {
        this.player = player;
        this.currentMap = map;

        switch (currentState) {
            case ALIVE:
                checkPlayerDeath();
                break;
            case PLAYING_DEATH_ANIMATION:
                updateDeathAnimation(delta);
                break;
            case DYING:
                updateDying(delta);
                break;
            case DEATH_SCREEN:
                updateDeathScreen(delta);
                break;
            case RESPAWNING:
                // Nada a fazer aqui, o respawn é tratado em outro lugar
                break;
        }
    }

    private void checkPlayerDeath() {
        if (player != null && player.getLife() <= 0 && currentState == DeathState.ALIVE) {
            startDeathAnimation();
        }
    }

    private void startDeathAnimation() {
        currentState = DeathState.PLAYING_DEATH_ANIMATION;
        deathTimer = 0f;
        screenAlpha = 0f;
        canRespawn = false;

        if (faceHUD != null) {
            faceHUD.triggerDeathAnimation();
            System.out.println("✅ [DeathSystem] Animação de morte do HUD iniciada");
        } else {
            System.err.println("❌ [DeathSystem] faceHUD é nulo!");
        }

        if (playerRenderer != null) {
            playerRenderer.startDeathAnimation();
            System.out.println("✅ [DeathSystem] Animação de morte do Player iniciada");
        } else {
            System.err.println("❌ [DeathSystem] playerRenderer é nulo!");
        }

        if (player != null && player.body != null) {
            player.body.setLinearVelocity(0, 0);
            player.body.setAngularVelocity(0);
            player.body.setActive(false);

            Vector2 currentPos = player.body.getPosition();
            player.body.setTransform(currentPos, 0);
            player.body.applyForceToCenter(new Vector2(0, 0), true);
            player.body.applyLinearImpulse(new Vector2(0, 0), player.body.getWorldCenter(), true);

            player.dir = Robertinhoo.IDLE;
            player.body.setLinearVelocity(0, 0);
        }

        System.out.println("🎬 [DeathSystem] Animação de morte INICIADA");
    }

    private void updateDeathAnimation(float delta) {
        deathTimer += delta;

        boolean playerRendererComplete = false;
        boolean hudComplete = false;

        if (playerRenderer != null) {
            playerRendererComplete = playerRenderer.isDeathAnimationComplete();
        }

        if (faceHUD != null) {
            hudComplete = faceHUD.isDeathAnimationComplete();
        }

        if ((playerRendererComplete && hudComplete) || deathTimer >= DEATH_ANIMATION_DURATION) {
            transitionToDeathScreen();
        }
    }

    private void transitionToDeathScreen() {
        currentState = DeathState.DYING;
        deathTimer = 0f;
        System.out.println("🎬 [DeathSystem] Todas animações de morte completas. Iniciando tela de morte...");
    }

    private void updateDying(float delta) {
        deathTimer += delta;

        // Fade in da tela preta
        screenAlpha = Interpolation.pow2.apply(0f, 0.8f, deathTimer / DYING_DURATION);

        if (deathTimer >= DYING_DURATION) {
            currentState = DeathState.DEATH_SCREEN;
            deathTimer = 0f;
            screenAlpha = 0.9f;

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    canRespawn = true;
                    System.out.println("✅ Agora pode respawnar (pressione ESPAÇO)");
                }
            }, DEATH_SCREEN_DURATION);
        }
    }

    private void updateDeathScreen(float delta) {
        deathTimer += delta;
        textPulse += delta * 2;

        if (canRespawn && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            triggerRespawn();
        }
    }

    private void triggerRespawn() {
        System.out.println("🔄 Iniciando respawn...");
        currentState = DeathState.RESPAWNING;

        if (playerRenderer != null) {
            playerRenderer.resetDeathAnimation();
        }

        applyDeathPenalties();

        if (gameScreen != null) {
            gameScreen.onPlayerRespawn();
        }
    }

    private void applyDeathPenalties() {
        if (player == null)
            return;
        player.setInvulnerable(false);
    }

    public void render(SpriteBatch batch, float screenWidth, float screenHeight) {
        if (currentState == DeathState.PLAYING_DEATH_ANIMATION) {
            renderDeathAnimationOverlay(batch, screenWidth, screenHeight);
            return;
        }

        if (currentState == DeathState.ALIVE || currentState == DeathState.RESPAWNING) {
            return;
        }

        Color originalColor = batch.getColor();
        batch.setColor(0, 0, 0, screenAlpha);
        batch.draw(getBlackPixel(), 0, 0, screenWidth, screenHeight);

        if (currentState == DeathState.DEATH_SCREEN) {
            renderDeathScreenContent(batch, screenWidth, screenHeight);
        }

        batch.setColor(originalColor);
    }

    private void renderDeathAnimationOverlay(SpriteBatch batch, float screenWidth, float screenHeight) {
        // Pode ser deixado vazio ou usado para efeitos adicionais
    }

    private void renderDeathScreenContent(SpriteBatch batch, float screenWidth, float screenHeight) {
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;

        if (skullTexture != null) {
            float skullSize = 260f;
            float skullY = centerY + 100;
            batch.setColor(Color.WHITE);
            batch.draw(skullTexture, centerX - skullSize / 2, skullY - skullSize / 2, skullSize, skullSize);
        }

        deathFont.setColor(0.9f, 0.1f, 0.1f, 1f);
        deathFont.draw(batch, "VOCÊ MORREU",
                centerX, centerY + 50,
                0, Align.center, false);

        promptFont.setColor(0.8f, 0.8f, 0.8f, 1f);
        promptFont.draw(batch, "Os corvos vão festejar hoje...",
                centerX, centerY,
                0, Align.center, false);

        if (canRespawn) {
            float pulseAlpha = (float) (Math.sin(textPulse * 3) * 0.3f + 0.7f);
            promptFont.setColor(1f, 0.8f, 0.2f, pulseAlpha);
            promptFont.draw(batch, "Pressione [ESPAÇO] para renascer",
                    centerX, centerY - 80,
                    0, Align.center, false);

            promptFont.setColor(0.6f, 0.6f, 0.6f, 0.8f);
            promptFont.getData().setScale(0.8f);
            promptFont.draw(batch, "(Você perdeu alguns recursos)",
                    centerX, centerY - 120,
                    0, Align.center, false);
            promptFont.getData().setScale(1f);
        } else {
            float timeLeft = DEATH_SCREEN_DURATION - deathTimer;
            if (timeLeft > 0) {
                promptFont.setColor(0.5f, 0.5f, 0.5f, 0.7f);
                promptFont.draw(batch, String.format("Renascendo em %.1f...", timeLeft),
                        centerX, centerY - 80,
                        0, Align.center, false);
            }
        }

        String[] tips = {
                "Dica: Use o roll para desviar de ataques!",
                "Dica: Aproveite a fogueira na Sala 0 para craftar.",
                "Dica: Não subestime os ratos...",
                "Dica: Guarde munição para o chefe.",
                "Dica: Explore cantos escuros para encontrar recursos."
        };

        int tipIndex = (int) (deathTimer * 0.5f) % tips.length;
        promptFont.setColor(0.4f, 0.6f, 0.8f, 0.6f);
        promptFont.getData().setScale(0.7f);
        promptFont.draw(batch, tips[tipIndex],
                centerX, 60,
                0, Align.center, false);
        promptFont.getData().setScale(1f);
    }

    private Texture blackPixel;

    private Texture getBlackPixel() {
        if (blackPixel == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1,
                    com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackPixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return blackPixel;
    }

    public boolean isPlayerDead() {
        return currentState != DeathState.ALIVE && currentState != DeathState.RESPAWNING;
    }

    public boolean isPlayingDeathAnimation() {
        return currentState == DeathState.PLAYING_DEATH_ANIMATION;
    }

    public boolean isRespawning() {
        return currentState == DeathState.RESPAWNING;
    }

    public void reset() {
        currentState = DeathState.ALIVE;
        deathTimer = 0f;
        screenAlpha = 0f;
        canRespawn = false;

        if (playerRenderer != null) {
            playerRenderer.resetDeathAnimation();
        }

        if (faceHUD != null) {
            faceHUD.resetDeathAnimation();
        }
    }

    public void updatePlayerRenderer(PlayerRenderer newPlayerRenderer, RobertinhoFaceHUD newFaceHUD) {
        this.playerRenderer = newPlayerRenderer;
        this.faceHUD = newFaceHUD;

        if (currentState == DeathState.PLAYING_DEATH_ANIMATION) {
            System.out.println("⚠️ Resetando animação de morte devido a troca de renderer/HUD");
            reset();
        }
    }

    public void dispose() {
        // As fontes NÃO são descartadas aqui – o FontsManager cuida disso globalmente
        if (skullTexture != null)
            skullTexture.dispose();
        if (blackPixel != null)
            blackPixel.dispose();
    }
}