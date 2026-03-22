package io.github.some_example_name.MapConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import box2dLight.RayHandler;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Contact.GameContactListener;
import io.github.some_example_name.Entities.Itens.CraftinItens.Polvora;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Particulas.BloodParticleSystem;
import io.github.some_example_name.Entities.Particulas.BloodPoolSystem;
import io.github.some_example_name.Entities.Particulas.MagicParticle.MagicParticleSystem;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Npcs.EsmeraldaNPC;
import io.github.some_example_name.Entities.Npcs.NPC;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Interface.CabanaInteractionSystem;
import io.github.some_example_name.Otimizations.MapBorderManager;
import io.github.some_example_name.Otimizations.WallOtimizations;
import io.github.some_example_name.MapConfig.Generator.MapGenerator;
import io.github.some_example_name.MapConfig.Generator.StartRoom;
import io.github.some_example_name.MapConfig.MapDisposer.MapDisposer;
import io.github.some_example_name.MapConfig.Rooms.Boulder;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;
import io.github.some_example_name.MapConfig.Rooms.Room0Cabana;
import io.github.some_example_name.MapConfig.Rooms.Room0Door;
import io.github.some_example_name.MapConfig.Rooms.Room0LayoutLoader;
import io.github.some_example_name.MapConfig.Rooms.StaticItem;
import io.github.some_example_name.MapConfig.Rooms.Items_sala_0.CampFire;
import io.github.some_example_name.MapConfig.Rooms.Itens_start_room.Engraving;
import io.github.some_example_name.MapConfig.Rooms.Itens_start_room.Pillar;
import io.github.some_example_name.MapConfig.Spawner.EntitySpawner;

public class Mapa implements RoomTransitionManager {

    public static List<Enemy> enemies;
    public static List<Weapon> weapons;
    public List<Ammo> ammo;
    public List<Polvora> polvoras;
    public List<Projectile> projectiles = new ArrayList<>();
    public List<Destructible> destructibles = new ArrayList<>();
    public List<Item> craftItems = new ArrayList<>();
    public List<Runnable> pendingActions = new ArrayList<>();
    public List<Rectangle> rooms = new ArrayList<>();
    public static final float BOX2D_SCALE = 1 / 64f;
    public List<Room0Cabana> cabanas = new ArrayList<>();
    public CabanaInteractionSystem cabanaInteraction;
    public List<StaticItem> staticItems = new ArrayList<>();
    private List<NPC> npcs = new ArrayList<>();

    public Room0Door room0Door;
    public boolean isRoom0;
    public RoomTransitionListener roomTransitionListener;
    public final int mapaId;
    public static int mapaCounter = 0;
    public static List<Boulder> boulders = new ArrayList<>();
    public List<Pillar> pillars = new ArrayList<>();
    public PathfindingSystem pathfindingSystem;
    public CampFire campFire;
    public Engraving engraving;

    public World world;
    public WallOtimizations agruparParedes;
    public MapGenerator mapGenerator;

    public static int TILE = 0x000000; // #000000 (tiles normais)
    public static int START = 0xFF0000; // #FF0000 (ponto de início)
    public static int PAREDE = 0x00FFF4; // #00FFF4 (paredes)
    public static int ENEMY = 0X913d77; // #913d77 (inimigos)
    public static int REVOLVER = 0X22ff00; // #22ff00
    public static int AMMO09MM = 0Xffffff; // #FFFFFF
    public static int BARRIL = 0Xff8f00; // #ff8f00

    private MagicParticleSystem magicParticleSystem;

    public ArrayList<Vector2> wallPositions = new ArrayList<>();

    public int mapWidth;
    public int mapHeight;

    public int[][] tiles;
    public Vector2 startPosition;

    public Robertinhoo robertinhoo;
    public Ratinho ratinho;
    public RayHandler rayHandler;
    public boolean lightsInitialized = false;
    public MapCleanUpManager cleanupManager;
    public boolean obstaclesChanged = false;
    public int previousDestructiblesCount = 0;
    public Set<Vector2> previousBarrelPositions = new HashSet<>();

    public GameContactListener contactListener;
    public BloodParticleSystem bloodParticleSystem;
    public BloodPoolSystem bloodPoolSystem;

