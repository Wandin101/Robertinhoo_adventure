package io.github.some_example_name.Entities.SoulShopSystem;

import com.badlogic.gdx.utils.Disposable;

public class SoulManager implements Disposable {
    private int souls = 0;
    private int totalCollected = 0;

    public void addSouls(int amount) {
        if (amount > 0) {
            souls += amount;
            totalCollected += amount;
            System.out.println("🟢 +" + amount + " almas. Total: " + souls);
        }
    }

    public boolean spendSouls(int amount) {
        if (souls >= amount) {
            souls -= amount;
            System.out.println("🔻 -" + amount + " almas. Restante: " + souls);
            return true;
        } else {
            System.out.println("❌ Almas insuficientes! Necessário: " + amount);
            return false;
        }
    }

    public int getSouls() {
        return souls;
    }

    public int getTotalCollected() {
        return totalCollected;
    }

    @Override
    public void dispose() {
        // Nada a liberar por enquanto
    }
}