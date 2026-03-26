package io.github.some_example_name.Entities.Itens.Weapon.Revolver;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Timer;

import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Weapon.IReloadSystem;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon.TipoMao;
import io.github.some_example_name.Entities.Particulas.RevolverShell.RevolverShellEjector;
import io.github.some_example_name.Entities.Particulas.Shell.ShellSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Player.WeaponSight;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;

public class Revolver extends Weapon {

    private Texture iconTexture;
    private int revolverMaxAmmo = 6;
    protected Vector2 position;

    private IReloadSystem reloadSystem; // Usamos a interface

    private static TextureRegion shellTexture;
    private static boolean shellTextureLoaded = false;
    private int shotsFired = 0;
    private boolean shellsEjected = false;
    private Vector2 lastAimDirection = new Vector2(1, 0);

    public void setAimDirection(Vector2 direction) {
        lastAimDirection.set(direction);
    }

    private float shootingTime = 0f;
    private static final float SHOOTING_STATE_DURATION = 0.5f;

    public Revolver(Mapa mapa, float x, float y, Inventory inventory) {
        super();
        this.maxAmmo = revolverMaxAmmo;
        this.ammo = this.maxAmmo;
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        this.inventory = inventory;
        this.fireRate = 1f;
        this.damage = 25f;
        this.iconTexture = new Texture("ITENS/Revolver/revolver_icon.png");
        this.icon = new TextureRegion(iconTexture);

        createBody(this.position);

        this.gridWidth = 3;
        this.gridHeight = 2;
        rebuildOccupiedCells();

        // Carregar textura da cápsula
        if (!shellTextureLoaded) {
            try {
                Texture tex = new Texture("ITENS/Revolver/capsulaRevolver.png");
                shellTexture = new TextureRegion(tex);
                RevolverShellEjector.getInstance().init(0.12f); // escala das cápsulas
                shellTextureLoaded = true;
                System.out.println("✅ [Revolver] Textura de cápsula carregada");
            } catch (Exception e) {
                System.err.println("❌ [Revolver] Erro ao carregar capsulaRevolver.png: " + e.getMessage());
            }
        }

        // Instancia o sistema de recarga específico
        reloadSystem = new RevolverReloadSystem();

        // Offsets do cano (muzzle)
        setMuzzleOffset(WeaponDirection.N, new Vector2(4, 8));
        setMuzzleOffset(WeaponDirection.NE, new Vector2(8, 6));
        setMuzzleOffset(WeaponDirection.NW, new Vector2(0, 6));
        setMuzzleOffset(WeaponDirection.E, new Vector2(10, 2));
        setMuzzleOffset(WeaponDirection.W, new Vector2(-2, 2));
        setMuzzleOffset(WeaponDirection.S, new Vector2(4, -2));
        setMuzzleOffset(WeaponDirection.SE, new Vector2(8, -2));
        setMuzzleOffset(WeaponDirection.SW, new Vector2(0, -2));

        // Offsets de renderização
        float baseX = -8f;
        float baseY = -12f;
        setRenderOffset(WeaponDirection.S, baseX + 5.0f, baseY + 18f);
        setRenderOffset(WeaponDirection.SW, baseX + 10f, baseY + 10f);
        setRenderOffset(WeaponDirection.SE, baseX + 8f, baseY + 10f);
        setRenderOffset(WeaponDirection.E, baseX + 8.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.W, baseX + 8.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.N, baseX + 8.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.NE, baseX + 8f, baseY + 10f);
        setRenderOffset(WeaponDirection.NW, baseX + 8f, baseY + 10f);
        setMapa(mapa);
    }

    @Override
    public TipoMao getTipoMao() {
        return TipoMao.UMA_MAO;
    }

    @Override
    public int getMaxAmmo() {
        return revolverMaxAmmo;
    }

    @Override
    public void reload() {
        if (currentState == WeaponState.RELOADING || inventory == null)
            return;
        if (currentState == WeaponState.SHOOTING)
            currentState = WeaponState.IDLE;

        reloadSystem.startReload(this, maxAmmo, ammo);
        if (reloadSystem.isReloading()) {
            currentState = WeaponState.RELOADING;
            reloadJustTriggered = true;
        }
        shellsEjected = false; // reset para nova recarga
    }

