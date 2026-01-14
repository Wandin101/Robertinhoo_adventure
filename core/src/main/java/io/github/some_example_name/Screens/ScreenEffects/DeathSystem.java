package io.github.some_example_name.Screens.ScreenEffects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Screens.GameScreen;

public class DeathSystem {

    private enum DeathState {
        ALIVE,
        PLAYING_DEATH_ANIMATION, // Novo estado
        DYING,
        DEATH_SCREEN,
        RESPAWNING
    }

    private DeathState currentState = DeathState.ALIVE;
    private float deathTimer = 0f;
    private float screenAlpha = 0f;
    private float textPulse = 0f;
    private boolean canRespawn = false;

    private BitmapFont deathFont;
    private BitmapFont promptFont;
    private Texture skullTexture;

    private Robertinhoo player;
    private Mapa currentMap;
    private GameScreen gameScreen;
    private PlayerRenderer playerRenderer;

    // Configurações
    private static final float DEATH_ANIMATION_DURATION = 3.5f;
    private static final float DYING_DURATION = 0f;
    private static final float DEATH_SCREEN_DURATION = 2.0f;
    private static final float FADE_DURATION = 1.0f;

    public DeathSystem(GameScreen gameScreen, PlayerRenderer playerRenderer) {
        this.gameScreen = gameScreen;
        this.playerRenderer = playerRenderer;
        loadAssets();
    }

    private void loadAssets() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal("HUD/04B_30__.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();

            params.size = 48;
            params.color = Color.WHITE;
            params.borderColor = Color.BLACK;
            params.borderWidth = 2;
            deathFont = generator.generateFont(params);

            params.size = 24;
            params.color = new Color(0.8f, 0.8f, 0.8f, 1);
            promptFont = generator.generateFont(params);

            generator.dispose();

            try {
                skullTexture = new Texture(Gdx.files.internal("HUD/DEATH ICON.png"));
            } catch (Exception e) {
                createFallbackTexture();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar assets da tela de morte: " + e.getMessage());
            createFallbackFonts();
        }
    }

    private void createFallbackFonts() {
        deathFont = new BitmapFont();
        promptFont = new BitmapFont();
        deathFont.getData().setScale(2);
        promptFont.getData().setScale(1);
    }

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
    if (playerRenderer != null) {
        playerRenderer.startDeathAnimation();
    } else {
        System.err.println("❌ [DeathSystem] playerRenderer é nulo!");
    }

   
    if (player != null && player.body != null) {
        System.out.println("   ❄️ Congelando corpo físico do jogador...");
        
        // 1. Para TODAS as velocidades
        player.body.setLinearVelocity(0, 0);
        player.body.setAngularVelocity(0);
        
        // 2. Desativa o corpo para ignorar física completamente
        player.body.setActive(false);
        
        // 3. Salva a posição atual para garantir que não se mova
        Vector2 currentPos = player.body.getPosition();
        player.body.setTransform(currentPos, 0);
        
        // 4. Remove todas as forças
        player.body.applyForceToCenter(new Vector2(0, 0), true);
        player.body.applyLinearImpulse(new Vector2(0, 0), player.body.getWorldCenter(), true);
        
        // 5. Para a movimentação do jogador
        player.dir = Robertinhoo.IDLE;
        player.body.setLinearVelocity(0, 0);
        
        System.out.println("   ✅ Corpo físico congelado");
    }
}
private void updateDeathAnimation(float delta) {
    deathTimer += delta;
    System.out.println("⏱️ [DeathSystem.updateDeathAnimation] deathTimer: " + deathTimer + "/" + DEATH_ANIMATION_DURATION);

    // Verifica se a animação de morte terminou
    if (playerRenderer != null) {
        boolean isComplete = playerRenderer.isDeathAnimationComplete();
        System.out.println("🔍 [DeathSystem.updateDeathAnimation] Animação completa? " + isComplete);
        
        if (isComplete) {
            // Transição para o estado de fade da tela
            currentState = DeathState.DYING;
            deathTimer = 0f;
            System.out.println("🎬 [DeathSystem] Animação de morte completa. Iniciando fade para tela de morte...");
        }
    } else {
        System.err.println("❌ [DeathSystem.updateDeathAnimation] playerRenderer é nulo!");
        
        // Fallback: após o tempo esperado, força a transição
        if (deathTimer >= DEATH_ANIMATION_DURATION) {
            currentState = DeathState.DYING;
            deathTimer = 0f;
            System.out.println("⚠️ [DeathSystem] Fallback: Forçando transição após timeout");
        }
    }
}

    private void updateDying(float delta) {
        deathTimer += delta;

        // Fade in da tela preta
        screenAlpha = Interpolation.pow2.apply(0f, 0.8f, deathTimer / DYING_DURATION);

        if (deathTimer >= DYING_DURATION) {
            currentState = DeathState.DEATH_SCREEN;
            deathTimer = 0f;
            screenAlpha = 0.9f;

            // Agenda o respawn para ser permitido após um tempo
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

        // Pulsação do texto
        float pulse = (float) (Math.sin(textPulse) * 0.2f + 0.8f);

        // Verifica input para respawn
        if (canRespawn && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            triggerRespawn();
        }
    }

    private void triggerRespawn() {
        System.out.println("🔄 Iniciando respawn...");
        currentState = DeathState.RESPAWNING;

        // Reseta a animação de morte no renderer
        if (playerRenderer != null) {
            playerRenderer.resetDeathAnimation();
        }

        // Aplica penalidades
        applyDeathPenalties();

        // Chama o respawn no GameScreen
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
        float progress = deathTimer / DEATH_ANIMATION_DURATION;
        float overlayAlpha = Math.min(0.2f, progress * 0.3f);
        Color originalColor = batch.getColor();
        batch.setColor(0, 0, 0, overlayAlpha);
        batch.draw(getBlackPixel(), 0, 0, screenWidth, screenHeight);
        batch.setColor(originalColor);
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
    }

    public void updatePlayerRenderer(PlayerRenderer newPlayerRenderer) {
       
        this.playerRenderer = newPlayerRenderer;
        if (currentState == DeathState.PLAYING_DEATH_ANIMATION) {
            System.out.println("   ⚠️ Resetando animação de morte devido a troca de renderer");
            reset();
        }
    }

    public void dispose() {
        if (deathFont != null)
            deathFont.dispose();
        if (promptFont != null)
            promptFont.dispose();
        if (skullTexture != null)
            skullTexture.dispose();
        if (blackPixel != null)
            blackPixel.dispose();
    }
}