package io.github.some_example_name.Sounds;

import com.badlogic.gdx.Gdx;

public class GameGameSoundsPaths {

    // === SONS DE EFEITO ===
    public static class Sounds {
        // Combate
        public static final String PARRY_SUCCESS = "parry_success";
        public static final String MELEE_ATACK = "melee_attack";

        //Passos
        public static final String FOOTSTEP_GRASS = "passos_grama";

        public static final String BLOOD_SPLASH = "blood_splash";
        public static final String BLOOD_POOL = "blood_pool";


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
        // public static final String UI_SELECT = "ui_select";
        // public static final String UI_CONFIRM = "ui_confirm";
        // public static final String UI_BACK = "ui_back";
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

    public static void loadAllAssets() {
        AudioManager audio = AudioManager.getInstance();

        // === CARREGAR SONS ===
        // Combate
        audio.loadSound(Sounds.PARRY_SUCCESS, "Sounds/Parry.wav");
        audio.loadSound(Sounds.FOOTSTEP_GRASS, "Sounds/passos_grama.wav");
        audio.loadSound(Sounds.MELEE_ATACK, "Sounds/melee.wav");
        audio.loadSound(Sounds.BLOOD_SPLASH, "Sounds/splash.wav");
        audio.loadSound(Sounds.BLOOD_POOL, "Sounds/pool.wav");
        
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
        audio.loadAmbient(Ambient.FOGUEIRA_SOUND, "Sounds/fogueiraSound.wav");

        // // UI
        // audio.loadSound(Sounds.UI_SELECT, "sounds/ui/select.wav");
        // audio.loadSound(Sounds.UI_CONFIRM, "sounds/ui/confirm.wav");
        // audio.loadSound(Sounds.UI_BACK, "sounds/ui/back.wav");

        // // === CARREGAR MÚSICAS ===
        // audio.loadMusic(Music.MAIN_THEME, "music/main_theme.mp3");
        // audio.loadMusic(Music.BOSS_BATTLE, "music/boss_battle.mp3");
        // audio.loadMusic(Music.MENU_MUSIC, "music/menu_music.mp3");
         audio.loadMusic(Music.FOOTSTEP_GRASS, "Sounds/passos_grama.wav");

        Gdx.app.log("ASSETS", "Todos os assets de áudio carregados!");
    }
}