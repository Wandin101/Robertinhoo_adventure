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
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Contact.GameContactListener;
import io.github.some_example_name.Entities.Itens.CraftinItens.Polvora;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraReforcada;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Itens.Weapon.Calibre12.Calibre12;
import io.github.some_example_name.Entities.Particulas.BloodParticleSystem;
import io.github.some_example_name.Entities.Particulas.BloodPoolSystem;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Interface.CabanaInteractionSystem;
import io.github.some_example_name.Otimizations.MapBorderManager;
import io.github.some_example_name.Otimizations.WallOtimizations;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.MapConfig.Generator.MapGenerator;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;
import io.github.some_example_name.MapConfig.Rooms.Room0Cabana;
import io.github.some_example_name.MapConfig.Rooms.Room0Door;
import io.github.some_example_name.MapConfig.Rooms.Room0LayoutLoader;
import io.github.some_example_name.MapConfig.Rooms.StaticItem;
import io.github.some_example_name.MapConfig.Rooms.Items_sala_0.CampFire;
import io.github.some_example_name.MapConfig.Spawner.BarrelSpawner;
import io.github.some_example_name.MapConfig.Spawner.GrassSpawner;

public class Mapa implements RoomTransitionManager {

    private List<Enemy> enemies;
    private List<Weapon> weapons;
    private List<Ammo> ammo;
    private List<Polvora> polvoras;
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Destructible> destructibles = new ArrayList<>();
    private List<Item> craftItems = new ArrayList<>();
    private List<Runnable> pendingActions = new ArrayList<>();
    private List<Rectangle> rooms = new ArrayList<>();
    public static final float BOX2D_SCALE = 1 / 64f;
    private List<Room0Cabana> cabanas = new ArrayList<>();
    public CabanaInteractionSystem cabanaInteraction;
    private List<StaticItem> staticItems = new ArrayList<>();
    private Room0Door room0Door;
    private boolean isRoom0;
    private RoomTransitionListener roomTransitionListener;
    private final int mapaId;
    private static int mapaCounter = 0;

    public PathfindingSystem pathfindingSystem;
    private CampFire campFire;

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

    ArrayList<Vector2> wallPositions = new ArrayList<>();

    public int mapWidth;
    public int mapHeight;

    public int[][] tiles;
    public Vector2 startPosition;

    public Robertinhoo robertinhoo;
    public Ratinho ratinho;
    private RayHandler rayHandler;
    private boolean lightsInitialized = false;
    private MapCleanUpManager cleanupManager;
    private boolean obstaclesChanged = false;
    private int previousDestructiblesCount = 0;
    private Set<Vector2> previousBarrelPositions = new HashSet<>();

