package io.github.some_example_name.Entities.Player;

public class StaminaSystem {
    private float currentStamina;
    private float maxStamina;
    private float normalRegenRate;
    private float exhaustedRegenRate;
    private boolean isExhausted;
    private float exhaustionRecoveryThreshold;
    private float tolerancePercentage = 0.1f; // 10% de tolerância

    public StaminaSystem(float maxStamina, float normalRegenRate, float exhaustedRegenRate, 
                        float exhaustionRecoveryThreshold) {
        this.maxStamina = maxStamina;
        this.currentStamina = maxStamina;
        this.normalRegenRate = normalRegenRate;
        this.exhaustedRegenRate = exhaustedRegenRate;
        this.exhaustionRecoveryThreshold = exhaustionRecoveryThreshold;
        this.isExhausted = false;
    }

    public void update(float deltaTime) {
        if (isExhausted) {
            currentStamina += exhaustedRegenRate * deltaTime;
            if (currentStamina >= maxStamina * exhaustionRecoveryThreshold) {
                isExhausted = false;
            }
        } else {
            currentStamina += normalRegenRate * deltaTime;
        }
        currentStamina = Math.min(currentStamina, maxStamina);
    }

    public boolean consumeStamina(float amount) {
        System.err.println("Tentando consumir stamina: " + amount + " | Current: " + currentStamina + " | Exhausted: " + isExhausted);
        
        // Se já está exausto, não permite nenhuma ação
        if (isExhausted) {
            System.err.println("Ação negada: já está exausto");
            return false;
        }
        
        // Calcula a tolerância (mínimo de stamina necessário para tentar a ação)
        float toleranceAmount = maxStamina * tolerancePercentage;
        
        // Verifica se tem stamina suficiente OU se está dentro da tolerância
        if (currentStamina >= amount) {
            // Tem stamina suficiente - consome normalmente
            currentStamina -= amount;
            System.err.println("Stamina consumida normalmente. Nova stamina: " + currentStamina);
        } else if (currentStamina >= toleranceAmount) {
            // Está dentro da tolerância - consome o que tem e entra em exaustão
            System.err.println("Dentro da tolerância - consumindo última stamina");
            currentStamina = 0;
            isExhausted = true;
        } else {
            // Abaixo da tolerância - não permite a ação e já está/entra em exaustão
            System.err.println("Abaixo da tolerância mínima - ação negada");
            if (currentStamina > 0) {
                currentStamina = 0;
            }
            isExhausted = true;
            return false;
        }
        
        // Verifica se entrou em exaustão após o consumo
        if (currentStamina <= 0) {
            isExhausted = true;
        }
        
        return true;
    }

    // Método auxiliar para verificar se PODE tentar uma ação (não garante execução)
    public boolean canAttemptAction(float amount) {
        if (isExhausted) return false;
        
        float toleranceAmount = maxStamina * tolerancePercentage;
        return currentStamina >= toleranceAmount;
    }

    // Método para ações que DEVEM ter stamina suficiente (como rolls)
    public boolean consumeStaminaStrict(float amount) {
        if (isExhausted || currentStamina < amount) {
            return false;
        }
        
        currentStamina -= amount;
        
        if (currentStamina <= 0) {
            isExhausted = true;
        }
        
        return true;
    }


    public float getCurrentStamina() {
        return currentStamina;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public boolean isExhausted() {
        return isExhausted;
    }
    
    public boolean canPerformAction() {
        return !isExhausted;
    }

        public boolean hasStamina(float amount) {
        return !isExhausted && currentStamina >= amount;
    }
}