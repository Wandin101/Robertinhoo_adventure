package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

import io.github.some_example_name.Entities.Interatibles.InteractionManager;
import io.github.some_example_name.Entities.Player.Robertinhoo;

import java.util.Arrays;

public class RobertinhoFaceHUD {
    // Frame base
    private final Texture metalFrame;

    // Animações principais
    private final Robertinhoo robertinhoo;
    private float faceStateTime = 0f;
    private float maskStateTime = 0f;
    private boolean lastExhaustedState = false;
    private boolean isDead = false;

    // Sistema de animações
    private AnimationState currentAnimationState;
    private Animation<TextureRegion> currentFaceAnimation;
    private Animation<TextureRegion> currentMaskAnimation;

    // Tempo para animações temporárias
    private float hitAnimationTime = 0f;
    private boolean isHitAnimationActive = false;
    private static final float HIT_ANIMATION_DURATION = 0.5f;

    // Animações individuais
    private final Animation<TextureRegion> idleFaceAnimation;
    private final Animation<TextureRegion> idleMediumMaskAnimation;
    private final Animation<TextureRegion> idleLowMaskAnimation;

    private final Animation<TextureRegion> exhaustedFaceAnimation;
    private final Animation<TextureRegion> exhaustedMediumMaskAnimation;
    private final Animation<TextureRegion> exhaustedLowMaskAnimation;

    private final Animation<TextureRegion> hitFaceAnimation;
    private final Animation<TextureRegion> hitMediumMaskAnimation;
    private final Animation<TextureRegion> hitLowMaskAnimation;

    // Animação de morte
    private final Animation<TextureRegion> deathFaceAnimation;
    private static final float DEATH_ANIMATION_DURATION = 2.8f;

    // Animações de leitura
    private final Animation<TextureRegion> readingEntryAnimation;
    private final Animation<TextureRegion> readingLoopAnimation;
    private boolean readingEntryDone = false;

    // Barra de stamina
    private StaminaBarRenderer staminaBarRenderer;

    // Batimento cardíaco
    private final VidaBatimentoCardiaco batimentoCardiaco;

    // Constantes de tamanho
    private static final float FACE_SIZE = 380;
    private static final float BATIMENTO_WIDTH = 225;
    private static final float BATIMENTO_HEIGHT = 82;
    private static final float SPACING = 2f;
    private static final float STAMINA_WIDTH = 230;
    private static final float STAMINA_HEIGHT = 15;
    private static final float STAMINA_Y_OFFSET = 2;
    private static final float STAMINA_X_OFFSET = 2;

    // Posições
    private float x;
    private float y;
    private float batimentoX;
    private float batimentoY;

    // Callback para quando a animação de morte terminar
    private DeathAnimationListener deathListener;

    // Interface para callback
    public interface DeathAnimationListener {
        void onDeathAnimationComplete();
    }

    // Enum para estados
    private enum AnimationState {
        IDLE,
        EXHAUSTED,
        HIT,
        DEATH,
        READING
    }

