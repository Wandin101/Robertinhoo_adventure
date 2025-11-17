package io.github.some_example_name.MapConfig;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import box2dLight.RayHandler;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

import io.github.some_example_name.Entities.Debug.DebugRenderers;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.TileRenderer;
import io.github.some_example_name.Entities.Renderer.AmmoRenderer.AmmoRenderer;
import io.github.some_example_name.Entities.Renderer.CorpsesManager.CorpseManager;
import io.github.some_example_name.Entities.Renderer.CraftItensRenderer.CraftItensRenderer;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Rat.RatRenderer;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor.CastorRenderer;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.DestructibleRenderer;
import io.github.some_example_name.Entities.Renderer.Projectile.ProjectileRenderer;
import io.github.some_example_name.Entities.Renderer.RenderInventory.RenderInventory;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowRenderer;
import io.github.some_example_name.Luz.EscurecedorAmbiente;
import io.github.some_example_name.Luz.SistemaLuz;
import io.github.some_example_name.MapConfig.Rooms.Room0TileRenderer;
import io.github.some_example_name.Screens.ScreenEffects.ScreenFreezeSystem;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Camera.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.some_example_name.Entities.Enemies.StateEnemy.StateEnemy;

public class MapRenderer {
    private RayHandler rayHandler;

    private Mapa mapa;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private ProjectileRenderer projectileRenderer;
    private PlayerRenderer playerRenderer;
    private RatRenderer ratRenderer;
    private CastorRenderer castorRenderer;
    private Camera cameraController;
    private AmmoRenderer ammoRenderer;
    public RenderInventory renderInventory;
    private ShadowRenderer shadowRenderer;
    private CraftItensRenderer craftItensRenderer;
    private CorpseManager corpseManager;
    private StateEnemy stateEnemy;
    private Room0TileRenderer room0TileRenderer;
    private boolean isRoom0 = false;
    private SistemaLuz sistemaLuz;
    private EscurecedorAmbiente escurecedor;

    // ADICIONADO: DebugRenderer
    private DebugRenderers debugRenderers;

    private DestructibleRenderer destructibleRenderer;
    public static final int TILE_SIZE = 64;
    public float offsetX;
    public float offsetY;

    private Stage uiStage;
    private Skin uiSkin;
    private TileRenderer tileRenderer;

    public MapRenderer(Mapa mapa) {
        this.mapa = mapa;

        this.rayHandler = mapa.getRayHandler();

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        spriteBatch = new SpriteBatch();
        cameraController = new Camera();

        // ADICIONADO: Inicialização do DebugRenderer
        this.debugRenderers = new DebugRenderers();

        this.tileRenderer = new TileRenderer(mapa, TILE_SIZE);
        this.projectileRenderer = new ProjectileRenderer(mapa, TILE_SIZE);
        this.playerRenderer = new PlayerRenderer(mapa.robertinhoo.getWeaponSystem());
        this.ammoRenderer = new AmmoRenderer(TILE_SIZE);

        castorRenderer = new CastorRenderer();
        ratRenderer = new RatRenderer();

        this.renderInventory = new RenderInventory(
                mapa.robertinhoo.getInventory(),
                64,
                new Vector2(175, 100), mapa.robertinhoo.getInventoryController());
        mapa.robertinhoo.getInventoryController().setContextMenu(renderInventory.getContextMenu());

        mapa.robertinhoo.setCamera(cameraController.getCamera());
        this.destructibleRenderer = new DestructibleRenderer(TILE_SIZE);
        this.shadowRenderer = new ShadowRenderer(shapeRenderer);
        this.craftItensRenderer = new CraftItensRenderer(TILE_SIZE);
        this.corpseManager = new CorpseManager();
        this.isRoom0 = mapa.getCampFire() != null;

        this.isRoom0 = detectIfRoom0(mapa);

        if (isRoom0) {
            this.room0TileRenderer = new Room0TileRenderer(mapa, TILE_SIZE);
            System.out.println("✅ Room0TileRenderer inicializado para Sala 0");
        }
        this.sistemaLuz = new SistemaLuz();
        this.escurecedor = new EscurecedorAmbiente();

    }

    public void render(float delta, Robertinhoo player) {
        // ✅ FUNDO MAIS ESCURO
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calcula offsets e atualiza câmera
        calculateOffsets();
        cameraController.centerOnPlayer(player, offsetX, offsetY);

        // Configura matrizes de projeção
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);

        // Atualiza física do mundo
        mapa.world.step(delta, 6, 2);

