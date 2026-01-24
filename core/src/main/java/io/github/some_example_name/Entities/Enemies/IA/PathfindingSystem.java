package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;
import java.util.List;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;

public class PathfindingSystem {
    private Grid grid;
    private final AStarPathFinder pathFinder;
    private final Mapa mapa;
    private final int tileSize = 64;
    private long lastGridUpdate = 0;
    private static final long MIN_UPDATE_INTERVAL = 2000;

    // Cache para verificar mudanças reais
    private int lastDestructiblesCount = 0;
    private String lastBarrelsHash = "";

    public PathfindingSystem(Mapa mapa) {
        this.mapa = mapa;
        this.grid = new Grid(mapa, tileSize); // INICIALIZA O GRID DIRETAMENTE
        this.pathFinder = new AStarPathFinder(this::getGrid, mapa);
        updateChangeCache(); // Inicializa o cache
    }

    private Grid getGrid() {
        long currentTime = System.currentTimeMillis();

        // CORREÇÃO: Sempre retorna um grid válido
        if (grid == null) {
            grid = createNewGrid();
            lastGridUpdate = currentTime;
            return grid;
        }

        // Só verifica atualizações se passou o intervalo mínimo
        if ((currentTime - lastGridUpdate) > MIN_UPDATE_INTERVAL) {
            if (hasObstaclesReallyChanged()) {
                grid = createNewGrid();
                lastGridUpdate = currentTime;
                updateChangeCache();
            } else {
                // Atualiza o tempo mesmo sem mudanças para não verificar todo frame
                lastGridUpdate = currentTime;
            }
        }

        return grid;
    }

    private Grid createNewGrid() {
        System.out.println("🛠️ Inicializando/Atualizando Grid em PathfindingSystem…");
        return new Grid(mapa, tileSize);
    }

    private boolean hasObstaclesReallyChanged() {
        // Verifica se o mapa está disponível
        if (mapa == null || mapa.getDestructibles() == null) {
            return false;
        }

        // Método 1: Verifica contagem de destructibles
        int currentCount = mapa.getDestructibles().size();
        if (currentCount != lastDestructiblesCount) {
            return true;
        }

        // Método 2: Verifica hash dos barris
        String currentHash = generateBarrelsHash();
        return !currentHash.equals(lastBarrelsHash);
    }

    private String generateBarrelsHash() {
        if (mapa == null || mapa.getDestructibles() == null) {
            return "";
        }

        StringBuilder hashBuilder = new StringBuilder();
        for (Destructible destructible : mapa.getDestructibles()) {
            if (destructible instanceof Barrel) {
                Barrel barrel = (Barrel) destructible;
                if (!barrel.isDestroyed()) {
                    Vector2 pos = barrel.getPosition();
                    hashBuilder.append(String.format("%.1f,%.1f|", pos.x, pos.y));
                }
            }
        }
        return hashBuilder.toString();
    }

    private void updateChangeCache() {
        if (mapa != null && mapa.getDestructibles() != null) {
            lastDestructiblesCount = mapa.getDestructibles().size();
            lastBarrelsHash = generateBarrelsHash();
        }
    }

    public List<Vector2> findPath(Vector2 start, Vector2 end) {
        // CORREÇÃO: Garante que temos um grid antes de buscar caminho
        if (grid == null) {
            grid = getGrid();
        }
        return pathFinder.findPath(start, end, tileSize);
    }

    public void updateGrid() {
        this.grid = null; // Força recriação na próxima chamada
    }

    public void forceGridUpdate() {
        this.grid = null;
        updateChangeCache();
    }
}