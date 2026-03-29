package io.github.some_example_name.Entities.Itens.Weapon.DesertEagle;

import io.github.some_example_name.Entities.Itens.Weapon.IReloadSystem;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

public class DesertEagleReloadSystem implements IReloadSystem {

    // Configurações específicas da Desert Eagle (ajustar conforme animação)
    private static final int OPEN_FRAMES = 5; // frames de abertura
    private static final int INSERT_FRAMES = 4; // frames de inserção do pente
    private static final int CLOSE_FRAMES = 4; // frames de fechamento
    private static final float FRAME_DURATION = WeaponAnimations.DESERT_EAGLE_RELOAD_FRAME_DURATION;

    private boolean active = false;
    private int stage = 0;
    private int shellsToInsert = 0;
    private float timer = 0f;
    private float duration = 0f;

    private boolean openSoundPlayed = false;
    private boolean clickSoundPlayed = false;

    private Weapon weapon;
    private Inventory inventory;
    private String ammoType = "50MAGNUM";

    @Override
    public void startReload(Weapon weapon, int maxAmmo, int currentAmmo) {
        this.weapon = weapon;
        this.inventory = weapon.getInventory();
        if (inventory == null)
            return;

        int needed = maxAmmo - currentAmmo;
        if (needed <= 0)
            return;

        int available = inventory.getAmmoCount(ammoType);
        if (available <= 0)
            return;

        shellsToInsert = Math.min(needed, available);

        active = true;
        stage = 0;
        timer = 0f;
        duration = OPEN_FRAMES * FRAME_DURATION;
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
            AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.DESERT_EAGLE_RELOAD_OPEN, 0.8f);
            openSoundPlayed = true;
        }

        if (timer >= duration) {
            if (stage == 0) {
                // Abertura → inserção
                stage = 1;
                timer = 0f;
                duration = INSERT_FRAMES * FRAME_DURATION;
            } else if (stage == 1) {
                // Inserção: consome toda a munição de uma vez
                if (consumeAllAmmo()) {
                    // Se consumiu, vai para fechamento
                    stage = 2;
                    timer = 0f;
                    duration = CLOSE_FRAMES * FRAME_DURATION;
                } else {
                    // Se não conseguiu (sem munição), fecha também
                    stage = 2;
                    timer = 0f;
                    duration = CLOSE_FRAMES * FRAME_DURATION;
                }
            } else if (stage == 2) {
                if (!clickSoundPlayed && timer >= duration - 0.05f) {
                    AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.DESERT_EAGLE_RELOAD_CLICK, 0.8f);
                    clickSoundPlayed = true;
                }
                if (timer >= duration) {
                    active = false;
                }
            }
        }
    }

    private boolean consumeAllAmmo() {
        if (inventory == null)
            return false;

        int currentAmmo = weapon.getCurrentAmmo();
        int maxAmmo = weapon.getMaxAmmo();
        int needed = maxAmmo - currentAmmo;
        if (needed <= 0)
            return false;

        int available = inventory.getAmmoCount(ammoType);
        if (available <= 0)
            return false;

        int toConsume = Math.min(needed, available);
        if (toConsume <= 0)
            return false;

        // Consome toda a munição de uma vez
        inventory.consumeAmmo(ammoType, toConsume);
        weapon.setCurrentAmmo(currentAmmo + toConsume);

        // Toca som de inserção do pente (apenas uma vez)
        AudioManager.getInstance().playSound(GameGameSoundsPaths.Sounds.DESERT_EAGLE_RELOAD_INSERT, 0.8f);

        return true;
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
        return 0;
    } // não usado para essa arma

    @Override
    public int getShellsToInsert() {
        return shellsToInsert;
    }

    @Override
    public void cancel() {
        active = false;
        stage = 0;
        timer = 0f;
        shellsToInsert = 0;
        openSoundPlayed = false;
        clickSoundPlayed = false;
    }

    @Override
    public void dispose() {
    }
}