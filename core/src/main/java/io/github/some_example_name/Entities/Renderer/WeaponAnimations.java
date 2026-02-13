package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import com.badlogic.gdx.Gdx;

public class WeaponAnimations implements Disposable {

    public enum WeaponState {
        IDLE, SHOOTING, RELOADING
    }

    public enum WeaponDirection {
        E, NE, N, NW, W, SW, S, SE
    }

    private final Animation<TextureRegion>[][] animations;
    private final Texture[] loadedTextures;
    private final WeaponType weaponType;
    private Texture shotgunReloadSheet; // Variável separada para o spritesheet de reload da shotgun

    public WeaponAnimations(String weaponTypeStr) {
        // Determinar tipo de arma
        if (weaponTypeStr.equals("Pistol")) {
            this.weaponType = WeaponType.PISTOL;
        } else if (weaponTypeStr.equals("Calibre12")) {
            this.weaponType = WeaponType.SHOTGUN;
        } else {
            this.weaponType = WeaponType.PISTOL; // Padrão
        }

        animations = new Animation[WeaponDirection.values().length][Weapon.WeaponState.values().length];
        loadedTextures = loadWeaponTextures(weaponTypeStr);
    }

    private Texture[] loadWeaponTextures(String weaponTypeStr) {
        java.util.List<Texture> texturesList = new java.util.ArrayList<>();

        if (weaponTypeStr.equals("Pistol")) {
            // Pistol: 1 arquivo, 8 linhas × 6 colunas
            loadMultiRowAnimation(texturesList, "ITENS/Pistol/Pistol_shoot_idle.png",
                    WeaponType.PISTOL, 8, 6);

        } else if (weaponTypeStr.equals("Calibre12")) {
            // Primeiro carregar o spritesheet de reload para shotgun
            shotgunReloadSheet = new Texture(Gdx.files.internal("ITENS/12/RELOAD_SHEET.png"));
            texturesList.add(shotgunReloadSheet);
            System.out.println("✅ [WeaponAnimations] Spritesheet de reload da shotgun carregado");

            // Depois carregar o spritesheet normal
            loadMultiRowAnimation(texturesList, "ITENS/12/12_shoot.png",
                    WeaponType.SHOTGUN, 6, 11);
        }

        return texturesList.toArray(new Texture[0]);
    }

    // Tipos de arma para configuração
    private enum WeaponType {
        PISTOL(8, 6, 0.5f / 6.0f, 0.1f, false),
        SHOTGUN(6, 11, 1.0f / 11.0f, 0.15f, true);

        public final int rows;
        public final int cols;
        public final float shootSpeed;
        public final float reloadSpeed;
        public final boolean usesFlipForMissingDiagonals;

        WeaponType(int rows, int cols, float shootSpeed, float reloadSpeed, boolean usesFlip) {
            this.rows = rows;
            this.cols = cols;
            this.shootSpeed = shootSpeed;
            this.reloadSpeed = reloadSpeed;
            this.usesFlipForMissingDiagonals = usesFlip;
        }
    }

    private void loadMultiRowAnimation(java.util.List<Texture> texturesList, String path,
            WeaponType type, int rows, int cols) {
        try {
            Texture sheet = new Texture(Gdx.files.internal(path));
            int frameWidth = sheet.getWidth() / cols;
            int frameHeight = sheet.getHeight() / rows;

            WeaponDirection[] rowDirections = getRowDirectionsForWeapon(type);

            if (rowDirections.length != rows) {
                return;
            }

            for (int i = 0; i < rowDirections.length; i++) {
                System.out.println("   Linha " + i + " -> " + rowDirections[i]);
            }

            // Para cada linha (direção), criar animações
            for (int row = 0; row < rows; row++) {
                WeaponDirection direction = rowDirections[row];

                // IDLE: apenas o primeiro frame
                TextureRegion[] idleFrames = new TextureRegion[1];

                // PARA A SHOTGUN (Calibre12), usar o frame 0 do spritesheet de reload APENAS
                // para a direção SUL (S)
                if (type == WeaponType.SHOTGUN && shotgunReloadSheet != null && direction == WeaponDirection.S) {
                    // Usar o primeiro frame do reload sheet (linha 0, coluna 0) APENAS para direção
                    // SUL
                    int reloadRows = 2;
                    int reloadCols = 5;
                    int reloadFrameWidth = shotgunReloadSheet.getWidth() / reloadCols;
                    int reloadFrameHeight = shotgunReloadSheet.getHeight() / reloadRows;

                    idleFrames[0] = new TextureRegion(shotgunReloadSheet, 0, 0, reloadFrameWidth, reloadFrameHeight);
                    System.out.println(
                            "🔫 [WeaponAnimations] Usando frame de reload para idle da shotgun APENAS na direção: "
                                    + direction);
                } else {
                    // Para outras direções ou outras armas, usar o primeiro frame do spritesheet
                    // normal
                    idleFrames[0] = new TextureRegion(sheet, 0, row * frameHeight, frameWidth, frameHeight);
                    if (type == WeaponType.SHOTGUN && direction != WeaponDirection.S) {
                        System.out.println("🎯 [WeaponAnimations] Direção " + direction
                                + " usando frame normal do spritesheet de shoot");
                    }
                }

                animations[direction.ordinal()][WeaponState.IDLE.ordinal()] = new Animation<>(0.2f, idleFrames);

                // SHOOTING: todos os frames do spritesheet normal (para todas as direções)
                TextureRegion[] shootFrames = new TextureRegion[cols];
                for (int col = 0; col < cols; col++) {
                    shootFrames[col] = new TextureRegion(sheet,
                            col * frameWidth,
                            row * frameHeight,
                            frameWidth,
                            frameHeight);
                }
                animations[direction.ordinal()][WeaponState.SHOOTING.ordinal()] = new Animation<>(type.shootSpeed,
                        shootFrames);
            }

            // Carregar animação de recarga se existir
            loadReloadAnimation(texturesList, type, sheet);

            texturesList.add(sheet);

        } catch (Exception e) {
            Gdx.app.error("WeaponAnimations", "Erro ao carregar " + path, e);
        }
    }

