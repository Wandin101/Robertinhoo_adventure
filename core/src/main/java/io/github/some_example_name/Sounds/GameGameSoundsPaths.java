package io.github.some_example_name.Sounds;

import com.badlogic.gdx.Gdx;

public class GameGameSoundsPaths {

    // === SONS DE EFEITO ===
    public static class Sounds {
        // Combate
        public static final String PARRY_SUCCESS = "parry_success";
        public static final String MELEE_ATACK = "melee_attack";

        // Passos
        public static final String FOOTSTEP_GRASS = "passos_grama";

        public static final String BLOOD_SPLASH = "blood_splash";
        public static final String BLOOD_POOL = "blood_pool";

        public static final String SHOTGUN_SHOOT = "tiro12";
        public static final String SHOTGUN_COCK = "punheta";
        public static final String SHOTGUN_RELOAD_INSERT = "recarga";
        public static final String SHOTGUN_RELOAD_TILT = "inclinacao";

        // Dentro da classe Sounds
        public static final String REVOLVER_SHOOT = "revolver_shoot";
        public static final String REVOLVER_RELOAD_OPEN = "revolver_reload_open";
        public static final String REVOLVER_RELOAD_INSERT = "revolver_reload_insert";
        public static final String REVOLVER_RELOAD_ROTATE = "revolver_reload_rotate";
        public static final String REVOLVER_RELOAD_CLICK = "revolver_reload_click";

        // Sons da Desert Eagle
        public static final String DESERT_EAGLE_SHOOT = "desert_shoot";
        public static final String DESERT_EAGLE_RELOAD_OPEN = "desert_open";
        public static final String DESERT_EAGLE_RELOAD_INSERT = "desert_insert";
        public static final String DESERT_EAGLE_RELOAD_CLICK = "desert_click";

        public static final String BULLET_GROUND_1 = "bullet_ground_1";
        public static final String BULLET_GROUND_2 = "bullet_ground_2";
        public static final String BULLET_GROUND_3 = "bullet_ground_3";

        // public static final String MELEE_ATTACK = "melee_attack";
        // public static final String RANGED_ATTACK = "ranged_attack";
        // public static final String RELOAD = "reload";
        // public static final String WEAPON_SWITCH = "weapon_switch";

        // // Personagem
        // public static final String PLAYER_HURT = "player_hurt";
        // public static final String PLAYER_DEATH = "player_death";
        // public static final String PLAYER_HEAL = "player_heal";

        // // Inimigos
        // public static final String ENEMY_HURT = "enemy_hurt";
        // public static final String ENEMY_DEATH = "enemy_death";

        // // Ambiente
        // public static final String ITEM_PICKUP = "item_pickup";
        // public static final String DOOR_OPEN = "door_open";
        // public static final String CHEST_OPEN = "chest_open";

        // // UI
        // Inventory sounds
        public static final String WEAPON_SELECTED = "arma_selecionada";
        public static final String ITEM_PLACE_ERROR = "erro_incerir_item";
        public static final String ITEM_PLACE_SUCCESS = "sucesso_incerir_item";
        public static final String ITEM_DRAG_START = "arrasta_item_selecionado";
    }

    // === MÚSICAS ===
    public static class Music {
        public static final String MAIN_THEME = "main_theme";
        public static final String BOSS_BATTLE = "boss_battle";
        public static final String MENU_MUSIC = "menu_music";
        public static final String FOOTSTEP_GRASS = "passos_grama";

    }

    public static class Ambient {
        public static final String FOGUEIRA_SOUND = "fogueira_sound";
    }

    public static class Voices {
        public static final String ESMERALDA_NEUTRAL_1 = "esmeralda_neutral_1";
        public static final String ESMERALDA_NEUTRAL_2 = "esmeralda_neutral_2";
        public static final String ESMERALDA_NEUTRAL_3 = "esmeralda_neutral_3";
        public static final String ESMERALDA_NEUTRAL_4 = "esmeralda_neutral_4";
    }

