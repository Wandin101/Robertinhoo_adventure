package io.github.some_example_name.Fonts;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontsManager {

    public static BitmapFont createInventoryFont() {
        return new BitmapFont(); // SEMPRE UMA NOVA INSTÂNCIA
    }

    public static BitmapFont createInventoryFont(float scaleFactor) {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(scaleFactor);
        return font;
    }
}