package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public abstract class WeaponSight {
    public Color color = new Color(0.9f, 0.9f, 0.9f, 0.25f); // Branco semi-transparente
    public float lineWidth = 1.5f;

    // Método abstrato para renderizar a mira
    public abstract void render(ShapeRenderer shapeRenderer, Vector2 startPos, Vector2 direction,
            float maxLength, float collisionLength);

    // NOVO: Mira com linha até o ponto de colisão (como no sistema antigo)
    public static class LineSight extends WeaponSight {
        @Override
        public void render(ShapeRenderer shapeRenderer, Vector2 startPos, Vector2 direction,
                float maxLength, float collisionLength) {
            float renderLength = collisionLength > 0 ? collisionLength : maxLength;
            float endX = startPos.x + direction.x * renderLength;
            float endY = startPos.y + direction.y * renderLength;
            shapeRenderer.rectLine(startPos.x, startPos.y, endX, endY, lineWidth);
        }
    }

    // MANTIDO: Cone para shotgun
    public static class ConeSight extends WeaponSight {
        public float spreadAngle = 20f;
        public int coneSegments = 8;

        @Override
        public void render(ShapeRenderer shapeRenderer, Vector2 startPos, Vector2 direction,
                float maxLength, float collisionLength) {
            float renderLength = collisionLength > 0 ? collisionLength : maxLength;

            // Linha central (agora transparente)
            float centerEndX = startPos.x + direction.x * renderLength;
            float centerEndY = startPos.y + direction.y * renderLength;
            shapeRenderer.rectLine(startPos.x, startPos.y, centerEndX, centerEndY, lineWidth);

            // Cone de dispersão
            float halfSpread = spreadAngle / 2f;
            Vector2 leftDir = direction.cpy().rotateDeg(-halfSpread);
            Vector2 rightDir = direction.cpy().rotateDeg(halfSpread);

            // Linhas laterais do cone
            float leftEndX = startPos.x + leftDir.x * renderLength;
            float leftEndY = startPos.y + leftDir.y * renderLength;
            float rightEndX = startPos.x + rightDir.x * renderLength;
            float rightEndY = startPos.y + rightDir.y * renderLength;

            shapeRenderer.rectLine(startPos.x, startPos.y, leftEndX, leftEndY, lineWidth);
            shapeRenderer.rectLine(startPos.x, startPos.y, rightEndX, rightEndY, lineWidth);

            // Arco conectando as extremidades
            Vector2 prevPoint = new Vector2(leftEndX, leftEndY);
            float angleStep = spreadAngle / coneSegments;

            for (int i = 1; i <= coneSegments; i++) {
                float angle = -halfSpread + angleStep * i;
                Vector2 segmentDir = direction.cpy().rotateDeg(angle);
                float segmentX = startPos.x + segmentDir.x * renderLength;
                float segmentY = startPos.y + segmentDir.y * renderLength;

                shapeRenderer.rectLine(prevPoint.x, prevPoint.y, segmentX, segmentY, lineWidth);
                prevPoint.set(segmentX, segmentY);
            }
        }
    }

    // NOVO: Mira estilo Brawl Stars para pistola (ponto com linha)
    public static class BrawlPistolSight extends WeaponSight {
        public float endMarkerSize = 3f; // Tamanho do marcador no final

        @Override
        public void render(ShapeRenderer shapeRenderer, Vector2 startPos, Vector2 direction,
                float maxLength, float collisionLength) {
            float renderLength = collisionLength > 0 ? collisionLength : maxLength;

            // Linha até o ponto de colisão/alcance máximo
            float endX = startPos.x + direction.x * renderLength;
            float endY = startPos.y + direction.y * renderLength;

            // Desenha a linha (bem transparente)
            shapeRenderer.rectLine(startPos.x, startPos.y, endX, endY, lineWidth);

            // Desenha um pequeno ponto/retângulo no final (estilo Brawl Stars)
            shapeRenderer.rect(endX - endMarkerSize, endY - endMarkerSize,
                    endMarkerSize * 2, endMarkerSize * 2);
        }
    }

    // Mantenha os outros tipos se precisar
    public static class DotSight extends WeaponSight {
        public float dotSize = 4f;

        @Override
        public void render(ShapeRenderer shapeRenderer, Vector2 startPos, Vector2 direction,
                float maxLength, float collisionLength) {
            float endX = startPos.x + direction.x * (collisionLength > 0 ? collisionLength : maxLength);
            float endY = startPos.y + direction.y * (collisionLength > 0 ? collisionLength : maxLength);
            shapeRenderer.circle(endX, endY, dotSize);
        }
    }

    public static class CrosshairSight extends WeaponSight {
        public float crossSize = 6f;
        public float gapSize = 2f;

        @Override
        public void render(ShapeRenderer shapeRenderer, Vector2 startPos, Vector2 direction,
                float maxLength, float collisionLength) {
            float endX = startPos.x + direction.x * (collisionLength > 0 ? collisionLength : maxLength);
            float endY = startPos.y + direction.y * (collisionLength > 0 ? collisionLength : maxLength);

            shapeRenderer.rectLine(endX - crossSize, endY, endX - gapSize, endY, lineWidth);
            shapeRenderer.rectLine(endX + gapSize, endY, endX + crossSize, endY, lineWidth);
            shapeRenderer.rectLine(endX, endY - crossSize, endX, endY - gapSize, lineWidth);
            shapeRenderer.rectLine(endX, endY + gapSize, endX, endY + crossSize, lineWidth);
        }
    }
}