    public RobertinhoFaceHUD(float screenWidth, float screenHeight, Robertinhoo robertinhoo) {
        this.robertinhoo = robertinhoo;
        this.staminaBarRenderer = new StaminaBarRenderer(robertinhoo);
        recalculatePosition(screenWidth, screenHeight);

        // Inicializa textura do frame
        metalFrame = new Texture("rober/interface/molde.png");

        // Carrega texturas
        Texture idleFaceSheet = new Texture("rober/interface/robertinhoo_idle-Sheet.png");
        Texture idleMediumMask = new Texture("rober/interface/medium-Sheet.png");
        Texture idleLowMask = new Texture("rober/interface/low-Sheet.png");

        Texture exhaustedFaceSheet = new Texture("rober/interface/cansado-Sheet.png");
        Texture exhaustedMaskSheet = new Texture("rober/interface/Cansado-Mask.png");

        Texture hitFaceSheet = new Texture("rober/interface/robertinho_hit-Sheet.png");
        Texture hitMaskSheet = new Texture("rober/interface/Rit-Mask.png");

        Texture deathFaceSheet = new Texture("rober/interface/robertinho_death.png");

        // Carrega textura de leitura (7 frames)
        Texture readingSheet = new Texture("rober/interface/robertinho_read.png");

        // Cria animações IDLE
        idleFaceAnimation = createAnimation(idleFaceSheet, 8, 0.4f);
        idleMediumMaskAnimation = createAnimation(idleMediumMask, 8, 0.4f);
        idleLowMaskAnimation = createAnimation(idleLowMask, 8, 0.4f);

        // Cria animações EXHAUSTED
        exhaustedFaceAnimation = createAnimation(exhaustedFaceSheet, 5, 0.4f);
        exhaustedMediumMaskAnimation = createMultiRowAnimation(exhaustedMaskSheet, 5, 0, 0.4f);
        exhaustedLowMaskAnimation = createMultiRowAnimation(exhaustedMaskSheet, 5, 1, 0.4f);

        // Cria animações HIT
        hitFaceAnimation = createAnimation(hitFaceSheet, 6, 0.083f);
        hitMediumMaskAnimation = createMultiRowAnimation(hitMaskSheet, 6, 0, 0.083f);
        hitLowMaskAnimation = createMultiRowAnimation(hitMaskSheet, 6, 1, 0.083f);

        // Cria animação DEATH (14 frames, sem loop)
        deathFaceAnimation = createAnimation(deathFaceSheet, 14, 0.2f);

        // Cria animações de leitura
        int readingCols = 7;
        TextureRegion[][] readingTemp = TextureRegion.split(readingSheet, readingSheet.getWidth() / readingCols,
                readingSheet.getHeight());
        TextureRegion[] readingFrames = readingTemp[0];
        // frames 0-1: entrada (normal)
        readingEntryAnimation = new Animation<>(0.2f, Arrays.copyOfRange(readingFrames, 0, 2));
        readingEntryAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        // frames 2-6: loop de leitura
        readingLoopAnimation = new Animation<>(0.4f, Arrays.copyOfRange(readingFrames, 2, 7));
        readingLoopAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Define estado inicial
        currentAnimationState = AnimationState.IDLE;
        currentFaceAnimation = idleFaceAnimation;
        currentMaskAnimation = null;

        lastExhaustedState = robertinhoo.getStaminaSystem().isExhausted();
        isDead = false;

        // Inicializa batimento cardíaco
        Texture batimentoSheet = new Texture("rober/interface/vida-full-Sheet.png");
        batimentoCardiaco = new VidaBatimentoCardiaco(
                batimentoSheet,
                robertinhoo,
                batimentoX,
                batimentoY,
                BATIMENTO_WIDTH,
                BATIMENTO_HEIGHT);

        faceStateTime = 0f;
        maskStateTime = 0f;
        hitAnimationTime = 0f;
        isHitAnimationActive = false;
    }

    // Método para ativar animação de morte
    public void triggerDeathAnimation() {
        if (isDead)
            return;
        isDead = true;
        currentAnimationState = AnimationState.DEATH;
        faceStateTime = 0f;
        Gdx.app.log("FaceHUD", "Animação de MORTE iniciada!");
        isHitAnimationActive = false;
        hitAnimationTime = 0f;
    }

    // Método para resetar quando o jogador renasce
    public void resetDeathAnimation() {
        isDead = false;
        currentAnimationState = AnimationState.IDLE;
        faceStateTime = 0f;
        maskStateTime = 0f;
        Gdx.app.log("FaceHUD", "Animação de morte resetada!");
    }

