package io.github.some_example_name.Entities.Itens.Weapon.Calibre12;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon.TipoMao;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Player.WeaponSight;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;

import com.badlogic.gdx.graphics.g2d.Animation;

public class Calibre12 extends Weapon {

    private Mapa mapa;
    private Inventory inventory;
    private Texture iconTexture;
    private float reloadTime = 0;
    private float reloadDuration = 3.0f; // Tempo maior de recarga
    private int shotgunMaxAmmo = 6; // Menor capacidade
    protected Vector2 position;
    private Texture idleTexture;
    private Texture shootTexture;

    private float animationTime = 0f;
    private Animation<TextureRegion> shootAnim;
    private float shootingTime = 0f;
    private static final float SHOOTING_STATE_DURATION = 1f;
    private int reloadStage = 0; // 0: inclinação, 1: inserção, 2: finalização
    private int shellsInserted = 0;
    private boolean reloadInterrupted = false;
    private boolean waitingForNextShell = false;
    private float shellInsertionTimer = 0f;
    private float shellInsertionInterval = 0.5f; // T
    private int shellsToInsert = 0;
    private float stageTimer = 0f;
    private float stageDuration = 0f;
    private boolean reloadInProgress = false;

    public Calibre12(Mapa mapa, float x, float y, Inventory inventory) {
        super();
        this.maxAmmo = shotgunMaxAmmo;
        this.ammo = this.maxAmmo;
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        this.inventory = inventory;
        this.fireRate = 0.8f; // Cadência mais lenta
        this.damage = 25f; // Dano maior por projetil
        this.iconTexture = new Texture("ITENS/12/12_icon.png");
        this.icon = new TextureRegion(iconTexture);

        createBody(this.position);

        this.gridWidth = 5;
        this.gridHeight = 2;
        this.occupiedCells = new Vector2[] {
                new Vector2(0, 0),
                new Vector2(1, 0),
                new Vector2(2, 0),
                new Vector2(0, 1),
                new Vector2(1, 1)
        };

        this.setMuzzleOffset(WeaponDirection.N, new Vector2(2, 2));
        this.setMuzzleOffset(WeaponDirection.NE, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.NW, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.E, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.S, new Vector2(6, 01));
        this.setMuzzleOffset(WeaponDirection.SW, new Vector2(6, 2));
        this.setMuzzleOffset(WeaponDirection.SE, new Vector2(6, 3.5f));
        this.setMuzzleOffset(WeaponDirection.W, new Vector2(7, -2));

        float baseX = -8f; // Move para esquerda
        float baseY = -12f; // Move para baixo
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
        return TipoMao.UMA_MAO; // Shotgun requer duas mãos
    }

