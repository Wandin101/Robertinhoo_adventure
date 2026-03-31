package io.github.some_example_name.Entities.Itens.Weapon.DesertEagle;

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
import io.github.some_example_name.Entities.Particulas.Shell.ShellSystem;
import io.github.some_example_name.Entities.Particulas.Smoke.SmokeSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Player.WeaponSight;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;

public class DesertEagle extends Weapon {

    private Mapa mapa;
    private Texture iconTexture;
    private int maxAmmo = 7; // capacidade padrão
    protected Vector2 position;

    private IReloadSystem reloadSystem;

    private static TextureRegion shellTexture;
    private static boolean shellTextureLoaded = false;
    private int shotsFired = 0;

    private float shootingTime = 0f;
    private static final float SHOOTING_STATE_DURATION = 0.5f;

    public DesertEagle(Mapa mapa, float x, float y, Inventory inventory) {
        super();
        this.maxAmmo = maxAmmo;
        this.ammo = maxAmmo; // começa cheia
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        this.inventory = inventory;
        this.fireRate = 1.2f; // um pouco mais lento que a pistola
        this.damage = 45f; // dano alto
        this.iconTexture = new Texture("ITENS/DesertEagle/desert_eagle_icon.png");
        this.icon = new TextureRegion(iconTexture);
        setMapa(mapa);
        createBody(this.position);

        this.gridWidth = 3;
        this.gridHeight = 2;
        rebuildOccupiedCells();

        // Carregar textura da cápsula (usar mesma da pistola ou uma específica)
        if (!shellTextureLoaded) {
            try {
                Texture tex = new Texture("ITENS/DesertEagle/capsula.png");
                shellTexture = new TextureRegion(tex);
                ShellSystem.getInstance().init(0.1f); // escala ajustável
                shellTextureLoaded = true;
                System.out.println("✅ [DesertEagle] Textura de cápsula carregada");
            } catch (Exception e) {
                System.err.println("❌ [DesertEagle] Erro ao carregar capsula.png: " + e.getMessage());
            }
        }

        // Instancia o sistema de recarga
        reloadSystem = new DesertEagleReloadSystem();

        // Offsets do cano (valores aproximados – ajustar depois)
        setMuzzleOffset(WeaponDirection.N, new Vector2(4, 8));
        setMuzzleOffset(WeaponDirection.NE, new Vector2(8, 6));
        setMuzzleOffset(WeaponDirection.NW, new Vector2(0, 6));
        setMuzzleOffset(WeaponDirection.E, new Vector2(10, 2));
        setMuzzleOffset(WeaponDirection.W, new Vector2(-2, 2));
        setMuzzleOffset(WeaponDirection.S, new Vector2(6, -2));
        setMuzzleOffset(WeaponDirection.SE, new Vector2(8, -2));
        setMuzzleOffset(WeaponDirection.SW, new Vector2(0, -2));

        // Offsets de renderização (baseados na pistola, podem ser ajustados)
        float baseX = -8f;
        float baseY = -12f;
        setRenderOffset(WeaponDirection.S, baseX + 5.0f, baseY + 18f);
        setRenderOffset(WeaponDirection.SW, baseX + 10f, baseY + 10f);
        setRenderOffset(WeaponDirection.SE, baseX + 8f, baseY + 10f);
        setRenderOffset(WeaponDirection.E, baseX + 8.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.W, baseX + 8.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.N, baseX + 8.0f, baseY + 8f);
        setRenderOffset(WeaponDirection.NE, baseX + 8f, baseY + 10f);
        setRenderOffset(WeaponDirection.NW, baseX + 8f, baseY + 10f);

    }

    @Override
    public TipoMao getTipoMao() {
        return TipoMao.UMA_MAO;
    }

    @Override
    public int getMaxAmmo() {
        return maxAmmo;
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
    }

    @Override
    public void update(float delta) {
        updateFloatation(delta);
        timeSinceLastShot += delta;

        if (timeSinceLastShot >= 1 / fireRate)
            canShoot = true;

        if (currentState == WeaponState.SHOOTING) {
            shootingTime += delta;
            if (shootingTime >= SHOOTING_STATE_DURATION) {
                currentState = WeaponState.IDLE;
                shootingTime = 0f;
            }
        }

        if (currentState == WeaponState.RELOADING) {
            reloadSystem.update(delta);
            if (!reloadSystem.isReloading()) {
                currentState = WeaponState.IDLE;
                reloadJustTriggered = false;
            }
        }
    }

    public void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);

        body = getMapa().world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.6f, 0.6f);

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
        return icon;
    }

    @Override
    public void shoot(Vector2 position, Vector2 direction) {
        if (currentState == WeaponState.RELOADING) {
            reloadSystem.cancel();
            currentState = WeaponState.SHOOTING;
            reloadJustTriggered = false;
        }

        if (!canShoot || ammo <= 0)
            return;

        // Normaliza a direção
        Vector2 normalizedDir = direction.cpy().nor();

        // Disparo
        new Projectile(getMapa(), position, normalizedDir.scl(45f), damage, getName());

        if (inventory != null && inventory.getPlayer() != null) {
            Robertinhoo player = inventory.getPlayer();
            Body playerBody = player.getBody();
            if (playerBody != null) {
                float recoilStrength = 1f;
                float recoilDuration = 0.25f;
                player.startRecoil(normalizedDir, recoilStrength, recoilDuration);
                Vector2 playerPos = player.getBody().getPosition();
                float angle = normalizedDir.angleDeg(); // ou ângulo da direção do tiro
                SmokeSystem.getInstance().spawn(playerPos, angle);
            }

        }

        // Ejeção da cápsula
        if (inventory != null && inventory.getPlayer() != null) {
            Robertinhoo player = inventory.getPlayer();
            final Body playerBody = player.getBody();
            if (playerBody != null) {
                final Vector2 playerPos = playerBody.getPosition().cpy();
                final Vector2 shootDir = normalizedDir.cpy();

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        Vector2 safeDir = shootDir.cpy().nor();

                        float perpX = safeDir.y;
                        float perpY = -safeDir.x;
                        float backAmount = 0.15f;
                        float sideAmount = 0.25f;

                        Vector2 ejectionOffset = new Vector2(
                                safeDir.x * -backAmount + perpX * sideAmount,
                                safeDir.y * -backAmount + perpY * sideAmount);

                        Vector2 shellSpawnPos = playerPos.cpy().add(ejectionOffset);
                        float groundY = playerPos.y - 0.25f;
                        ShellSystem.getInstance().spawn(shellSpawnPos, safeDir, shellTexture, groundY);
                    }
                }, 0.1f);
            }
        }

        // Atualiza estado da arma
        shotTriggered = true;
        ammo--;
        canShoot = false;
        timeSinceLastShot = 0f;
        currentState = WeaponState.SHOOTING;
        shootingTime = 0f;

        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.DESERT_EAGLE_SHOOT, 0.8f);
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
    public DesertEagle copy() {
        return new DesertEagle(mapa, (int) position.x, (int) position.y, inventory);
    }

    @Override
    public String getName() {
        return "desertEagle";
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public WeaponSight getWeaponSight() {
        // Mira de ponto (precisa)
        WeaponSight.ConeSight sight = new WeaponSight.ConeSight();
        sight.color = new Color(1f, 0.5f, 0f, 0.3f);
        sight.lineWidth = 1f;
        sight.spreadAngle = 3f;
        sight.coneSegments = 8;
        return sight;
    }

    // Getters para o renderizador (usados na recarga)
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

    public boolean isReloading() {
        return reloadSystem.isReloading();
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