    // Set listener para callback
    public void setDeathAnimationListener(DeathAnimationListener listener) {
        this.deathListener = listener;
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        int FRAME_COLS = frameCount;
        int FRAME_ROWS = 1;
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / FRAME_COLS,
                sheet.getHeight() / FRAME_ROWS);
        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        System.arraycopy(tmp[0], 0, frames, 0, FRAME_COLS);
        return new Animation<>(frameDuration, frames);
    }

    private Animation<TextureRegion> createMultiRowAnimation(Texture sheet, int frameCount, int row,
            float frameDuration) {
        int frameHeight = sheet.getHeight() / 2;
        int FRAME_COLS = frameCount;
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / FRAME_COLS, frameHeight);
        if (row >= 2)
            row = 0;
        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        System.arraycopy(tmp[row], 0, frames, 0, FRAME_COLS);
        return new Animation<>(frameDuration, frames);
    }

    public void triggerHitAnimation() {
        if (isDead)
            return;
        isHitAnimationActive = true;
        hitAnimationTime = 0f;
        currentAnimationState = AnimationState.HIT;
        faceStateTime = 0f;
        maskStateTime = 0f;
    }

    private void recalculatePosition(float screenWidth, float screenHeight) {
        float margin = 10f;
        this.x = screenWidth - FACE_SIZE - margin;
        this.y = margin;
        this.batimentoX = x + (FACE_SIZE - BATIMENTO_WIDTH) / 2;
        this.batimentoY = y + FACE_SIZE + SPACING;

        float staminaX = screenWidth - FACE_SIZE - 10 + STAMINA_X_OFFSET;
        float staminaY = 10 + STAMINA_Y_OFFSET;
        staminaBarRenderer.setPosition(staminaX, staminaY);
    }

    public void update(float delta) {
        // Prioridade máxima: morte
        if (isDead) {
            faceStateTime += delta;
            if (currentAnimationState != AnimationState.DEATH) {
                currentAnimationState = AnimationState.DEATH;
                System.out.println("✅ [FaceHUD] Estado atualizado para DEATH");
            }
            if (faceStateTime >= DEATH_ANIMATION_DURATION && deathListener != null) {
                deathListener.onDeathAnimationComplete();
            }
            return;
        }

        // Prioridade: hit
        if (isHitAnimationActive) {
            hitAnimationTime += delta;
            faceStateTime += delta;
            maskStateTime += delta;
            if (hitAnimationTime >= HIT_ANIMATION_DURATION) {
                isHitAnimationActive = false;
                hitAnimationTime = 0f;
                // Volta para estado normal ou leitura
                boolean dialogueActive = InteractionManager.getInstance().isDialogueActive();
                if (dialogueActive) {
                    currentAnimationState = AnimationState.READING;
                    readingEntryDone = false;
                    faceStateTime = 0f;
                } else {
                    boolean exhausted = robertinhoo.getStaminaSystem().isExhausted();
                    currentAnimationState = exhausted ? AnimationState.EXHAUSTED : AnimationState.IDLE;
                    currentFaceAnimation = exhausted ? exhaustedFaceAnimation : idleFaceAnimation;
                    faceStateTime = 0f;
                    maskStateTime = 0f;
                }
            }
            return;
        }

        // Prioridade: leitura
        boolean dialogueActive = InteractionManager.getInstance().isDialogueActive();
        if (dialogueActive) {
            if (currentAnimationState != AnimationState.READING) {
                currentAnimationState = AnimationState.READING;
                readingEntryDone = false;
                faceStateTime = 0f;
            }
            faceStateTime += delta;
            if (!readingEntryDone && readingEntryAnimation.isAnimationFinished(faceStateTime)) {
                readingEntryDone = true;
                faceStateTime = 0f; // começa o loop
            }
            return;
        }

        // Estado normal (idle/exhausted)
        if (currentAnimationState == AnimationState.READING) {
            // Saiu da leitura
            boolean exhausted = robertinhoo.getStaminaSystem().isExhausted();
            currentAnimationState = exhausted ? AnimationState.EXHAUSTED : AnimationState.IDLE;
            currentFaceAnimation = exhausted ? exhaustedFaceAnimation : idleFaceAnimation;
            faceStateTime = 0f;
            maskStateTime = 0f;
        } else {
            // Lógica normal de idle/exhausted (código original)
            faceStateTime += delta;
            maskStateTime += delta;

            boolean currentExhausted = robertinhoo.getStaminaSystem().isExhausted();
            if (currentExhausted != lastExhaustedState) {
                faceStateTime = 0f;
                maskStateTime = 0f;
                lastExhaustedState = currentExhausted;
                if (currentExhausted) {
                    currentAnimationState = AnimationState.EXHAUSTED;
                    currentFaceAnimation = exhaustedFaceAnimation;
                } else {
                    currentAnimationState = AnimationState.IDLE;
                    currentFaceAnimation = idleFaceAnimation;
                }
                currentMaskAnimation = null;
            }
        }

        // Atualiza batimento cardíaco
        if (batimentoCardiaco != null) {
            batimentoCardiaco.update(delta);
        }
    }

    public void draw(SpriteBatch batch, float delta) {
        Matrix4 originalMatrix = batch.getProjectionMatrix().cpy();
        Matrix4 hudMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudMatrix);

        float scale = Math.min(1.0f, Gdx.graphics.getWidth() / 1280f);
        float scaledFaceSize = FACE_SIZE * scale;
        float scaledX = Gdx.graphics.getWidth() - scaledFaceSize - (10f * scale);
        float scaledY = 10f * scale;

        // Desenha frame
        batch.draw(metalFrame, scaledX, scaledY, scaledFaceSize, scaledFaceSize);

        // Obtém frame atual baseado no estado
        TextureRegion faceFrame;
        switch (currentAnimationState) {
            case DEATH:
                if (faceStateTime >= DEATH_ANIMATION_DURATION) {
                    faceFrame = deathFaceAnimation.getKeyFrames()[13];
                } else {
                    faceFrame = deathFaceAnimation.getKeyFrame(faceStateTime, false);
                }
                break;
            case HIT:
                faceFrame = hitFaceAnimation.getKeyFrame(faceStateTime, false);
                break;
            case READING:
                if (!readingEntryDone) {
                    faceFrame = readingEntryAnimation.getKeyFrame(faceStateTime, false);
                } else {
                    faceFrame = readingLoopAnimation.getKeyFrame(faceStateTime, true);
                }
                break;
            default: // IDLE ou EXHAUSTED
                faceFrame = currentFaceAnimation.getKeyFrame(faceStateTime, true);
                break;
        }

        // Calcula posição e tamanho da face
        float faceMargin = scaledFaceSize * 0.001f;
        float faceRenderSize = scaledFaceSize - 2 * faceMargin;
        float faceX = scaledX + faceMargin;
        float faceY = scaledY + faceMargin;

        // Desenha face
        batch.draw(faceFrame, faceX, faceY, faceRenderSize, faceRenderSize);

        // Desenha máscara apenas se não estiver morto e não estiver lendo
        if (currentAnimationState != AnimationState.DEATH && currentAnimationState != AnimationState.READING) {
            int vida = (int) robertinhoo.getLife();
            Animation<TextureRegion> maskToUse = null;

            switch (currentAnimationState) {
                case IDLE:
                    if (vida < 30)
                        maskToUse = idleLowMaskAnimation;
                    else if (vida < 70)
                        maskToUse = idleMediumMaskAnimation;
                    break;
                case EXHAUSTED:
                    if (vida < 30)
                        maskToUse = exhaustedLowMaskAnimation;
                    else if (vida < 70)
                        maskToUse = exhaustedMediumMaskAnimation;
                    break;
                case HIT:
                    if (vida < 30)
                        maskToUse = hitLowMaskAnimation;
                    else if (vida < 70)
                        maskToUse = hitMediumMaskAnimation;
                    break;
            }

            if (maskToUse != null) {
                TextureRegion maskFrame;
                if (currentAnimationState == AnimationState.HIT) {
                    maskFrame = maskToUse.getKeyFrame(maskStateTime, false);
                } else {
                    maskFrame = maskToUse.getKeyFrame(maskStateTime, true);
                }
                batch.draw(maskFrame, faceX, faceY, faceRenderSize, faceRenderSize);
            }
        }

        // Desenha batimento cardíaco
        if (batimentoCardiaco != null) {
            float batimentoScaleX = BATIMENTO_WIDTH * scale;
            float batimentoScaleY = BATIMENTO_HEIGHT * scale;
            float batimentoX = scaledX + (scaledFaceSize - batimentoScaleX) / 2 + (-7.15f * scale);
            float batimentoY = scaledY + (scaledFaceSize * 0.175f) - (batimentoScaleY / 2);
            batimentoCardiaco.setPosition(batimentoX, batimentoY);
            batimentoCardiaco.setSize(batimentoScaleX, batimentoScaleY);
            batimentoCardiaco.draw(batch);
        }

        // Desenha barra de stamina (se não estiver morto)
        if (staminaBarRenderer != null && currentAnimationState != AnimationState.DEATH) {
            float staminaScaleX = STAMINA_WIDTH * scale;
            float staminaScaleY = STAMINA_HEIGHT * scale;
            float staminaX = scaledX + (scaledFaceSize - staminaScaleX) / 2 + (3.00f * scale);
            float staminaY = scaledY + (scaledFaceSize * 0.100f) - (staminaScaleY / 2);
            staminaBarRenderer.setPosition(staminaX, staminaY);
            staminaBarRenderer.setSize(staminaScaleX, staminaScaleY);
            staminaBarRenderer.draw(batch, delta);
        }

        batch.setProjectionMatrix(originalMatrix);
    }

    public void updateScreenSize(float width, float height) {
        recalculatePosition(width, height);
        batimentoCardiaco.setPosition(batimentoX, batimentoY);
    }

    public boolean isDeathAnimationComplete() {
        return isDead && faceStateTime >= DEATH_ANIMATION_DURATION;
    }

    public boolean isDead() {
        return isDead;
    }

    public void dispose() {
        metalFrame.dispose();
        staminaBarRenderer.dispose();
        if (batimentoCardiaco != null)
            batimentoCardiaco.dispose();
    }
}