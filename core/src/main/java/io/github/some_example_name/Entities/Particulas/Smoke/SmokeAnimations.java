package io.github.some_example_name.Entities.Particulas.Smoke;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

public class SmokeAnimations implements Disposable {
    private static final String SMOKE_SHEET_PATH = "ParticulasFumaça/FumacaTIRO.png";
    private static final int FRAME_COUNT = 12;
    private static final float FRAME_DURATION = 0.05f;

    private Texture sheet;
    private TextureRegion[] frames;
    private Animation<TextureRegion> animation;

    public SmokeAnimations() {
        load();
    }

    private void load() {
        try {
            sheet = new Texture(Gdx.files.internal(SMOKE_SHEET_PATH));
            int frameWidth = sheet.getWidth() / FRAME_COUNT;
            int frameHeight = sheet.getHeight();
            frames = new TextureRegion[FRAME_COUNT];
            for (int i = 0; i < FRAME_COUNT; i++) {
                frames[i] = new TextureRegion(sheet, i * frameWidth, 0, frameWidth, frameHeight);
            }
            animation = new Animation<>(FRAME_DURATION, frames);
            animation.setPlayMode(Animation.PlayMode.NORMAL);
            System.out.println("✅ [SmokeAnimations] Carregado com sucesso");
        } catch (Exception e) {
            System.err.println("❌ [SmokeAnimations] Erro ao carregar: " + e.getMessage());
        }
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public TextureRegion[] getFrames() {
        return frames;
    }

    @Override
    public void dispose() {
        if (sheet != null)
            sheet.dispose();
    }
}