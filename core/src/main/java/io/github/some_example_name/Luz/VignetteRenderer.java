package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class VignetteRenderer {
    public enum Shape {
        RECTANGLE, ELLIPSE, CORRIDOR
    }

    private Texture vignetteTexture;
    private float intensity; // 0 a 1
    private float falloffExponent; // >1 suaviza transição
    private Shape shape;

    // Para corredores (efeito direcional)
    private boolean horizontalCorridor = true;

    public VignetteRenderer(float intensity, float falloffExponent, Shape shape) {
        this.intensity = Math.max(0f, Math.min(1f, intensity));
        this.falloffExponent = Math.max(0.5f, falloffExponent);
        this.shape = shape;
        generateTexture();
    }

    public void setHorizontalCorridor(boolean horizontal) {
        this.horizontalCorridor = horizontal;
        generateTexture();
    }

    private void generateTexture() {
        int size = 512;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        float centerX = size / 2f;
        float centerY = size / 2f;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist;

                switch (shape) {
                    case ELLIPSE:
                        float nx = dx / centerX;
                        float ny = dy / centerY;
                        dist = (float) Math.sqrt(nx * nx + ny * ny);
                        break;
                    case CORRIDOR:
                        // Para corredores: fade mais forte nas laterais (ou topo/base)
                        float aspect = 2.0f;
                        if (horizontalCorridor) {
                            nx = dx / centerX;
                            ny = dy / (centerY / aspect);
                        } else {
                            nx = dx / (centerX / aspect);
                            ny = dy / centerY;
                        }
                        dist = (float) Math.sqrt(nx * nx + ny * ny);
                        break;
                    case RECTANGLE:
                    default:
                        float maxDim = Math.max(Math.abs(dx), Math.abs(dy));
                        dist = maxDim / centerX;
                        break;
                }

                dist = Math.min(1f, dist);
                float alpha = (float) Math.pow(dist, falloffExponent) * intensity;

                pixmap.setColor(0f, 0f, 0f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        if (vignetteTexture != null)
            vignetteTexture.dispose();
        vignetteTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void render(SpriteBatch batch, Rectangle bounds, float offsetX, float offsetY,
            int tileSize, int mapHeight) {
        if (vignetteTexture == null)
            return;

        float screenX = offsetX + bounds.x * tileSize;
        float screenY = offsetY + (mapHeight - 1 - (bounds.y + bounds.height - 1)) * tileSize;

        float widthPixels = bounds.width * tileSize;
        float heightPixels = bounds.height * tileSize;

        batch.draw(vignetteTexture, screenX, screenY, widthPixels, heightPixels);
    }

    // Versão para corredores (posições arbitrárias)
    public void renderCorridor(SpriteBatch batch, float screenX, float screenY,
            float width, float height) {
        if (vignetteTexture == null)
            return;
        batch.draw(vignetteTexture, screenX, screenY, width, height);
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0f, Math.min(1f, intensity));
        generateTexture();
    }

    public void setFalloffExponent(float exponent) {
        this.falloffExponent = Math.max(0.5f, exponent);
        generateTexture();
    }

    public void dispose() {
        if (vignetteTexture != null) {
            vignetteTexture.dispose();
            vignetteTexture = null;
        }
    }
}