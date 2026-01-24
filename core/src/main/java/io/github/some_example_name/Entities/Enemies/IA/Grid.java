package io.github.some_example_name.Entities.Enemies.IA;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.MapConfig.Mapa;

public class Grid {
    private final int width, height;
    private final Node[][] nodes;
    private final Mapa mapa;

    public Grid(Mapa mapa, int tileSize) {
        this.width = mapa.mapWidth;
        this.height = mapa.mapHeight;
        this.nodes = new Node[width][height];
        this.mapa = mapa;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean blocked = mapa.isTileBlocked(x, y);
                nodes[x][y] = new Node(x, y, !blocked);

                // if (blocked) {
                // System.out.println("Tile bloqueado em: (" + x + ", " + y + ")");
                // }
            }
        }
        updateDynamicObstacles();
    }

    public Node getNode(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            return null;
        return nodes[x][y];
    }

    public List<Node> getNeighbors(Node node) {
        List<Node> neigh = new ArrayList<>();

        final int[][] deltas = {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
        };
        for (int[] d : deltas) {
            Node n = getNode(node.x + d[0], node.y + d[1]);
            if (n != null && n.walkable) {
                if (Math.abs(d[0]) + Math.abs(d[1]) == 2) {
                    Node n1 = getNode(node.x + d[0], node.y);
                    Node n2 = getNode(node.x, node.y + d[1]);
                    if (n1 == null || n2 == null || !n1.walkable || !n2.walkable)
                        continue;
                }
                neigh.add(n);
            }
        }
        return neigh;
    }

    public void updateDynamicObstacles() {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Só reseta tiles que não são permanentemente bloqueados
                if (!mapa.isTileBlocked(x, y)) {
                    nodes[x][y].walkable = true;
                }
            }
        }
        for (Destructible destructible : mapa.getDestructibles()) {
            if (destructible instanceof Barrel) {
                Barrel barrel = (Barrel) destructible;
                // IMPORTANTE: Só bloqueia se o barril não estiver destruído
                if (!barrel.isDestroyed()) {
                    Vector2 tilePos = mapa.worldToTile(barrel.getPosition());
                    int tileX = (int) tilePos.x;
                    int tileY = (int) tilePos.y;

                    if (isValidPosition(tileX, tileY)) {
                        nodes[tileX][tileY].walkable = false;
                        Gdx.app.log("Grid", "Barril bloqueando tile: (" + tileX + ", " + tileY + ")");
                    }
                }
            }
        }
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

}
