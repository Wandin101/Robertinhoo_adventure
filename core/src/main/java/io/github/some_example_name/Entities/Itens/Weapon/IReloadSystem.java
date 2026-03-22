package io.github.some_example_name.Entities.Itens.Weapon;

public interface IReloadSystem {
    /**
     * Inicia o processo de recarga.
     * 
     * @param weapon      A arma a ser recarregada (permite acesso ao inventário,
     *                    munição, etc.)
     * @param maxAmmo     Capacidade máxima do carregador
     * @param currentAmmo Munição atual
     */
    void startReload(Weapon weapon, int maxAmmo, int currentAmmo);

    /**
     * Atualiza a lógica de recarga, chamada a cada frame.
     * 
     * @param delta Tempo desde o último frame
     */
    void update(float delta);

    /** Indica se ainda está recarregando. */
    boolean isReloading();

    /** Retorna o estágio atual da recarga (0,1,2,...). */
    int getCurrentStage();

    /** Progresso do estágio atual (0 a 1). */
    float getStageProgress();

    /** Número de balas já inseridas neste ciclo. */
    int getShellsInserted();

    /** Total de balas a serem inseridas neste ciclo. */
    int getShellsToInsert();

    /** Interrompe a recarga, resetando o estado. */
    void cancel();

    /** Libera recursos (se necessário). */
    void dispose();
}