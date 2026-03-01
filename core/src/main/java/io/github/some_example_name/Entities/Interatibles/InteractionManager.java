package io.github.some_example_name.Entities.Interatibles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class InteractionManager {

    private static InteractionManager instance;

    private Texture dialogueBoxTexture;
    private BitmapFont font;
    private GlyphLayout layout;

    private String currentText;
    private boolean isDialogueActive = false;

    private static final float BOX_SCALE = 8f;
    private static final int ORIG_LEFT_MARGIN = 19;
    private static final int ORIG_RIGHT_MARGIN = 19;
    private static final int ORIG_TOP_MARGIN = 12;
    private static final int ORIG_BOTTOM_MARGIN = 12;

    private float boxWidth, boxHeight;
    private float leftPad, rightPad, topPad, bottomPad;
    private float innerWidth, innerHeight;

    // Animação
    private float animProgress = 0f;
    private boolean isAnimating = false;
    private boolean closing = false;
    private static final float ANIM_SPEED = 3f; // velocidade da animação (por segundo)

    private InteractionManager() {
        dialogueBoxTexture = new Texture("rober/interface/caixaDeTexto.png");
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.getData().setLineHeight(font.getLineHeight() * 0.4f);
        layout = new GlyphLayout();
    }

    public static InteractionManager getInstance() {
        if (instance == null)
            instance = new InteractionManager();
        return instance;
    }

    public void startDialogue(String text) {
        currentText = text;
        isDialogueActive = true;
        closing = false;
        animProgress = 0f;
        isAnimating = true;

        boxWidth = dialogueBoxTexture.getWidth() * BOX_SCALE;
        boxHeight = dialogueBoxTexture.getHeight() * BOX_SCALE;

        leftPad = ORIG_LEFT_MARGIN * BOX_SCALE;
        rightPad = ORIG_RIGHT_MARGIN * BOX_SCALE;
        topPad = ORIG_TOP_MARGIN * BOX_SCALE;
        bottomPad = ORIG_BOTTOM_MARGIN * BOX_SCALE;

        innerWidth = boxWidth - leftPad - rightPad;
        innerHeight = boxHeight - topPad - bottomPad;

        layout.setText(font, currentText, font.getColor(), innerWidth, Align.center, true);
    }

    public void update(float delta) {
        if (!isDialogueActive)
            return;

        if (isAnimating) {
            if (!closing) {
                // Animação de entrada
                animProgress += delta * ANIM_SPEED;
                if (animProgress >= 1f) {
                    animProgress = 1f;
                    isAnimating = false;
                }
            } else {
                // Animação de saída
                animProgress -= delta * ANIM_SPEED;
                if (animProgress <= 0f) {
                    animProgress = 0f;
                    isAnimating = false;
                    isDialogueActive = false; // só desativa após a animação de saída
                }
            }
        }

        // Se não está animando e está ativo, pode fechar com espaço
        if (!isAnimating && isDialogueActive && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            closeDialogue();
        }
    }

    public void closeDialogue() {
        if (!isDialogueActive)
            return;
        if (closing)
            return; // já está fechando
        closing = true;
        isAnimating = true;
        // Não desativa imediatamente, a animação de saída vai fazer isso quando
        // terminar
    }

    public void render(SpriteBatch batch) {
        if (!isDialogueActive)
            return;

        float screenWidth = Gdx.graphics.getWidth();
        float targetY = 40; // posição final no bottom
        float startY = -boxHeight; // começa abaixo da tela

        // Interpola Y baseado no progresso da animação
        float y;
        if (!closing) {
            // Entrada: de startY até targetY
            y = startY + (targetY - startY) * animProgress;
        } else {
            // Saída: de targetY até startY
            y = targetY + (startY - targetY) * (1f - animProgress);
            // Quando animProgress chegar a 0, y será startY (fora da tela)
        }

        float x = (screenWidth - boxWidth) / 2f;

        batch.draw(dialogueBoxTexture, x, y, boxWidth, boxHeight);

        float innerX = x + leftPad;
        float innerY = y + bottomPad;

        // Centraliza verticalmente o texto
        float textY = innerY + (innerHeight + layout.height) / 2f;

        font.setColor(1, 1, 1, 1);
        font.draw(batch, layout, innerX, textY);
    }

    public boolean isDialogueActive() {
        return isDialogueActive;
    }

    public void dispose() {
        dialogueBoxTexture.dispose();
        font.dispose();
    }
}