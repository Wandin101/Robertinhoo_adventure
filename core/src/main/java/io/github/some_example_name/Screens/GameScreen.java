package io.github.some_example_name.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.CabanaInteractionSystem;
import io.github.some_example_name.Interface.DebugHUD;
import io.github.some_example_name.Interface.RobertinhoFaceHUD;
import io.github.some_example_name.Interface.WeaponHUD;
import io.github.some_example_name.MapConfig.MapRenderer;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Screens.ScreenEffects.DeathSystem;
import io.github.some_example_name.Screens.ScreenEffects.ScreenFreezeSystem;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.MapConfig.RoomManager;
import com.badlogic.gdx.utils.Timer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Cursor;

public class GameScreen extends CatScreen implements Mapa.RoomTransitionListener {

    private Mapa mapa;
    private MapRenderer renderer;
    private Robertinhoo robertinhoo;
    private WeaponHUD weaponHUD;
    private SpriteBatch hudBatch;
    private OrthographicCamera hudCamera;
    private RobertinhoFaceHUD robertinhoFaceHUD;
    private DebugHUD debugHUD;
    private boolean debugEnabled = true;
    private AudioManager audioManager;
    private RoomManager roomManager = RoomManager.getInstance();
    private CabanaInteractionSystem cabanaInteraction;
    private com.badlogic.gdx.graphics.Texture blackPixelTexture;
    private DeathSystem deathSystem;
    private boolean isRespawning = false;
    private float respawnTimer = 0f;
    private static final float RESPAWN_TRANSITION_DURATION = 1.5f;
    private int currentRoom = 0;

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        audioManager = AudioManager.getInstance();
        GameGameSoundsPaths.loadAllAssets();

        // CARREGA A SALA 0 (SALA INICIAL)
        loadRoom0();
        deathSystem = new DeathSystem(this, renderer.getPlayerRenderer(), robertinhoFaceHUD);
        hudBatch = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudBatch.setProjectionMatrix(hudCamera.combined);

        weaponHUD = new WeaponHUD(robertinhoo);
        robertinhoo.setMapRenderer(renderer);
        robertinhoo.setFaceHUD(robertinhoFaceHUD);
        weaponHUD.setBatch(hudBatch);

        debugHUD = new DebugHUD();

        System.out.println("Sala 0 carregada - Player posicionado no centro");
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // transparente
        pixmap.fill();
        Cursor blankCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap.dispose();

