package io.github.some_example_name.Interface;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.Room0Cabana;

public class CabanaInteractionSystem {
    private final Mapa mapa;
    private  Robertinhoo player;
    private BitmapFont font;
    private boolean showInteractPrompt = false;
    private Room0Cabana nearbyCabana = null;
    
    // Efeito de transição
    private boolean isTransitioning = false;
    private float transitionTimer = 0f;
    private static final float FADE_IN_DURATION = 0.3f;
    private static final float BLACK_DURATION = 0.4f;
    private static final float FADE_OUT_DURATION = 0.3f;
    private static final float TOTAL_TRANSITION_DURATION = FADE_IN_DURATION + BLACK_DURATION + FADE_OUT_DURATION;
    
    private boolean armorSwapped = false; 
    private Texture blackPixelTexture;

    public CabanaInteractionSystem(Mapa mapa, Robertinhoo player) {
        this.mapa = mapa;
        this.player = player;
        this.font = new BitmapFont();
        font.getData().setScale(0.8f);
        createBlackPixelTexture();
        System.out.println("✅ CabanaInteractionSystem inicializado");
    }

    public void update(float delta) {
        // ✅ DEBUG: Log do estado atual
        if (isTransitioning) {
            System.out.println("🔄 UPDATE: Transição em andamento - Timer: " + transitionTimer + "/" + TOTAL_TRANSITION_DURATION);
        }
        
        if (isTransitioning) {
            updateTransition(delta);
            return;
        }
        checkNearbyCabana();
        checkInteractInput();
    }

    private void checkNearbyCabana() {
        showInteractPrompt = false;
        nearbyCabana = null;
        
        if (mapa.getCabanas().isEmpty()) {
            System.out.println("❌ Nenhuma cabana no mapa");
            return;
        }
        
        Vector2 playerPos = player.pos;
        
        for (Room0Cabana cabana : mapa.getCabanas()) {
            Vector2 cabanaPos = cabana.getPosition();
            float distance = playerPos.dst(cabanaPos);
            
            if (distance < 2.0f) {
                showInteractPrompt = true;
                nearbyCabana = cabana;
                break;
            }
        }
    }
    
    private void checkInteractInput() {
        if (showInteractPrompt && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            System.out.println("🎮 TECLA E PRESSIONADA - Iniciando transição");
            startArmorTransition();
        }
    }
    
    private void startArmorTransition() {
        isTransitioning = true;
        transitionTimer = 0f;
        armorSwapped = false;
        System.out.println("🚀 TRANSIÇÃO INICIADA - isTransitioning: " + isTransitioning);
    }
    
    private void updateTransition(float delta) {
        transitionTimer += delta;
        
        if (transitionTimer >= TOTAL_TRANSITION_DURATION) {
            isTransitioning = false;
            transitionTimer = 0f;
            armorSwapped = false;
            return;
        }
        
        // Fase 1: Fade in
        if (transitionTimer < FADE_IN_DURATION) {
        }
        // Fase 2: Preto total - troca armadura
        else if (transitionTimer < FADE_IN_DURATION + BLACK_DURATION) {
            if (!armorSwapped) {
                completeArmorTransition();
                armorSwapped = true;
            }
        }
        // Fase 3: Fade out
        else {
            float fadeOutTime = transitionTimer - (FADE_IN_DURATION + BLACK_DURATION);
        }
    }

    private void completeArmorTransition() {
        if (player.hasArmor) {
            player.hasArmor = false;
        } else {
            player.hasArmor = true;
        }       
        if (nearbyCabana != null) {
            nearbyCabana.setHasArmorStored(!player.hasArmor);
        }
    }

    public void renderUI(SpriteBatch batch, float offsetX, float offsetY) {
        if (isTransitioning) {
            renderTransitionEffect(batch);
        } else if (showInteractPrompt) {
            renderSimpleInteractPrompt(batch, offsetX, offsetY);
        }
    }
    
    private void renderSimpleInteractPrompt(SpriteBatch batch, float offsetX, float offsetY) {
        Vector2 screenPos = getPlayerScreenPositionWithOffset(offsetX, offsetY);
        float x = screenPos.x;
        float y = screenPos.y + 70f;
        
        float floatOffset = (float) Math.sin(System.currentTimeMillis() * 0.005f) * 3f;
        
        // Sombra
        font.setColor(0f, 0f, 0f, 0.7f);
        font.getData().setScale(1.3f);
        font.draw(batch, "E", x - 6f, y + floatOffset - 2f);
        
        // E principal com pulsação
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.01f) + 1f) / 2f;
        font.setColor(1f, 0.8f + pulse * 0.2f, 0.2f, 1f);
        font.getData().setScale(1.3f);
        font.draw(batch, "E", x - 8f, y + floatOffset);
        
        font.getData().setScale(0.8f);
    }

    private Vector2 getPlayerScreenPositionWithOffset(float offsetX, float offsetY) {
        float screenX = offsetX + (player.pos.x * 64);
        float screenY = offsetY + (player.pos.y * 64);
        return new Vector2(screenX, screenY);
    }


    
    private void createBlackPixelTexture() {
        try {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackPixelTexture = new Texture(pixmap);
            pixmap.dispose();
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar blackPixelTexture: " + e.getMessage());
        }
    }
    
    public float getTransitionAlpha() {
        if (transitionTimer < FADE_IN_DURATION) {
            return transitionTimer / FADE_IN_DURATION;
        } else if (transitionTimer < FADE_IN_DURATION + BLACK_DURATION) {
            return 1.0f;
        } else if (transitionTimer < TOTAL_TRANSITION_DURATION) {
            float fadeOutTime = transitionTimer - (FADE_IN_DURATION + BLACK_DURATION);
            return 1.0f - (fadeOutTime / FADE_OUT_DURATION);
        } else {
            return 0f;
        }
    }
    
    public Vector2 getPlayerScreenPosition(Mapa mapa) {
        return mapa.worldToScreen(player.pos.x, player.pos.y);
    }

        public void renderInteractPrompt(SpriteBatch batch, float offsetX, float offsetY) {
        if (showInteractPrompt && !isTransitioning) {
            renderSimpleInteractPrompt(batch, offsetX, offsetY);
        }
    }

    public void renderTransitionEffect(SpriteBatch batch) {
        if (isTransitioning) {
            renderTransition(batch);
        }
    }

    private void renderTransition(SpriteBatch batch) {
        float alpha = getTransitionAlpha();
        
        if (alpha <= 0f) return;
        
        batch.setColor(0, 0, 0, alpha);
        batch.draw(blackPixelTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1);
        
    }

        public boolean shouldShowInteractPrompt() {
        return showInteractPrompt && !isTransitioning;
    }
    
    public boolean isTransitioning() {
        return isTransitioning;
    }
    public void setPlayer(Robertinhoo player) {
    this.player = player;
    System.out.println("✅ Jogador atualizado no CabanaInteractionSystem: " + 
                      (player != null ? player.hashCode() : "null"));
}
}