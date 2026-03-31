package io.github.some_example_name.Entities.Itens.Weapon.Calibre12;

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
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon.TipoMao;
import io.github.some_example_name.Entities.Particulas.Shell.ShellSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Player.WeaponSight;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;

public class Calibre12 extends Weapon {
    private Inventory inventory;
    private Texture iconTexture;
    private float reloadTime = 0;
    private float reloadDuration = 3.0f;
    private int shotgunMaxAmmo = 6;
    protected Vector2 position;
    private Texture idleTexture;
    private Texture shootTexture;

    private float animationTime = 0f;
    private float shootingTime = 0f;
    private static final float SHOOTING_STATE_DURATION = 1f;
    private int reloadStage = 0;
    private int shellsInserted = 0;
    private boolean reloadInterrupted = false;
    private boolean waitingForNextShell = false;
    private float shellInsertionTimer = 0f;
    private float shellInsertionInterval = 0.5f;
    private int shellsToInsert = 0;
    private float stageTimer = 0f;
    private float stageDuration = 0f;
    private boolean reloadInProgress = false;
    private boolean reloadCockPlayed = false;
    private static TextureRegion shellTexture;
    private static boolean shellTextureLoaded = false;

    public Calibre12(Mapa mapa, float x, float y, Inventory inventory) {
        super();
        this.maxAmmo = shotgunMaxAmmo;
        this.ammo = this.maxAmmo;
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        this.inventory = inventory;
        this.fireRate = 0.8f;
        this.damage = 25f;
        this.iconTexture = new Texture("ITENS/12/12_icon.png");
        this.icon = new TextureRegion(iconTexture);

        createBody(this.position);

        this.gridWidth = 5;
        this.gridHeight = 2;

        // 🔥 Inicializa as células ocupadas como um retângulo cheio 5x2
        rebuildOccupiedCells();

        if (!shellTextureLoaded) {
            try {
                Texture tex = new Texture("ITENS/12/bala.png");
                shellTexture = new TextureRegion(tex);
                ShellSystem.getInstance().init(0.3f);
                shellTextureLoaded = true;
                System.out.println("✅ [Calibre12] Textura de cápsula carregada");
            } catch (Exception e) {
                System.err.println("❌ [Calibre12] Erro ao carregar bala.png: " + e.getMessage());
            }
        }

        this.setMuzzleOffset(WeaponDirection.N, new Vector2(2, 2));
        this.setMuzzleOffset(WeaponDirection.NE, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.NW, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.E, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.S, new Vector2(6, 01));
        this.setMuzzleOffset(WeaponDirection.SW, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.SE, new Vector2(6, 3.5f));
        this.setMuzzleOffset(WeaponDirection.W, new Vector2(7, -2));

        float baseX = -8f;
        float baseY = -12f;
        setRenderOffset(WeaponDirection.S, baseX + 6.0f, baseY + 20f);
        setRenderOffset(WeaponDirection.SW, baseX + 10f, baseY + 10f);
        setRenderOffset(WeaponDirection.SE, baseX + 8f, baseY + 10f);
        setRenderOffset(WeaponDirection.E, baseX + 15.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.W, baseX + 8.0f, baseY + 10f);
        setRenderOffset(WeaponDirection.N, baseX + 8.0f, baseY + 8f);
        setRenderOffset(WeaponDirection.NE, baseX + 8f, baseY + 10f);
        setRenderOffset(WeaponDirection.NW, baseX + 10f, baseY + 10f);
    }

    @Override
    public TipoMao getTipoMao() {
        return TipoMao.UMA_MAO;
    }

    @Override
    public int getMaxAmmo() {
        return shotgunMaxAmmo;
    }

