package io.github.some_example_name.Interface.Npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;

public class EsmeraldaDialogue implements NpcDialogue {
    private Texture talkSheet;
    private Texture idleSheet;
    private Animation<TextureRegion> talkAnimation;
    private Animation<TextureRegion> idleAnimation;
    private JsonValue dialogueData;
    private String[] talkResponses;
    private String[] welcomeMessages;
    private String[] goodbyeMessages;
    private Random random = new Random();
    private boolean talking = true;

    // Listas de índices disponíveis para cada categoria (para evitar repetição)
    private List<Integer> availableWelcomeIndices;
    private List<Integer> availableTalkIndices;
    private List<Integer> availableGoodbyeIndices;

    // Controle de vozes
    private String[] voiceNames;
    private int lastVoiceIndex = -1;

    private enum State {
        WELCOME, MENU, TALK, SHOP, GOODBYE
    }

    private State state = State.WELCOME;
    private String currentText;
    private String[] menuOptions = new String[3];
    private boolean waitingForChoice = false;

    public EsmeraldaDialogue() {
        // Carrega sprite sheets
        talkSheet = new Texture("npcs/Esmeralda/Esmeralda_dialoge_neutral.png");
        int talkCols = 7;
        int talkRows = 1;
        TextureRegion[][] talkTmp = TextureRegion.split(talkSheet,
                talkSheet.getWidth() / talkCols,
                talkSheet.getHeight() / talkRows);
        TextureRegion[] talkFrames = new TextureRegion[talkCols];
        System.arraycopy(talkTmp[0], 0, talkFrames, 0, talkCols);
        talkAnimation = new Animation<>(0.15f, talkFrames);
        talkAnimation.setPlayMode(Animation.PlayMode.LOOP);

        idleSheet = new Texture("npcs/Esmeralda/EsmeraldaInterfaceIdle.png");
        int idleCols = 8; // ajuste conforme sua imagem
        int idleRows = 1;
        TextureRegion[][] idleTmp = TextureRegion.split(idleSheet,
                idleSheet.getWidth() / idleCols,
                idleSheet.getHeight() / idleRows);
        TextureRegion[] idleFrames = new TextureRegion[idleCols];
        System.arraycopy(idleTmp[0], 0, idleFrames, 0, idleCols);
        idleAnimation = new Animation<>(0.2f, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Inicializa vozes
        voiceNames = new String[] {
                GameGameSoundsPaths.Voices.ESMERALDA_NEUTRAL_1,
                GameGameSoundsPaths.Voices.ESMERALDA_NEUTRAL_2,
                GameGameSoundsPaths.Voices.ESMERALDA_NEUTRAL_3,
                GameGameSoundsPaths.Voices.ESMERALDA_NEUTRAL_4
        };

        loadJson();
    }

    private void loadJson() {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("npcs/Esmeralda/EsmeraldaDialoge.json"));
            JsonValue esmeralda = root.get("npcs").get("esmeralda");

            // Welcome messages (array)
            JsonValue welcomeArray = esmeralda.get("welcome");
            welcomeMessages = new String[welcomeArray.size];
            for (int i = 0; i < welcomeArray.size; i++) {
                welcomeMessages[i] = welcomeArray.getString(i);
            }

            // Goodbye messages (array)
            JsonValue goodbyeArray = esmeralda.get("goodbye");
            goodbyeMessages = new String[goodbyeArray.size];
            for (int i = 0; i < goodbyeArray.size; i++) {
                goodbyeMessages[i] = goodbyeArray.getString(i);
            }

            String shopNotAvailable = esmeralda.getString("shop_not_available");

            JsonValue options = esmeralda.get("options");
            menuOptions[0] = options.getString("talk");
            menuOptions[1] = options.getString("shop");
            menuOptions[2] = options.getString("leave");

            JsonValue responses = esmeralda.get("talk_responses");
            talkResponses = new String[responses.size];
            for (int i = 0; i < responses.size; i++) {
                talkResponses[i] = responses.getString(i);
            }

            // Inicializa listas de índices disponíveis
            resetAvailableIndices();

            // Seleciona a primeira mensagem de boas-vindas
            currentText = getNextWelcomeMessage();
            playRandomVoice();
        } catch (Exception e) {
            e.printStackTrace();
            currentText = "Olá, viajante!";
            menuOptions = new String[] { "Conversar", "Comprar", "Sair" };
            talkResponses = new String[] { "Que bom ver você!" };
            welcomeMessages = new String[] { "Olá!" };
            goodbyeMessages = new String[] { "Tchau!" };
        }
    }

    private void resetAvailableIndices() {
        availableWelcomeIndices = new ArrayList<>();
        for (int i = 0; i < welcomeMessages.length; i++) {
            availableWelcomeIndices.add(i);
        }
        Collections.shuffle(availableWelcomeIndices, random);

        availableTalkIndices = new ArrayList<>();
        for (int i = 0; i < talkResponses.length; i++) {
            availableTalkIndices.add(i);
        }
        Collections.shuffle(availableTalkIndices, random);

        availableGoodbyeIndices = new ArrayList<>();
        for (int i = 0; i < goodbyeMessages.length; i++) {
            availableGoodbyeIndices.add(i);
        }
        Collections.shuffle(availableGoodbyeIndices, random);
    }

    private String getNextWelcomeMessage() {
        if (availableWelcomeIndices.isEmpty()) {
            // Reinicia o ciclo
            for (int i = 0; i < welcomeMessages.length; i++) {
                availableWelcomeIndices.add(i);
            }
            Collections.shuffle(availableWelcomeIndices, random);
        }
        int index = availableWelcomeIndices.remove(0);
        return welcomeMessages[index];
    }

    private String getNextTalkResponse() {
        if (availableTalkIndices.isEmpty()) {
            for (int i = 0; i < talkResponses.length; i++) {
                availableTalkIndices.add(i);
            }
            Collections.shuffle(availableTalkIndices, random);
        }
        int index = availableTalkIndices.remove(0);
        return talkResponses[index];
    }

    private String getNextGoodbyeMessage() {
        if (availableGoodbyeIndices.isEmpty()) {
            for (int i = 0; i < goodbyeMessages.length; i++) {
                availableGoodbyeIndices.add(i);
            }
            Collections.shuffle(availableGoodbyeIndices, random);
        }
        int index = availableGoodbyeIndices.remove(0);
        return goodbyeMessages[index];
    }

    private void playRandomVoice() {
        if (voiceNames.length == 0)
            return;
        int index;
        do {
            index = random.nextInt(voiceNames.length);
        } while (index == lastVoiceIndex && voiceNames.length > 1);
        lastVoiceIndex = index;
        AudioManager.getInstance().playSound(voiceNames[index], 0.7f);
    }

    @Override
    public Animation<TextureRegion> getFaceAnimation() {
        return talking ? talkAnimation : idleAnimation;
    }

    public void setTalking(boolean talking) {
        this.talking = talking;
    }

    @Override
    public String getCurrentText() {
        return currentText;
    }

    @Override
    public boolean next() {
        if (state == State.WELCOME) {
            state = State.MENU;
            currentText = "O que você quer?";
            waitingForChoice = true;
            setTalking(true);
            playRandomVoice();
            return true;
        } else if (state == State.TALK || state == State.SHOP) {
            state = State.MENU;
            currentText = "O que você quer?";
            waitingForChoice = true;
            setTalking(true);
            playRandomVoice();
            return true;
        } else if (state == State.GOODBYE) {
            return false;
        }
        return true;
    }

    public void chooseOption(int option) {
        if (!waitingForChoice)
            return;
        waitingForChoice = false;
        setTalking(true);

        switch (option) {
            case 0:
                state = State.TALK;
                currentText = getNextTalkResponse();
                playRandomVoice();
                break;
            case 1:
                state = State.SHOP;
                currentText = "A loja ainda não está pronta, volte depois.";
                playRandomVoice();
                break;
            case 2:
                state = State.GOODBYE;
                currentText = getNextGoodbyeMessage();
                playRandomVoice();
                break;
        }
    }

    public boolean isWaitingForChoice() {
        return waitingForChoice;
    }

    public String[] getMenuOptions() {
        return menuOptions;
    }

    @Override
    public void reset() {
        state = State.WELCOME;
        // Reinicia as listas de índices para começar um novo ciclo
        resetAvailableIndices();
        currentText = getNextWelcomeMessage();
        waitingForChoice = false;
        setTalking(true);
        playRandomVoice();
    }

    @Override
    public void dispose() {
        if (talkSheet != null)
            talkSheet.dispose();
        if (idleSheet != null)
            idleSheet.dispose();
    }
}