    private WeaponDirection[] getRowDirectionsForWeapon(WeaponType type) {
        switch (type) {
            case PISTOL:
                // Ordem das linhas na pistola: E, W, N, S, NE, NW, SE, SW
                return new WeaponDirection[] {
                        WeaponDirection.E, // linha 0
                        WeaponDirection.W, // linha 1
                        WeaponDirection.N, // linha 2
                        WeaponDirection.S, // linha 3
                        WeaponDirection.NE, // linha 4
                        WeaponDirection.NW, // linha 5
                        WeaponDirection.SE, // linha 6
                        WeaponDirection.SW // linha 7
                };

            case SHOTGUN:
                // Ordem das linhas na shotgun: S, W, E, N, NE, SE
                return new WeaponDirection[] {
                        WeaponDirection.S, // linha 0 (Sul) - IMPORTANTE: esta é a direção que usará o frame de reload
                        WeaponDirection.W, // linha 1 (Oeste)
                        WeaponDirection.E, // linha 2 (Leste)
                        WeaponDirection.NE, // linha 3 (Nordeste)
                        WeaponDirection.SE, // linha 4 (Sudeste)
                        WeaponDirection.N // linha 5 (Norte)
                };
            default:
                return new WeaponDirection[0];
        }
    }

    private void loadReloadAnimation(java.util.List<Texture> texturesList, WeaponType type, Texture mainSheet) {
        try {
            String reloadPath;

            switch (type) {
                case PISTOL:
                    reloadPath = "ITENS/Pistol/pistolReload-Sheet.png";
                    break;
                case SHOTGUN:
                    reloadPath = "ITENS/12/RELOAD_SHEET.png";
                    break;
                default:
                    return;
            }

            Texture reloadSheet = new Texture(Gdx.files.internal(reloadPath));

            if (type == WeaponType.SHOTGUN) {
                // Shotgun: 2 linhas, 5 colunas (total 10 frames)
                int rows = 2;
                int cols = 5;
                int frameWidth = reloadSheet.getWidth() / cols;
                int frameHeight = reloadSheet.getHeight() / rows;

                // Criar frames para cada direção (mesmo spritesheet para todas)
                for (WeaponDirection dir : WeaponDirection.values()) {
                    TextureRegion[] reloadFramesArray = new TextureRegion[10];

                    // Preencher frames em ordem: linha 0 (frames 0-4), linha 1 (frames 5-9)
                    for (int i = 0; i < 10; i++) {
                        int row = i < 5 ? 0 : 1;
                        int col = i < 5 ? i : i - 5;
                        reloadFramesArray[i] = new TextureRegion(reloadSheet,
                                col * frameWidth,
                                row * frameHeight,
                                frameWidth,
                                frameHeight);
                    }

                    Animation<TextureRegion> reloadAnim = new Animation<>(type.reloadSpeed, reloadFramesArray);
                    animations[dir.ordinal()][WeaponState.RELOADING.ordinal()] = reloadAnim;
                }
            } else {
                // Pistola: formato padrão (1 linha, 10 colunas)
                int reloadFrames = 10;
                int frameWidth = reloadSheet.getWidth() / reloadFrames;

                TextureRegion[] reloadFramesArray = new TextureRegion[reloadFrames];
                for (int i = 0; i < reloadFrames; i++) {
                    reloadFramesArray[i] = new TextureRegion(reloadSheet,
                            i * frameWidth, 0, frameWidth, reloadSheet.getHeight());
                }

                Animation<TextureRegion> reloadAnim = new Animation<>(type.reloadSpeed, reloadFramesArray);

                for (WeaponDirection dir : WeaponDirection.values()) {
                    animations[dir.ordinal()][WeaponState.RELOADING.ordinal()] = reloadAnim;
                }
            }

            // Só adicionar à lista se não for a shotgun (já adicionamos antes)
            if (type != WeaponType.SHOTGUN) {
                texturesList.add(reloadSheet);
            }

        } catch (Exception e) {
            Gdx.app.log("WeaponAnimations", "Animação de recarga não encontrada para " + type);
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    public Animation<TextureRegion> getAnimation(WeaponDirection direction, Weapon.WeaponState state) {
        // Verificar se a animação existe
        Animation<TextureRegion> anim = animations[direction.ordinal()][state.ordinal()];

        if (anim == null) {
            anim = getFallbackAnimation(direction, state);
        }

        return anim;
    }

    private Animation<TextureRegion> getFallbackAnimation(WeaponDirection direction, Weapon.WeaponState state) {
        if (weaponType == WeaponType.SHOTGUN) {
            switch (direction) {
                case NW:
                    return animations[WeaponDirection.NE.ordinal()][state.ordinal()];
                case SW:
                    return animations[WeaponDirection.SE.ordinal()][state.ordinal()];
                default:
                    // Para direções que não existem na shotgun, usar idle da direção S
                    return animations[WeaponDirection.S.ordinal()][Weapon.WeaponState.IDLE.ordinal()];
            }
        } else {
            return animations[WeaponDirection.S.ordinal()][Weapon.WeaponState.IDLE.ordinal()];
        }
    }

    public boolean needsFlip(WeaponDirection direction) {
        return weaponType == WeaponType.SHOTGUN &&
                (direction == WeaponDirection.NW || direction == WeaponDirection.SW);
    }
}