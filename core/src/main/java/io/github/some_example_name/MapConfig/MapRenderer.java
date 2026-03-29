package io.github.some_example_name.MapConfig;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import box2dLight.RayHandler;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

import io.github.some_example_name.Entities.Debug.DebugRenderers;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Npcs.NPC;
import io.github.some_example_name.Entities.Particulas.BloodParticleRenderer;
import io.github.some_example_name.Entities.Particulas.BloodParticleSystem;
import io.github.some_example_name.Entities.Particulas.MagicParticle.MagicParticleSystem;
import io.github.some_example_name.Entities.Particulas.RevolverShell.RevolverShellEjector;
import io.github.some_example_name.Entities.Particulas.Shell.ShellSystem;
import io.github.some_example_name.Entities.Particulas.Smoke.SmokeAnimations;
import io.github.some_example_name.Entities.Particulas.Smoke.SmokeSystem;
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
import io.github.some_example_name.Interface.CabanaInteractionSystem;
import io.github.some_example_name.Luz.EscurecedorAmbiente;
import io.github.some_example_name.Luz.SistemaLuz;
import io.github.some_example_name.MapConfig.Rooms.Boulder;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;
import io.github.some_example_name.MapConfig.Rooms.Room0Cabana;
import io.github.some_example_name.MapConfig.Rooms.Room0Door;
import io.github.some_example_name.MapConfig.Rooms.Room0TileRenderer;
import io.github.some_example_name.MapConfig.Rooms.Room0WallRenderer;
import io.github.some_example_name.MapConfig.Rooms.StaticItem;
import io.github.some_example_name.MapConfig.Rooms.Itens_start_room.Engraving;
import io.github.some_example_name.MapConfig.Rooms.Itens_start_room.Pillar;
import io.github.some_example_name.Screens.ScreenEffects.ScreenFreezeSystem;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Entities.Renderer.SpawnRoomRenderer;
import io.github.some_example_name.Camera.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.some_example_name.Entities.Enemies.StateEnemy.StateEnemy;
import io.github.some_example_name.Entities.Inventory.InventoryController;

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
    private Room0WallRenderer room0WallRenderer;
    private Room0Door room0Door;
    private SpawnRoomRenderer spawnRoomRenderer;
    private BloodParticleSystem bloodParticleSystem;
    private BloodParticleRenderer bloodParticleRenderer;
    private OrthographicCamera hudCamera;
    private ShapeRenderer hudShapeRenderer;
    private SpriteBatch hudSpriteBatch;
    private boolean useHudForInventory = true; // Co
    private MagicParticleSystem magicParticleSystem;

    // ADICIONADO: DebugRenderer
    private DebugRenderers debugRenderers;

    private DestructibleRenderer destructibleRenderer;
    public static final int TILE_SIZE = 64;
    public float offsetX;
    public float offsetY;

    private Stage uiStage;
    private Skin uiSkin;
    private TileRenderer tileRenderer;
    private SpriteBatch particleBatch;

    public MapRenderer(Mapa mapa) {
        this.mapa = mapa;

        this.rayHandler = mapa.getRayHandler();
        System.out.println("=== MAP RENDERER CONSTRUTOR ===");
        System.out.println("Tela: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        spriteBatch = new SpriteBatch();
        particleBatch = new SpriteBatch();
        cameraController = new Camera();

        // ADICIONADO: Inicialização do DebugRenderer
        this.debugRenderers = new DebugRenderers();

        this.tileRenderer = new TileRenderer(mapa, TILE_SIZE);
        this.projectileRenderer = new ProjectileRenderer(mapa, TILE_SIZE);
        this.playerRenderer = new PlayerRenderer(mapa.robertinhoo.getWeaponSystem());
        mapa.robertinhoo.setRenderer(playerRenderer);
        this.ammoRenderer = new AmmoRenderer(TILE_SIZE);

        castorRenderer = new CastorRenderer();
        ratRenderer = new RatRenderer();

        float startX = (Gdx.graphics.getWidth() - 5 * 64 * 1.2f) / 2f;
        float startY = (Gdx.graphics.getHeight() - 5 * 64 * 1.2f) / 2f;
        Vector2 inventoryPos = new Vector2(startX, startY);
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();
        this.renderInventory = new RenderInventory(
                mapa.robertinhoo.getInventory(),
                64,
                inventoryPos,
                mapa.robertinhoo.getInventoryController());
        mapa.robertinhoo.getInventoryController().setContextMenu(renderInventory.getContextMenu());
        this.renderInventory.setHudCamera(hudCamera);
        System.out.println("Inventário criado na posição: " + renderInventory.position);

        mapa.robertinhoo.setCamera(cameraController.getCamera());
        this.destructibleRenderer = new DestructibleRenderer(TILE_SIZE);
        this.shadowRenderer = new ShadowRenderer(shapeRenderer);
        this.craftItensRenderer = new CraftItensRenderer(TILE_SIZE);
        this.corpseManager = new CorpseManager();
        this.isRoom0 = mapa.getCampFire() != null;

        this.isRoom0 = detectIfRoom0(mapa);

        if (isRoom0) {
            this.room0TileRenderer = new Room0TileRenderer(mapa, TILE_SIZE);
            this.room0WallRenderer = new Room0WallRenderer(mapa, TILE_SIZE);

            this.magicParticleSystem = mapa.getMagicParticleSystem();
            System.out.println("✅ Room0TileRenderer inicializado para Sala 0");
        } else {
            if (mapa.getMapGenerator() != null) {
                FixedRoom spawnRoom = mapa.getMapGenerator().getSpawnRoom();
                if (spawnRoom != null && spawnRoom.getBounds() != null) {
                    System.out.println("🎨 Criando SpawnRoomRenderer para sala fixa");
                    spawnRoomRenderer = new SpawnRoomRenderer(spawnRoom, mapa, TILE_SIZE);

                }
            }
        }
        this.room0Door = mapa.getDoor0();
        this.sistemaLuz = new SistemaLuz();
        this.escurecedor = new EscurecedorAmbiente();
        bloodParticleSystem = mapa.getBloodParticleSystem();
        bloodParticleRenderer = new BloodParticleRenderer();
        if (mapa.robertinhoo != null) {
            mapa.robertinhoo.setMapRenderer(this);
            System.out.println("✅ MapRenderer: PlayerWeaponSystem vinculado ao novo MapRenderer");
        }
        SmokeAnimations smokeAnim = new SmokeAnimations();
        SmokeSystem.getInstance().init(smokeAnim.getAnimation(), 0.2f); // escala

        hudShapeRenderer = new ShapeRenderer();
        hudSpriteBatch = new SpriteBatch();

    }

    public void render(float delta, Robertinhoo player) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        calculateOffsets();
        cameraController.centerOnPlayer(player, offsetX, offsetY);
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
        mapa.world.step(delta, 6, 2);
        playerRenderer.update(delta, player);
        if (room0Door != null) {
            room0Door.updateLightSpherePosition(offsetX, offsetY);
            room0Door.update(delta);
        }

        List<ShadowEntity> shadowEntities = new ArrayList<>();
        shadowEntities.add(player);

        Vector2 cameraPosWorld = new Vector2(
                cameraController.getCamera().position.x / TILE_SIZE,
                cameraController.getCamera().position.y / TILE_SIZE);
        bloodParticleSystem.update(delta, cameraPosWorld);

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

        // ==================== RENDERIZAÇÃO DO CENÁRIO ====================
        spriteBatch.begin();

        // 1. CENÁRIO (CHÃO, PAREDES, ELEMENTOS FIXOS)
        if (isRoom0 && room0TileRenderer != null) {
            room0TileRenderer.renderFloor(spriteBatch, offsetX, offsetY);
            room0WallRenderer.renderWalls(spriteBatch, offsetX, offsetY);
            for (Boulder boulder : mapa.getBoulders()) {
                boulder.render(spriteBatch, offsetX, offsetY, TILE_SIZE);
            }
        } else {
            tileRenderer.render(spriteBatch, offsetX, offsetY, delta);
            tileRenderer.setSpawnRoomBounds(mapa.getSpawnRoom().getBounds());
            spawnRoomRenderer.render(spriteBatch, offsetX, offsetY);
        }

        // Fogueira
        if (isRoom0 && mapa.getCampFire() != null) {
            Vector2 campfireTilePos = mapa.getCampFire().getPosition();
            float screenX = offsetX + campfireTilePos.x * TILE_SIZE;
            float screenY = offsetY + campfireTilePos.y * TILE_SIZE;
            mapa.getCampFire().update(delta);
            mapa.getCampFire().render(spriteBatch, screenX, screenY);
        }

        // Cabanas
        for (Room0Cabana cabana : mapa.getCabanas()) {
            Vector2 cabanaTilePos = cabana.getPosition();
            float screenX = offsetX + cabanaTilePos.x * TILE_SIZE;
            float screenY = offsetY + cabanaTilePos.y * TILE_SIZE;
            cabana.render(spriteBatch, screenX, screenY);
        }

        for (NPC npc : mapa.getNPCs()) {
            npc.render(spriteBatch, offsetX, offsetY);
        }

        // Itens estáticos
        for (StaticItem staticItem : mapa.getStaticItems()) {
            Vector2 itemTilePos = staticItem.getPosition();
            float screenX = offsetX + itemTilePos.x * TILE_SIZE;
            float screenY = offsetY + itemTilePos.y * TILE_SIZE;
            staticItem.update(delta);
            staticItem.render(spriteBatch, screenX, screenY);
        }

        if (room0Door != null) {
            room0Door.render(spriteBatch, offsetX, offsetY, cameraController.getCamera().combined);
        }

        for (Pillar pillar : mapa.pillars) {
            pillar.update(delta);
            pillar.render(spriteBatch, offsetX, offsetY, TILE_SIZE);
        }
        if (mapa.engraving != null) {

            mapa.engraving.render(spriteBatch, offsetX, offsetY, TILE_SIZE);
        }

        spriteBatch.end(); // Fim do cenário

        // ==================== SOMBRAS ====================
        shadowRenderer.renderShadows(shadowEntities, offsetX, offsetY, TILE_SIZE);

        // ==================== ENTIDADES ====================
        spriteBatch.begin();

        destructibleRenderer.render(spriteBatch, mapa.getDestructibles(), offsetX, offsetY);
        projectileRenderer.render(spriteBatch, delta, offsetX, offsetY);
        corpseManager.render(spriteBatch, offsetX, offsetY);

        // Jogador
        playerRenderer.render(spriteBatch, player, delta, offsetX, offsetY);

        // Arma do jogador (precisa das coordenadas do jogador)
        float playerX = offsetX + (player.bounds.x * TILE_SIZE) - (playerRenderer.getRenderScale(player) - 1) * 8;
        float playerY = offsetY + (player.bounds.y * TILE_SIZE) - (playerRenderer.getRenderScale(player) - 1) * 8;
        player.getWeaponSystem().renderWeapon(spriteBatch, delta, player, playerX, playerY);
        // ==================== PARTÍCULAS ====================
        particleBatch.setProjectionMatrix(cameraController.getCamera().combined);
        bloodParticleSystem.update(delta, cameraPosWorld);
        ShellSystem.getInstance().update(delta);
        RevolverShellEjector.getInstance().update(delta);
        SmokeSystem.getInstance().update(delta);
        if (magicParticleSystem != null) {
            float left = offsetX;
            float right = offsetX + mapa.mapWidth * TILE_SIZE;
            float bottom = offsetY;
            float top = offsetY + mapa.mapHeight * TILE_SIZE;
            magicParticleSystem.update(delta, left, right, bottom, top);
        }

        particleBatch.begin();
        bloodParticleRenderer.render(particleBatch, bloodParticleSystem, offsetX, offsetY);
        bloodParticleSystem.renderPools(particleBatch, offsetX, offsetY, TILE_SIZE);
        ShellSystem.getInstance().render(particleBatch, offsetX, offsetY, TILE_SIZE);
        RevolverShellEjector.getInstance().render(particleBatch, offsetX, offsetY, TILE_SIZE);
        SmokeSystem.getInstance().render(particleBatch, offsetX, offsetY, TILE_SIZE);
        if (magicParticleSystem != null) {
            magicParticleSystem.render(particleBatch);
        }
        particleBatch.end();
        // Inimigos
        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof Ratinho) {
                Ratinho rat = (Ratinho) enemy;
                if (rat.isDead()) {
                    if (rat.isDeathAnimationFinished() && !rat.isMarkedForDestruction()) {
                        corpseManager.addCorpse(rat, ratRenderer);
                        rat.markForDestruction();
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
                    if (castor.isDeathAnimationFinished() && !castor.isMarkedForDestruction()) {
                        corpseManager.addCorpse(castor, castorRenderer);
                        castor.markForDestruction();
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

        // Armas no chão
        for (Weapon weapon : mapa.getWeapons()) {
            weapon.update(delta);
            TextureRegion frame = weapon.getCurrentFrame(delta);
            float floatY = weapon.getPosition().y * TILE_SIZE + weapon.getFloatOffset();
            spriteBatch.draw(
                    frame,
                    offsetX + weapon.getPosition().x * TILE_SIZE,
                    offsetY + floatY,
                    25, 25);
        }

        // Munição
        ammoRenderer.render(spriteBatch, mapa.getAmmo(), offsetX, offsetY);

        // Itens de craft
        craftItensRenderer.render(spriteBatch, mapa.getCraftItems(), offsetX, offsetY);

        spriteBatch.end(); // Fim das entidades

        // ==================== ILUMINAÇÃO ====================
        if (isRoom0 && room0Door != null) {
            room0Door.updateLightSpherePosition(offsetX, offsetY);
        }

        escurecedor.aplicarEscurecimentoSuave(cameraController.getCamera().combined);

        sistemaLuz.setProjectionMatrix(cameraController.getCamera().combined);
        sistemaLuz.begin();

        // Luz do jogador
        Vector2 playerWorldPos = player.getBody().getPosition();
        float playerScreenX = offsetX + playerWorldPos.x * TILE_SIZE;
        float playerScreenY = offsetY + playerWorldPos.y * TILE_SIZE;
        sistemaLuz.renderLight(playerScreenX, playerScreenY, 90f, new Color(0.7f, 0.8f, 1.0f, 0.3f));

        // Fogueira
        if (isRoom0 && mapa.getCampFire() != null) {
            Vector2 campfireTilePos = mapa.getCampFire().getPosition();
            float screenX = offsetX + campfireTilePos.x * TILE_SIZE;
            float screenY = offsetY + campfireTilePos.y * TILE_SIZE;
            float lightX = screenX + TILE_SIZE / 2f;
            float lightY = screenY + TILE_SIZE / 2f;
            sistemaLuz.renderFogueira(lightX, lightY, 350f, delta);
        }

        // Luz da caveira da porta
        if (isRoom0 && room0Door != null) {
            room0Door.renderLight(sistemaLuz, offsetX, offsetY);
        }
        // Luz dos pilares
        for (Pillar pillar : mapa.pillars) {
            pillar.renderLight(sistemaLuz, offsetX, offsetY, TILE_SIZE);
        }

        if (mapa.engraving != null) {
            mapa.engraving.renderLight(sistemaLuz, offsetX, offsetY, TILE_SIZE);
        }

        sistemaLuz.end();

        if (isRoom0 && room0Door != null && room0Door.getLightSphere() != null) {
            if (!escurecedor.lightSpheres.contains(room0Door.getLightSphere())) {
                escurecedor.adicionarLightSphere(room0Door.getLightSphere());
            }
        }

        // ==================== MIRA E INTERFACE ====================
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        if (player.getInventory().getEquippedWeapon() != null) {
            player.getWeaponSystem().renderMiraArma(shapeRenderer);
        }

        // Inventário
        InventoryController ctrl = player.getInventoryController();
        if (ctrl.GetIsOpen() || ctrl.isInPlacementMode()) {
            renderInventoryWithHUD(player, ctrl.isInPlacementMode());
        }

        // RayHandler (se ainda estiver usando)
        if (rayHandler != null) {
            rayHandler.setCombinedMatrix(
                    cameraController.getCamera().combined,
                    cameraController.getCamera().position.x,
                    cameraController.getCamera().position.y,
                    cameraController.getCamera().viewportWidth,
                    cameraController.getCamera().viewportHeight);
            rayHandler.updateAndRender();
        }

        // Interação com cabana
        if (mapa.getCabanaInteractionSystem() != null) {
            CabanaInteractionSystem cabanaSystem = mapa.getCabanaInteractionSystem();
            if (cabanaSystem.shouldShowInteractPrompt()) {
                spriteBatch.begin();
                spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
                cabanaSystem.renderInteractPrompt(spriteBatch, offsetX, offsetY);
                spriteBatch.end();
            }
            if (cabanaSystem.isTransitioning()) {
                spriteBatch.begin();
                spriteBatch.setProjectionMatrix(
                        new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
                cabanaSystem.renderTransitionEffect(spriteBatch);
                spriteBatch.end();
            }
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

        // Atualiza a câmera HUD também
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();

        if (renderInventory != null) {
            renderInventory.updateScreenSize(width, height);
        }

    }

    public PlayerRenderer getPlayerRenderer() {
        return playerRenderer;
    }

    private void renderInventoryWithHUD(Robertinhoo player, boolean isPlacementMode) {
        hudShapeRenderer.setProjectionMatrix(hudCamera.combined);
        hudSpriteBatch.setProjectionMatrix(hudCamera.combined);

        InventoryController ctrl = player.getInventoryController();

        renderInventory.render(
                hudShapeRenderer,
                hudSpriteBatch,
                isPlacementMode ? ctrl.getCurrentPlacementItem() : null,
                isPlacementMode ? ctrl.getPlacementGridX() : 0,
                isPlacementMode ? ctrl.getPlacementGridY() : 0,
                isPlacementMode ? ctrl.isValidPlacement() : false,
                ctrl.getSelectedItem(),
                ctrl.getOriginalGridX(),
                ctrl.getOriginalGridY(),
                ctrl.getCursorGridX(),
                ctrl.getCursorGridY(),
                ctrl.getAvailableRecipes(),
                ctrl.getSelectedRecipe());
    }

    public void dispose() {
        if (mapa.getCampFire() != null) {
            mapa.getCampFire().dispose();
        }

        if (spawnRoomRenderer != null) {
            spawnRoomRenderer.dispose();
        }

        if (room0WallRenderer != null) {
            room0WallRenderer.dispose();
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
        if (bloodParticleSystem != null) {
            bloodParticleSystem.clear();
        }
        if (particleBatch != null)
            particleBatch.dispose();

        hudShapeRenderer.dispose();
        hudSpriteBatch.dispose();

        ShellSystem.getInstance().dispose();

        if (magicParticleSystem != null) {
            magicParticleSystem.dispose();// Isso deve liberar a textura interna
            magicParticleSystem = null;
        }
    }
}