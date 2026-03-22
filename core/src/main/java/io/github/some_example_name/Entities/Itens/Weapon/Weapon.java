package io.github.some_example_name.Entities.Itens.Weapon;

import java.util.EnumMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.Body;
import java.util.Map;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.Item;

import io.github.some_example_name.Entities.Player.WeaponSight;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Entities.Renderer.WeaponRenderer;

public abstract class Weapon implements Item {
    protected float fireRate;
    protected float damage;
    protected int ammo;
    protected float timeSinceLastShot = 0f;
    public boolean canShoot = true;
    protected Vector2 position;
    protected WeaponAnimations animations;
    protected float animationTime = 0f;
    protected boolean shotTriggered = false;
    protected boolean reloadJustTriggered = false;
    protected Map<WeaponDirection, Vector2> renderOffsets;
    protected float reloadDuration = 1.5f;
    protected Inventory inventory;
    protected float rotation = 0f;

    public Body body;

    public enum WeaponState {
        IDLE, SHOOTING, RELOADING
    }

    protected WeaponState currentState = WeaponState.IDLE;
    private WeaponRenderer renderer = new WeaponRenderer();

    private float floatTime = 0f;
    private static final float FLOAT_SPEED = 2f;
    private static final float FLOAT_AMPLITUDE = 1f;

    protected TextureRegion icon;
    protected boolean reloading = false;
    protected float reloadProgress = 0;
    protected int maxAmmo;
    protected Vector2[] occupiedCells;

    protected int gridWidth;
    protected int gridHeight;

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public enum TipoMao {
        UMA_MAO, DUAS_MAOS;
    }

    public abstract TipoMao getTipoMao();

    public TextureRegion getIcon() {
        return icon;
    }

    public boolean isReloading() {
        return reloading;
    }

    public float getReloadProgress() {
        return reloadProgress;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public Weapon() {
        this.maxAmmo = 30;
        renderer.loadWeaponAnimations(this);
    }

    public abstract void shoot(Vector2 position, Vector2 direction);

    public abstract void update(float delta);

    public float getFireRate() {
        return fireRate;
    }

    public float getDamage() {
        return damage;
    }

    public int getAmmo() {
        return ammo;
    }

    public abstract TextureRegion getCurrentFrame(float delta);

    public abstract Vector2 getPosition();

    public abstract void createBody(Vector2 position);

    public void destroyBody() {

    }

    public abstract WeaponState getCurrentState();

    public void update(float delta, Vector2 aimDirection) {
        renderer.update(
                delta,
                aimDirection,
                currentState,
                shotTriggered,
                reloadJustTriggered);

        resetShotTrigger();
        reloadJustTriggered = false;
    }

    public void render(SpriteBatch batch, Vector2 position, float offsetX, float offsetY) {
        renderer.render(batch, position, offsetX, offsetY);
    }

    public void setPosition(Vector2 position) {
        this.position = position.cpy();
    }

    public abstract Vector2 getMuzzleOffset();

    public void rotate() {
        // Alterna a rotação entre 0 e 90
        rotation = (rotation == 0f) ? 90f : 0f;

        // Troca as dimensões do grid
        int temp = gridWidth;
        gridWidth = gridHeight;
        gridHeight = temp;

        // Reconstroi as células ocupadas para a nova orientação
        rebuildOccupiedCells();
    }

    public void rebuildOccupiedCells() {
        occupiedCells = new Vector2[gridWidth * gridHeight];
        int index = 0;
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                occupiedCells[index++] = new Vector2(x, y);
            }
        }
    }

    public Vector2[] getOccupiedCells() {
        Vector2[] cells = new Vector2[gridWidth * gridHeight];
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                cells[y * gridWidth + x] = new Vector2(x, y);
            }
        }
        return cells;
    }

    public void setGridSize(int width, int height) {
        this.gridWidth = width;
        this.gridHeight = height;
    }

    public abstract void reload();

    public void updateFloatation(float delta) {
        floatTime += delta * FLOAT_SPEED;
    }

    public float getFloatOffset() {
        return (float) Math.sin(floatTime) * FLOAT_AMPLITUDE;
    }

    public void resetShootingState() {
        if (currentState == WeaponState.SHOOTING) {
            currentState = WeaponState.IDLE;
            animationTime = 0f;
        }
    }

    public boolean isShotTriggered() {
        return shotTriggered;
    }

    public void resetShotTrigger() {
        shotTriggered = false;
    }

    public WeaponRenderer getRenderer() {
        return renderer;
    }

    private EnumMap<WeaponDirection, Vector2> muzzleOffsets = new EnumMap<>(WeaponDirection.class);

    public void setMuzzleOffset(WeaponDirection direction, Vector2 offset) {
        muzzleOffsets.put(direction, offset);
    }

    public Vector2 getMuzzleOffset(WeaponDirection direction) {
        return muzzleOffsets.getOrDefault(direction, new Vector2(0, 0));
    }

    public void setRenderOffset(WeaponDirection direction, float offsetX, float offsetY) {
        if (renderOffsets == null) {
            renderOffsets = new EnumMap<>(WeaponDirection.class);
        }
        renderOffsets.put(direction, new Vector2(offsetX, offsetY));
    }

    public Vector2 getRenderOffset(WeaponDirection direction) {
        if (renderOffsets != null && renderOffsets.containsKey(direction)) {
            return renderOffsets.get(direction);
        }
        return new Vector2(0f, 0f);
    }

    public void setRenderOffsetForAllDirections(float offsetX, float offsetY) {
        for (WeaponDirection dir : WeaponDirection.values()) {
            setRenderOffset(dir, offsetX, offsetY);
        }
    }

    public WeaponSight getWeaponSight() {
        // Padrão: mira de linha reta (para pistola)
        WeaponSight.LineSight sight = new WeaponSight.LineSight();
        sight.color = new Color(0.5f, 0.5f, 0.5f, 0.6f); // Cinza semi-transparente
        sight.lineWidth = 2f;
        return sight;
    }

    public float getReloadDuration() {
        return reloadDuration;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    // Na classe Weapon
    public Inventory getInventory() {
        return inventory;
    }

    public int getCurrentAmmo() {
        return ammo;
    }

    public void setCurrentAmmo(int ammo) {
        this.ammo = ammo;
    }
}