    public void setRayHandler(RayHandler rayHandler) {
        this.rayHandler = rayHandler;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public Mapa(boolean isRoom0) {
        world = new World(new Vector2(0, 0), true);
        enemies = new ArrayList<>();
        weapons = new ArrayList<>();
        ammo = new ArrayList<>();
        polvoras = new ArrayList<>();
        projectiles = new ArrayList<>();
        destructibles = new ArrayList<>();
        craftItems = new ArrayList<>();
        pendingActions = new ArrayList<>();
        rooms = new ArrayList<>();
        this.isRoom0 = isRoom0;
        mapaCounter++;
        mapaId = mapaCounter;
        Texture bloodTexture = new Texture(Gdx.files.internal("ParticulasSangue/Sangue.png"));
        Texture bloodPoolTex = new Texture(Gdx.files.internal("ParticulasSangue/Poça.png"));
        TextureRegion bloodRegion = new TextureRegion(bloodTexture);
        TextureRegion poolRegion = new TextureRegion(bloodPoolTex);
        this.bloodPoolSystem = new BloodPoolSystem(poolRegion, 5); // Usa a variável de instância
        System.out.println("🩸 Sistema de poças criado com " + 5 + " tipos de sprites");

        bloodParticleSystem = new BloodParticleSystem(bloodRegion, 4, this.bloodPoolSystem);
        System.out.println("💉 Sistema de partículas criado com poças integradas");

        System.out.println("\n🗺️ [Mapa#" + mapaId + "] CONSTRUTOR INICIADO");
        System.out.println("   - isRoom0: " + isRoom0);
        System.out.println("   - World criado: " + world);
        System.out.println("   - World hashCode: " + System.identityHashCode(world));

        agruparParedes = new WallOtimizations(this);
        this.pathfindingSystem = new PathfindingSystem(this);
        this.cleanupManager = new MapCleanUpManager(this);

        initializeLights();

        if (isRoom0) {
            setupRoom0();
            Texture magicParticleTex = new Texture(Gdx.files.internal("ParticulasMagicas/magic.png"));
            TextureRegion magicRegion = new TextureRegion(magicParticleTex);
            magicParticleSystem = new MagicParticleSystem(magicRegion);
            System.out.println("🔮 [Mapa] MagicParticleSystem CRIADO. Textura: " + magicParticleTex);
            EntitySpawner spawner = new EntitySpawner(this);
            spawner.spawnAll();
        } else {
            this.mapGenerator = new MapGenerator(50, 50, true);
            this.mapWidth = mapGenerator.getMapWidth();
            this.mapHeight = mapGenerator.getMapHeight();
            this.tiles = mapGenerator.getTiles();
            this.wallPositions = mapGenerator.getWallPositions();

            Vector2 worldStartPos = mapGenerator.getWorldStartPosition(mapHeight);
            robertinhoo = new Robertinhoo(this, worldStartPos.x, worldStartPos.y, null, null);
            this.startPosition = mapGenerator.getStartPosition();
            agruparEPCriarParedes();

            generateProceduralMap(mapWidth, mapHeight, mapGenerator);
            EntitySpawner spawner = new EntitySpawner(this);
            spawner.spawnAll();
            if (mapGenerator != null) {
                FixedRoom spawnRoom = mapGenerator.getSpawnRoom();
                if (spawnRoom != null && spawnRoom.getBounds() != null) {
                    Rectangle bounds = spawnRoom.getBounds();

                    // Cria a porta (já existente)
                    int doorX = (int) bounds.x + StartRoom.DOOR_TILE_X;
                    int doorY = (int) bounds.y + StartRoom.DOOR_TILE_Y;
                    Room0Door door = new Room0Door(this, doorX, doorY, false);
                    setDoor(door);
                    System.out.println("🚪 Porta criada em tile mundial: (" + doorX + "," + doorY + ")");

                    // Cria os pilares
                    StartRoom startRoom = mapGenerator.getStartRoomInstance();
                    if (startRoom != null) {
                        for (Vector2 pos : startRoom.getPillarPositions()) {
                            int worldX = (int) bounds.x + (int) pos.x;
                            int worldY = (int) bounds.y + (int) pos.y;
                            System.out.println("🏛️ Criando pilar em tile mundial: (" + worldX + "," + worldY + ")");
                            Pillar pillar = new Pillar(this, worldX, worldY, "rooms/pilar.png");
                            addPillar(pillar);
                        }
                        // Cria a gravura
                        if (startRoom.getEngravingPosition() != null) {
                            Vector2 pos = startRoom.getEngravingPosition();
                            int worldX = (int) bounds.x + (int) pos.x;
                            int worldY = (int) bounds.y + (int) pos.y;
                            System.out.println("🎨 Criando gravura em tile mundial: (" + worldX + "," + worldY + ")");
                            Engraving engraving = new Engraving(this, worldX, worldY, "rooms/gravura.png");
                            setEngraving(engraving);
                        }

                    }
                }
            }

        }
    }

    public void setDoor(Room0Door door) {
        this.room0Door = door;
    }

    private void generateProceduralMap(int width, int height, MapGenerator mapGenerator) {

        this.mapWidth = mapGenerator.getMapWidth();
        this.mapHeight = mapGenerator.getMapHeight();
        this.tiles = mapGenerator.getTiles();
        this.startPosition = mapGenerator.getStartPosition();
        this.wallPositions = mapGenerator.getWallPositions();
        this.rooms = mapGenerator.getRooms();

        agruparEPCriarParedes();
    }

    private void setupRoom0() {
        System.out.println("Inicializando Sala 0 - Com Layout da Imagem");
        loadRoom0LayoutFromImage("sala_0/layoyt_sala_0.png");
        Vector2 worldStartPos = tileToWorld((int) startPosition.x, (int) startPosition.y);
        robertinhoo = new Robertinhoo(this, worldStartPos.x, worldStartPos.y, null, null);
        Room0LayoutLoader.loadRoom0Specifics(this, "sala_0/layoyt_sala_0.png");
        cabanaInteraction = new CabanaInteractionSystem(this, robertinhoo);
        agruparEPCriarParedes();
        setupContactListener(robertinhoo);

        System.out.println("Sala 0 criada - Tamanho: " + mapWidth + "x" + mapHeight);
    }

    // NOVO: Getter para o sistema de interação
    public CabanaInteractionSystem getCabanaInteractionSystem() {
        return cabanaInteraction;
    }

    private void loadRoom0LayoutFromImage(String imagePath) {
        try {
            Pixmap pixmap = new Pixmap(Gdx.files.internal(imagePath));

            // Define o tamanho da sala pela imagem
            this.mapWidth = pixmap.getWidth();
            this.mapHeight = pixmap.getHeight();
            this.tiles = new int[mapWidth][mapHeight];

            System.out.println("Definindo tamanho da sala 0 pela imagem: " + mapWidth + "x" + mapHeight);
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    tiles[x][y] = TILE;
                }
            }
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    if (x == 0 || y == 0 || x == mapWidth - 1 || y == mapHeight - 1) {
                        tiles[x][y] = PAREDE;
                        wallPositions.add(new Vector2(x, y));
                    }
                }
            }

            pixmap.dispose();

        } catch (Exception e) {
            System.err.println("Erro ao carregar layout da imagem: " + e.getMessage());
            this.mapWidth = 10;
            this.mapHeight = 10;
            this.tiles = new int[mapWidth][mapHeight];

            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    if (x == 0 || y == 0 || x == mapWidth - 1 || y == mapHeight - 1) {
                        tiles[x][y] = PAREDE;
                        wallPositions.add(new Vector2(x, y));
                    } else {
                        tiles[x][y] = TILE;
                    }
                }
            }
        }

        // Define posição inicial no centro
        int startX = mapWidth / 2;
        int startY = mapHeight / 2;
        this.startPosition = new Vector2(startX, startY);
    }

    public void initializeLights() {
        if (rayHandler == null) {
            try {
                rayHandler = new RayHandler(world);
                rayHandler.setAmbientLight(1, 1, 1, 1);
                rayHandler.setShadows(false); // desativa sombras para não crashar
                rayHandler.setBlurNum(0);

                lightsInitialized = true;
                System.out.println("✅ RayHandler com sombras suaves");
            } catch (Exception e) {
                System.err.println("❌ Erro no RayHandler: " + e.getMessage());
            }
        }
    }

    public Rectangle findRoomContainingTile(Vector2 tilePos) {
        for (Rectangle room : rooms) {

            if (tilePos.x >= room.x + 1 && tilePos.x < room.x + room.width - 1 &&
                    tilePos.y >= room.y + 1 && tilePos.y < room.y + room.height - 1) {
                Gdx.app.log("Mapa", "✅ Sala encontrada: " + room);
                return room;
            }
        }
        return null;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    private void agruparEPCriarParedes() {
        // 1. Criar bordas do mapa otimizadas (apenas onde necessário)
        MapBorderManager.createOptimizedMapBorders(this);

        // 2. Otimizar e criar paredes internas (salas + corredores)
        List<Rectangle> retangulos = agruparParedes.optimizeWalls(wallPositions);
        for (Rectangle ret : retangulos) {
            createWallBody(ret);
        }

        Gdx.app.log("Mapa", "Sistema de colisão otimizado criado:");
        Gdx.app.log("Mapa", "- Bordas: criadas apenas onde necessário");
        Gdx.app.log("Mapa", "- Paredes internas: " + retangulos.size() + " retângulos otimizados");
    }

    public void clearEnemies() {
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.getBody() != null) {
                    world.destroyBody(enemy.getBody());
                }
            }
            enemies.clear();
            System.out.println("Inimigos removidos da sala");
        }
    }

    public void clearItems() {
        if (weapons != null)
            weapons.clear();
        if (ammo != null)
            ammo.clear();
        if (polvoras != null)
            polvoras.clear();
        if (craftItems != null)
            craftItems.clear();
    }

    public void createWallBody(Rectangle ret) {
        float escala = 1.0f;

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyType.StaticBody;
        float posY = (mapHeight - ret.y - ret.height / 2) * escala;
        bodyDef.position.set(
                (ret.x + ret.width / 2) * escala,
                posY);

        Body body = world.createBody(bodyDef);
        body.setUserData("WALL");

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                (ret.width / 2) * escala,
                (ret.height / 2) * escala);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Constants.BIT_WALL;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public boolean isTileBlocked(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= mapWidth || tileY >= mapHeight) {
            return true;
        }
        return tiles[tileX][tileY] == PAREDE || isPhysicalWallAt(tileX, tileY);
    }

    private boolean isPhysicalWallAt(int tileX, int tileY) {
        Vector2 worldPos = tileToWorld(tileX, tileY);

        com.badlogic.gdx.utils.Array<Body> bodies = new com.badlogic.gdx.utils.Array<>();
        world.getBodies(bodies);

        for (Body body : bodies) {
            if ("WALL".equals(body.getUserData())) {
                Vector2 bodyPos = body.getPosition();
                if (MathUtils.isEqual(bodyPos.x, worldPos.x, 0.5f) &&
                        MathUtils.isEqual(bodyPos.y, worldPos.y, 0.5f)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Ammo> getAmmo() {
        return ammo;
    }

    public void update(float deltaTime) {
        cleanupManager.clean(deltaTime);
        processPendingActions();

        if (obstaclesChanged) {
            pathfindingSystem.updateGrid();
            obstaclesChanged = false;
        }
        checkObstaclesChanges(deltaTime);
        if (cabanaInteraction != null) {
            cabanaInteraction.update(deltaTime);
        }

        if (room0Door != null) {
            room0Door.update(deltaTime);
        }
        if (npcs != null) {
            for (NPC npc : npcs) {
                npc.update(deltaTime);
            }
        }

    }

    public void markObstaclesChanged() {
        this.obstaclesChanged = true;
    }

    public void forcePathfindingUpdate() {
        pathfindingSystem.forceGridUpdate();
    }

    public void addPendingAction(Runnable action) {
        pendingActions.add(action);
    }

    public void processPendingActions() {
        for (Runnable action : pendingActions) {
            action.run();
        }
        pendingActions.clear();
    }

    public List<Destructible> getDestructibles() {
        return destructibles;
    }

    boolean match(int src, int dst) {
        return src == dst;
    }

    public void addCraftItem(Item item) {
        System.out.println("📦 [Mapa#" + mapaId + "] ADICIONANDO craftItem: " + item);
        System.out.println("   - HashCode: " + System.identityHashCode(item));
        System.out.println("   - Lista antes: " + craftItems.size() + " itens");
        craftItems.add(item);
        System.out.println("   - Lista depois: " + craftItems.size() + " itens");

        // Verifique se o item já está em outra lista
        if (weapons.contains(item)) {
            System.out.println("⚠️ Item também está na lista weapons!");
        }
        if (ammo.contains(item)) {
            System.out.println("⚠️ Item também está na lista ammo!");
        }
        if (polvoras != null && polvoras.contains(item)) {
            System.out.println("⚠️ Item também está na lista polvoras!");
        }
    }

    public void removeCraftItem(Item item) {
        craftItems.remove(item);
    }

    public List<Item> getCraftItems() {
        return craftItems;
    }

    public void dispose() {
        if (rayHandler != null) {
            rayHandler.dispose();
        }
        for (StaticItem item : staticItems) {
            item.dispose();
        }
        staticItems.clear();
    }

    public PathfindingSystem getPathfindingSystem() {
        return pathfindingSystem;
    }

    public Vector2 worldToTile(Vector2 worldPos) {
        return new Vector2(
                (int) Math.floor(worldPos.x),
                mapHeight - 1 - (int) Math.floor(worldPos.y) // Inverte Y
        );
    }

    public Vector2 tileToWorld(int tileX, int tileY) {
        return new Vector2(
                tileX + 0.5f,
                mapHeight - 1 - tileY + 0.5f // Inverte Y
        );
    }

    public void renderDebug(ShapeRenderer renderer) {
        // Desenha tiles bloqueados
        renderer.setColor(Color.RED);
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (isTileBlocked(x, y)) {
                    Vector2 worldPos = tileToWorld(x, y);
                    renderer.rect(worldPos.x - 0.4f, worldPos.y - 0.4f, 0.8f, 0.8f);
                }
            }
        }

        // Desenha caminhos ativos
        renderer.setColor(Color.GREEN);
        for (Enemy enemy : enemies) {
            if (enemy instanceof Ratinho) {
                Ratinho rat = (Ratinho) enemy;
                List<Vector2> path = rat.getCurrentPath();
                if (path != null && !path.isEmpty()) {
                    Vector2 prev = rat.getBody().getPosition();
                    for (Vector2 point : path) {
                        renderer.line(prev.x, prev.y, point.x, point.y);
                        prev = point;
                    }
                }
            }
        }
    }

    public void checkPlayerItemContacts() {
        for (Item item : craftItems) {
            if (item instanceof PolvoraBruta) {
                PolvoraBruta polvora = (PolvoraBruta) item;

                // Verificar proximidade mesmo sem contato físico
                float distance = robertinhoo.getPosition().dst(polvora.getPosition());

                // Se estiver dentro do raio de coleta e ainda não registrado
                if (distance < 1.5f && !robertinhoo.getItemHandler().isPlayerTouching(polvora)) {
                    robertinhoo.getItemHandler().forceItemContact(polvora);
                }
            }
        }
    }

    public List<Rectangle> getRooms() {
        return rooms;
    }

    public Rectangle findRoomContaining(Vector2 position) {
        Vector2 tilePos = worldToTile(position);

        for (Rectangle room : rooms) {
            if (room.contains(tilePos)) {
                return room;
            }
        }

        return null;
    }

    public MapGenerator getMapGenerator() {
        return mapGenerator;
    }

    public CampFire getCampFire() {
        return campFire;
    }

    public void setCampFire(CampFire campFire) {
        this.campFire = campFire;
    }

    private float obstacleCheckTimer = 0f;
    private static final float OBSTACLE_CHECK_INTERVAL = 1.0f; // Verifica a cada 1 segundo

    public void onItemDestroyed() {
        obstaclesChanged = true;
        pathfindingSystem.forceGridUpdate();
        Gdx.app.log("Mapa", "Barril destruído - Pathfinding atualizado");
    }

    private void checkObstaclesChanges(float deltaTime) {
        obstacleCheckTimer += deltaTime;

        if (obstacleCheckTimer >= OBSTACLE_CHECK_INTERVAL) {
            obstacleCheckTimer = 0f;

            int currentCount = destructibles.size();
            if (currentCount != previousDestructiblesCount) {
                obstaclesChanged = true;
                previousDestructiblesCount = currentCount;
            }

            if (!obstaclesChanged) {
                Set<Vector2> currentBarrelPositions = new HashSet<>();
                for (Destructible destructible : destructibles) {
                    if (destructible instanceof Barrel) {
                        Barrel barrel = (Barrel) destructible;
                        if (!barrel.isDestroyed()) {
                            Vector2 tilePos = worldToTile(barrel.getPosition());
                            currentBarrelPositions.add(new Vector2((int) tilePos.x, (int) tilePos.y));
                        }
                    }
                }

                if (!currentBarrelPositions.equals(previousBarrelPositions)) {
                    obstaclesChanged = true;
                    previousBarrelPositions = currentBarrelPositions;
                }
            }

            if (obstaclesChanged) {
                pathfindingSystem.updateGrid();
                obstaclesChanged = false;
                Gdx.app.log("Mapa", "Mudanças detectadas - Grid atualizado");
            }
        }
    }

    public List<Room0Cabana> getCabanas() {
        return cabanas;
    }

    public void addCabana(Room0Cabana cabana) {
        this.cabanas.add(cabana);
    }

    public Vector2 worldToScreen(float worldX, float worldY) {
        // Implemente conforme sua câmera e viewport
        // Exemplo simples:
        return new Vector2(worldX * MapRenderer.TILE_SIZE, worldY * MapRenderer.TILE_SIZE);
    }

    public void addStaticItem(StaticItem item) {
        staticItems.add(item);
    }

    public void addBoulder(Boulder boulder) {
        boulders.add(boulder);
    }

    public List<Boulder> getBoulders() {
        return boulders;
    }

    public List<StaticItem> getStaticItems() {
        return staticItems;
    }

    public void addDoor(Room0Door door) {
        this.room0Door = door;
    }

    public Room0Door getDoor0() {
        return this.room0Door;
    }

    public interface RoomTransitionListener {
        void onRoomTransition(boolean toRoom0);
    }

    public interface RoomTransitionManager {
        void transitionToRoom1();

        void transitionToRoom0();
    }

    public void setRoomTransitionListener(RoomTransitionListener listener) {
        this.roomTransitionListener = listener;
    }

    @Override
    public void transitionToRoom1() {
        System.out.println("🎯 MÉTODO transitionToRoom1 EXECUTADO");
        System.out.println("🎯 Condições: isRoom0=" + isRoom0 +
                ", listener=" + (roomTransitionListener != null));

        if (isRoom0 && roomTransitionListener != null) {
            System.out.println("✅ Condições satisfeitas - Chamando listener...");
            roomTransitionListener.onRoomTransition(false);
        } else {
            System.out.println("❌ Condições NÃO satisfeitas!");
            if (!isRoom0)
                System.out.println("❌ Não é Sala 0!");
            if (roomTransitionListener == null)
                System.out.println("❌ Listener é null!");
        }
    }

    @Override
    public void transitionToRoom0() {
        if (!isRoom0 && roomTransitionListener != null) {
            System.out.println("🚪 Transicionando para Sala 0...");
            roomTransitionListener.onRoomTransition(true); // true = para Sala 0
        }
    }

    public void disposeSafely() {
        MapDisposer.disposeSafely(this);
    }

    public void setupContactListener(Robertinhoo player) {
        if (contactListener != null) {
            System.out.println("⚠️ ContactListener já existe, atualizando...");
            contactListener.updatePlayerReference(player);
        } else {
            System.out.println("🎯 Criando novo ContactListener...");
            contactListener = new GameContactListener(player);
        }

        world.setContactListener(contactListener);
        System.out.println("✅ ContactListener configurado no mundo");
    }

    /**
     * Atualiza a referência do jogador no ContactListener
     */
    public void updatePlayerInContactListener(Robertinhoo newPlayer) {
        if (contactListener != null) {
            contactListener.updatePlayerReference(newPlayer);
        } else {
            // Se não existe, cria um novo
            setupContactListener(newPlayer);
        }
    }

    public FixedRoom getSpawnRoom() {
        if (mapGenerator != null) {
            return mapGenerator.getSpawnRoom();
        }
        return null;
    }

    public GameContactListener getContactListener() {
        return contactListener;
    }

    public BloodParticleSystem getBloodParticleSystem() {
        return bloodParticleSystem;

    }

    public MagicParticleSystem getMagicParticleSystem() {
        return magicParticleSystem;
    }

    public BloodPoolSystem getBloodPoolSystem() {
        return bloodPoolSystem;
    }

    public void addPillar(Pillar pillar) {
        pillars.add(pillar);
    }

    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    public List<NPC> getNPCs() {
        return npcs;
    }

    public void setEngraving(Engraving engraving) {
        this.engraving = engraving;
    }
}