    @Override
    public Vector2[] getOccupiedCells() {
        return occupiedCells;
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

        // Verificar se há munição disponível e precisa recarregar
        String requiredType = "12gauge";
        int available = inventory.getAmmoCount(requiredType);
        int needed = maxAmmo - ammo;

        if (available <= 0 || needed <= 0) {
            return;
        }

        // Calcular quantas cápsulas podemos inserir
        shellsToInsert = Math.min(needed, available);

        // Iniciar recarga
        currentState = WeaponState.RELOADING;
        reloadStage = 0; // Começar com inclinação
        stageTimer = 0f;
        stageDuration = 0.5f; // 0.5s para inclinação
        reloadInProgress = true;
        reloadJustTriggered = true;

        System.out.println("🔁 [Calibre12.reload] Iniciando recarga. Inserir " + shellsToInsert + " cápsulas");
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

                if (reloadStage == 0) { // Inclinação concluída
                    reloadStage = 1;
                    shellsInserted = 0;
                    stageTimer = 0f;
                    stageDuration = 0.7f; // 0.7s por cápsula
                    System.out.println("📐 [Calibre12] Inclinação concluída, iniciando inserção");

                } else if (reloadStage == 1) { // Inserção de cápsulas
                    // Verificar se já inserimos todas as cápsulas necessárias
                    if (shellsInserted >= shellsToInsert) {
                        reloadStage = 2;
                        stageTimer = 0f;
                        stageDuration = 0.5f; // 0.5s para finalização
                        System.out.println("✅ [Calibre12] Todas cápsulas inseridas (" + shellsInserted + ")");
                        return;
                    }

                    // Tentar inserir uma cápsula
                    if (insertShell()) {
                        shellsInserted++;
                        System.out.println("💾 [Calibre12] Cápsula " + shellsInserted + "/" + shellsToInsert +
                                " inserida. Munição: " + ammo + "/" + maxAmmo);

                        // Verificar se inseriu todas ou se a arma está cheia
                        if (shellsInserted >= shellsToInsert || ammo >= maxAmmo) {
                            reloadStage = 2;
                            stageTimer = 0f;
                            stageDuration = 0.5f; // 0.5s para finalização
                            System.out.println("✅ [Calibre12] Todas cápsulas inseridas (" + shellsInserted + ")");
                        } else {
                            // Resetar timer para próxima cápsula
                            stageTimer = 0f;
                        }
                    } else {
                        // Sem munição, finalizar
                        reloadStage = 2;
                        stageTimer = 0f;
                        stageDuration = 0.5f;
                        System.out.println("⚠️ [Calibre12] Sem munição, finalizando");
                    }

                } else if (reloadStage == 2) { // Finalização
                    currentState = WeaponState.IDLE;
                    reloadInProgress = false;
                    reloadStage = 0;
                    shellsInserted = 0;
                    stageTimer = 0f;
                    System.out.println("🏁 [Calibre12] Recarga finalizada. Munição: " + ammo + "/" + maxAmmo);
                }
            }
        }
    }

    public void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.7f, 0.7f); // Tamanho maior

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
            currentState = WeaponState.SHOOTING;
            reloadTime = 0;
            reloadStage = 0;
            shellsInserted = 0;
            waitingForNextShell = false;
            System.out.println("⚠️ [Calibre12] Recarga interrompida para atirar");
        }

        if (!canShoot || ammo <= 0) {
            return;
        }

        int pelletCount = 8;
        float spreadAngle = 20f;

        for (int i = 0; i < pelletCount; i++) {
            float angleVariation = (float) Math.toRadians(spreadAngle * (Math.random() - 0.5));
            Vector2 pelletDir = new Vector2(direction).rotateRad(angleVariation);
            new Projectile(mapa, position, pelletDir.nor().scl(30f), damage * 0.7f, getName());
        }

        shotTriggered = true;
        ammo--;
        canShoot = false;
        timeSinceLastShot = 0f;
        currentState = WeaponState.SHOOTING;
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
    public void rotate() {
        int temp = gridWidth;
        gridWidth = gridHeight;
        gridHeight = temp;
    }

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
        // Shotgun: cone de dispersão transparente
        WeaponSight.ConeSight sight = new WeaponSight.ConeSight();
        sight.color = new Color(0.9f, 0.9f, 0.9f, 0.2f); // Branco bem transparente
        sight.lineWidth = 1f;
        sight.spreadAngle = 30f; // Mesmo ângulo da dispersão do tiro
        sight.coneSegments = 8;
        return sight;
    }

    public float getReloadDuration() {
        return reloadDuration;
    }

    private boolean insertShell() {
        if (inventory == null || ammo >= maxAmmo) {
            System.out.println("❌ [Calibre12.insertShell] Inventário nulo ou arma cheia");
            return false;
        }

        String requiredType = "12gauge";
        int available = inventory.getAmmoCount(requiredType);

        if (available <= 0) {
            System.out.println("❌ [Calibre12.insertShell] Sem munição disponível");
            return false;
        }

        // Consumir UMA cápsula do inventário
        boolean consumed = inventory.consumeAmmoOneByOne(requiredType, 1);

        if (!consumed) {
            System.out.println("❌ [Calibre12.insertShell] Falha ao consumir munição do inventário");
            return false;
        }

        // Adicionar uma munição à arma
        int oldAmmo = ammo;
        ammo = Math.min(ammo + 1, maxAmmo);

        // Verificar se realmente aumentou
        if (ammo > oldAmmo) {
            System.out.println("✅ [Calibre12.insertShell] Munição aumentada de " + oldAmmo + " para " + ammo);
            return true;
        } else {
            System.out.println("⚠️ [Calibre12.insertShell] Munição não aumentou. Permanece em " + ammo);
            return false;
        }
    }

    // Adicione métodos getter:
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
    }
}