    @Override
    public void update(float delta) {
        updateFloatation(delta);
        timeSinceLastShot += delta;

        if (timeSinceLastShot >= 1 / fireRate) {
            canShoot = true;
        }

        if (currentState == WeaponState.SHOOTING) {
            shootingTime += delta;
            if (shootingTime >= SHOOTING_STATE_DURATION) {
                currentState = WeaponState.IDLE;
                shootingTime = 0f;
            }
        }

        if (currentState == WeaponState.RELOADING && reloadSystem.isReloading()) {
            reloadSystem.update(delta);

            int stage = reloadSystem.getCurrentStage();
            float progress = reloadSystem.getStageProgress();
            if (stage == 0) {
                System.out.println("[Revolver] stage=0, progress=" + progress + ", shellsEjected=" + shellsEjected
                        + ", shotsFired=" + shotsFired);
            }
            if (stage == 0 && progress > 0.8f && !shellsEjected && shotsFired > 0 && shellTexture != null) {
                Vector2 playerPos = inventory.getPlayer().getBody().getPosition();
                RevolverShellEjector.getInstance().ejectMultiple(
                        playerPos,
                        lastAimDirection,
                        shotsFired,
                        shellTexture);
                System.out.println("[Revolver] Ejetou " + shotsFired + " cápsulas.");
                shotsFired = 0;
                shellsEjected = true;
            }
        }
        if (currentState == WeaponState.RELOADING && !reloadSystem.isReloading()) {
            currentState = WeaponState.IDLE;
            reloadJustTriggered = false;
            shellsEjected = false;
        }
    }

    public boolean isReloading() {
        return reloadSystem.isReloading();
    }

    public int getReloadStage() {
        return reloadSystem.getCurrentStage();
    }

    public int getShellsInserted() {
        return reloadSystem.getShellsInserted();
    }

    public int getShellsToInsert() {
        return reloadSystem.getShellsToInsert();
    }

    public float getStageProgress() {
        return reloadSystem.getStageProgress();
    }

    public void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);

        body = getMapa().world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.7f, 0.7f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_ITEM;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;
        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData(this);
    }

    @Override
    public TextureRegion getCurrentFrame(float delta) {
        return new TextureRegion(iconTexture);
    }

    @Override
    public void shoot(Vector2 position, Vector2 direction) {
        if (currentState == WeaponState.RELOADING) {
            reloadSystem.cancel();
            currentState = WeaponState.SHOOTING;
            reloadJustTriggered = false;
            shellsEjected = false;
        }

        if (!canShoot || ammo <= 0)
            return;

        new Projectile(getMapa(), position, direction.nor().scl(40f), damage, getName());
        shotsFired++;

        shotTriggered = true;
        ammo--;
        canShoot = false;
        timeSinceLastShot = 0f;
        currentState = WeaponState.SHOOTING;
        shootingTime = 0f;

        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.REVOLVER_SHOOT, 0.8f);
    }

    @Override
    public WeaponState getCurrentState() {
        return currentState;
    }

    @Override
    public Vector2 getPosition() {
        return body != null ? body.getPosition() : position;
    }

    @Override
    public Vector2 getMuzzleOffset() {
        return new Vector2(0.001f, 0.001f);
    }

    @Override
    public void destroyBody() {
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }

    @Override
    public Revolver copy() {
        return new Revolver(mapa, (int) position.x, (int) position.y, inventory);
    }

    @Override
    public String getName() {
        return "Revolver";
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public WeaponSight getWeaponSight() {

        WeaponSight.BrawlPistolSight sight = new WeaponSight.BrawlPistolSight();
        sight.color = new Color(0.9f, 0.9f, 0.9f, 0.25f); // Branco transparente
        sight.lineWidth = 4f;
        sight.endMarkerSize = 6;
        return sight;
    }

    public void dispose() {
        if (iconTexture != null)
            iconTexture.dispose();
        if (shellTexture != null && shellTexture.getTexture() != null) {
            shellTexture.getTexture().dispose();
            shellTexture = null;
            shellTextureLoaded = false;
        }
        if (reloadSystem != null)
            reloadSystem.dispose();
    }
}