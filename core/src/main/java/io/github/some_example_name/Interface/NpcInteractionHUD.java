package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;

import io.github.some_example_name.Interface.Npcs.EsmeraldaDialogue;
import io.github.some_example_name.Interface.Npcs.NpcDialogue;

public class NpcInteractionHUD {
    private static NpcInteractionHUD instance;

    private Texture moldeTexture;
    private Texture dialogueBoxTexture;
    private BitmapFont font;
    private GlyphLayout layout;

    private boolean active = false;
    private NpcDialogue currentNpcDialogue;
    private float stateTime = 0f;

    // --- Controle de animações ---
    private float animTime = 0f;
    private static final float FRAME_SLIDE_IN_DURATION = 0.4f; // duração da entrada da moldura
    private static final float BOX_FADE_IN_DURATION = 0.3f; // duração do fade da caixa
    private static final float CHAR_DELAY = 0.03f; // tempo por caractere (digitação)

    private String fullText = ""; // texto completo da fala atual
    private String displayedText = ""; // texto parcial exibido
    private float textTimer = 0f;
    private int charIndex = 0;
    private boolean textFinished = false; // se a digitação da fala atual terminou

    // Constantes de tamanho (mesmas do RobertinhoFaceHUD)
    private static final float FRAME_SIZE = 380f;
    private static final float MARGIN = 10f;
    private static final float REFERENCE_WIDTH = 1280f;

    private static final float DIALOGUE_BOX_WIDTH = 650f; // mantenha ou aumente
    private static final float DIALOGUE_BOX_HEIGHT = 180f; // mantenha ou aumente
    private static final float TEXT_MARGIN = 26f;
    private static final float BOOTOM_OFFSET = 20f;
    // Posições calculadas
    private float frameX, frameY, frameSize; // moldura
    private float faceX, faceY, faceSize; // área do rosto
    private float boxX, boxY, boxWidth, boxHeight; // caixa de diálogo
    private float textMargin;

    // Posições alvo (para animação)
    private float targetFrameX;
    private float startFrameX; // posição inicial fora da tela (esquerda)
    private float targetBoxAlpha = 1f;
    private float currentBoxAlpha = 0f;

    private NpcInteractionHUD() {
        try {
            moldeTexture = new Texture("npcs/Molde.png");
            dialogueBoxTexture = new Texture("npcs/Caixa de dialogo.png");
            font = new BitmapFont();
            font.getData().setScale(1.5f);
            layout = new GlyphLayout();
            System.out.println("✅ NpcInteractionHUD: texturas carregadas");
        } catch (Exception e) {
            System.err.println("❌ NpcInteractionHUD: erro ao carregar texturas: " + e.getMessage());
            createPlaceholderTextures();
        }
        // Pré-calcula posições alvo
        recalcularPosicoes();
        targetFrameX = frameX;
        startFrameX = -frameSize - 50; // fora da tela à esquerda
        frameX = startFrameX; // começa escondido
    }

    private void createPlaceholderTextures() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(380, 380,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1);
        pixmap.fill();
        moldeTexture = new Texture(pixmap);
        pixmap.dispose();

        pixmap = new com.badlogic.gdx.graphics.Pixmap(124, 59,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 1, 1);
        pixmap.fill();
        dialogueBoxTexture = new Texture(pixmap);
        pixmap.dispose();