    @Override
    public void reload() {
        if (currentState == WeaponState.RELOADING || inventory == null) {
            return;
        }

        if (currentState == WeaponState.SHOOTING) {
            currentState = WeaponState.IDLE;
            shootingTime = 0f;
        }

        String requiredType = "12gauge";
        int available = inventory.getAmmoCount(requiredType);
        int needed = maxAmmo - ammo;

        if (available <= 0 || needed <= 0) {
            return;
        }

        shellsToInsert = Math.min(needed, available);

        currentState = WeaponState.RELOADING;
        reloadStage = 0;
        stageTimer = 0f;
        stageDuration = 0.5f;
        reloadInProgress = true;
        reloadJustTriggered = true;
        reloadCockPlayed = false;

        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.SHOTGUN_RELOAD_TILT, 0.7f);
    }

    @Override
    public void update(float delta) {
        updateFloatation(delta);
        timeSinceLastShot += delta;

        if (currentState == WeaponState.SHOOTING) {
            shootingTime += delta;
            if (shootingTime >= SHOOTING_STATE_DURATION) {
                currentState = WeaponState.IDLE;
                shootingTime = 0f;
            }
        }

        if (timeSinceLastShot >= 1 / fireRate) {
            canShoot = true;
        }

        if (currentState == WeaponState.RELOADING && reloadInProgress) {
            stageTimer += delta;

            if (stageTimer >= stageDuration) {
                if (reloadStage == 0) {
                    reloadStage = 1;
                    shellsInserted = 0;
                    stageTimer = 0f;
                    stageDuration = 0.7f;

                } else if (reloadStage == 1) {
                    if (shellsInserted >= shellsToInsert) {
                        reloadStage = 2;
                        stageTimer = 0f;
                        stageDuration = 0.5f;
                        return;
                    }

                    if (insertShell()) {
                        shellsInserted++;
                        if (shellsInserted >= shellsToInsert || ammo >= maxAmmo) {
                            reloadStage = 2;
                            stageTimer = 0f;
                            stageDuration = 0.5f;
                        } else {
                            stageTimer = 0f;
                        }
                    } else {
                        reloadStage = 2;
                        stageTimer = 0f;
                        stageDuration = 0.5f;
                    }

                } else if (reloadStage == 2) {
                    currentState = WeaponState.IDLE;
                    reloadInProgress = false;
                    reloadStage = 0;
                    shellsInserted = 0;
                    stageTimer = 0f;
                }
            }

            if (reloadStage == 2 && !reloadCockPlayed) {
                AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.SHOTGUN_COCK, 0.7f);
                reloadCockPlayed = true;
            }
        }
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
        return icon;
    }

    @Override
    public void shoot(Vector2 position, Vector2 direction) {
        if (currentState == WeaponState.RELOADING) {
            currentState = WeaponState.SHOOTING;
            reloadTime = 0;
            reloadStage = 0;
            shellsInserted = 0;
            waitingForNextShell = false;
        }

        if (!canShoot || ammo <= 0)
            return;

        int pelletCount = 8;
        float spreadAngle = 20f;
        for (int i = 0; i < pelletCount; i++) {
            float angleVariation = (float) Math.toRadians(spreadAngle * (Math.random() - 0.5));
            Vector2 pelletDir = new Vector2(direction).rotateRad(angleVariation);
            new Projectile(getMapa(), position, pelletDir.nor().scl(30f), damage * 0.7f, getName());
        }

        if (inventory != null && inventory.getPlayer() != null) {
            Robertinhoo player = inventory.getPlayer();
            final Body playerBody = player.getBody();
            if (playerBody != null) {
                float knockbackForce = 12.0f;
                final Vector2 recoilForce = new Vector2(direction).scl(-knockbackForce);
                playerBody.applyForceToCenter(recoilForce, true);

                final Vector2 playerPos = playerBody.getPosition().cpy();
                final Vector2 shootDir = direction.cpy();

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
                }, 0.5f);
            }
        }

        shotTriggered = true;
        ammo--;
        canShoot = false;
        timeSinceLastShot = 0f;
        currentState = WeaponState.SHOOTING;

        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.SHOTGUN_SHOOT, 0.8f);
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.SHOTGUN_COCK, 0.7f);
            }
        }, 0.35f);
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

    // 🔥 O método rotate() herdado de Weapon já faz tudo (troca dimensões,
    // alterna rotation e reconstrói occupiedCells). Se precisar de algo extra,
    // descomente e chame super.rotate():
    /*
     * @Override
     * public void rotate() {
     * super.rotate();
     * // lógica adicional, se houver
     * }
     */

    @Override
    public void destroyBody() {
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }

    @Override
    public Calibre12 copy() {
        Calibre12 copy = new Calibre12(mapa, (int) position.x, (int) position.y, inventory);
        return copy;
    }

    @Override
    public String getName() {
        return "Calibre12";
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public WeaponSight getWeaponSight() {
        WeaponSight.ConeSight sight = new WeaponSight.ConeSight();
        sight.color = new Color(0.9f, 0.9f, 0.9f, 0.2f);
        sight.lineWidth = 1f;
        sight.spreadAngle = 30f;
        sight.coneSegments = 8;
        return sight;
    }

    public float getReloadDuration() {
        return reloadDuration;
    }

    private boolean insertShell() {
        if (inventory == null || ammo >= maxAmmo) {
            return false;
        }

        String requiredType = "12gauge";
        int available = inventory.getAmmoCount(requiredType);

        if (available <= 0) {
            return false;
        }

        boolean consumed = inventory.consumeAmmoOneByOne(requiredType, 1);

        if (!consumed) {
            return false;
        }

        int oldAmmo = ammo;
        ammo = Math.min(ammo + 1, maxAmmo);

        if (ammo > oldAmmo) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.SHOTGUN_RELOAD_INSERT, 0.8f);
            return true;
        } else {
            return false;
        }
    }

    public float getReloadTime() {
        return reloadTime;
    }

    public int getReloadStage() {
        return reloadStage;
    }

    public int getShellsInserted() {
        return shellsInserted;
    }

    public int getShellsToInsert() {
        return shellsToInsert;
    }

    public float getStageProgress() {
        return stageDuration > 0 ? stageTimer / stageDuration : 0;
    }

    public boolean isWaitingForNextShell() {
        return waitingForNextShell;
    }

    public void dispose() {
        if (idleTexture != null) {
            idleTexture.dispose();
        }
        if (shootTexture != null && shootTexture != idleTexture) {
            shootTexture.dispose();
        }
        if (iconTexture != null) {
            iconTexture.dispose();
        }

        if (shellTexture != null && shellTexture.getTexture() != null) {
            shellTexture.getTexture().dispose();
            shellTexture = null;
            shellTextureLoaded = false;
        }
    }
}