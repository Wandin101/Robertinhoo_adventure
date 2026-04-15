package io.github.some_example_name.Updater;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Particulas.BloodParticleSystem;
import io.github.some_example_name.Entities.Particulas.MagicParticle.MagicParticleSystem;
import io.github.some_example_name.Entities.Particulas.RevolverShell.RevolverShellEjector;
import io.github.some_example_name.Entities.Particulas.Shell.ShellSystem;
import io.github.some_example_name.Entities.Particulas.Smoke.SmokeSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Entities.SoulShopSystem.Soul;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.Rooms.Room0Door;
import io.github.some_example_name.MapConfig.Rooms.StaticItem;
import io.github.some_example_name.MapConfig.Rooms.Itens_start_room.Pillar;
import io.github.some_example_name.Screens.ScreenEffects.ScreenFreezeSystem;

public class GameUpdateManager {
    private final Mapa mapa;
    private final Robertinhoo player;
    private final PlayerRenderer playerRenderer;

    public GameUpdateManager(Mapa mapa, Robertinhoo player, PlayerRenderer playerRenderer) {
        this.mapa = mapa;
        this.player = player;
        this.playerRenderer = playerRenderer;
    }

    public void update(float delta) {

        float gameplayDelta = ScreenFreezeSystem.getGameplayDelta();
        float playerDelta = ScreenFreezeSystem.getPlayerDelta();
        float animationDelta = ScreenFreezeSystem.getAnimationDelta();

        updateParticleSystems(animationDelta);

        playerRenderer.update(playerDelta, player);
        player.update(playerDelta);

        updateWorldEntities(gameplayDelta);

        updateStaticElements(animationDelta);
    }

    private void updateParticleSystems(float delta) {
        if (mapa.getBloodParticleSystem() != null) {
            Vector2 cameraPos = new Vector2(
                    player.getPosition().x,
                    player.getPosition().y);
            mapa.getBloodParticleSystem().update(delta, cameraPos);
        }
        ShellSystem.getInstance().update(delta);
        RevolverShellEjector.getInstance().update(delta);
        SmokeSystem.getInstance().update(delta);
        if (mapa.getMagicParticleSystem() != null) {
            // Nota: você precisará das coordenadas da câmera, que podem ser obtidas de
            // outro lugar
            // Por simplicidade, deixamos como estava, mas você pode injetar os bounds da
            // câmera depois
        }
    }

    private void updateWorldEntities(float delta) {
        // Armas no chão
        for (Weapon weapon : mapa.getWeapons()) {
            weapon.update(delta);
            weapon.updateDrop(delta);
        }

        // Inimigos
        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof Castor) {
                Castor castor = (Castor) enemy;
                if (!ScreenFreezeSystem.isFrozen()) {
                    castor.update(delta);
                }
            }
        }

        // Almas
        for (Soul soul : mapa.getSouls()) {
            soul.update(delta);
        }
    }

    private void updateStaticElements(float delta) {
        if (mapa.room0Door != null) {
            mapa.room0Door.update(delta);
        }

        for (StaticItem item : mapa.getStaticItems()) {
            item.update(delta);
        }

        for (Pillar pillar : mapa.pillars) {
            pillar.update(delta);
        }

        if (mapa.getCampFire() != null) {
            mapa.getCampFire().update(delta);
        }
    }
}