    public static void loadAllAssets() {
        AudioManager audio = AudioManager.getInstance();

        // === CARREGAR SONS ===
        // Combate
        audio.loadSound(Sounds.PARRY_SUCCESS, "Sounds/Parry.wav");
        audio.loadSound(Sounds.FOOTSTEP_GRASS, "Sounds/passos_grama.wav");
        audio.loadSound(Sounds.MELEE_ATACK, "Sounds/melee.wav");
        audio.loadSound(Sounds.BLOOD_SPLASH, "Sounds/splash.wav");
        audio.loadSound(Sounds.BLOOD_POOL, "Sounds/pool.wav");

        audio.loadSound(Sounds.SHOTGUN_SHOOT, "Sounds/tiro12.wav");
        audio.loadSound(Sounds.SHOTGUN_COCK, "Sounds/punheta12.wav");
        audio.loadSound(Sounds.SHOTGUN_RELOAD_INSERT, "Sounds/recarga.wav");
        audio.loadSound(Sounds.SHOTGUN_RELOAD_TILT, "Sounds/inclinacao.wav");

        // audio.loadSound(Sounds.MELEE_ATTACK, "sounds/combat/melee_attack.wav");
        // audio.loadSound(Sounds.RANGED_ATTACK, "sounds/combat/ranged_attack.wav");
        // audio.loadSound(Sounds.RELOAD, "sounds/combat/reload.wav");
        // audio.loadSound(Sounds.WEAPON_SWITCH, "sounds/combat/weapon_switch.wav");

        // // Personagem
        // audio.loadSound(Sounds.PLAYER_HURT, "sounds/player/player_hurt.wav");
        // audio.loadSound(Sounds.PLAYER_DEATH, "sounds/player/player_death.wav");
        // audio.loadSound(Sounds.PLAYER_HEAL, "sounds/player/player_heal.wav");

        // // Inimigosddddddddddddd
        // audio.loadSound(Sounds.ENEMY_HURT, "sounds/enemies/enemy_hurt.wav");
        // audio.loadSound(Sounds.ENEMY_DEATH, "sounds/enemies/enemy_death.wav");

        // // Ambiente
        // audio.loadSound(Sounds.ITEM_PICKUP, "sounds/environment/item_pickup.wav");
        // audio.loadSound(Sounds.DOOR_OPEN, "sounds/environment/door_open.wav");
        // audio.loadSound(Sounds.CHEST_OPEN, "sounds/environment/chest_open.wav");
        audio.loadAmbient(Ambient.FOGUEIRA_SOUND, "Sounds/fogueiraSound.ogg");

        // // UI
        // audio.loadSound(Sounds.UI_SELECT, "sounds/ui/select.wav");
        // audio.loadSound(Sounds.UI_CONFIRM, "sounds/ui/confirm.wav");
        // audio.loadSound(Sounds.UI_BACK, "sounds/ui/back.wav");

        // // === CARREGAR MÚSICAS ===
        // audio.loadMusic(Music.MAIN_THEME, "music/main_theme.mp3");
        // audio.loadMusic(Music.BOSS_BATTLE, "music/boss_battle.mp3");
        // audio.loadMusic(Music.MENU_MUSIC, "music/menu_music.mp3");
        audio.loadMusic(Music.FOOTSTEP_GRASS, "Sounds/passos_grama.wav");
        audio.loadSound(Voices.ESMERALDA_NEUTRAL_1, "sounds/voices/Esmeralda_voice_line_neutral2.ogg");
        audio.loadSound(Voices.ESMERALDA_NEUTRAL_2, "sounds/voices/Esmeralda_voice_line_neutral3.ogg");
        audio.loadSound(Voices.ESMERALDA_NEUTRAL_3, "sounds/voices/Esmeralda_voice_line_neutral4.ogg");
        audio.loadSound(Voices.ESMERALDA_NEUTRAL_4, "sounds/voices/Esmeralda_voice_line_neutral5.ogg");

        audio.loadSound(Sounds.REVOLVER_SHOOT, "Sounds/revolver/revolver_shoot.ogg");
        audio.loadSound(Sounds.REVOLVER_RELOAD_OPEN, "Sounds/revolver/revolver_reload_open.ogg");
        audio.loadSound(Sounds.REVOLVER_RELOAD_INSERT, "Sounds/revolver/revolver_reload_insert.ogg");
        audio.loadSound(Sounds.REVOLVER_RELOAD_ROTATE, "Sounds/revolver/revolver_reload_rotate.ogg");
        audio.loadSound(Sounds.REVOLVER_RELOAD_CLICK, "Sounds/revolver/revolver_reload_click.ogg");

        audio.loadSound(Sounds.DESERT_EAGLE_SHOOT, "Sounds/desert/desert_shoot.ogg");
        audio.loadSound(Sounds.DESERT_EAGLE_RELOAD_OPEN, "Sounds/desert/desert_open.ogg");
        audio.loadSound(Sounds.DESERT_EAGLE_RELOAD_INSERT, "Sounds/desert/desert_incert.ogg");
        audio.loadSound(Sounds.DESERT_EAGLE_RELOAD_CLICK, "Sounds/desert/click_desert.ogg");

        audio.loadSound(Sounds.BULLET_GROUND_1, "Sounds/bullet_caindo1.ogg");
        audio.loadSound(Sounds.BULLET_GROUND_2, "Sounds/bullet_caindo2.ogg");
        audio.loadSound(Sounds.BULLET_GROUND_3, "Sounds/bullet_caindo3.ogg");
        audio.loadSound(Sounds.WEAPON_SELECTED, "Sounds/arma_selecionada.ogg");
        audio.loadSound(Sounds.ITEM_PLACE_ERROR, "Sounds/erro_incerir_item.ogg");
        audio.loadSound(Sounds.ITEM_PLACE_SUCCESS, "Sounds/sucesso_incerir_item.ogg");
        audio.loadSound(Sounds.ITEM_DRAG_START, "Sounds/arrasta_item_selecionado.ogg");
        Gdx.app.log("ASSETS", "Todos os assets de áudio carregados!");
    }
}