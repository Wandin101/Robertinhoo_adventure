package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

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
    private Texture shotgunReloadSheet;
    private Texture revolverIdleSheet; // Para o idle separado do revólver

    public WeaponAnimations(String weaponTypeStr) {
        if (weaponTypeStr.equals("Pistol")) {
            this.weaponType = WeaponType.PISTOL;
        } else if (weaponTypeStr.equals("Calibre12")) {
            this.weaponType = WeaponType.SHOTGUN;
        } else if (weaponTypeStr.equals("Revolver")) {
            this.weaponType = WeaponType.REVOLVER;
        } else {
            this.weaponType = WeaponType.PISTOL;
        }

        animations = new Animation[WeaponDirection.values().length][Weapon.WeaponState.values().length];
        loadedTextures = loadWeaponTextures(weaponTypeStr);
    }

    private Texture[] loadWeaponTextures(String weaponTypeStr) {
        java.util.List<Texture> texturesList = new java.util.ArrayList<>();

        if (weaponTypeStr.equals("Pistol")) {
            loadMultiRowAnimation(texturesList, "ITENS/Pistol/Pistol_shoot_idle.png",
                    WeaponType.PISTOL, 8, 6);
        } else if (weaponTypeStr.equals("Calibre12")) {
            shotgunReloadSheet = new Texture(Gdx.files.internal("ITENS/12/RELOAD_SHEET.png"));
            texturesList.add(shotgunReloadSheet);
            System.out.println("✅ [WeaponAnimations] Spritesheet de reload da shotgun carregado");
            loadMultiRowAnimation(texturesList, "ITENS/12/12_shoot.png",
                    WeaponType.SHOTGUN, 6, 11);
        } else if (weaponTypeStr.equals("Revolver")) {
            // Carrega a folha de tiro (5 linhas × 6 colunas)
            loadMultiRowAnimation(texturesList, "ITENS/Revolver/revolver_shoot.png",
                    WeaponType.REVOLVER, 5, 6);
            // Carrega a folha de idle (1 linha × 4 colunas)
            revolverIdleSheet = new Texture(Gdx.files.internal("ITENS/Revolver/idle.png"));
            texturesList.add(revolverIdleSheet);
            System.out.println("✅ [WeaponAnimations] Spritesheet de idle do revólver carregado");
            setupRevolverIdle();
        }

        return texturesList.toArray(new Texture[0]);
    }

    // Configura as animações de idle do revólver a partir da folha separada
    private void setupRevolverIdle() {
        int cols = 4;
        int rows = 1;
        int frameWidth = revolverIdleSheet.getWidth() / cols;
        int frameHeight = revolverIdleSheet.getHeight();

        // Colunas: 0 = W (L), 1 = E, 2 = S, 3 = N
        WeaponDirection[] idleDirs = {
            WeaponDirection.W, // col 0
            WeaponDirection.E, // col 1
            WeaponDirection.S, // col 2
            WeaponDirection.N  // col 3
        };

        for (int col = 0; col < cols; col++) {
            TextureRegion idleFrame = new TextureRegion(revolverIdleSheet,
                    col * frameWidth, 0, frameWidth, frameHeight);
            Animation<TextureRegion> idleAnim = new Animation<>(0.2f, idleFrame);
            animations[idleDirs[col].ordinal()][WeaponState.IDLE.ordinal()] = idleAnim;
        }

        // Para direções diagonais, deixamos nulo – serão convertidas para cardeais no renderer
    }

    private enum WeaponType {
        PISTOL(8, 6, 0.5f / 6.0f, 0.1f, false),
        SHOTGUN(6, 11, 1.0f / 11.0f, 0.15f, true),
        REVOLVER(5, 6, 0.5f / 6.0f, 0.1f, true); // usa flip para E, SE, NW

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
                System.err.println("Número de linhas não corresponde às direções esperadas.");
                return;
            }

            // Para cada linha (direção), criar animações de SHOOTING
            for (int row = 0; row < rows; row++) {
                WeaponDirection direction = rowDirections[row];

                // SHOOTING: todos os frames da linha
                TextureRegion[] shootFrames = new TextureRegion[cols];
                for (int col = 0; col < cols; col++) {
                    shootFrames[col] = new TextureRegion(sheet,
                            col * frameWidth,
                            row * frameHeight,
                            frameWidth,
                            frameHeight);
                }
                animations[direction.ordinal()][WeaponState.SHOOTING.ordinal()] =
                        new Animation<>(type.shootSpeed, shootFrames);

                // Para revólver, o IDLE é carregado separadamente; para outras armas, usamos o primeiro frame
                if (type != WeaponType.REVOLVER) {
                    TextureRegion idleFrame = new TextureRegion(sheet, 0, row * frameHeight, frameWidth, frameHeight);
                    animations[direction.ordinal()][WeaponState.IDLE.ordinal()] =
                            new Animation<>(0.2f, idleFrame);
                }
            }

            // Carregar animação de recarga se existir (ignoramos para revólver por enquanto)
            if (type != WeaponType.REVOLVER) {
                loadReloadAnimation(texturesList, type, sheet);
            }

            texturesList.add(sheet);

        } catch (Exception e) {
            Gdx.app.error("WeaponAnimations", "Erro ao carregar " + path, e);
        }
    }

    private WeaponDirection[] getRowDirectionsForWeapon(WeaponType type) {
        switch (type) {
            case PISTOL:
                return new WeaponDirection[] {
                        WeaponDirection.E,
                        WeaponDirection.W,
                        WeaponDirection.N,
                        WeaponDirection.S,
                        WeaponDirection.NE,
                        WeaponDirection.NW,
                        WeaponDirection.SE,
                        WeaponDirection.SW
                };
            case SHOTGUN:
                return new WeaponDirection[] {
                        WeaponDirection.S,
                        WeaponDirection.W,
                        WeaponDirection.E,
                        WeaponDirection.NE,
                        WeaponDirection.SE,
                        WeaponDirection.N
                };
            case REVOLVER:
                // Ordem das linhas: L (W), SW, S, NE, N
                return new WeaponDirection[] {
                        WeaponDirection.W,  // linha 0
                        WeaponDirection.SW, // linha 1
                        WeaponDirection.S,  // linha 2
                        WeaponDirection.NE, // linha 3
                        WeaponDirection.N   // linha 4
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
    public Animation<TextureRegion> getAnimation(WeaponDirection direction, Weapon.WeaponState state) {
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
                    return animations[WeaponDirection.S.ordinal()][Weapon.WeaponState.IDLE.ordinal()];
            }
        } else if (weaponType == WeaponType.REVOLVER) {
            // Direções que devem ser obtidas por flip dos opostos
            switch (direction) {
                case E:
                    return animations[WeaponDirection.W.ordinal()][state.ordinal()];
                case SE:
                    return animations[WeaponDirection.SW.ordinal()][state.ordinal()];
                case NW:
                    return animations[WeaponDirection.NE.ordinal()][state.ordinal()];
                default:
                    // Fallback para idle de S se algo mais faltar
                    return animations[WeaponDirection.S.ordinal()][Weapon.WeaponState.IDLE.ordinal()];
            }
        } else {
            return animations[WeaponDirection.S.ordinal()][Weapon.WeaponState.IDLE.ordinal()];
        }
    }

    public boolean needsFlip(WeaponDirection direction) {
        if (weaponType == WeaponType.SHOTGUN) {
            return (direction == WeaponDirection.NW || direction == WeaponDirection.SW);
        } else if (weaponType == WeaponType.REVOLVER) {
            // Direções que não existem na sheet e serão obtidas por flip
            return (direction == WeaponDirection.E ||
                    direction == WeaponDirection.SE ||
                    direction == WeaponDirection.NW);
        }
        return false;
    }

    @Override
    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }
}