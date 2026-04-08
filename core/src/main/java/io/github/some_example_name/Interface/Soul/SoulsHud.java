package io.github.some_example_name.Interface.Soul;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Fonts.FontsManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;

public class SoulsHud {
    private Texture soulTexture;
    private TextureRegion soulIcon;
    private BitmapFont font;
    private Robertinhoo player;
    private float x, y;
    private float iconWidth, iconHeight;
    private float textOffsetX;

    public SoulsHud(Robertinhoo player) {
        this.player = player;
        soulTexture = new Texture("Almas/Alma.png");
        int frameWidth = soulTexture.getWidth() / 10;
        int frameHeight = soulTexture.getHeight();
        soulIcon = new TextureRegion(soulTexture, 0, 0, frameWidth, frameHeight);

        iconWidth = 64;
        iconHeight = 64;
        textOffsetX = 8;

        font = FontsManager.getInstance().getMenuFont(24, Color.WHITE);
        font.setColor(Color.valueOf("befff6"));

        recalculatePosition();
    }

    private void recalculatePosition() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        x = 10;
        y = screenHeight - iconHeight - 10;
    }

    public void update() {
        recalculatePosition();
    }

    public void draw(SpriteBatch batch) {
        if (player == null)
            return;
        int souls = player.getSoulManager().getSouls();

        batch.draw(soulIcon, x, y, iconWidth, iconHeight);
        float textX = x + iconWidth + textOffsetX;
        float textY = y + (iconHeight / 2) + (font.getCapHeight() / 2);
        font.draw(batch, String.valueOf(souls), textX, textY);
    }

    public void dispose() {
        if (soulTexture != null)
            soulTexture.dispose();
    }
}