package io.github.some_example_name.MapConfig.Rooms;

public enum RoomType {
    SPAWN,           // Sala de início - vazia, apenas spawn
    MERCHANT,        // Sala do mercador - NPC, itens à venda
    TREASURE,        // Sala do tesouro - baús, itens raros
    ARENA,           // Sala de arena - muitos inimigos
    BOSS,            // Sala do chefe
    PROCEDURAL,      // Sala procedural padrão (inimigos, barris, etc.)
    SAFE_HOUSE,      // Cabana/casa segura
    TRAP_ROOM,       // Sala de armadilhas
    PUZZLE_ROOM      // Sala de quebra-cabeças
}