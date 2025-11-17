package io.github.some_example_name.Entities.Debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import box2dLight.PointLight;
import io.github.some_example_name.MapConfig.Mapa;

public class DebugRenderers {
    private float debugTimer = 0f;
    private float stateTime = 0f;
    
    public void renderAllDebug(
            ShapeRenderer shapeRenderer, 
            float delta, 
            float offsetX, 
            float offsetY, 
            Robertinhoo player, 
            Mapa mapa, 
            PointLight testLight) {
        
        stateTime += delta;
        debugTimer += delta;
        
        // Debug das luzes no console a cada 2 segundos
        if (debugTimer > 2.0f) {
            debugTimer = 0f;
            debugAllLights(player, mapa, testLight, offsetX, offsetY);
        }
        
        // Debug visual das posições
        renderPositionDebug(shapeRenderer, offsetX, offsetY, player, mapa, testLight);
        
        // Debug dos inimigos
        debugRenderEnemies(shapeRenderer, offsetX, offsetY, mapa);
        
        // Debug da hitbox de melee
        debugMeleeHitbox(shapeRenderer, offsetX, offsetY, player, mapa);
    }
    
    private void renderPositionDebug(
            ShapeRenderer shapeRenderer, 
            float offsetX, 
            float offsetY, 
            Robertinhoo player, 
            Mapa mapa, 
            PointLight testLight) {
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Posição do jogador (VERDE)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(
            offsetX + player.pos.x * 64, 
            offsetY + player.pos.y * 64, 
            10f
        );
        
        // Posição da fogueira (LARANJA)
        if (mapa.getCampFire() != null) {
            Vector2 campfirePos = mapa.getCampFire().getPosition();
            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.circle(
                offsetX + campfirePos.x * 64, 
                offsetY + campfirePos.y * 64, 
                15f
            );
        }
        
        // Luz de teste (VERMELHO)
        if (testLight != null) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.circle(
                offsetX + testLight.getX() * 64, 
                offsetY + testLight.getY() * 64, 
                8f
            );
        }
        
        shapeRenderer.end();
    }
    
    private void debugRenderEnemies(ShapeRenderer shapeRenderer, float offsetX, float offsetY, Mapa mapa) {
        shapeRenderer.setProjectionMatrix(shapeRenderer.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Enemy e : mapa.getEnemies()) {
            if (e instanceof Ratinho) {
                ((Ratinho) e).debugDraw(shapeRenderer, offsetX, offsetY);
            }
        }

        shapeRenderer.end();
    }
    
    private void debugMeleeHitbox(
            ShapeRenderer shapeRenderer, 
            float offsetX, 
            float offsetY, 
            Robertinhoo player, 
            Mapa mapa) {
        
        Body hitbox = player.getMeleeAttackSystem().getMeleeHitboxBody();
        if (hitbox == null) return;

        shapeRenderer.setProjectionMatrix(shapeRenderer.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);

        for (Fixture f : hitbox.getFixtureList()) {
            if (f.getShape() instanceof PolygonShape) {
                PolygonShape poly = (PolygonShape) f.getShape();
                Vector2[] verts = new Vector2[poly.getVertexCount()];
                for (int i = 0; i < poly.getVertexCount(); i++) {
                    Vector2 local = new Vector2();
                    poly.getVertex(i, local);

                    float worldX = local.x * 64;
                    float worldY = local.y * 64;

                    Vector2 bodyPos = hitbox.getPosition();
                    float px = offsetX + bodyPos.x * 64 + worldX;
                    float py = offsetY + bodyPos.y * 64 + worldY;

                    verts[i] = new Vector2(px, py);
                }
                
                for (int i = 0; i < verts.length; i++) {
                    Vector2 a = verts[i];
                    Vector2 b = verts[(i + 1) % verts.length];
                    shapeRenderer.line(a, b);
                }
            }
        }

        shapeRenderer.end();
    }
    
    private void debugAllLights(Robertinhoo player, Mapa mapa, PointLight testLight, float offsetX, float offsetY) {
        System.out.println("=== 🔍 DEBUG COMPLETO DAS LUZES ===");
        
        Vector2 playerWorld = player.getBody().getPosition();
        System.out.println("👤 JOGADOR:");
        System.out.println("   Mundo Box2D: " + playerWorld);
        System.out.println("   Tela: " + (offsetX + playerWorld.x *64) + ", " + (offsetY + playerWorld.y *64));
        
        if (mapa.getCampFire() != null) {
            Vector2 campfireWorld = mapa.getCampFire().getPosition();
            System.out.println("🔥 FOGUEIRA:");
            System.out.println("   Mundo Box2D: " + campfireWorld);
            System.out.println("   Tela: " + (offsetX + campfireWorld.x *64) + ", " + (offsetY + campfireWorld.y *64));
        }
        
        if (testLight != null) {
            System.out.println("🔴 LUZ TESTE:");
            System.out.println("   Mundo Box2D: " + testLight.getPosition());
            System.out.println("   Tela: " + (offsetX + testLight.getX() *64) + ", " + (offsetY + testLight.getY() *64));
        }
        
        System.out.println("📐 Offsets: " + offsetX + ", " + offsetY);
        System.out.println("===================================");
    }
}