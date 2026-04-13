// MapGenerator.java
package io.github.some_example_name.MapConfig.Generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.FixedRoom;
import io.github.some_example_name.MapConfig.Rooms.RoomConfiguration;
import io.github.some_example_name.MapConfig.Rooms.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenerator {
    public static final int MAX_SALA_WIDTH = 15;
    public static final int MIN_SALA_WIDTH = 10;
    public static final int MAX_SALA_HEIGHT = 15;
    public static final int MIN_SALA_HEIGHT = 10;
    public static final int TUNEL_WIDTH = 1;

    private int mapWidth;
    private int mapHeight;
    private int[][] tiles;
    private Vector2 startPosition;
    private ArrayList<Vector2> wallPositions = new ArrayList<>();
    private List<Rectangle> rooms = new ArrayList<>();
    private static final int MAX_TUNNEL_LEN = 12;
    private List<FixedRoom> fixedRooms = new ArrayList<>();
    private boolean hasStartRoom = false;
    private boolean hasTresaureRomm = false;
    private FixedRoom spawnRoom;
    private StartRoom startRoomInstance;
    private FixedRoom treasureRoom;
    private TreasureRoom treasureRoomInstance;

    public MapGenerator(int width, int height, boolean includeStartRoom, boolean includeTreasureRoom) {
        Gdx.app.log("MapGenerator", "Iniciando MapGenerator...");
        this.mapWidth = width;
        this.mapHeight = height;
        this.tiles = new int[mapWidth][mapHeight];

        if (includeStartRoom) {
            // Cria sala de spawn fixa SEM entidades
            RoomConfiguration spawnConfig = new RoomConfiguration.Builder(RoomType.SPAWN)
                    .hasEnemies(false)
                    .hasBarrels(false)
                    .hasChests(false)
                    .build();

            spawnRoom = new FixedRoom(spawnConfig);
            fixedRooms.add(spawnRoom);
            this.hasStartRoom = true;

        }

        if (includeTreasureRoom) {
            RoomConfiguration treasureConfig = new RoomConfiguration.Builder(RoomType.TREASURE)
                    .hasEnemies(false) // por enquanto sem inimigos
                    .hasBarrels(false)
                    .hasChests(true) // indica que tem baú
                    .build();
            treasureRoom = new FixedRoom(treasureConfig);
            fixedRooms.add(treasureRoom);
            this.hasTresaureRomm = true;
        }
        generateRandomMap();
        Gdx.app.log("MapGenerator", "Mapa gerado com sucesso.");
    }

    private void generateRandomMap() {
        Gdx.app.log("MapGenerator", "Iniciando geração aleatória do mapa...");

        // Inicializa tudo como parede
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                tiles[x][y] = Mapa.PAREDE;
            }
        }
        Gdx.app.log("MapGenerator", "Mapa inicializado com paredes.");

        Random rand = new Random();
        int numSalas = 3 + rand.nextInt(3); // 3-5 salas aleatórias
        rooms.clear();

        if (hasStartRoom && spawnRoom != null) {
            Rectangle spawnRect = placeStartRoomInMap();

            // ✅ ALTERAÇÃO CRÍTICA: Carrega os tiles da StartRoom
            StartRoom startRoom = new StartRoom();
            this.startRoomInstance = startRoom;
            int[][] spawnTiles = startRoom.getTiles(); // Pega os tiles da imagem

            // ✅ Usa os tiles corretos da StartRoom
            drawRoom(spawnRect, spawnTiles); // Passe os tiles reais da imagem
            rooms.add(spawnRect);
            spawnRoom.setBounds(spawnRect);

            // ✅ Usa a posição de spawn da StartRoom
            Vector2 relativeStartPos = startRoom.getStartPosition(); // Da StartRoom, não da FixedRoom
            startPosition = new Vector2(
                    spawnRect.x + relativeStartPos.x,
                    spawnRect.y + relativeStartPos.y);

            Gdx.app.log("MapGenerator", "✅ Sala SPAWN posicionada em: " + spawnRect);
            Gdx.app.log("MapGenerator", "✅ Posição inicial: " + startPosition);

            // ✅ DEBUG: Verifique se as paredes foram colocadas
            debugSpawnRoomTiles(spawnRect);
        }

        if (hasTresaureRomm && treasureRoom != null) {
            TreasureRoom tRoom = new TreasureRoom();
            this.treasureRoomInstance = tRoom;
            Rectangle treasureRect = placeTreasureRoomInMap();
            drawRoom(treasureRect, tRoom.getTiles());
            rooms.add(treasureRect);
            treasureRoom.setBounds(treasureRect);
            startPosition = new Vector2(
                    treasureRect.x + treasureRect.width / 2f,
                    treasureRect.y + treasureRect.height / 2f);

            Gdx.app.log("MapGenerator", "✅ Sala TREASURE posicionada em: " + treasureRect);
            Gdx.app.log("MapGenerator", "🎯 Nova posição inicial (debug): " + startPosition);
        }

        // 2. DEPOIS: Cria salas aleatórias normais
        for (int i = 0; i < numSalas; i++) {
            Rectangle novaSala;
            int attempts = 0;
            boolean valido;

            do {
                novaSala = generateRandomRoom(rand);
                attempts++;
                valido = true;

                // Verifica se não sobrepõe salas existentes
                for (Rectangle r : rooms) {
                    if (novaSala.overlaps(r)) {
                        valido = false;
                        break;
                    }
                }

                if (attempts > 50) {
                    Gdx.app.error("MapGenerator", "Não conseguiu posicionar sala " + i);
                    valido = false;
                    break;
                }
            } while (!valido);

            if (valido) {
                drawRoom(novaSala);
                rooms.add(novaSala);
                Gdx.app.log("MapGenerator", "Sala aleatória " + i + " criada: " + novaSala);
            }
        }

        // 3. Conecta TODAS as salas (incluindo a de spawn)
        for (int i = 0; i < rooms.size() - 1; i++) {
            connectRooms(rooms.get(i), rooms.get(i + 1), rand);
        }

        collectWallPositions();
        Gdx.app.log("MapGenerator", "Mapa gerado com " + rooms.size() + " salas.");
    }

    private Rectangle generateRandomRoom(Random rand) {
        int width = rand.nextInt(MAX_SALA_WIDTH - MIN_SALA_WIDTH) + MIN_SALA_WIDTH;
        int height = rand.nextInt(MAX_SALA_HEIGHT - MIN_SALA_HEIGHT) + MIN_SALA_HEIGHT;
        int x = rand.nextInt(mapWidth - width - 2) + 1;
        int y = rand.nextInt(mapHeight - height - 2) + 1;
        return new Rectangle(x, y, width, height);
    }

    // ✅ NOVO: Desenha sala com tiles específicos
    private void drawRoom(Rectangle room, int[][] roomTiles) {
        int roomWidth = roomTiles.length;
        int roomHeight = roomTiles[0].length;

        for (int rx = 0; rx < roomWidth; rx++) {
            for (int ry = 0; ry < roomHeight; ry++) {
                int worldX = (int) room.x + rx;
                int worldY = (int) room.y + ry;

                if (worldX >= 0 && worldX < mapWidth && worldY >= 0 && worldY < mapHeight) {
                    tiles[worldX][worldY] = roomTiles[rx][ry];
                }
            }
        }
    }

    private void drawRoom(Rectangle room) {
        for (int x = (int) room.x; x < room.x + room.width; x++) {
            for (int y = (int) room.y; y < room.y + room.height; y++) {
                // Deixa espaço para paredes externas
                if (x == room.x || x == room.x + room.width - 1 ||
                        y == room.y || y == room.y + room.height - 1) {
                    tiles[x][y] = Mapa.PAREDE;
                } else {
                    tiles[x][y] = Mapa.TILE;
                }
            }
        }
    }

    private void connectRooms(Rectangle sala1, Rectangle sala2, Random rand) {
        // pega os pontos de borda mais próximos (em tiles)
        Vector2 p1 = getClosestEdgePoint(sala1, sala2);
        Vector2 p2 = getClosestEdgePoint(sala2, sala1);

        // conectar os pontos com possibilidade de subdividir se muito longo
        connectPointsWithSplits((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y, rand);
    }

    private Vector2 getClosestEdgePoint(Rectangle from, Rectangle to) {
        float cxTo = to.x + to.width / 2f;
        float cyTo = to.y + to.height / 2f;

        // considera quatro opções: centro de cada borda (top, bottom, left, right)
        Vector2 top = new Vector2(from.x + from.width / 2f, from.y + from.height - 1);
        Vector2 bottom = new Vector2(from.x + from.width / 2f, from.y + 0);
        Vector2 left = new Vector2(from.x + 0, from.y + from.height / 2f);
        Vector2 right = new Vector2(from.x + from.width - 1, from.y + from.height / 2f);

        Vector2[] candidates = new Vector2[] { top, bottom, left, right };
        Vector2 best = candidates[0];
        float bestDist = best.dst2(cxTo, cyTo);
        for (Vector2 c : candidates) {
            float d = c.dst2(cxTo, cyTo);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        // converte para tile int (center of edge tile)
        return new Vector2(Math.round(best.x), Math.round(best.y));
    }

    private void connectPointsWithSplits(int x1, int y1, int x2, int y2, Random rand) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int maxDist = Math.max(dx, dy);

        if (maxDist <= MAX_TUNNEL_LEN) {
            // cria túnel direto L-shaped com pequena aleatoriedade na ordem
            if (rand.nextBoolean()) {
                createHorizontalTunnel(x1, x2, y1);
                createVerticalTunnel(y1, y2, x2);
            } else {
                createVerticalTunnel(y1, y2, x1);
                createHorizontalTunnel(x1, x2, y2);
            }
        } else {
            // split: criar ponto no meio com jitter, e conectar recursivamente
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;

            // jitter para evitar linha reta perfeita (entre -MAX_TUNNEL_LEN/4 .. +..)
            int jitterRange = Math.max(1, MAX_TUNNEL_LEN / 4);
            int jitterX = rand.nextInt(jitterRange * 2 + 1) - jitterRange;
            int jitterY = rand.nextInt(jitterRange * 2 + 1) - jitterRange;

            int mx = Math.max(1, Math.min(mapWidth - 2, midX + jitterX));
            int my = Math.max(1, Math.min(mapHeight - 2, midY + jitterY));

            // opcional: cria uma "mini-sala" no ponto médio para quebrar túnel longo
            createMiniRoomIfNeeded(mx, my, rand);

            // conectar recursivamente
            connectPointsWithSplits(x1, y1, mx, my, rand);
            connectPointsWithSplits(mx, my, x2, y2, rand);
        }
    }

    private void createMiniRoomIfNeeded(int cx, int cy, Random rand) {
        int roomW = rand.nextInt(3) + 2; // 2..4
        int roomH = rand.nextInt(3) + 2;
        int x = cx - roomW / 2;
        int y = cy - roomH / 2;

        // garante dentro do mapa
        x = Math.max(1, Math.min(mapWidth - roomW - 1, x));
        y = Math.max(1, Math.min(mapHeight - roomH - 1, y));

        Rectangle mini = new Rectangle(x, y, roomW, roomH);
        drawRoom(mini);
        rooms.add(mini);
    }

    /** Substitui e amplia os túneis horizontais para largura TUNEL_WIDTH real. */
    private void createHorizontalTunnel(int xStart, int xEnd, int y) {
        int start = Math.min(xStart, xEnd);
        int end = Math.max(xStart, xEnd);
        int half = TUNEL_WIDTH / 2;

        for (int x = start; x <= end; x++) {
            for (int dy = -half; dy <= half; dy++) {
                int yy = y + dy;
                if (x >= 0 && x < mapWidth && yy >= 0 && yy < mapHeight) {
                    tiles[x][yy] = Mapa.TILE;
                }
            }
        }
    }

    /** Substitui e amplia os túneis verticais para largura TUNEL_WIDTH real. */
    private void createVerticalTunnel(int yStart, int yEnd, int x) {
        int start = Math.min(yStart, yEnd);
        int end = Math.max(yStart, yEnd);
        int half = TUNEL_WIDTH / 2;

        for (int y = start; y <= end; y++) {
            for (int dx = -half; dx <= half; dx++) {
                int xx = x + dx;
                if (xx >= 0 && xx < mapWidth && y >= 0 && y < mapHeight) {
                    tiles[xx][y] = Mapa.TILE;
                }
            }
        }
    }

    private void collectWallPositions() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (tiles[x][y] == Mapa.PAREDE) {
                    wallPositions.add(new Vector2(x, y));
                }
            }
        }
    }

    public int[][] getTiles() {
        return tiles;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public Vector2 getWorldStartPosition(int mapHeight) {
        return new Vector2(
                startPosition.x + 0.5f,
                mapHeight - 1 - startPosition.y + 0.5f);
    }

    public List<Vector2> generateFixedPatrolRoute(PathfindingSystem pathfindingSystem) {
        List<Vector2> route = new ArrayList<>();

        if (rooms.isEmpty()) {
            return route;
        }

        // Ordenar salas para criar uma rota lógica (usando centro das salas)
        List<Vector2> roomCenters = new ArrayList<>();
        for (Rectangle room : rooms) {
            Vector2 center = new Vector2(room.x + room.width / 2, room.y + room.height / 2);
            roomCenters.add(center);
        }

        // Ordenar salas pela proximidade (algoritmo do vizinho mais próximo)
        List<Vector2> sortedCenters = new ArrayList<>();
        Vector2 current = roomCenters.remove(0);
        sortedCenters.add(current);

        while (!roomCenters.isEmpty()) {
            Vector2 nearest = null;
            float nearestDist = Float.MAX_VALUE;

            for (Vector2 center : roomCenters) {
                float dist = current.dst2(center);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = center;
                }
            }

            if (nearest != null) {
                roomCenters.remove(nearest);
                sortedCenters.add(nearest);
                current = nearest;
            }
        }

        // Converter para coordenadas mundiais e conectar com pathfinding
        List<Vector2> worldCenters = new ArrayList<>();
        for (Vector2 center : sortedCenters) {
            worldCenters.add(tileToWorld((int) center.x, (int) center.y));
        }

        // Conectar todos os pontos em um loop contínuo
        for (int i = 0; i < worldCenters.size(); i++) {
            Vector2 start = worldCenters.get(i);
            Vector2 end = worldCenters.get((i + 1) % worldCenters.size());

            List<Vector2> pathSegment = pathfindingSystem.findPath(start, end);
            if (pathSegment != null && !pathSegment.isEmpty()) {
                // Suavizar transições entre segmentos
                if (!route.isEmpty()) {
                    Vector2 lastPoint = route.get(route.size() - 1);
                    if (lastPoint.dst2(pathSegment.get(0)) > 0.1f) {
                        route.addAll(pathfindingSystem.findPath(lastPoint, pathSegment.get(0)));
                    }
                }
                route.addAll(pathSegment);
            }
        }

        // Fechar o loop conectando último ponto ao primeiro
        if (!route.isEmpty()) {
            Vector2 lastPoint = route.get(route.size() - 1);
            Vector2 firstPoint = route.get(0);
            if (lastPoint.dst2(firstPoint) > 0.1f) {
                route.addAll(pathfindingSystem.findPath(lastPoint, firstPoint));
            }
        }

        return route;
    }

    public boolean isSpawnRoomTile(int x, int y) {
        if (spawnRoom == null || spawnRoom.getBounds() == null) {
            return false;
        }
        Rectangle bounds = spawnRoom.getBounds();
        return x >= bounds.x && x < bounds.x + bounds.width &&
                y >= bounds.y && y < bounds.y + bounds.height;
    }

    public FixedRoom getSpawnRoom() {
        return spawnRoom;
    }

    public Vector2 tileToWorld(int tileX, int tileY) {
        return new Vector2(tileX + 0.5f, tileY + 0.5f);
    }

    public List<Vector2> getFixedPatrolRoute(PathfindingSystem pathfindingSystem) {
        return generateFixedPatrolRoute(pathfindingSystem);
    }

    public ArrayList<Vector2> getWallPositions() {
        return wallPositions;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getStartX() {
        return (int) startPosition.x;
    }

    public int getStartY() {
        return (int) startPosition.y;
    }

    public List<Rectangle> getRooms() {
        return rooms;
    }

    private Rectangle placeTreasureRoomInMap() {
        // Posiciona no canto superior direito como exemplo
        int x = mapWidth - treasureRoom.getWidth() - 5;
        int y = mapHeight - treasureRoom.getHeight() - 5;
        return new Rectangle(x, y, treasureRoom.getWidth(), treasureRoom.getHeight());
    }

    // Getters para uso externo
    public TreasureRoom getTreasureRoomInstance() {
        return treasureRoomInstance;
    }

    public FixedRoom getTreasureFixedRoom() {
        return treasureRoom;
    }

    private void debugSpawnRoomTiles(Rectangle spawnRect) {
        System.out.println("=== DEBUG SPAWN ROOM TILES ===");
        for (int y = (int) spawnRect.y + (int) spawnRect.height - 1; y >= spawnRect.y; y--) {
            for (int x = (int) spawnRect.x; x < spawnRect.x + spawnRect.width; x++) {
                System.out.print(tiles[x][y] + " ");
            }
            System.out.println();
        }
        System.out.println("=== END DEBUG ===");
    }

    private Rectangle placeStartRoomInMap() {
        // Posiciona a StartRoom em uma posição fixa (canto inferior esquerdo)
        int x = 5;
        int y = 5;

        // Usa o tamanho REAL da StartRoom
        int width = spawnRoom.getWidth(); // 16
        int height = spawnRoom.getHeight(); // 16

        return new Rectangle(x, y, width, height);
    }

    public FixedRoom getFixedRoomAt(int tileX, int tileY) {
        for (FixedRoom fr : fixedRooms) {
            Rectangle bounds = fr.getBounds();
            if (bounds != null && bounds.contains(tileX, tileY)) {
                return fr;
            }
        }
        return null;
    }

    public List<FixedRoom> getFixedRooms() {
        return fixedRooms;
    }

    public StartRoom getStartRoomInstance() {
        return startRoomInstance;
    }

}