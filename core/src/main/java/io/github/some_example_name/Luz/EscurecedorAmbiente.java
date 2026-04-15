
package io.github.some_example_name.Luz;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.GL20;

public class EscurecedorAmbiente {
    private ShapeRenderer shapeRenderer;
    private float intensidadeEscuro;
    public List<EsferaDeLuz> lightSpheres;

    public EscurecedorAmbiente() {
        shapeRenderer = new ShapeRenderer();
        intensidadeEscuro = 0.3f;
        lightSpheres = new ArrayList<>();
    }

    public void adicionarLightSphere(EsferaDeLuz sphere) {
        lightSpheres.add(sphere);
    }

    public void removerLightSphere(EsferaDeLuz sphere) {
        lightSpheres.remove(sphere);
    }

    public void aplicarEscurecimentoSuave(Matrix4 cameraMatrix) {
        // ✅ DEBUG: Contar luzes ativas
        int activeLights = 0;
        for (EsferaDeLuz sphere : lightSpheres) {
            if (sphere.isActive() && sphere.getIntensity() > 0) {
                activeLights++;
            }
        }

        // ✅ FASE 1: SE NÃO HÁ LUZES, APENAS ESCURIDÃO UNIFORME EM TELA CHEIA
        if (activeLights == 0) {
            // ✅ USAR MATRIZ DE TELA CHEIA
            Matrix4 screenMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.setProjectionMatrix(screenMatrix);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, intensidadeEscuro);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
            return;
        }

        // ✅ FASE 2: USAR STENCIL PARA ÁREAS DE LUZ (usando matriz da câmera)
        Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);

        // Configurar stencil para marcar áreas de luz
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
        Gdx.gl.glStencilMask(0xFF);
        Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);

        // ✅ MARCAR ÁREAS DE LUZ NO STENCIL (usando matriz da câmera)
        Gdx.gl.glColorMask(false, false, false, false);
        shapeRenderer.setProjectionMatrix(cameraMatrix); // Matriz da câmera para as luzes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 1);

        for (EsferaDeLuz sphere : lightSpheres) {
            if (sphere.isActive() && sphere.getIntensity() > 0) {
                float radius = sphere.getRadius();

                // Círculo principal
                shapeRenderer.circle(sphere.getPosition().x, sphere.getPosition().y, radius, 32);

                // Círculos menores para suavizar bordas
                shapeRenderer.circle(sphere.getPosition().x, sphere.getPosition().y, radius * 0.8f, 32);
                shapeRenderer.circle(sphere.getPosition().x, sphere.getPosition().y, radius * 0.6f, 32);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glColorMask(true, true, true, true);

        // ✅ FASE 3: DESENHAR ESCURIDÃO PRINCIPAL (FORA DAS LUZES) - TELA CHEIA
        Matrix4 screenMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.setProjectionMatrix(screenMatrix); // ✅ MUDANÇA CRÍTICA: Matriz de tela cheia

        Gdx.gl.glStencilFunc(GL20.GL_NOTEQUAL, 1, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);
        Gdx.gl.glStencilMask(0x00);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, intensidadeEscuro);

        // ✅ DEBUG: Desenhar borda verde para ver a área real
        // shapeRenderer.setColor(0f, 1f, 0f, 0.3f); // Verde para debug
        // shapeRenderer.rect(10, 10, Gdx.graphics.getWidth() - 20,
        // Gdx.graphics.getHeight() - 20);

        shapeRenderer.setColor(0, 0, 0, intensidadeEscuro);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // ✅ FASE 4: DESENHAR ESCURIDÃO SUAVE DENTRO DAS LUZES - TELA CHEIA
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, intensidadeEscuro * 0.3f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // ✅ FASE 5: LIMPAR STENCIL
        Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
        Gdx.gl.glStencilMask(0xFF);
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 0, 0xFF);

    }

    public void setIntensidade(float intensidade) {
        this.intensidadeEscuro = Math.max(0f, Math.min(1f, intensidade));
    }

    public float getIntensidade() {
        return intensidadeEscuro;
    }

    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}