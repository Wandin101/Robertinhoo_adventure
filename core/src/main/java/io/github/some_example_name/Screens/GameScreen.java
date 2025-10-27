package io.github.some_example_name.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.DebugHUD;
import io.github.some_example_name.Interface.RobertinhoFaceHUD;
import io.github.some_example_name.Interface.WeaponHUD;
import io.github.some_example_name.MapConfig.MapRenderer;
import io.github.some_example_name.MapConfig.Mapa;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Cursor;

public class GameScreen extends CatScreen {

    private Mapa mapa;
    private MapRenderer renderer;
    private Robertinhoo robertinhoo;
    private WeaponHUD weaponHUD;
    private SpriteBatch hudBatch;
    private OrthographicCamera hudCamera;
    private RobertinhoFaceHUD robertinhoFaceHUD;
    private DebugHUD debugHUD;
    private boolean debugEnabled = true;

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        mapa = new Mapa();
        robertinhoo = mapa.robertinhoo;
        renderer = new MapRenderer(mapa);

        hudBatch = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudBatch.setProjectionMatrix(hudCamera.combined);

        weaponHUD = new WeaponHUD(robertinhoo);
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        robertinhoFaceHUD = new RobertinhoFaceHUD(width, height, robertinhoo);

        robertinhoo.setMapRenderer(renderer);
        weaponHUD.setBatch(hudBatch);

        // DebugHUD sem Viewport problemático
        debugHUD = new DebugHUD();

        System.out.println("MapRenderer configurado no Robertinhoo.");
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // transparente
        pixmap.fill();
        Cursor blankCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap.dispose();

        Gdx.graphics.setCursor(blankCursor);
    }

    @Override
    public void render(float delta) {
        long startTime = TimeUtils.nanoTime();

        delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update
        long updateStart = TimeUtils.nanoTime();
        robertinhoo.update(delta);
        mapa.update(delta);
        float updateTime = (TimeUtils.nanoTime() - updateStart) / 1000000f; // ms

        // Render do jogo
        long renderStart = TimeUtils.nanoTime();
        renderer.render(delta, robertinhoo);
        float renderTime = (TimeUtils.nanoTime() - renderStart) / 1000000f; // ms

        // HUD normal
        weaponHUD.update(delta);
        robertinhoFaceHUD.update(delta);

        hudBatch.begin();
        weaponHUD.draw();
        robertinhoFaceHUD.draw(hudBatch, delta);
        hudBatch.end();

        // Debug HUD (sempre por último) - SEM Viewport problemático
        debugHUD.update(delta);
        if (debugEnabled) {
            debugHUD.render();
        }

        // Toggle debug com F3
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3)) {
            debugEnabled = !debugEnabled;
        }
    }

    @Override
    public void resize(int width, int height) {
        // Atualize PRIMEIRO a câmera do jogo principal
        renderer.resize(width, height);

        // Atualize AGORA a câmera HUD (método antigo que funcionava)
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
        hudBatch.setProjectionMatrix(hudCamera.combined);

        // Atualize os elementos HUD
        if (weaponHUD != null) {
            weaponHUD.resize(width, height);
        }

        robertinhoFaceHUD.updateScreenSize(width, height);

        System.out.println("[RESIZE] Tela: " + width + "x" + height);
    }

    @Override
    public void hide() {

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
    }
}