    private GameContactListener contactListener;
    private BloodParticleSystem bloodParticleSystem;
    private BloodPoolSystem bloodPoolSystem;

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
            addRandomEntities();
            generateProceduralMap(mapWidth, mapHeight, mapGenerator);

        }
    }

    private void generateProceduralMap(int width, int height, MapGenerator mapGenerator) {

        this.mapWidth = mapGenerator.getMapWidth();
        this.mapHeight = mapGenerator.getMapHeight();
        this.tiles = mapGenerator.getTiles();
        this.startPosition = mapGenerator.getStartPosition();
        this.wallPositions = mapGenerator.getWallPositions();
        this.rooms = mapGenerator.getRooms();
        addRandomEntities();
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
        addTestWeaponsToRoom0();
        addTestEnemiesToRoom0();

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
                rayHandler.setAmbientLight(1f, 1f, 1f, 1f);
                rayHandler.setShadows(true);

                rayHandler.setBlurNum(1);
                RayHandler.useDiffuseLight(true);
                RayHandler.setGammaCorrection(true);

                lightsInitialized = true;
                System.out.println("✅ RayHandler com sombras suaves");
            } catch (Exception e) {
                System.err.println("❌ Erro no RayHandler: " + e.getMessage());
            }
        }
    }

    private void addRandomEntities() {
        Random rand = new Random();
        List<Vector2> validRoomPositions = new ArrayList<>();

        for (Rectangle room : rooms) {
            // Verifica configuração da sala
            boolean roomAllowsEnemies = roomAllowsEnemies(room);
            boolean roomAllowsItems = roomAllowsItems(room);

            for (int x = (int) room.x + 1; x < room.x + room.width - 1; x++) {
                for (int y = (int) room.y + 1; y < room.y + room.height - 1; y++) {
                    if (tiles[x][y] == TILE) {
                        // Não spawna na posição inicial do jogador
                        if (x != (int) startPosition.x || y != (int) startPosition.y) {
                            validRoomPositions.add(new Vector2(x, y));
                        }
                    }
                }
            }

            // Se sala não permite inimigos, remove posições para inimigos
            if (!roomAllowsEnemies) {
                // Remover posições desta sala para spawn de inimigos
                // (vamos lidar com isso depois, no loop de inimigos)
            }

            // Se sala não permite itens, remove posições para itens
            if (!roomAllowsItems) {
                // Remover posições desta sala para spawn de itens
                // (vamos lidar com isso depois, no loop de itens)
            }
        }

        java.util.Collections.shuffle(validRoomPositions, rand);

        // Spawn de armas/munição (respeitando configurações de sala)
        int itemsSpawned = 0;
        for (int i = 0; i < validRoomPositions.size() && itemsSpawned < 3; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Rectangle room = findRoomContainingTile(tilePos);

            if (room != null && roomAllowsItems(room)) {
                Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);

                if (rand.nextBoolean()) {
                    weapons.add(new Pistol(this, worldPos.x, worldPos.y, robertinhoo.getInventory()));
                    weapons.add(new Calibre12(this, worldPos.x + 1.2f, worldPos.y, robertinhoo.getInventory()));
                } else {
                    ammo.add(new Ammo9mm(this, worldPos.x, worldPos.y));
                }
                itemsSpawned++;
            }
        }

        // Spawn de ratos (respeitando configurações de sala)
        int ratsAdded = 0;
        for (int i = 0; i < validRoomPositions.size() && ratsAdded < 14; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Rectangle room = findRoomContainingTile(tilePos);

            if (room != null && roomAllowsEnemies(room)) {
                Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
                enemies.add(new Ratinho(this, worldPos.x, worldPos.y, robertinhoo, room));
                ratsAdded++;
            }
        }

        // Spawn de castores (respeitando configurações de sala)
        int castoresAdded = 0;
        for (int i = 0; i < validRoomPositions.size() && castoresAdded < 4; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Rectangle room = findRoomContainingTile(tilePos);

            if (room != null && roomAllowsEnemies(room)) {
                Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
                enemies.add(new Castor(this, worldPos.x, worldPos.y, robertinhoo));
                castoresAdded++;
            }
        }

        // Spawn de barris e grama (já ajustados para respeitar configurações)
        BarrelSpawner.spawnBarrels(this, 10);
        GrassSpawner.spawnGrass(this, 80);
    }

    private boolean roomAllowsEnemies(Rectangle room) {
        if (mapGenerator == null)
            return true;

        if (mapGenerator.isSpawnRoomTile((int) room.x, (int) room.y)) {
            FixedRoom spawnRoom = mapGenerator.getSpawnRoom();
            if (spawnRoom != null) {
                return spawnRoom.getConfiguration().hasEnemies();
            }
            return false;
        }
        return true; // Salas aleatórias têm inimigos
    }

    private boolean roomAllowsItems(Rectangle room) {
        if (mapGenerator == null)
            return true;

        if (mapGenerator.isSpawnRoomTile((int) room.x, (int) room.y)) {
            FixedRoom spawnRoom = mapGenerator.getSpawnRoom();
            if (spawnRoom != null) {
                return false;
            }
            return false;
        }
        return true;
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
        checkRoomTransition();

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

    private void checkRoomTransition() {
        if (isRoom0 && room0Door != null && room0Door.isPlayerInteracting()) {
            transitionToRoom1();
        }

    }

    public void disposeSafely() {
        System.out.println("🧹 [Mapa] disposeSafely iniciado");

        // 1. Limpar contact listener
        this.roomTransitionListener = null;

        // 2. Parar todos os sons
        AudioManager.getInstance().stopAllAmbientSounds();

        // 3. Destruir corpos de todos os itens ANTES de destruir o mundo
        System.out.println("💥 Destruindo corpos de todos os itens...");

        // CraftItems
        for (Item item : craftItems) {
            if (item.getBody() != null) {
                try {
                    item.destroyBody();
                } catch (Exception e) {
                    System.err.println("Erro ao destruir corpo de craftItem: " + e.getMessage());
                }
            }
        }

        // Weapons
        for (Weapon weapon : weapons) {
            if (weapon.getBody() != null) {
                try {
                    weapon.destroyBody();
                } catch (Exception e) {
                    System.err.println("Erro ao destruir corpo de weapon: " + e.getMessage());
                }
            }
        }

        // Ammo
        for (Ammo ammo : ammo) {
            if (ammo.getBody() != null) {
                try {
                    ammo.destroyBody();
                } catch (Exception e) {
                    System.err.println("Erro ao destruir corpo de ammo: " + e.getMessage());
                }
            }
        }

        // 4. Destruir RayHandler
        if (rayHandler != null) {
            try {
                rayHandler.dispose();
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao dispor rayHandler: " + e.getMessage());
            }
            rayHandler = null;
        }

        // 5. Destruir o World (isso vai destruir quaisquer corpos restantes)
        if (world != null) {
            System.out.println("💥 Destruindo World...");
            try {
                world.dispose();
                System.out.println("✅ World destruído");
            } catch (Exception e) {
                System.err.println("❌ ERRO ao dispor world: " + e.getMessage());
            }
            world = null;
        }

        // 6. Limpar todas as listas
        clearAllLists();

        System.out.println("✅ disposeSafely completo");
    }

    /**
     * Limpa todas as listas de forma segura
     */
    private void clearAllLists() {

        if (campFire != null) {
            campFire.dispose();
            campFire = null;
        }
        // Limpa enemies
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                // Remove referência ao body, mas não destrói aqui
                enemy.getBody().setUserData(null);
            }
            enemies.clear();
        }

        // Limpa weapons
        if (weapons != null) {
            for (Weapon weapon : weapons) {
                if (weapon.getBody() != null) {
                    weapon.getBody().setUserData(null);
                }
            }
            weapons.clear();
        }

        // Limpa ammo
        if (ammo != null) {
            for (Ammo ammoItem : ammo) {
                if (ammoItem.getBody() != null) {
                    ammoItem.getBody().setUserData(null);
                }
            }
            ammo.clear();
        }

        // Limpa projectiles
        if (projectiles != null) {
            for (Projectile projectile : projectiles) {
                if (projectile.getBody() != null) {
                    projectile.getBody().setUserData(null);
                }
            }
            projectiles.clear();
        }

        // Limpa destructibles
        if (destructibles != null) {
            for (Destructible destructible : destructibles) {
                if (destructible.getBody() != null) {
                    destructible.getBody().setUserData(null);
                }
            }
            destructibles.clear();
        }
        if (polvoras != null)
            polvoras.clear();
        if (craftItems != null)
            craftItems.clear();
        if (pendingActions != null)
            pendingActions.clear();
        if (rooms != null)
            rooms.clear();
        if (cabanas != null)
            cabanas.clear();
        if (staticItems != null)
            staticItems.clear();
        if (wallPositions != null)
            wallPositions.clear();
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

    private void addTestWeaponsToRoom0() {
        System.out.println("🔫 Adicionando armas de teste na Sala 0...");

        Vector2 pistolPos = tileToWorld(3, 3);
        Vector2 calibre12Pos = tileToWorld(6, 3);

        // Adicionar Pistol
        Pistol pistol = new Pistol(this, pistolPos.x, pistolPos.y, robertinhoo.getInventory());
        weapons.add(pistol);
        System.out.println("✅ Pistol adicionada em: " + pistolPos);

        // Adicionar Calibre12
        Calibre12 calibre12 = new Calibre12(this, calibre12Pos.x, calibre12Pos.y, robertinhoo.getInventory());
        weapons.add(calibre12);
        System.out.println("✅ Calibre12 adicionada em: " + calibre12Pos);

        // Adicionar munição
        Vector2 ammoPos1 = tileToWorld(3, 5);
        Vector2 ammoPos2 = tileToWorld(6, 5);

        Ammo9mm ammo1 = new Ammo9mm(this, ammoPos1.x, ammoPos1.y);
        Ammo9mm ammo2 = new Ammo9mm(this, ammoPos2.x, ammoPos2.y);

        ammo.add(ammo1);
        ammo.add(ammo2);
        System.out.println("✅ Munição 9mm adicionada para teste");
        // 🔥 TESTE: adicionar 4 Polvoras Reforçadas
        for (int i = 0; i < 4; i++) {
            Vector2 polvoraPos = tileToWorld(4 + i, 7);

            PolvoraReforcada polvora = new PolvoraReforcada(
                    this.world,
                    polvoraPos.x,
                    polvoraPos.y);

            polvora.createBody(polvoraPos);
            addCraftItem(polvora);

            System.out.println("🧪 Polvora Reforçada adicionada em: " + polvoraPos);
        }

        // 🔥 TESTE: adicionar 4 Polvoras Brutas
        for (int i = 0; i < 4; i++) {
            Vector2 polvoraPos = tileToWorld(4 + i, 8);

            PolvoraBruta polvora = new PolvoraBruta(
                    this.world,
                    polvoraPos.x,
                    polvoraPos.y);

            polvora.createBody(polvoraPos);
            addCraftItem(polvora);

            System.out.println("🧪 Polvora Bruta adicionada em: " + polvoraPos);
        }

    }

    private void addTestEnemiesToRoom0() {
        System.out.println("🐀🐿️ Adicionando inimigos de teste na Sala 0");

        // Define a área jogável da sala 0
        Rectangle room0Rect = new Rectangle(1, 1, mapWidth - 2, mapHeight - 2);

        // ========== RATOS ==========
        float[][] ratPositions = {
                { startPosition.x + 3, startPosition.y + 2 }, // Leste
                { startPosition.x - 3, startPosition.y - 2 }, // Oeste
                { startPosition.x + 2, startPosition.y - 3 }, // Sudeste
                { startPosition.x - 2, startPosition.y + 3 } // Noroeste
        };

        for (float[] pos : ratPositions) {
            int tileX = (int) pos[0];
            int tileY = (int) pos[1];

            if (isValidTile(tileX, tileY)) {
                Vector2 worldPos = tileToWorld(tileX, tileY);
                Ratinho rat = new Ratinho(this, worldPos.x, worldPos.y, robertinhoo, room0Rect);
                enemies.add(rat);
                System.out.println("✅ Rato adicionado em: (" + tileX + ", " + tileY + ")");
            }
        }

        // ========== CASTORES ==========
        float[][] castorPositions = {
                { startPosition.x + 5, startPosition.y + 1 }, // Mais distante leste
                { startPosition.x - 5, startPosition.y - 1 }, // Mais distante oeste
                { startPosition.x + 1, startPosition.y - 5 }, // Sul
                { startPosition.x - 1, startPosition.y + 5 } // Norte
        };

        for (float[] pos : castorPositions) {
            int tileX = (int) pos[0];
            int tileY = (int) pos[1];

            if (isValidTile(tileX, tileY)) {
                Vector2 worldPos = tileToWorld(tileX, tileY);
                Castor castor = new Castor(this, worldPos.x, worldPos.y, robertinhoo);
                enemies.add(castor);
                System.out.println("✅ Castor adicionado em: (" + tileX + ", " + tileY + ")");
            }
        }
    }

    // Método auxiliar para validar tiles (evita repetição)
    private boolean isValidTile(int tileX, int tileY) {
        return tileX > 0 && tileX < mapWidth - 1 &&
                tileY > 0 && tileY < mapHeight - 1 &&
                tiles[tileX][tileY] == TILE;
    }

    public BloodPoolSystem getBloodPoolSystem() {
        return bloodPoolSystem;
    }
}
