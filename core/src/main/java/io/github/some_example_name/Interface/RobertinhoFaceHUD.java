package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

import io.github.some_example_name.Entities.Player.Robertinhoo;

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
    
    // NOVA animação de morte
    private final Animation<TextureRegion> deathFaceAnimation;
    private static final float DEATH_ANIMATION_DURATION = 2.8f; // 14 frames * 0.2s
    
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
        DEATH
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
        
        // NOVA textura de morte
        Texture deathFaceSheet = new Texture("rober/interface/robertinho_death.png");
        
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
        if (isDead) return; // Já está morto
        
        isDead = true;
        currentAnimationState = AnimationState.DEATH;
        faceStateTime = 0f;
        
        Gdx.app.log("FaceHUD", "Animação de MORTE iniciada!");
        
        // Para todas as outras animações
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
        
        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / FRAME_COLS,
                sheet.getHeight() / FRAME_ROWS);
        
        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        System.arraycopy(tmp[0], 0, frames, 0, FRAME_COLS);
        
        return new Animation<>(frameDuration, frames);
    }
    
    private Animation<TextureRegion> createMultiRowAnimation(Texture sheet, int frameCount, int row, float frameDuration) {
        // Calcula número de linhas na spritesheet
        int frameHeight = sheet.getHeight() / 2;
        int FRAME_COLS = frameCount;
        int FRAME_ROWS = 2;
        
        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / FRAME_COLS,
                frameHeight);
        
        if (row >= FRAME_ROWS) {
            row = 0;
        }
        
        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        System.arraycopy(tmp[row], 0, frames, 0, FRAME_COLS);
        
        return new Animation<>(frameDuration, frames);
    }
    
    public void triggerHitAnimation() {
        if (isDead) return; // Se está morto, não mostra hit
        
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
         if (isDead) {
        faceStateTime += delta;
        if (currentAnimationState != AnimationState.DEATH) {
            currentAnimationState = AnimationState.DEATH;
            System.out.println("✅ [FaceHUD] Estado atualizado para DEATH");
        }
        if (faceStateTime >= DEATH_ANIMATION_DURATION) {
            if (deathListener != null) {
                deathListener.onDeathAnimationComplete();
            }
            System.out.println("✅ [FaceHUD] Animação de morte COMPLETA");
        }
        return;
    }
        
        // Se não está morto, continua com o sistema normal
        faceStateTime += delta;
        maskStateTime += delta;
        
        // Atualiza animação de hit se estiver ativa
        if (isHitAnimationActive) {
            hitAnimationTime += delta;
            
            if (hitAnimationTime >= HIT_ANIMATION_DURATION) {
                isHitAnimationActive = false;
                hitAnimationTime = 0f;
                
                boolean currentExhausted = robertinhoo.getStaminaSystem().isExhausted();
                if (currentExhausted) {
                    currentAnimationState = AnimationState.EXHAUSTED;
                    currentFaceAnimation = exhaustedFaceAnimation;
                } else {
                    currentAnimationState = AnimationState.IDLE;
                    currentFaceAnimation = idleFaceAnimation;
                }
                
                faceStateTime = 0f;
                maskStateTime = 0f;
            }
        } else {
            // Verifica mudança de estado normal
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
        if (currentAnimationState == AnimationState.DEATH) {
            // Para morte, trava no último frame quando termina
            if (faceStateTime >= DEATH_ANIMATION_DURATION) {
                faceFrame = deathFaceAnimation.getKeyFrames()[13]; // Último frame
            } else {
                faceFrame = deathFaceAnimation.getKeyFrame(faceStateTime, false);
            }
        } else if (currentAnimationState == AnimationState.HIT) {
            faceFrame = hitFaceAnimation.getKeyFrame(faceStateTime, false);
        } else {
            faceFrame = currentFaceAnimation.getKeyFrame(faceStateTime, true);
        }
        
        // Calcula posição e tamanho da face
        float faceMargin = scaledFaceSize * 0.001f;
        float faceRenderSize = scaledFaceSize - 2 * faceMargin;
        float faceX = scaledX + faceMargin;
        float faceY = scaledY + faceMargin;
        
        // Desenha face
        batch.draw(faceFrame, faceX, faceY, faceRenderSize, faceRenderSize);
        
        // Desenha máscara apenas se NÃO estiver morto
        if (currentAnimationState != AnimationState.DEATH) {
            int vida = (int)robertinhoo.getLife();
            Animation<TextureRegion> maskToUse = null;
            
            switch (currentAnimationState) {
                case IDLE:
                    if (vida < 30) maskToUse = idleLowMaskAnimation;
                    else if (vida < 70) maskToUse = idleMediumMaskAnimation;
                    break;
                case EXHAUSTED:
                    if (vida < 30) maskToUse = exhaustedLowMaskAnimation;
                    else if (vida < 70) maskToUse = exhaustedMediumMaskAnimation;
                    break;
                case HIT:
                    if (vida < 30) maskToUse = hitLowMaskAnimation;
                    else if (vida < 70) maskToUse = hitMediumMaskAnimation;
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
        
        // Desenha batimento cardíaco (se não estiver morto ou mostrar batimento fraco)
        if (batimentoCardiaco != null) {
            float batimentoScaleX = BATIMENTO_WIDTH * scale;
            float batimentoScaleY = BATIMENTO_HEIGHT * scale;
            
            float batimentoX = scaledX + (scaledFaceSize - batimentoScaleX) / 2;
            float horizontalOffset = -7.15f * scale;
            batimentoX += horizontalOffset;
            
            float offsetVertical = scaledFaceSize * 0.175f;
            float batimentoY = scaledY + offsetVertical - (batimentoScaleY / 2);
            
            batimentoCardiaco.setPosition(batimentoX, batimentoY);
            batimentoCardiaco.setSize(batimentoScaleX, batimentoScaleY);
            
            // Se está morto, mostra batimento "flatline" ou nenhum
            if (currentAnimationState == AnimationState.DEATH) {
                // Você pode adicionar um estado especial para batimento de morte
                // Por enquanto, desenha normal
            }
            batimentoCardiaco.draw(batch);
        }
        
        // Desenha barra de stamina (se não estiver morto)
        if (staminaBarRenderer != null && currentAnimationState != AnimationState.DEATH) {
            float staminaScaleX = STAMINA_WIDTH * scale;
            float staminaScaleY = STAMINA_HEIGHT * scale;
            
            float staminaX = scaledX + (scaledFaceSize - staminaScaleX) / 2;
            float horizontalOffset = 3.00f * scale;
            staminaX += horizontalOffset;
            
            float offsetVertical = scaledFaceSize * 0.100f;
            float staminaY = scaledY + offsetVertical - (staminaScaleY / 2);
            
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
    
    // Métodos auxiliares
    public boolean isDeathAnimationComplete() {
        return isDead && faceStateTime >= DEATH_ANIMATION_DURATION;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    public void dispose() {
        metalFrame.dispose();
        staminaBarRenderer.dispose();
        
        if (batimentoCardiaco != null) {
            batimentoCardiaco.dispose();
        }
    }
}