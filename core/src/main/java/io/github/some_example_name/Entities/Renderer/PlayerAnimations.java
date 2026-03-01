package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class PlayerAnimations implements Disposable {
    // Categorias de animações
    public static class BasicAnimations {
        public final Animation<TextureRegion> idleDown;
        public final Animation<TextureRegion> idleUp;
        public final Animation<TextureRegion> idleLeft;
        public final Animation<TextureRegion> idleRight;
        public final Animation<TextureRegion> walkLeft;
        public final Animation<TextureRegion> walkRight;
        public final Animation<TextureRegion> walkUp;
        public final Animation<TextureRegion> walkDown;
        public final Animation<TextureRegion> walkSE;
        public final Animation<TextureRegion> walkSW;
        public final Animation<TextureRegion> walkNortEast;
        public final Animation<TextureRegion> walkNortWast;
        public final Animation<TextureRegion> idleNorthWest;
        public final Animation<TextureRegion> idleNorthEast;
        public final Animation<TextureRegion> idleSouthWest;
        public final Animation<TextureRegion> idleSouthEast;

        // Construtor
        public BasicAnimations() {
            idleDown = AnimationLoader.loadAnimation("rober/idle/idle_S-Sheet.png", 0.2f, false, 12);
            idleUp = AnimationLoader.loadAnimation("rober/idle/idle_N-Sheet.png", 0.2f, false, 12);
            idleLeft = AnimationLoader.loadAnimation("rober/idle/idle_E-Sheet.png", 0.2f, true, 12);
            idleRight = AnimationLoader.loadAnimation("rober/idle/idle_E-Sheet.png", 0.2f, false, 12);
            walkLeft = AnimationLoader.loadAnimation("rober/walk/walk_E-Sheet.png", 0.15f, false, 8);
            walkRight = AnimationLoader.loadAnimation("rober/walk/walk_E-Sheet.png", 0.15f, false, 8);
            walkUp = AnimationLoader.loadAnimation("rober/walk/walk_N-Sheet.png", 0.15f, false, 8);
            walkNortEast = AnimationLoader.loadAnimation("rober/walk/walk_NE-Sheet.png", 0.15f, false, 8);
            walkNortWast = AnimationLoader.loadAnimation("rober/walk/walk_NE-Sheet.png", 0.15f, true, 8);
            walkDown = AnimationLoader.loadAnimation("rober/walk/walk_S-Sheet.png", 0.15f, false, 8);
            walkSE = AnimationLoader.loadAnimation("rober/walk/walk_SE-Sheet.png", 0.15f, false, 8);
            walkSW = AnimationLoader.loadAnimation("rober/walk/walk_SE-Sheet.png", 0.15f, true, 8);
            idleNorthWest = AnimationLoader.loadAnimation("rober/idle/idle_NE-Sheet.png", 0.2f, true, 12);
            idleNorthEast = AnimationLoader.loadAnimation("rober/idle/idle_NE-Sheet.png", 0.2f, false, 12);
            idleSouthWest = AnimationLoader.loadAnimation("rober/idle/idle_SE-Sheet.png", 0.2f, true, 12);
            idleSouthEast = AnimationLoader.loadAnimation("rober/idle/idle_SE-Sheet.png", 0.2f, false, 12);
        }
    }

    public static class WeaponAnimations {
        public final Animation<TextureRegion> idleDown;
        public final Animation<TextureRegion> idleUp;
        public final Animation<TextureRegion> idleLeft;
        public final Animation<TextureRegion> idleRight;
        public final Animation<TextureRegion> idleSE;
        public final Animation<TextureRegion> idleSW;
        public final Animation<TextureRegion> idleNE;
        public final Animation<TextureRegion> idleNW;
        public final Animation<TextureRegion> runLeft;
        public final Animation<TextureRegion> runRight;
        public final Animation<TextureRegion> runUp;
        public final Animation<TextureRegion> runDown;
        public final Animation<TextureRegion> runNE;
        public final Animation<TextureRegion> runNW;
        public final Animation<TextureRegion> runSE;
        public final Animation<TextureRegion> runSW;

        // Construtor
        public WeaponAnimations() {
            idleDown = AnimationLoader.loadAnimation("rober/idle_with_weapon/idle_S_with_weapon_Sheet.png", 0.2f, false,
                    12);
            idleLeft = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_E-Sheet.png", 0.2f, true, 12);
            idleRight = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_E-Sheet.png", 0.2f, false, 12);
            idleUp = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_N-Sheet.png", 0.2f, true, 12);
            idleSE = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_SE-Sheet.png", 0.2f, false, 12);
            idleSW = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_SE-Sheet.png", 0.2f, true, 12);
            idleNE = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_NE-Sheet.png", 0.2f, false, 12);
            idleNW = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_NE-Sheet.png", 0.2f, true, 12);
            runLeft = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_E-Sheet.png", 0.1f, true, 8);
            runRight = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_E-Sheet.png", 0.1f, false, 8);
            runDown = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_S-With_weapon.png", 0.1f, false, 8);
            runUp = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_N-Sheet.png", 0.1f, true, 8);
            runSE = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_SE-Sheet.png", 0.1f, false, 8);
            runSW = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_SE-Sheet.png", 0.1f, true, 8);
            runNE = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_NE-Sheet.png", 0.1f, false, 8);
            runNW = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_NE-Sheet.png", 0.1f, true, 8);
        }
    }

    public static class SpecialAnimations {
        public final Animation<TextureRegion> rollDown;
        public final Animation<TextureRegion> rollUp;
        public final Animation<TextureRegion> rollSide;
        // Novas animações de roll diagonal
        public final Animation<TextureRegion> rollDownRight;
        public final Animation<TextureRegion> rollUpRight;
        public final Animation<TextureRegion> rollDownLeft;
        public final Animation<TextureRegion> rollUpLeft;

        public final Animation<TextureRegion> meleeAttackRight;
        public final Animation<TextureRegion> meleeAttackLeft;
        public final Animation<TextureRegion> meleeAttackUp;
        public final Animation<TextureRegion> meleeAttackDown;

        public final Animation<TextureRegion> parryRight;
        public final Animation<TextureRegion> parryLeft;
        public final Animation<TextureRegion> parryUp;
        public final Animation<TextureRegion> parryDown;

        // Construtor
        public SpecialAnimations() {
            Texture rollTexture = AnimationLoader.loadTexture("rober/roll/roll-Sheet.png");
            int frameWidth = rollTexture.getWidth() / 21;
            int frameHeight = rollTexture.getHeight();
            TextureRegion[][] rollFrames = TextureRegion.split(rollTexture, frameWidth, frameHeight);
            TextureRegion[] rollDownFrames = Arrays.copyOfRange(rollFrames[0], 0, 7);
            TextureRegion[] rollUpFrames = Arrays.copyOfRange(rollFrames[0], 14, 21);
            TextureRegion[] rollSideFrames = Arrays.copyOfRange(rollFrames[0], 7, 14);
            float basicRollFrameDuration = 0.7f / 7;

            rollDown = new Animation<>(basicRollFrameDuration, rollDownFrames);
            rollUp = new Animation<>(basicRollFrameDuration, rollUpFrames);
            rollSide = new Animation<>(basicRollFrameDuration, rollSideFrames);

            Texture rollDiagonalTexture = AnimationLoader.loadTexture("rober/roll/roll_NEW.png");

            int frameCols = 7;
            int frameRows = 2;
            int diagFrameWidth = rollDiagonalTexture.getWidth() / frameCols;
            int diagFrameHeight = rollDiagonalTexture.getHeight() / frameRows;

            TextureRegion[][] diagFrames = TextureRegion.split(rollDiagonalTexture, diagFrameWidth, diagFrameHeight);
            TextureRegion[] rollDownRightFrames = Arrays.copyOf(diagFrames[0], 7);
            TextureRegion[] rollUpRightFrames = Arrays.copyOf(diagFrames[1], 7);
            TextureRegion[] rollDownLeftFrames = new TextureRegion[rollDownRightFrames.length];
            TextureRegion[] rollUpLeftFrames = new TextureRegion[rollUpRightFrames.length];

            for (int i = 0; i < rollDownRightFrames.length; i++) {
                rollDownLeftFrames[i] = new TextureRegion(rollDownRightFrames[i]);
                rollDownLeftFrames[i].flip(true, false);
            }

            for (int i = 0; i < rollUpRightFrames.length; i++) {
                rollUpLeftFrames[i] = new TextureRegion(rollUpRightFrames[i]);
                rollUpLeftFrames[i].flip(true, false);
            }

            float diagonalRollFrameDuration = 0.7f / 7;

            rollDownRight = new Animation<>(diagonalRollFrameDuration, rollDownRightFrames);
            rollUpRight = new Animation<>(diagonalRollFrameDuration, rollUpRightFrames);
            rollDownLeft = new Animation<>(diagonalRollFrameDuration, rollDownLeftFrames);
            rollUpLeft = new Animation<>(diagonalRollFrameDuration, rollUpLeftFrames);

            // Melee attack animations (mantido igual)
            Texture meleeTexture = AnimationLoader.loadTexture("rober/corpo_a_corpo/ataque_sheet.png");
            int frameWidthMelee = meleeTexture.getWidth() / 7;
            int frameHeightMelee = meleeTexture.getHeight() / 4;
            TextureRegion[][] meleeFrames = TextureRegion.split(meleeTexture, frameWidthMelee, frameHeightMelee);
            meleeAttackRight = new Animation<>(0.088f, meleeFrames[0]);
            meleeAttackLeft = new Animation<>(0.088f, meleeFrames[1]);
            meleeAttackDown = new Animation<>(0.088f, meleeFrames[2]);
            meleeAttackUp = new Animation<>(0.088f, meleeFrames[3]);

            Texture parryTexture = AnimationLoader.loadTexture("rober/Parry/Parry_LEFT_AND_RIGHT-Sheet.png");
            int frameWidthParry = parryTexture.getWidth() / 7;
            int frameHeightParry = parryTexture.getHeight();

            TextureRegion[] parryFrames = new TextureRegion[7];
            for (int i = 0; i < 7; i++) {
                parryFrames[i] = new TextureRegion(parryTexture, i * frameWidthParry, 0, frameWidthParry,
                        frameHeightParry);
            }

            parryLeft = new Animation<>(0.080f, parryFrames);

            TextureRegion[] rightFrames = new TextureRegion[7];
            for (int i = 0; i < 7; i++) {
                rightFrames[i] = new TextureRegion(parryFrames[i]);
                rightFrames[i].flip(true, false);
            }
            parryRight = new Animation<>(0.088f, rightFrames);
            parryUp = new Animation<>(0.088f, parryFrames);
            parryDown = new Animation<>(0.088f, parryFrames);

        }
    }

    public static class NoArmorAnimations {
        public final Animation<TextureRegion> idleDown;
        public final Animation<TextureRegion> idleUp;
        public final Animation<TextureRegion> idleLeft;
        public final Animation<TextureRegion> idleRight;
        public final Animation<TextureRegion> walkDown;
        public final Animation<TextureRegion> walkUp;
        public final Animation<TextureRegion> walkLeft;
        public final Animation<TextureRegion> walkRight;

        public NoArmorAnimations() {
            // Carrega o spritesheet único
            Texture spritesheet = AnimationLoader.loadTexture("rober/no_armor/robertinho_no_armor.png");

            int frameWidth = spritesheet.getWidth() / 7; // 7 colunas
            int frameHeight = spritesheet.getHeight() / 5; // 5 linhas (corrigido para 5)

            TextureRegion[][] allFrames = TextureRegion.split(spritesheet, frameWidth, frameHeight);

            // ANIMAÇÕES IDLE - 7 frames cada (linhas 0, 1, 2)
            // Linha 0: Idle Sul (7 frames)
            idleDown = createAnimationFromRow(allFrames[0], 0.2f, false, 7);

            // Linha 1: Idle Esquerda (7 frames)
            idleLeft = createAnimationFromRow(allFrames[1], 0.2f, false, 7);
            // Idle Direita é a mesma linha espelhada
            idleRight = createAnimationFromRow(allFrames[1], 0.2f, true, 7);

            // Linha 2: Idle Norte (7 frames)
            idleUp = createAnimationFromRow(allFrames[2], 0.2f, false, 7);

            // ANIMAÇÕES WALK - Estrutura complexa (linhas 3 e 4)
            // Linha 3: Walk Sul (4 frames) + Walk Esquerda (3 frames)
            // Linha 4: Walk Esquerda (1 frame) + Walk Norte (4 frames) + 2 vazios

            // Walk Sul: Linha 3, frames 0-3 (4 frames)
            walkDown = createAnimationFromRange(allFrames[3], 0, 3, 0.15f, false);

            // Walk Esquerda: Linha 3 frames 4-6 (3 frames) + Linha 4 frame 0 (1 frame) = 4
            // frames
            TextureRegion[] walkLeftFrames = new TextureRegion[4];
            walkLeftFrames[0] = allFrames[3][4];
            walkLeftFrames[1] = allFrames[3][5];
            walkLeftFrames[2] = allFrames[3][6];
            walkLeftFrames[3] = allFrames[4][0];
            walkLeft = new Animation<>(0.15f, walkLeftFrames);

            // Walk Direita: mesma sequência do Walk Esquerda, mas espelhada
            TextureRegion[] walkRightFrames = new TextureRegion[4];
            for (int i = 0; i < walkLeftFrames.length; i++) {
                walkRightFrames[i] = new TextureRegion(walkLeftFrames[i]);
                walkRightFrames[i].flip(true, false);
            }
            walkRight = new Animation<>(0.15f, walkRightFrames);

            // Walk Norte: Linha 4, frames 1-4 (4 frames)
            walkUp = createAnimationFromRange(allFrames[4], 1, 4, 0.15f, false);
        }

        private Animation<TextureRegion> createAnimationFromRow(TextureRegion[] row, float frameDuration, boolean flip,
                int frameCount) {
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = new TextureRegion(row[i]);
                if (flip) {
                    frames[i].flip(true, false);
                }
            }
            return new Animation<>(frameDuration, frames);
        }

        private Animation<TextureRegion> createAnimationFromRange(TextureRegion[] row, int start, int end,
                float frameDuration, boolean flip) {
            int frameCount = end - start + 1;
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = new TextureRegion(row[start + i]);
                if (flip) {
                    frames[i].flip(true, false);
                }
            }
            return new Animation<>(frameDuration, frames);
        }
    }

    public static class DeathAnimations {
        public final Animation<TextureRegion> deathAnimation;

        public DeathAnimations() {
            // Carrega a textura de morte (5 colunas x 5 linhas = 25 frames)
            Texture deathTexture = AnimationLoader.loadTexture("rober/death/death-sheet.png");

            // O spritesheet tem 5 colunas e 5 linhas
            int frameCols = 6;
            int frameRows = 5;
            int frameWidth = deathTexture.getWidth() / frameCols;
            int frameHeight = deathTexture.getHeight() / frameRows;

            // Divide o spritesheet em uma matriz de TextureRegion
            TextureRegion[][] tempFrames = TextureRegion.split(deathTexture, frameWidth, frameHeight);

            // Cria um array com todos os 25 frames (5x5)
            TextureRegion[] deathFrames = new TextureRegion[frameCols * frameRows];

            // Preenche o array percorrendo as linhas e colunas
            int index = 0;
            for (int row = 0; row < frameRows; row++) {
                for (int col = 0; col < frameCols; col++) {
                    deathFrames[index++] = tempFrames[row][col];
                }
            }

            // Define a animação com duração total de 2.0 segundos
            // 25 frames / 2.0s = 0.08s por frame
            float frameDuration = 3.5f / deathFrames.length; // 2.0 segundos dividido por 25 frames

            // Cria a animação que NÃO faz loop
            deathAnimation = new Animation<>(frameDuration, deathFrames);
            deathAnimation.setPlayMode(Animation.PlayMode.NORMAL); // NORMAL = não faz loop
        }
    }

    public static class DamageAnimations {
        public final Animation<TextureRegion> ritDamage;

        public DamageAnimations() {
            Texture ritTexture = AnimationLoader.loadTexture("rober/rit/robertinho_Rit.png");

            int frameWidth = ritTexture.getWidth() / 5; // 5 frames na horizontal
            int frameHeight = ritTexture.getHeight(); // 1 linha

            TextureRegion[] ritFrames = new TextureRegion[5];
            for (int i = 0; i < 5; i++) {
                ritFrames[i] = new TextureRegion(ritTexture, i * frameWidth, 0, frameWidth, frameHeight);
            }
            ritDamage = new Animation<>(0.1f, ritFrames);
        }
    }

    // Dentro da classe PlayerAnimations, adicione uma nova classe interna:
    public static class EnterAnimations {
        public final Animation<TextureRegion> enterAnimation;

        public EnterAnimations() {
            // Ajuste o caminho da textura conforme seu projeto
            Texture enterTexture = AnimationLoader.loadTexture("rober/EnterandBackDOR/Enter.png");
            int frameCols = 9;
            int frameRows = 1;
            int frameWidth = enterTexture.getWidth() / frameCols;
            int frameHeight = enterTexture.getHeight() / frameRows;
            TextureRegion[][] temp = TextureRegion.split(enterTexture, frameWidth, frameHeight);
            TextureRegion[] frames = temp[0]; // primeira (e única) linha
            float frameDuration = 0.09f; // duração de cada frame (0.9s no total)
            enterAnimation = new Animation<>(frameDuration, frames);
            enterAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        }
    }

    public static class BackAnimations {
        public final Animation<TextureRegion> backAnimation;

        public BackAnimations() {
            Texture backTexture = AnimationLoader.loadTexture("rober/EnterandBackDOR/Back.png"); // ajuste o caminho
            int frameCols = 7; // mesma quantidade de frames da entrada
            int frameRows = 1;
            int frameWidth = backTexture.getWidth() / frameCols;
            int frameHeight = backTexture.getHeight() / frameRows;
            TextureRegion[][] temp = TextureRegion.split(backTexture, frameWidth, frameHeight);
            TextureRegion[] frames = temp[0];
            float frameDuration = 0.09f; // mesma duração da entrada
            backAnimation = new Animation<>(frameDuration, frames);
            backAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        }
    }

    public final BasicAnimations basic;
    public final WeaponAnimations weapon;
    public final SpecialAnimations special;
    public final NoArmorAnimations noArmor;
    public final DamageAnimations damage;
    public final DeathAnimations death;
    public final EnterAnimations enter;
    public final BackAnimations back;

    private final Array<Texture> loadedTextures = new Array<>();

    public PlayerAnimations() {
        // Carregar todas as texturas primeiro
        loadAllTextures();

        // Inicializar categorias
        basic = new BasicAnimations();
        weapon = new WeaponAnimations();
        special = new SpecialAnimations();
        noArmor = new NoArmorAnimations();
        damage = new DamageAnimations();
        death = new DeathAnimations();
        enter = new EnterAnimations();
        back = new BackAnimations();
    }

    private void loadAllTextures() {
        // Lista de caminhos de textura
        String[] texturePaths = {
                "rober/idle/idle_S-Sheet.png",
                "rober/idle/idle_N-Sheet.png",
                "rober/idle/idle_E-Sheet.png",
                "rober/run/2_Template_Run_Left-Sheet.png",
                "rober/run/2_Template_Run_Up-Sheet.png",
                "rober/run/2_Template_Run_Down-Sheet.png",
                "rober/idle_with_weapon/Idle_down_With_weapon-Sheet.png",
                "rober/idle_with_weapon/idle_left_with_weapon.png",
                "rober/idle_with_weapon/Idle_E-Sheet.png",
                "rober/idle_with_weapon/Idle_N-Sheet.png",
                "rober/run_with_weapon/2_Template_Run_Up_With_One_HandWEAPON-Sheet.png",
                "rober/idle_with_weapon/1_Template_Idle_Up_with_weapon-Sheet.png",
                "rober/run_with_weapon/2_Template_Run_Left_withe_oneHand_WEAPON.png",
                "rober/run_with_weapon/runDown_With_One_HandWEAPON-Sheet.png",
                "rober/run_with_weapon/2_Template_Run_Up_With_One_HandWEAPON-Sheet.png",
                "rober/walk/walk_SE-Sheet.png",
                "rober/roll/roll-Sheet.png",
                "rober/roll/roll_NEW.png",
                "rober/corpo_a_corpo/ataque_sheet.png",
                "rober/Parry/Parry_LEFT_AND_RIGHT-Sheet.png",
                "rober/no_armor/robertinho_no_armor.png",
                "rober/death/death-sheet.png",
                "rober/EnterandBackDOR/Enter.png",
                "rober/EnterandBackDOR/Back.png"

        };

        // Carregar e armazenar texturas
        for (String path : texturePaths) {
            Texture texture = AnimationLoader.loadTexture(path);
            loadedTextures.add(texture);
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}