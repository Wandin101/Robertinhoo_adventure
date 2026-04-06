package io.github.some_example_name.Fonts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import java.util.HashMap;
import java.util.Map;

public class FontsManager {
    private static FontsManager instance;
    private FreeTypeFontGenerator menuGenerator; // para fonte original corrigida
    private FreeTypeFontGenerator dialogueGenerator; // para Fredoka
    private Map<String, BitmapFont> fontsCache;

    // Caminhos das fontes
    private static final String MENU_FONT_PATH = "HUD/fonte_original_corrigida.ttf";
    private static final String DIALOGUE_FONT_PATH = "HUD/Fredoka-Bold.ttf";

    // Conjunto de caracteres completo (inclui vírgula, acentos, reticências)
    private static final String CHARS = FreeTypeFontGenerator.DEFAULT_CHARS +
            "áàâãéèêíìîóòôõúùûçÁÀÂÃÉÈÊÍÌÎÓÒÔÕÚÙÛÇ" +
            ".,!?;:()[]{}<>/\\|`~@#$%^&*_-+=€£¥§" +
            "…—\"'";

    private FontsManager() {
        menuGenerator = new FreeTypeFontGenerator(Gdx.files.internal(MENU_FONT_PATH));
        dialogueGenerator = new FreeTypeFontGenerator(Gdx.files.internal(DIALOGUE_FONT_PATH));
        fontsCache = new HashMap<>();
    }

    public static FontsManager getInstance() {
        if (instance == null)
            instance = new FontsManager();
        return instance;
    }

    // Método genérico para gerar fonte a partir de um gerador específico
    private BitmapFont generateFont(FreeTypeFontGenerator generator, int size, Color color,
            Color borderColor, float borderWidth) {
        if (size < 4)
            size = 12;
        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = size;
        params.color = color;
        params.characters = CHARS;
        if (borderColor != null && borderWidth > 0) {
            params.borderColor = borderColor;
            params.borderWidth = borderWidth;
        }
        return generator.generateFont(params);
    }

    // ========== FONTE DE MENU (fonte original corrigida) ==========
    public BitmapFont getMenuFont(int size, Color color, Color borderColor, float borderWidth) {
        String key = "menu|" + size + "|" + color.toString() + "|" +
                (borderColor != null ? borderColor.toString() : "null") + "|" + borderWidth;
        if (fontsCache.containsKey(key))
            return fontsCache.get(key);
        BitmapFont font = generateFont(menuGenerator, size, color, borderColor, borderWidth);
        fontsCache.put(key, font);
        return font;
    }

    public BitmapFont getMenuFont(int size, Color color) {
        return getMenuFont(size, color, null, 0);
    }

    public BitmapFont getDefaultMenuFont(int size) {
        return getDialogueFont(size, Color.WHITE, Color.BLACK, 4f); // bo
    }

    // ========== FONTE DE DIÁLOGO (Fredoka) ==========
    public BitmapFont getDialogueFont(int size, Color color, Color borderColor, float borderWidth) {
        String key = "dialogue|" + size + "|" + color.toString() + "|" +
                (borderColor != null ? borderColor.toString() : "null") + "|" + borderWidth;
        if (fontsCache.containsKey(key))
            return fontsCache.get(key);
        BitmapFont font = generateFont(dialogueGenerator, size, color, borderColor, borderWidth);
        fontsCache.put(key, font);
        return font;
    }

    public BitmapFont getDialogueFont(int size, Color color) {
        return getDialogueFont(size, color, null, 0);
    }

    public BitmapFont getDefaultDialogueFont(int size) {
        return getDialogueFont(size, Color.WHITE, Color.BLACK, 1.5f); // borda mais fina para diálogo
    }

    // ========== MÉTODOS LEGADOS (compatibilidade) ==========
    public static BitmapFont createInventoryFont() {
        return getInstance().getMenuFont(16, Color.WHITE);
    }

    public static BitmapFont createInventoryFont(float scaleFactor) {
        int finalSize = Math.round(16 * scaleFactor);
        return getInstance().getMenuFont(finalSize, Color.WHITE);
    }

    public static BitmapFont createInventoryFont(int size, Color color) {
        return getInstance().getMenuFont(size, color);
    }

    public void dispose() {
        for (BitmapFont font : fontsCache.values())
            font.dispose();
        fontsCache.clear();
        if (menuGenerator != null)
            menuGenerator.dispose();
        if (dialogueGenerator != null)
            dialogueGenerator.dispose();
        instance = null;
    }
}