package io.github.some_example_name.MapConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Otimizations.MapBorderManager;
import io.github.some_example_name.Otimizations.WallOtimizations;
import io.github.some_example_name.MapConfig.Rooms.Room0LayoutLoader;
import io.github.some_example_name.MapConfig.Rooms.Items_sala_0.CampFire;
import io.github.some_example_name.MapConfig.Spawner.BarrelSpawner;
import io.github.some_example_name.MapConfig.Spawner.GrassSpawner;

public class Mapa {

    private List<Enemy> enemies;
    private List<Weapon> weapons;
    private List<Ammo> ammo;
    private List<Polvora> polvoras;
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Destructible> destructibles = new ArrayList<>();
    private List<Item> craftItems = new ArrayList<>();
    private List<Runnable> pendingActions = new ArrayList<>();
    private List<Rectangle> rooms = new ArrayList<>();
    public static final float BOX2D_SCALE = 1/64f;

    public PathfindingSystem pathfindingSystem;
    private CampFire campFire;

    public World world;
    public WallOtimizations agruparParedes;
    public MapGenerator mapGenerator;

    public static int TILE = 0x000000; // #000000 (tiles normais)
    static int START = 0xFF0000; // #FF0000 (ponto de início)
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

    public void setRayHandler(RayHandler rayHandler) {
        this.rayHandler = rayHandler;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public Mapa(boolean isRoom0) {
        // Inicializações básicas
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

        agruparParedes = new WallOtimizations(this);
        this.pathfindingSystem = new PathfindingSystem(this);
        this.cleanupManager = new MapCleanUpManager(this);

        initializeLights();

        if (isRoom0) {
            // Configuração específica para a Sala 0
            setupRoom0();
        } else {
            // Código normal do mapa procedural (seu código original)
            this.mapGenerator = new MapGenerator(50, 50);
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

        world.setContactListener(new GameContactListener(robertinhoo));
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
    
    // PRIMEIRO carrega o layout da imagem para definir o tamanho
    loadRoom0LayoutFromImage("sala_0/layoyt_sala_0.png");
    
    // DEPOIS cria o Robertinhoo
    Vector2 worldStartPos = tileToWorld((int)startPosition.x, (int)startPosition.y);
    robertinhoo = new Robertinhoo(this, worldStartPos.x, worldStartPos.y, null, null);
    
    // AGORA carrega os elementos específicos (fogueira) ANTES de criar o MapRenderer
    Room0LayoutLoader.loadRoom0Specifics(this, "sala_0/layoyt_sala_0.png");
    
    // FINALMENTE cria as paredes físicas
    agruparEPCriarParedes();
    
    System.out.println("Sala 0 criada - Tamanho: " + mapWidth + "x" + mapHeight);
}

private void loadRoom0LayoutFromImage(String imagePath) {
    try {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(imagePath));
        
        // Define o tamanho da sala pela imagem
        this.mapWidth = pixmap.getWidth();
        this.mapHeight = pixmap.getHeight();
        this.tiles = new int[mapWidth][mapHeight];
        
        System.out.println("Definindo tamanho da sala 0 pela imagem: " + mapWidth + "x" + mapHeight);
        
        // Preenche com chão por padrão
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                tiles[x][y] = TILE;
            }
        }
        
        // Adiciona paredes nas bordas (ou você pode definir isso na imagem também)
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
        // Fallback: tamanho fixo 10x10
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
                
                // CONFIGURAÇÕES MELHORADAS PARA MAIS CLAREZA
                rayHandler.setAmbientLight(0.2f, 0.2f, 0.2f, 0.8f); // MUITO MAIS CLARO
                rayHandler.setShadows(true);
                rayHandler.setBlur(true);
                rayHandler.setBlurNum(2);
                
                // Configurações de performance e qualidade
                RayHandler.useDiffuseLight(true);
                RayHandler.setGammaCorrection(true);
                
                lightsInitialized = true;
                System.out.println("✅ RayHandler inicializado - Ambiente: 0.6f");
            } catch (Exception e) {
                System.err.println("❌ Erro catastrófico no RayHandler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void addRandomEntities() {
        Random rand = new Random();
        List<Vector2> validRoomPositions = new ArrayList<>();
        for (Rectangle room : rooms) {
            for (int x = (int) room.x + 1; x < room.x + room.width - 1; x++) {
                for (int y = (int) room.y + 1; y < room.y + room.height - 1; y++) {
                    if (tiles[x][y] == TILE) {
                        if (x != (int) startPosition.x || y != (int) startPosition.y) {
                            validRoomPositions.add(new Vector2(x, y));
                        }
                    }
                }
            }
        }

        java.util.Collections.shuffle(validRoomPositions, rand);

        for (int i = 0; i < 3 && i < validRoomPositions.size(); i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);

            if (rand.nextBoolean()) {
                weapons.add(new Pistol(this, worldPos.x, worldPos.y, robertinhoo.getInventory()));
            } else {
                ammo.add(new Ammo9mm(this, worldPos.x, worldPos.y));
            }
        }

        int ratsAdded = 0;
        for (int i = 0; i < validRoomPositions.size() && ratsAdded < 14; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            Rectangle ratRoom = findRoomContainingTile(tilePos);
            enemies.add(new Ratinho(this, worldPos.x, worldPos.y, robertinhoo, ratRoom));
            ratsAdded++;
        }

        int castoresAdded = 0;
        for (int i = 8; i < validRoomPositions.size() && castoresAdded < 4; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            enemies.add(new Castor(this, worldPos.x, worldPos.y, robertinhoo));
            castoresAdded++;
        }

        BarrelSpawner.spawnBarrels(this, 10);
        GrassSpawner.spawnGrass(this, 80);

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
        craftItems.add(item);
    }

    public List<Item> getCraftItems() {
        return craftItems;
    }

    public void dispose() {
        if (rayHandler != null) {
            rayHandler.dispose();
        }
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
}