        // --- COLETA DE ENTIDADES PARA SOMBRA ---
        List<ShadowEntity> shadowEntities = new ArrayList<>();
        shadowEntities.add(player); // Jogador

        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof ShadowEntity) {
                shadowEntities.add((ShadowEntity) enemy);
            }
        }

        for (Destructible d : mapa.getDestructibles()) {
            if (d instanceof ShadowEntity) {
                shadowEntities.add((ShadowEntity) d);
            }
        }

        // 1. RENDERIZAÇÃO DO CHÃO (TILES) - MAIS ESCURO
        spriteBatch.begin();
        // ✅ APLICA COR ESCURA AOS TILES
        spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1f);

        if (isRoom0 && room0TileRenderer != null) {
            room0TileRenderer.renderFloor(spriteBatch, offsetX, offsetY);
        } else {
            tileRenderer.render(spriteBatch, offsetX, offsetY, delta);
        }

        // ✅ RESTAURA COR NORMAL
        spriteBatch.setColor(1f, 1f, 1f, 1f);
        spriteBatch.end();

        spriteBatch.begin();
        if (isRoom0 && mapa.getCampFire() != null) {
            Vector2 campfireTilePos = mapa.getCampFire().getPosition();

            // ✅ POSIÇÃO CORRETA: Usar a mesma lógica de conversão
            float screenX = offsetX + campfireTilePos.x * TILE_SIZE;
            float screenY = offsetY + campfireTilePos.y * TILE_SIZE;

            mapa.getCampFire().update(delta);
            mapa.getCampFire().render(spriteBatch, screenX, screenY);
        }
        spriteBatch.end();

        // 3. RENDERIZAÇÃO DAS SOMBRAS
        shadowRenderer.renderShadows(shadowEntities, offsetX, offsetY, TILE_SIZE);

        // 4. RENDERIZAÇÃO DOS OBJETOS E ENTIDADES
        spriteBatch.begin();
        {
            destructibleRenderer.render(spriteBatch, mapa.getDestructibles(), offsetX, offsetY);
            projectileRenderer.render(spriteBatch, delta, offsetX, offsetY);

            float playerX = offsetX + (player.bounds.x * TILE_SIZE) - (playerRenderer.getRenderScale() - 1) * 8;
            float playerY = offsetY + (player.bounds.y * TILE_SIZE) - (playerRenderer.getRenderScale() - 1) * 8;
            corpseManager.render(spriteBatch, offsetX, offsetY);

            playerRenderer.render(spriteBatch, player, delta, offsetX, offsetY);
            player.getWeaponSystem().renderWeapon(spriteBatch, delta, player, playerX, playerY);

            // Renderiza inimigos
            for (Enemy enemy : mapa.getEnemies()) {
                if (enemy instanceof Ratinho) {
                    Ratinho rat = (Ratinho) enemy;

                    if (rat.isDead()) {
                        if (rat.isDeathAnimationFinished()) {
                            if (!rat.isMarkedForDestruction()) {
                                corpseManager.addCorpse(rat, ratRenderer);
                                rat.markForDestruction();
                            }
                        } else {
                            ratRenderer.render(spriteBatch, delta, rat, offsetX, offsetY);
                        }
                    } else {
                        ratRenderer.render(spriteBatch, delta, rat, offsetX, offsetY);
                    }
                }

                if (enemy instanceof Castor) {
                    Castor castor = (Castor) enemy;

                    if (!ScreenFreezeSystem.isFrozen()) {
                        castor.update(delta);
                    }
                    if (castor.isDead()) {
                        if (castor.isDeathAnimationFinished()) {
                            if (!castor.isMarkedForDestruction()) {
                                corpseManager.addCorpse(castor, castorRenderer);
                                castor.markForDestruction();
                            }
                        } else {
                            castorRenderer.render(spriteBatch, castor, offsetX, offsetY, delta);
                        }
                    } else {
                        castorRenderer.render(spriteBatch, castor, offsetX, offsetY, delta);
                        Vector2 worldPos = castor.getBody().getPosition();
                        float screenX = offsetX + worldPos.x * TILE_SIZE;
                        float screenY = offsetY + worldPos.y * TILE_SIZE;
                        castor.ai.getStateEnemy().updatePosition(new Vector2(screenX, screenY));
                        castor.ai.getStateEnemy().render(spriteBatch);
                    }
                }
            }

            // Renderiza armas no chão
            for (Weapon weapon : mapa.getWeapons()) {
                weapon.update(delta);
                TextureRegion frame = weapon.getCurrentFrame(delta);
                float floatY = weapon.getPosition().y * TILE_SIZE + weapon.getFloatOffset();

                spriteBatch.draw(
                        frame,
                        offsetX + weapon.getPosition().x * TILE_SIZE,
                        offsetY + floatY,
                        40, 25);
            }

            ammoRenderer.render(spriteBatch, mapa.getAmmo(), offsetX, offsetY);
            craftItensRenderer.render(spriteBatch, mapa.getCraftItems(), offsetX, offsetY);
        }
        spriteBatch.end(); // ✅ FECHA o spriteBatch principal PRIMEIRO

        sistemaLuz.setProjectionMatrix(cameraController.getCamera().combined);
        sistemaLuz.begin();

        // Luz do jogador (suave)
        Vector2 playerWorldPos = player.getBody().getPosition();
        float playerScreenX = offsetX + playerWorldPos.x * TILE_SIZE;
        float playerScreenY = offsetY + playerWorldPos.y * TILE_SIZE;

        sistemaLuz.renderLight(playerScreenX, playerScreenY, 90f,
                new Color(0.7f, 0.8f, 1.0f, 0.3f));

        if (isRoom0 && mapa.getCampFire() != null) {
            Vector2 campfireTilePos = mapa.getCampFire().getPosition();

            // ✅ USAR A MESMA POSIÇÃO DO RENDER - centralizada
            float screenX = offsetX + campfireTilePos.x * TILE_SIZE;
            float screenY = offsetY + campfireTilePos.y * TILE_SIZE;

            // Ajuste para a luz ficar centralizada com o sprite
            float lightX = screenX + TILE_SIZE / 2f; // Centro do tile
            float lightY = screenY + TILE_SIZE / 2f;

            sistemaLuz.renderFogueira(lightX, lightY, 350f, delta);
        }

        sistemaLuz.end();
        // --- CONTINUA COM O RESTO DO RENDER ---
        // --- RENDERIZAÇÃO DE FORMAS (MIRA E DEBUG) ---
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);

        // Renderiza mira apenas se jogador estiver com arma equipada
        if (player.getInventory().getEquippedWeapon() != null) {
            player.getWeaponSystem().renderMiraArma(shapeRenderer);
        }

        // SUBSTITUÍDO: Todo o debug agora é feito pelo DebugRenderer
        // debugRenderers.renderAllDebug(shapeRenderer, delta, offsetX, offsetY, player,
        // mapa, null);

        // --- RENDERIZAÇÃO DO RAYHANDLER (apenas para preenchimento) ---
        if (rayHandler != null) {
            rayHandler.setCombinedMatrix(
                    cameraController.getCamera().combined,
                    cameraController.getCamera().position.x,
                    cameraController.getCamera().position.y,
                    cameraController.getCamera().viewportWidth,
                    cameraController.getCamera().viewportHeight);
            rayHandler.updateAndRender();
        }

        // --- RENDERIZAÇÃO DA INTERFACE ---
        if (player.getInventoryController().GetIsOpen()) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
            renderInventory.render(
                    null,
                    0, 0,
                    false,
                    player.getInventoryController().getSelectedItem(),
                    player.getInventoryController().getOriginalGridX(),
                    player.getInventoryController().getOriginalGridY(),
                    player.getInventoryController().getCursorGridX(),
                    player.getInventoryController().getCursorGridY(),
                    player.getInventoryController().getAvailableRecipes(),
                    player.getInventoryController().getSelectedRecipe());
        }

        if (player.getInventoryController().isInPlacementMode()) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
            renderInventory.render(
                    player.getInventoryController().getCurrentPlacementItem(),
                    player.getInventoryController().getPlacementGridX(),
                    player.getInventoryController().getPlacementGridY(),
                    player.getInventoryController().isValidPlacement(),
                    null,
                    -1, -1,
                    player.getInventoryController().getPlacementGridX(),
                    player.getInventoryController().getPlacementGridY(),
                    player.getInventoryController().getAvailableRecipes(),
                    player.getInventoryController().getSelectedRecipe());
        }

        if (rayHandler != null) {
            rayHandler.setCombinedMatrix(
                    cameraController.getCamera().combined,
                    cameraController.getCamera().position.x,
                    cameraController.getCamera().position.y,
                    cameraController.getCamera().viewportWidth,
                    cameraController.getCamera().viewportHeight);
            rayHandler.updateAndRender();
        }
    }

    private boolean detectIfRoom0(Mapa mapa) {
        if (mapa.getCampFire() != null) {
            System.out.println("✅ Detectada sala 0 pela fogueira");
            return true;
        }

        if (mapa.mapWidth == 64 && mapa.mapHeight == 64) {
            System.out.println("✅ Detectada sala 0 pelo tamanho 64x64");
            return true;
        }

        if (mapa.mapGenerator == null) {
            System.out.println("✅ Detectada sala 0 pela ausência de mapGenerator");
            return true;
        }

        System.out.println("❌ Não é sala 0");
        return false;
    }

    public void calculateOffsets() {
        float viewportWidth = cameraController.getCamera().viewportWidth;
        float viewportHeight = cameraController.getCamera().viewportHeight;

        offsetX = (viewportWidth - (mapa.mapWidth * TILE_SIZE)) / 2f;
        offsetY = (viewportHeight - (mapa.mapHeight * TILE_SIZE)) / 2f;
    }

    public void resize(int width, int height) {
        cameraController.resize(width, height);
        calculateOffsets();
    }

    public void dispose() {
        if (mapa.getCampFire() != null) {
            mapa.getCampFire().dispose();
        }

        if (sistemaLuz != null) {
            sistemaLuz.dispose();
        }
        shapeRenderer.dispose();
        spriteBatch.dispose();
        tileRenderer.dispose();
        playerRenderer.dispose();
        if (uiStage != null)
            uiStage.dispose();
        if (uiSkin != null)
            uiSkin.dispose();
        castorRenderer.dispose();
        ratRenderer.dispose();
        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof Castor) {
                ((Castor) enemy).ai.getStateEnemy().dispose();
            }
        }
        if (escurecedor != null) {
            escurecedor.dispose();
        }
    }
}