package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
public class EsferaDeLuz {
    private Vector2 position;
    private float currentRadius;
    private float targetRadius;
    private float intensity;
    private boolean active;
    public Color color;

    // Para animação suave
    private float pulseTime;
    private float baseRadius;
    private float animationTime;
    private boolean isExpanding;
    private boolean isPulsating; // ✅ NOVO: controla se está apenas pulsando

    // Configurações de animação
    private static final float EXPAND_DURATION = 0.8f;
    private static final float CONTRACT_DURATION = 0.6f;
    private static final float MIN_RADIUS = 5f;

    public EsferaDeLuz(float x, float y, float radius, Color color) {
        this.position = new Vector2(x, y);
        this.baseRadius = radius;
        this.currentRadius = MIN_RADIUS;
        this.targetRadius = MIN_RADIUS;
        this.color = new Color(color);
        this.intensity = 0f;
        this.active = false;
        this.pulseTime = 0;
        this.animationTime = 0f;
        this.isExpanding = false;
        this.isPulsating = false; // ✅ Inicialmente não está pulsando
    }

    public void update(float delta) {
        // ✅ ANIMAÇÃO DE EXPANSÃO/CONTRAÇÃO (só quando não está apenas pulsando)
        if (!isPulsating && currentRadius != targetRadius) {
            animationTime += delta;

            float progress;
            float duration;

            if (isExpanding) {
                duration = EXPAND_DURATION;
                progress = Math.min(1f, animationTime / duration);
                progress = easeOutCubic(progress);
            } else {
                duration = CONTRACT_DURATION;
                progress = Math.min(1f, animationTime / duration);
                progress = easeInCubic(progress);
            }

            currentRadius = MIN_RADIUS + (targetRadius - MIN_RADIUS) * progress;

            // ✅ Quando termina a expansão, começa a pulsação
            if (progress >= 1f) {
                animationTime = 0f;
                if (isExpanding) {
                    isPulsating = true; // ✅ Agora só pulsa
                }
            }
        }

        // ✅ PULSAÇÃO SUAVE (apenas quando expandida e ativa)
        if (isPulsating && active) {
            pulseTime += delta;
            float pulse = (float) (Math.sin(pulseTime * 0.2f) * 0.05f + 1f);
            currentRadius = baseRadius * pulse; // ✅ Mantém o tamanho base com pulsação
        }

        // Partículas (opcional)
        if (active && isExpanding && currentRadius > baseRadius * 0.5f) {
            if (Math.random() < 0.1f) {
                createParticleAtEdge();
            }
        }
    }

    private void createParticleAtEdge() {
        // Implementação de partículas (mantida igual)
        float angle = (float) (Math.random() * Math.PI * 2);
        float particleX = position.x + (float) (currentRadius * Math.cos(angle));
        float particleY = position.y + (float) (currentRadius * Math.sin(angle));
    }

    // ✅ FUNÇÕES DE EASING (mantidas iguais)
    private float easeOutCubic(float t) {
        return (float) (1f - Math.pow(1f - t, 3f));
    }

    private float easeInCubic(float t) {
        return t * t * t;
    }

    // ✅ INICIAR EXPANSÃO
    public void startExpansion() {
        if (!isExpanding || targetRadius != baseRadius) {
            isExpanding = true;
            isPulsating = false; // ✅ Para de pulsar durante expansão
            targetRadius = baseRadius;
            animationTime = 0f;
            active = true;
        }
    }

    // ✅ INICIAR CONTRAÇÃO
    public void startContraction() {
        if (isExpanding || targetRadius != MIN_RADIUS) {
            isExpanding = false;
            isPulsating = false; // ✅ Para de pulsar durante contração
            targetRadius = MIN_RADIUS;
            animationTime = 0f;
        }
    }

    public void render(ShapeRenderer shapeRenderer, Matrix4 projectionMatrix) {
        if (!active || intensity <= 0 || currentRadius < MIN_RADIUS + 1f) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Color renderColor = new Color(color);
        float expansionProgress = (currentRadius - MIN_RADIUS) / (baseRadius - MIN_RADIUS);
        float alpha = intensity * 0.4f * expansionProgress;

        renderColor.a = alpha;
        shapeRenderer.setColor(renderColor);

        // Camada principal
        shapeRenderer.circle(position.x, position.y, currentRadius, 32);

        // Camada interna
        renderColor.a = alpha * 0.5f;
        shapeRenderer.setColor(renderColor);
        shapeRenderer.circle(position.x, position.y, currentRadius * 0.7f, 32);

        // Núcleo
        renderColor.a = alpha * 0.8f;
        shapeRenderer.setColor(renderColor);
        shapeRenderer.circle(position.x, position.y, currentRadius * 0.3f, 32);

        shapeRenderer.end();
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            if (active) {
                startExpansion();
            } else {
                startContraction();
            }
        }
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0f, Math.min(1f, intensity));

        // ✅ SE INTENSIDADE CHEGOU A ZERO, INICIAR CONTRAÇÃO
        if (intensity <= 0.01f && active) {
            startContraction();
        }

        // ✅ SE INTENSIDADE ESTÁ AUMENTANDO E NÃO ESTÁ EXPANDINDO, INICIAR EXPANSÃO
        if (intensity > 0.1f && !isExpanding && active && !isPulsating) {
            startExpansion();
        }
    }

    // ✅ NOVO MÉTODO: Forçar pulsação (quando o player está perto)
    public void startPulsation() {
        if (active && !isExpanding && !isPulsating) {
            isPulsating = true;
            currentRadius = baseRadius; // ✅ Volta ao tamanho base
        }
    }

    // ✅ NOVO MÉTODO: Parar pulsação (quando o player sai)
    public void stopPulsation() {
        if (isPulsating) {
            isPulsating = false;
            startContraction(); // ✅ Volta a contrair
        }
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return currentRadius;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFullyExpanded() {
        return isPulsating || (isExpanding && animationTime >= EXPAND_DURATION);
    }

    public boolean isFullyContracted() {
        return currentRadius <= MIN_RADIUS + 1f && !isExpanding;
    }

    public boolean isPulsating() {
        return isPulsating;
    }

    public boolean containsPoint(float x, float y) {
        return Vector2.dst(position.x, position.y, x, y) <= currentRadius;
    }

    public float getIntensity() {
        return intensity;
    }
}