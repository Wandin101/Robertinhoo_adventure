
package io.github.some_example_name.MapConfig;

import java.util.Iterator;
import com.badlogic.gdx.Gdx;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Itens.CenarioItens.Grass;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Entities.SoulShopSystem.Soul;

import com.badlogic.gdx.physics.box2d.Body;

public class MapCleanUpManager {

    private final Mapa mapa;

    public MapCleanUpManager(Mapa mapa) {
        this.mapa = mapa;
    }

    public void clean(float deltaTime) {
        processProjectiles(deltaTime);
        processEnemies();
        processDestructibles(deltaTime);
        processSouls();
    }

    private void processProjectiles(float deltaTime) {
        Iterator<Projectile> it = mapa.getProjectiles().iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(deltaTime);
            if (p.isMarkedForDestruction()) {
                p.destroy();
                it.remove();
                Gdx.app.log("MapCleanupManager", "Projectile removido.");
            }
        }
    }

    private void processEnemies() {
        Iterator<Enemy> iterator = mapa.getEnemies().iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();

            if (enemy instanceof Ratinho) {
                Ratinho rat = (Ratinho) enemy;
                if (rat.isMarkedForDestruction()) {
                    Body b = rat.getBody();
                    if (b != null) {
                        mapa.world.destroyBody(b);
                    }
                    iterator.remove();
                    Gdx.app.log("MapCleanupManager", "Ratinho removido do jogo.");
                }
            } else if (enemy instanceof Castor) {
                Castor castor = (Castor) enemy;
                if (castor.isMarkedForDestruction()) {
                    Body b = castor.getBody();
                    if (b != null) {
                        mapa.world.destroyBody(b);
                    }
                    iterator.remove();
                    Gdx.app.log("MapCleanupManager", "Castor removido do jogo.");
                }
            }
        }
    }

    private void processDestructibles(float deltaTime) {
        for (Destructible d : mapa.getDestructibles()) {
            d.update(deltaTime);
        }

        Iterator<Destructible> destructibleIterator = mapa.getDestructibles().iterator();
        while (destructibleIterator.hasNext()) {
            Destructible d = destructibleIterator.next();

            if (d instanceof Barrel) {
                Barrel barrel = (Barrel) d;

                if (barrel.isBodyMarkedForDestruction()) {
                    barrel.destroyBody();
                    barrel.setBodyMarkedForDestruction(false);
                    Gdx.app.log("MapCleanupManager", "Body do barril destruído.");
                }

                if (barrel.isAnimationFinished() && barrel.isDestroyed()) {
                    destructibleIterator.remove();
                    Gdx.app.log("MapCleanupManager", "Barril removido da lista de destructibles.");
                }
            } else if (d instanceof Grass) {
                Grass grass = (Grass) d;
                if (grass.isDestroyed() && grass.isAnimationFinished()) {
                    if (grass.isBodyMarkedForDestruction()) {
                        grass.destroyBody();
                        grass.setBodyMarkedForDestruction(false);
                    }
                    destructibleIterator.remove();
                    Gdx.app.log("MapCleanUpManager", "Grass removida");
                }
            } else {
                if (d.isDestroyed()) {
                    destructibleIterator.remove();
                    Gdx.app.log("MapCleanupManager", "Destructible removido.");
                }
            }
        }
    }

    private void processSouls() {
        Iterator<Soul> it = mapa.getSouls().iterator();
        while (it.hasNext()) {
            Soul soul = it.next();
            if (soul.isMarkedForRemoval()) {
                soul.dispose(); // destrói corpo e libera textura
                it.remove();
                Gdx.app.log("MapCleanupManager", "Alma removida.");
            }
        }
    }
}
