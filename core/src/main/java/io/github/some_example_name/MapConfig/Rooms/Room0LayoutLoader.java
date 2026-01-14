package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.Items_sala_0.CampFire;

import io.github.some_example_name.Entities.Itens.CenarioItens.Room0Flower;
import io.github.some_example_name.Entities.Itens.CenarioItens.Room0Grass;

public class Room0LayoutLoader {
    // Cores para identificar elementos no layout
    public static final Color COLOR_CAMPFIRE = new Color(0x99e550ff);
    public static final Color COLOR_PLAYER_SPAWN = new Color(0xff0000ff); // Mantemos por compatibilidade
    public static final Color COLOR_GRASS = new Color(0xdeff00ff); // Grama - #deff00
    public static final Color COLOR_FLOWER = new Color(0xc800ffff); // Flor - #c800ff
    public static final Color COLOR_RUSTY_SWORD = new Color(0xf6a4a4ff); // espada - #f6a4a4
    public static final Color COLOR_DOOR = new Color(0xffffffff); // porta - #ffffffff

    // Novas cores para cabanas
    public static final Color COLOR_CABANA_PLAYER = new Color(0x351e1eff);// #351e1e

    public static void loadRoom0Specifics(Mapa mapa, String imagePath) {
        try {
            Pixmap pixmap = new Pixmap(Gdx.files.internal(imagePath));

            int layoutWidth = pixmap.getWidth();
            int layoutHeight = pixmap.getHeight();

            System.out.println("Layout da sala 0: " + layoutWidth + "x" + layoutHeight + " pixels");
            System.out.println("Tamanho da sala: " + mapa.mapWidth + "x" + mapa.mapHeight + " tiles");

            Vector2 playerSpawnPos = null;
            Vector2 playerCabanaPos = null;

            for (int x = 0; x < layoutWidth; x++) {
                for (int y = 0; y < layoutHeight; y++) {
                    Color pixelColor = new Color();
                    Color.rgba8888ToColor(pixelColor, pixmap.getPixel(x, y));

                    if (colorsMatch(pixelColor, COLOR_PLAYER_SPAWN)) {
                        playerSpawnPos = new Vector2(x, y);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out
                                .println("🎯 Spawn tradicional em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                        + ") -> mundo: " + worldPos);
                    } else if (colorsMatch(pixelColor, COLOR_CAMPFIRE)) {
                        addCampfire(mapa, x, y);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out.println("🔥 Fogueira em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                + ") -> mundo: " + worldPos);
                    } else if (colorsMatch(pixelColor, COLOR_GRASS)) {
                        addGrass(mapa, x, y);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out.println("🌿 Grama em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                + ") -> mundo: " + worldPos);
                    } else if (colorsMatch(pixelColor, COLOR_FLOWER)) {
                        addFlower(mapa, x, y);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out.println("🌸 Flor em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                + ") -> mundo: " + worldPos);
                    } else if (colorsMatch(pixelColor, COLOR_CABANA_PLAYER)) {
                        addCabana(mapa, x, y, Room0Cabana.CabanaType.PLAYER_HOUSE);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out.println("🏠 Cabana do Player em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                + ") -> mundo: " + worldPos);

                        playerCabanaPos = new Vector2(x, y + 1F);
                        System.out.println("📍 Cabana definida como spawn point: " + x + ", " + y + 1F);
                    } else if (colorsMatch(pixelColor, COLOR_RUSTY_SWORD)) {
                        addStaticItem(mapa, x, y, StaticItem.ItemType.RUSTY_SWORD);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out
                                .println("⚔️ Espada enferrujada em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                        + ") -> mundo: " + worldPos);
                    } else if (colorsMatch(pixelColor, COLOR_DOOR)) {
                        addDoor(mapa, x, y);
                        Vector2 worldPos = mapa.tileToWorld(x, y);
                        System.out.println("🚪 Porta em pixel: " + x + "," + y + " -> tile: (" + x + ", " + y
                                + ") -> mundo: " + worldPos);
                    }

                }
            }

            // ✅ PRIORIDADE: Spawn na cabana tem precedência sobre spawn tradicional
            Vector2 finalSpawnPos = null;
            if (playerCabanaPos != null) {
                finalSpawnPos = playerCabanaPos;
                System.out.println("✅ Spawn definido na CABANA do player");
            } else if (playerSpawnPos != null) {
                finalSpawnPos = playerSpawnPos;
                System.out.println("⚠️ Spawn definido na POSIÇÃO TRADICIONAL (cabana não encontrada)");
            } else {
                // Fallback: spawn no centro do mapa
                finalSpawnPos = new Vector2(mapa.mapWidth / 2, mapa.mapHeight / 2);
                System.out.println("⚠️ Spawn FALLBACK no centro: " + finalSpawnPos);
            }

            // Posiciona o player
            if (finalSpawnPos != null && mapa.robertinhoo != null) {
                Vector2 worldSpawnPos = mapa.tileToWorld((int) finalSpawnPos.x, (int) finalSpawnPos.y);
                mapa.robertinhoo.body.setTransform(worldSpawnPos, 0);
                mapa.robertinhoo.pos.set(finalSpawnPos);
                System.out.println("✅ Player posicionado em: " + worldSpawnPos +
                        " | Tile: " + finalSpawnPos);
            }

            pixmap.dispose();
            System.out.println("✅ Elementos carregados!");

        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean colorsMatch(Color color1, Color color2) {
        return Math.abs(color1.r - color2.r) < 0.1f &&
                Math.abs(color1.g - color2.g) < 0.1f &&
                Math.abs(color1.b - color2.b) < 0.1f;
    }

    private static void addCampfire(Mapa mapa, int tileX, int tileY) {
        CampFire campfire = new CampFire(mapa, tileX, tileY);
        mapa.setCampFire(campfire);
        System.out.println("📍 Fogueira criada em tile: " + tileX + ", " + tileY);
    }

    private static void addGrass(Mapa mapa, int tileX, int tileY) {
        try {
            Vector2 worldPos = mapa.tileToWorld(tileX, tileY);
            Room0Grass grass = new Room0Grass(mapa, worldPos.x, worldPos.y);
            mapa.getDestructibles().add(grass);
            System.out.println("🌿 Grama (Sala0) criada em tile: " + tileX + ", " + tileY);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar grama da sala 0: " + e.getMessage());
        }
    }

    private static void addFlower(Mapa mapa, int tileX, int tileY) {
        try {
            Vector2 worldPos = mapa.tileToWorld(tileX, tileY);
            Room0Flower flower = new Room0Flower(mapa, worldPos.x, worldPos.y);
            mapa.getDestructibles().add(flower);
            System.out.println("🌸 Flor (Sala0) criada em tile: " + tileX + ", " + tileY);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar flor da sala 0: " + e.getMessage());
        }
    }

    private static void addCabana(Mapa mapa, int tileX, int tileY, Room0Cabana.CabanaType type) {
        try {
            Room0Cabana cabana = new Room0Cabana(mapa, tileX, tileY, type);
            mapa.addCabana(cabana);
            System.out.println("🏠 Cabana adicionada ao mapa - Tipo: " + type + " em tile: " + tileX + ", " + tileY);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar cabana: " + e.getMessage());
        }
    }

    private static void addStaticItem(Mapa mapa, int tileX, int tileY, StaticItem.ItemType type) {
        try {
            StaticItem staticItem = new StaticItem(mapa, tileX, tileY, type);
            mapa.addStaticItem(staticItem);
            System.out.println("✅ Item estático adicionado: " + type + " em tile: " + tileX + ", " + tileY);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar item estático: " + e.getMessage());
        }
    }

    private static void addDoor(Mapa mapa, int tileX, int tileY) {
        try {
            Room0Door door = new Room0Door(mapa, tileX, tileY);
            
            mapa.addDoor(door);
            System.out.println("🚪 Porta criada em tile: " + tileX + ", " + tileY);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar porta: " + e.getMessage());
        }
    }
}