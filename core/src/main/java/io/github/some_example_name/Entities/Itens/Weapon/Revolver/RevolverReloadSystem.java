package io.github.some_example_name.Entities.Itens.Weapon.Revolver;

import io.github.some_example_name.Entities.Itens.Weapon.IReloadSystem;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

public class RevolverReloadSystem implements IReloadSystem {

    // Configurações específicas do revólver
    private static final int OPEN_FRAMES = 9;
    private static final int INSERT_FRAMES = 4;
    private static final int CLOSE_FRAMES = 4;
    private static final float FRAME_DURATION = WeaponAnimations.REVOLVER_RELOAD_FRAME_DURATION;

    // Estados
    private boolean active = false;
    private int stage = 0; // 0=abertura, 1=inserção, 2=fechamento
    private int shellsInserted = 0;
    private int shellsToInsert = 0;
    private float timer = 0f;
    private float duration = 0f;

    // Controle de sons
    private boolean openSoundPlayed = false;
    private boolean clickSoundPlayed = false;

    private Weapon weapon;
    private Inventory inventory;
    private String ammoType = "9mm";

    @Override
    public void startReload(Weapon weapon, int maxAmmo, int currentAmmo) {
        this.weapon = weapon;
        this.inventory = weapon.getInventory();

        int needed = maxAmmo - currentAmmo;
        if (needed <= 0)
            return;

        int available = inventory.getAmmoCount(ammoType);
        if (available <= 0)
            return;

        shellsToInsert = Math.min(needed, available);

        // Inicia a recarga
        active = true;
        stage = 0;
        timer = 0f;
        duration = OPEN_FRAMES * FRAME_DURATION;
        shellsInserted = 0;
        openSoundPlayed = false;
        clickSoundPlayed = false;
    }

    @Override
    public void update(float delta) {
        if (!active)
            return;

        timer += delta;

        // Som de abertura no quinto frame (índice 4)
        if (stage == 0 && !openSoundPlayed && (timer / duration) >= (4f / OPEN_FRAMES)) {
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.REVOLVER_RELOAD_OPEN, 0.8f);
            openSoundPlayed = true;
        }

        if (timer >= duration) {
            if (stage == 0) {
                // Abertura → inserção
                stage = 1;
                timer = 0f;
                duration = INSERT_FRAMES * FRAME_DURATION;
                shellsInserted = 0;
            } else if (stage == 1) {
                // Inserção
                if (shellsInserted < shellsToInsert) {
                    if (consumeAmmo()) {
                        shellsInserted++;
                        if (shellsInserted < shellsToInsert) {
                            timer = 0f; // repete o ciclo de inserção
                        } else {
                            // Todas inseridas → fechamento
                            stage = 2;
                            timer = 0f;
                            duration = CLOSE_FRAMES * FRAME_DURATION;
                        }
                    } else {
                        // Sem munição → fecha
                        stage = 2;
                        timer = 0f;
                        duration = CLOSE_FRAMES * FRAME_DURATION;
                    }
                } else {
                    // Caso de borda (já inseriu todas) → fecha
                    stage = 2;
                    timer = 0f;
                    duration = CLOSE_FRAMES * FRAME_DURATION;
                }
            } else if (stage == 2) {
                if (!clickSoundPlayed && timer >= duration - 0.05f) {
                    AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.REVOLVER_RELOAD_CLICK, 0.8f);
                    clickSoundPlayed = true;
                }
                // Finaliza a recarga
                active = false;
                // Não reseta os valores (shellsInserted, stage, timer, duration) para que sejam
                // lidos depois
            }
        }
    }

    private boolean consumeAmmo() {
        if (inventory == null)
            return false;
        int available = inventory.getAmmoCount(ammoType);
        if (available <= 0)
            return false;

        boolean consumed = inventory.consumeAmmoOneByOne(ammoType, 1);
        if (!consumed)
            return false;

        // ✅ Aumenta a munição da arma AGORA (como no código original)
        int currentAmmo = weapon.getCurrentAmmo();
        int maxAmmo = weapon.getMaxAmmo();
        if (currentAmmo < maxAmmo) {
            weapon.setCurrentAmmo(currentAmmo + 1);
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.REVOLVER_RELOAD_INSERT, 0.8f);
            return true;
        }
        return false;
    }

    @Override
    public boolean isReloading() {
        return active;
    }

    @Override
    public int getCurrentStage() {
        return stage;
    }

    @Override
    public float getStageProgress() {
        return duration > 0 ? timer / duration : 0;
    }

    @Override
    public int getShellsInserted() {
        return shellsInserted;
    }

    @Override
    public int getShellsToInsert() {
        return shellsToInsert;
    }

    @Override
    public void cancel() {
        active = false;
        stage = 0;
        timer = 0f;
        shellsInserted = 0;
        shellsToInsert = 0;
        openSoundPlayed = false;
        clickSoundPlayed = false;
    }

    @Override
    public void dispose() {
        // Nada específico a liberar
    }
}