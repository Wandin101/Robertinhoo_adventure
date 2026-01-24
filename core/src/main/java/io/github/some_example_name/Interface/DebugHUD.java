package io.github.some_example_name.Interface;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

public class DebugHUD implements Disposable {
    private BitmapFont font;
    private SpriteBatch batch;
    private PerformanceCounter performanceCounter;
    private long frameCount = 0;
    private long startTime = TimeUtils.nanoTime();
    private boolean enabled = true;

    // Para medição de FPS médio
    private long lastFpsUpdate = 0;
    private int fpsCounter = 0;
    private float averageFps = 0;

    // Estatísticas de memória
    private Runtime runtime = Runtime.getRuntime();

    // ADICIONE ESTES CAMPOS:
    private float updateTime = 0;
    private float renderTime = 0;

    public DebugHUD() {
        font = new BitmapFont();
        font.setColor(Color.YELLOW);
        batch = new SpriteBatch();

        performanceCounter = new PerformanceCounter("Game");
    }

    public void update(float delta) {
        if (!enabled)
            return;

        // Usar PerformanceCounter corretamente
        performanceCounter.start();
        // Seu código de update vai aqui (se tiver algo específico para medir)
        performanceCounter.stop();

        frameCount++;
        fpsCounter++;

        // Calcular FPS médio a cada segundo
        long currentTime = TimeUtils.nanoTime();
        if (currentTime - lastFpsUpdate > 1000000000) { // 1 segundo em nanosegundos
            averageFps = fpsCounter;
            fpsCounter = 0;
            lastFpsUpdate = currentTime;
        }
    }

    // ADICIONE ESTES MÉTODOS:
    public void setUpdateTime(float time) {
        this.updateTime = time;
    }

    public void setRenderTime(float time) {
        this.renderTime = time;
    }

    public void render() {
        if (!enabled)
            return;

        batch.begin();

        float y = Gdx.graphics.getHeight() - 20;
        float x = 10;

        // FPS atual
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), x, y);
        y -= 20;

        // FPS médio
        font.draw(batch, String.format("Avg FPS: %.1f", averageFps), x, y);
        y -= 20;

        // Uso de memória
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        font.draw(batch, "Memory: " + usedMemory + " / " + maxMemory + " MB", x, y);
        y -= 20;

        // Java Heap
        font.draw(batch, "Java Heap: " + (runtime.totalMemory() / (1024 * 1024)) + " MB", x, y);
        y -= 20;

        // Performance counter - tempo de update
        font.draw(batch, "Update Time: " + String.format("%.3f", performanceCounter.time.value) + " ms", x, y);
        y -= 20;

        // ADICIONE ESTAS LINHAS PARA MOSTRAR OS TEMPOS MEDIDOS:
        font.draw(batch, "Measured Update: " + String.format("%.3f", updateTime) + " ms", x, y);
        y -= 20;
        font.draw(batch, "Measured Render: " + String.format("%.3f", renderTime) + " ms", x, y);
        y -= 20;

        // Draw calls
        font.draw(batch, "Draw Calls: " + batch.renderCalls, x, y);
        y -= 20;

        // Delta time
        font.draw(batch, String.format("Delta: %.3f ms", Gdx.graphics.getDeltaTime() * 1000), x, y);
        y -= 20;

        // Resolução
        font.draw(batch, "Resolution: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight(), x, y);

        batch.end();
    }

    // Método para medir seções específicas do código
    public void startMeasurement(String section) {
        if (!enabled)
            return;
        performanceCounter.start();
    }

    public void endMeasurement(String section) {
        if (!enabled)
            return;
        performanceCounter.stop();
    }

    public void toggle() {
        enabled = !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BitmapFont getFont(){
        return font;
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}