        Gdx.graphics.setCursor(blankCursor);
    }

    // No GameScreen.java, modifique o método render:
    @Override
    public void render(float delta) {
        long startTime = TimeUtils.nanoTime();

        float gameplayDelta = ScreenFreezeSystem.getGameplayDelta();
        float playerDelta = ScreenFreezeSystem.getPlayerDelta();
        float animationDelta = ScreenFreezeSystem.getAnimationDelta();
        ScreenFreezeSystem.update(delta);
        gameplayDelta = Math.min(0.06f, gameplayDelta);
        playerDelta = Math.min(0.06f, playerDelta);
        animationDelta = Math.min(0.06f, animationDelta);
        deathSystem.update(delta, robertinhoo, mapa);

        if (deathSystem.isPlayingDeathAnimation()) {
            gameplayDelta = 0;
            playerDelta = 0;
        }
        // Substitua o bloco if (deathSystem.isPlayerDead()) {...} por:
        if (deathSystem.isPlayerDead()) {
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            robertinhoFaceHUD.update(animationDelta);

            renderer.render(animationDelta, robertinhoo);
            hudBatch.begin();
            robertinhoFaceHUD.draw(hudBatch, animationDelta);
            hudBatch.end();
            hudBatch.begin();
            deathSystem.render(hudBatch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            hudBatch.end();

            return;
        }
        if (isRespawning) {
            if (deathSystem != null) {
                deathSystem.updatePlayerRenderer(renderer.getPlayerRenderer(), robertinhoFaceHUD);
                System.out.println("✅ DeathSystem atualizado com PlayerRenderer da Sala 1");
            } else {
                System.err.println("❌ ERRO: deathSystem é null na Sala 1!");
                deathSystem = new DeathSystem(this, renderer.getPlayerRenderer(), robertinhoFaceHUD);
            }
            updateRespawnTransition(delta);
            return;
        }

        // JOGO NORMAL
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // UPDATE:
        long updateStart = TimeUtils.nanoTime();
        robertinhoo.update(playerDelta);
        mapa.update(gameplayDelta);

        // RENDER:
        renderer.render(animationDelta, robertinhoo);

        // HUD:
        weaponHUD.update(animationDelta);
        robertinhoFaceHUD.update(animationDelta);

        hudBatch.begin();
        weaponHUD.draw();
        robertinhoFaceHUD.draw(hudBatch, animationDelta);
        hudBatch.end();

        // Debug HUD
        debugHUD.update(animationDelta);
        if (debugEnabled) {
            debugHUD.render();
        }

        // Toggle debug com F3
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3)) {
            debugEnabled = !debugEnabled;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F10)) {
            AudioManager.getInstance().debugAudioState();
        }

        // TESTE: Tecla F1 para forçar morte
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F1)) {
            System.out.println("🔫 TESTE: Forçando morte do player...");
            forceDeath();
        }
    }

    private void updateRespawnTransition(float delta) {
        respawnTimer += delta;

        // Limpa a tela
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderiza o jogo durante a transição
        renderer.render(delta, robertinhoo);

        // Renderiza HUD durante transição
        weaponHUD.update(delta);
        robertinhoFaceHUD.update(delta);

        hudBatch.begin();
        weaponHUD.draw();
        robertinhoFaceHUD.draw(hudBatch, delta);

        // Overlay de fade out
        float alpha = 1f - (respawnTimer / RESPAWN_TRANSITION_DURATION);
        hudBatch.setColor(0, 0, 0, alpha);

        hudBatch.setColor(1, 1, 1, 1);
        hudBatch.end();

        if (respawnTimer >= RESPAWN_TRANSITION_DURATION) {
            isRespawning = false;
            respawnTimer = 0f;
            deathSystem.reset();

            System.out.println("✅ Transição de respawn completa! Jogador vivo.");
        }
    }

    private void loadRoom0() {
        AudioManager.getInstance().stopAllAmbientSounds();

        // Salva o player atual se existir
        Robertinhoo currentPlayer = (mapa != null) ? mapa.robertinhoo : robertinhoo;

        if (mapa != null) {
            try {
                mapa.disposeSafely();
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao dispor mapa antigo: " + e.getMessage());
            }
        }

        mapa = roomManager.createOrResetRoom0(currentPlayer);
        robertinhoo = mapa.robertinhoo;
        renderer = new MapRenderer(mapa);

        // ✅ RECRIA o faceHUD com as novas dimensões
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        robertinhoFaceHUD = new RobertinhoFaceHUD(width, height, robertinhoo);

        // ✅ ATUALIZA A REFERÊNCIA NO ROBERTINHOO
        if (robertinhoo != null) {
            robertinhoo.setFaceHUD(robertinhoFaceHUD);
            System.out.println("✅ [GameScreen] FaceHUD atualizado no Robertinhoo (Sala 0)");
        }

        // ✅ ATUALIZA o DeathSystem com o NOVO faceHUD
        if (deathSystem != null) {
            deathSystem.updatePlayerRenderer(renderer.getPlayerRenderer(), robertinhoFaceHUD);
            System.out.println("✅ [GameScreen] DeathSystem atualizado com novo faceHUD da Sala 0");
        } else {
            deathSystem = new DeathSystem(this, renderer.getPlayerRenderer(), robertinhoFaceHUD);
            System.out.println("✅ [GameScreen] DeathSystem criado com novo faceHUD da Sala 0");
        }

        cabanaInteraction = mapa.getCabanaInteractionSystem();
        mapa.setRoomTransitionListener(this);

        System.out.println("✅ Sala 0 carregada - Player e FaceHUD configurados");
    }

    private void loadRoom1() {
        System.out.println("=== INICIANDO LOAD ROOM 1 ===");
        currentRoom = 1;
        AudioManager.getInstance().stopAllAmbientSounds();

        Robertinhoo currentPlayer = null;
        if (mapa != null) {
            System.out.println("📝 Mapa atual existe, obtendo jogador...");
            currentPlayer = mapa.robertinhoo;
            System.out.println("📝 Jogador obtido: " + (currentPlayer != null));
        } else {
            System.out.println("📝 Mapa atual é null, usando robertinhoo: " + (robertinhoo != null));
            currentPlayer = robertinhoo;
        }

        if (mapa != null) {
            System.out.println("🗑️ Destruindo mapa antigo...");
            try {
                mapa.disposeSafely();
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao dispor mapa antigo: " + e.getMessage());
                System.err.println("⚠️ Continuando mesmo com erro...");
            }
            mapa = null;
            System.out.println("🗑️ Mapa antigo destruído");
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignora
        }

        mapa = createRoom1ReusingPlayer(currentPlayer);
        robertinhoo = mapa.robertinhoo;
        renderer = new MapRenderer(mapa);

        // ✅ ATUALIZA A REFERÊNCIA NO ROBERTINHOO
        if (robertinhoo != null) {
            robertinhoo.setFaceHUD(robertinhoFaceHUD);
            System.out.println("✅ [GameScreen] FaceHUD atualizado no Robertinhoo (Sala 1)");
        }

        // ✅ ATUALIZA o DeathSystem com o NOVO faceHUD
        if (deathSystem != null) {
            deathSystem.updatePlayerRenderer(renderer.getPlayerRenderer(), robertinhoFaceHUD);
            System.out.println("✅ [GameScreen] DeathSystem atualizado com novo faceHUD da Sala 1");
        } else {
            deathSystem = new DeathSystem(this, renderer.getPlayerRenderer(), robertinhoFaceHUD);
            System.out.println("✅ [GameScreen] DeathSystem criado com novo faceHUD da Sala 1");
        }

        cabanaInteraction = null;
        mapa.setRoomTransitionListener(this);
    }

    private Mapa createRoom1ReusingPlayer(Robertinhoo existingPlayer) {
        System.out.println("🔧 Criando Sala 1 com jogador existente: " + (existingPlayer != null));

        // Cria sala 1 normalmente
        Mapa room1 = new Mapa(false);

        // Se temos um jogador existente, reutiliza
        if (existingPlayer != null) {
            System.out.println("👤 Reutilizando jogador existente...");
            if (room1.robertinhoo != null && room1.robertinhoo.getBody() != null) {
                room1.world.destroyBody(room1.robertinhoo.getBody());
                System.out.println("🗑️ Jogador automático removido");
            }

            Vector2 worldStartPos = room1.getMapGenerator().getWorldStartPosition(room1.mapHeight);
            existingPlayer.switchToNewMap(room1, worldStartPos);
            room1.robertinhoo = existingPlayer;
            room1.setupContactListener(existingPlayer);

            System.out.println("✅ Jogador reutilizado e ContactListener configurado");
        } else {
            System.out.println("👤 Usando jogador novo da Sala 1");
            room1.setupContactListener(room1.robertinhoo);
        }

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        robertinhoFaceHUD = new RobertinhoFaceHUD(width, height, room1.robertinhoo);

        room1.robertinhoo.setFaceHUD(robertinhoFaceHUD);
        System.out.println("✅ [createRoom1] FaceHUD configurado no jogador da Sala 1");

        return room1;
    }

    @Override
    public void onRoomTransition(boolean toRoom0) {
        if (toRoom0) {
            loadRoom0();
        } else {
            loadRoom1();
        }
        renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);

        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
        hudBatch.setProjectionMatrix(hudCamera.combined);

        if (weaponHUD != null) {
            weaponHUD.resize(width, height);
        }

        robertinhoFaceHUD.updateScreenSize(width, height);

        System.out.println("[RESIZE] Tela: " + width + "x" + height);
    }

    @Override
    public void hide() {

    }

    public void onPlayerRespawn() {
        System.out.println("🔄 GameScreen: Iniciando respawn...");
        isRespawning = true;
        respawnTimer = 0f;
        loadRoom0();
        applyRespawnPenalties();
        System.out.println("🏠 Jogador respawnado na Sala 0");
    }

    private void applyRespawnPenalties() {
        if (robertinhoo == null)
            return;

        // 1. Vida parcial (70% da vida máxima)
        int maxLife = robertinhoo.getMaxLife();
        int respawnLife = (int) (maxLife * 0.7f);
        System.out.println("   - Vida após respawn: " + respawnLife + "/" + maxLife);

        // 4. Efeito de invulnerabilidade temporária pós-respawn
        robertinhoo.setInvulnerable(true);
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                robertinhoo.setInvulnerable(false);
                System.out.println("   - Invulnerabilidade pós-respawn terminada");
            }
        }, 3f); // 3 segundos de invulnerabilidade
    }

    public void forceDeath() {
        if (robertinhoo != null && !deathSystem.isPlayerDead()) {
            robertinhoo.takeDamage(robertinhoo.getLife()); // Dano fatal
        }
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (weaponHUD != null) {
            weaponHUD.dispose();
        }
        if (hudBatch != null) {
            hudBatch.dispose();
        }
        if (robertinhoFaceHUD != null) {
            robertinhoFaceHUD.dispose();
        }
        if (debugHUD != null) {
            debugHUD.dispose();
        }

        if (audioManager != null) {
            audioManager.dispose();
        }

        if (mapa != null) {
            mapa.setRoomTransitionListener(null);
        }
    }
}