        font = new BitmapFont();
        layout = new GlyphLayout();
        System.out.println("⚠️ NpcInteractionHUD: usando placeholders");
    }

    public static NpcInteractionHUD getInstance() {
        if (instance == null) {
            instance = new NpcInteractionHUD();
        }
        return instance;
    }

    public void startDialogue(NpcDialogue npcDialogue) {
        this.currentNpcDialogue = npcDialogue;
        this.active = true;
        this.stateTime = 0f;
        this.animTime = 0f;
        this.frameX = startFrameX; // reinicia posição da moldura
        this.currentBoxAlpha = 0f;
        loadCurrentText();
        System.out.println("🟢 NpcInteractionHUD: diálogo iniciado com " + npcDialogue.getClass().getSimpleName());
    }

    private void loadCurrentText() {
        if (currentNpcDialogue != null) {
            fullText = currentNpcDialogue.getCurrentText();
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;
            textFinished = false;
        }
    }

    public void hide() {
        active = false;
        if (currentNpcDialogue != null) {
            currentNpcDialogue.dispose();
            currentNpcDialogue = null;
        }
        System.out.println("🔴 NpcInteractionHUD: hide()");
    }

    public void toggle() {
        if (active) {
            hide();
        } else {
            active = true;
            recalcularPosicoes();
            frameX = startFrameX;
            currentBoxAlpha = 0f;
        }
    }

    public void recalcularPosicoes() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float scale = Math.min(1.0f, screenWidth / REFERENCE_WIDTH);

        // Ajusta fonte
        float fontScale = 1.5f * scale;
        font.getData().setScale(fontScale);

        frameSize = FRAME_SIZE * scale;
        frameX = MARGIN * scale;
        frameY = MARGIN * scale;

        float faceMargin = 10 * scale;
        faceX = frameX + faceMargin;
        faceY = frameY + faceMargin;
        faceSize = frameSize - 2 * faceMargin;

        boxWidth = DIALOGUE_BOX_WIDTH * scale;
        boxHeight = DIALOGUE_BOX_HEIGHT * scale;
        boxX = (screenWidth - boxWidth) / 2f;
        float baseY = MARGIN * scale;
        boxY = baseY + BOOTOM_OFFSET * scale;

        textMargin = TEXT_MARGIN * scale;

        targetFrameX = frameX;
        startFrameX = -frameSize - 50 * scale;
    }

    public void update(float delta) {
        if (!active)
            return;

        // Atualiza tempo da animação e do fade-in (sempre)
        stateTime += delta;
        animTime += delta;

        // Animação de entrada da moldura
        if (frameX < targetFrameX) {
            float progress = Math.min(1f, animTime / FRAME_SLIDE_IN_DURATION);
            frameX = MathUtils.lerp(startFrameX, targetFrameX, progress);
        }

        // Fade-in da caixa
        if (animTime > FRAME_SLIDE_IN_DURATION * 0.5f) {
            float boxProgress = Math.min(1f, (animTime - FRAME_SLIDE_IN_DURATION * 0.5f) / BOX_FADE_IN_DURATION);
            currentBoxAlpha = Math.min(1f, boxProgress);
        }

        // Efeito de digitação (sempre)
        if (currentNpcDialogue != null && !textFinished) {
            textTimer += delta;
            int targetChars = (int) (textTimer / CHAR_DELAY);
            if (targetChars > charIndex) {
                charIndex = Math.min(targetChars, fullText.length());
                displayedText = fullText.substring(0, charIndex);
                if (charIndex >= fullText.length()) {
                    textFinished = true;
                    currentNpcDialogue.setTalking(false);
                }
            }
        }

        boolean shopVisible = (currentNpcDialogue instanceof EsmeraldaDialogue) &&
                ((EsmeraldaDialogue) currentNpcDialogue).isShopVisible();

        if (!shopVisible) {
            // Lógica específica para Esmeralda (menu de opções)
            if (currentNpcDialogue instanceof EsmeraldaDialogue) {
                EsmeraldaDialogue ed = (EsmeraldaDialogue) currentNpcDialogue;
                if (ed.isWaitingForChoice()) {
                    // Navegação por setas
                    if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                        ed.navigateUp();
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                        ed.navigateDown();
                    }
                    // Confirmação com Enter ou Espaço
                    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                        ed.chooseOption(ed.getSelectedOption());
                        loadCurrentText();
                        currentNpcDialogue.setTalking(true);
                        textFinished = false;
                    }
                    // Também aceita números
                    else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                        ed.chooseOption(0);
                        loadCurrentText();
                        currentNpcDialogue.setTalking(true);
                        textFinished = false;
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                        ed.chooseOption(1);
                        loadCurrentText();
                        currentNpcDialogue.setTalking(true);
                        textFinished = false;
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                        ed.chooseOption(2);
                        loadCurrentText();
                        currentNpcDialogue.setTalking(true);
                        textFinished = false;
                    }
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                boolean isInMenu = (currentNpcDialogue instanceof EsmeraldaDialogue) &&
                        ((EsmeraldaDialogue) currentNpcDialogue).isWaitingForChoice();
                if (!isInMenu) {
                    if (currentNpcDialogue != null) {
                        if (!textFinished) {
                            displayedText = fullText;
                            charIndex = fullText.length();
                            textFinished = true;
                            currentNpcDialogue.setTalking(false);
                        } else {
                            if (!currentNpcDialogue.next()) {
                                hide();
                            } else {
                                loadCurrentText();
                                currentNpcDialogue.setTalking(true);
                                currentBoxAlpha = 1f;
                            }
                        }
                    } else {
                        hide();
                    }
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!active)
            return;
        if (moldeTexture == null)
            return;

        Matrix4 originalMatrix = batch.getProjectionMatrix().cpy();
        Matrix4 hudMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudMatrix);

        // Desenha a moldura na posição atual (animada)
        batch.draw(moldeTexture, frameX, frameY, frameSize, frameSize);

        if (currentNpcDialogue != null) {
            // Desenha a animação facial
            Animation<TextureRegion> anim = currentNpcDialogue.getFaceAnimation();
            if (anim != null) {
                TextureRegion currentFrame = anim.getKeyFrame(stateTime, true);
                batch.draw(currentFrame, faceX, faceY, faceSize, faceSize);
            }

            // Desenha a caixa de diálogo com alpha
            if (dialogueBoxTexture != null && currentBoxAlpha > 0) {
                batch.setColor(1, 1, 1, currentBoxAlpha);
                batch.draw(dialogueBoxTexture, boxX, boxY, boxWidth, boxHeight);
                batch.setColor(1, 1, 1, 1);

                // Desenha o texto (parcial ou completo)
                if (displayedText != null && !displayedText.isEmpty()) {
                    float maxWidth = boxWidth - 2 * textMargin;
                    layout.setText(font, displayedText, font.getColor(), maxWidth, Align.left, true);
                    float textX = boxX + textMargin;
                    float textY = boxY + boxHeight - textMargin - layout.height;
                    font.draw(batch, layout, textX, textY);
                }
            }
        }

        if (currentNpcDialogue instanceof EsmeraldaDialogue) {
            EsmeraldaDialogue ed = (EsmeraldaDialogue) currentNpcDialogue;
            if (ed.isWaitingForChoice()) {
                String[] options = ed.getMenuOptions();
                float maxWidth = boxWidth - 2 * textMargin;
                float baseY = boxY + boxHeight - textMargin - layout.height - 20;
                for (int i = 0; i < options.length; i++) {
                    String prefix = (i == ed.getSelectedOption()) ? "> " : "  ";
                    String opt = prefix + (i + 1) + ": " + options[i];
                    layout.setText(font, opt, font.getColor(), maxWidth, Align.left, true);
                    float textY = baseY - i * (layout.height + 5);

                    font.draw(batch, layout, boxX + textMargin, textY);
                }
                font.setColor(1, 1, 1, 1);
            }
        }

        batch.setProjectionMatrix(originalMatrix);
    }

    public boolean isActive() {
        return active;
    }

    public NpcDialogue getCurrentNpcDialogue() {
        return currentNpcDialogue;
    }

    public void reloadCurrentText() {
        if (currentNpcDialogue != null) {
            fullText = currentNpcDialogue.getCurrentText();
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;
            textFinished = false;
        }
    }

    public void dispose() {
        if (moldeTexture != null)
            moldeTexture.dispose();
        if (dialogueBoxTexture != null)
            dialogueBoxTexture.dispose();
        if (font != null)
            font.dispose();
        if (currentNpcDialogue != null)
            currentNpcDialogue.dispose();
    }
}