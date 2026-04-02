package io.github.some_example_name.Interface.Npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.NpcInteractionHUD;
import io.github.some_example_name.Interface.ShopUI;

public class EsmeraldaDialogue implements NpcDialogue {
    private Texture talkSheet;
    private Texture idleSheet;
    private Animation<TextureRegion> talkAnimation;
    private Animation<TextureRegion> idleAnimation;
    private JsonValue dialogueData;
    private String[] talkResponses;
    private String[] welcomeMessages;
    private String[] goodbyeMessages;
    private String[] shopOpenMessages;
    private String[] shopPurchaseMessages;
    private String[] shopNoPurchaseMessages;
    private Random random = new Random();
    private boolean talking = true;
    private int selectedOption = 0;
    private ShopUI shopUI; // Referência para abrir a loja
    private Robertinhoo player;
    // Listas de índices disponíveis para cada categoria (para evitar repetição)
    private List<Integer> availableWelcomeIndices;
    private List<Integer> availableTalkIndices;
    private List<Integer> availableGoodbyeIndices;
    private String[] shopInsufficientFundsMessages;
    private State previousState; // para restaurar após a mensagem

    // Controle de vozes
    private String[] voiceNames;
    private int lastVoiceIndex = -1;

    private enum State {
        WELCOME, MENU, TALK, SHOP, SHOP_MESSAGE, GOODBYE
    }

    private State state = State.WELCOME;
    private String currentText;
    private String[] menuOptions = new String[3];
    private boolean waitingForChoice = false;

    public EsmeraldaDialogue(Robertinhoo player) {

        this.player = player;
        this.shopUI = new ShopUI(player); // l
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

            JsonValue shopOpenArray = esmeralda.get("shop_open_messages");
            shopOpenMessages = new String[shopOpenArray.size];
            for (int i = 0; i < shopOpenArray.size; i++)
                shopOpenMessages[i] = shopOpenArray.getString(i);

            JsonValue shopPurchaseArray = esmeralda.get("shop_purchase_messages");
            shopPurchaseMessages = new String[shopPurchaseArray.size];
            for (int i = 0; i < shopPurchaseArray.size; i++)
                shopPurchaseMessages[i] = shopPurchaseArray.getString(i);

            JsonValue shopNoPurchaseArray = esmeralda.get("shop_no_purchase_messages");
            shopNoPurchaseMessages = new String[shopNoPurchaseArray.size];
            for (int i = 0; i < shopNoPurchaseArray.size; i++)
                shopNoPurchaseMessages[i] = shopNoPurchaseArray.getString(i);

            JsonValue insufficientArray = esmeralda.get("shop_insufficient_funds_messages");
            shopInsufficientFundsMessages = new String[insufficientArray.size];
            for (int i = 0; i < insufficientArray.size; i++) {
                shopInsufficientFundsMessages[i] = insufficientArray.getString(i);
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

    public void showShopOpenMessage() {
        previousState = State.SHOP;
        state = State.SHOP_MESSAGE;
        currentText = shopOpenMessages[random.nextInt(shopOpenMessages.length)];
        waitingForChoice = false;
        setTalking(true);
        playRandomVoice();
        NpcInteractionHUD.getInstance().reloadCurrentText();
    }

    public void showInsufficientFundsMessage() {
        previousState = State.SHOP;
        state = State.SHOP_MESSAGE;
        currentText = shopInsufficientFundsMessages[random.nextInt(shopInsufficientFundsMessages.length)];
        waitingForChoice = false;
        setTalking(true);
        playRandomVoice();
        NpcInteractionHUD.getInstance().reloadCurrentText();
    }

    public void showShopResultMessage(boolean bought) {
        previousState = State.MENU;
        state = State.SHOP_MESSAGE;
        if (bought) {
            currentText = shopPurchaseMessages[random.nextInt(shopPurchaseMessages.length)];
        } else {
            currentText = shopNoPurchaseMessages[random.nextInt(shopNoPurchaseMessages.length)];
        }
        waitingForChoice = false;
        setTalking(true);
        playRandomVoice();
        NpcInteractionHUD.getInstance().reloadCurrentText();
    }

    @Override
    public boolean next() {
        if (state == State.WELCOME) {
            state = State.MENU;
            currentText = "O que você quer?";
            waitingForChoice = true;
            setTalking(true);
            playRandomVoice();
            NpcInteractionHUD.getInstance().reloadCurrentText();
            return true;
        } else if (state == State.TALK || state == State.SHOP) {
            state = State.MENU;
            currentText = "O que você quer?";
            waitingForChoice = true;
            setTalking(true);
            playRandomVoice();
            NpcInteractionHUD.getInstance().reloadCurrentText();
            return true;
        } else if (state == State.SHOP_MESSAGE) {
            state = previousState;

            if (state == State.SHOP) {
                currentText = "";
                waitingForChoice = false;
                setTalking(false);
            } else if (state == State.MENU) {
                currentText = "O que você quer?";
                waitingForChoice = true;
                setTalking(true);
            } else {

                currentText = getCurrentText();
                setTalking(true);
            }

            NpcInteractionHUD.getInstance().reloadCurrentText();
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
                if (shopUI != null) {
                    shopUI.show();
                }
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

    public void reset() {
        state = State.WELCOME;
        resetAvailableIndices();
        currentText = getNextWelcomeMessage();
        waitingForChoice = false;
        setTalking(true);
        playRandomVoice();
        selectedOption = 0;
    }

    public void navigateUp() {
        selectedOption--;
        if (selectedOption < 0)
            selectedOption = menuOptions.length - 1;
    }

    public void navigateDown() {
        selectedOption++;
        if (selectedOption >= menuOptions.length)
            selectedOption = 0;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void updateShop(float delta) {
        if (shopUI != null && shopUI.isVisible()) {
            shopUI.update(delta);
        }
    }

    public void renderShop() {
        if (shopUI != null && shopUI.isVisible()) {
            shopUI.render();
        }
    }

    public boolean isShopVisible() {
        return shopUI != null && shopUI.isVisible();
    }

    @Override
    public void dispose() {
        if (talkSheet != null)
            talkSheet.dispose();
        if (idleSheet != null)
            idleSheet.dispose();
    }
}