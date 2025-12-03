package io.github.some_example_name.MapConfig.Rooms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Luz.EscurecedorAmbiente;
import io.github.some_example_name.Luz.EsferaDeLuz;
import io.github.some_example_name.Luz.SistemaLuz;
import io.github.some_example_name.MapConfig.Mapa;

public class Room0Door {
    private Mapa mapa;
    private Vector2 position;
    private Texture doorSpriteSheet;
    private int tileSize;
    private boolean debugMode = false;
    private boolean lightEnabled = false;
    private float lightStateTime = 0;
    private EsferaDeLuz lightSphere;
    private float lightIntensity = 0f;
    private static final float LIGHT_SPHERE_RADIUS = 200f;
    private static final float LIGHT_TRANSITION_SPEED = 2f;

    // Estados da porta
    public enum DoorState {
        FECHADA,
        ABRINDO,
        ABERTA,
        FECHANDO
    }

    private DoorState currentState;
    private float stateTime;
    private boolean playerInContact;
    private int contactCount;

    // Animações
    private TextureRegion frameFechada;
    private Animation<TextureRegion> animacaoAbrindo;
    private Animation<TextureRegion> animacaoAberta;
    private TextureRegion[] framesAbrindo;
    private TextureRegion[] framesAberta;

    // Box2D
    private Body body;

    // Configurações
    private static final float FRAME_DURATION_ABRINDO = 0.08f;
    private static final float FRAME_DURATION_ABERTA = 0.15f;
    private static final float MAX_DISTANCE_FOR_CONTACT = 3.0f;

    public Room0Door(Mapa mapa, int tileX, int tileY) {
        this.mapa = mapa;
        this.tileSize = 64;
        this.position = new Vector2(tileX, tileY);
        this.currentState = DoorState.FECHADA;
        this.stateTime = 0;
        this.playerInContact = false;
        this.contactCount = 0;
        this.lightStateTime = 0;
        this.lightEnabled = false;

        loadSpriteSheet();
        extractFrames();
        setupAnimations();
        createPhysicsBody();
        Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
        float sphereX = worldPos.x * tileSize;
        float sphereY = worldPos.y * tileSize;
        this.lightSphere = new EsferaDeLuz(sphereX, sphereY, LIGHT_SPHERE_RADIUS,
                new Color(1f, 1f, 1f, 1f)); // Cor branca
        this.lightSphere.setActive(false);
        System.out.println("🚪 Porta criada em: " + position);
    }

    private void createPhysicsBody() {
        try {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;

            // ✅ CORREÇÃO: Converter coordenadas de tile para mundo (com Y invertido)
            Vector2 worldPos = convertTileToWorldPosition((int) position.x, (int) position.y);
            bodyDef.position.set(worldPos.x, worldPos.y);

            body = mapa.world.createBody(bodyDef);
            body.setUserData(this);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(tileSize * 0.3f, tileSize * 0.3f);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.isSensor = true;
            fixtureDef.filter.categoryBits = Constants.BIT_DOOR;
            fixtureDef.filter.maskBits = Constants.BIT_PLAYER;

            body.createFixture(fixtureDef);
            shape.dispose();

            System.out.println("✅ Corpo físico da porta criado em mundo: " + worldPos +
                    " | Tile original: " + position);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar corpo físico da porta: " + e.getMessage());
        }
    }

    private Vector2 convertTileToWorldPosition(int tileX, int tileY) {
        // Se o mapa tem altura 20 tiles, e queremos a porta no topo (tileY=0),
        // no mundo isso deve ser Y=19.5 (próximo ao topo do mundo)
        float worldX = tileX + 0.5f; // Centro do tile em X
        float worldY = (mapa.mapHeight - 1 - tileY) + 0.5f; // Inverter Y e centralizar

        return new Vector2(worldX, worldY);
    }

    private void loadSpriteSheet() {
        try {
            doorSpriteSheet = new Texture(Gdx.files.internal("sala_0/porta_sala_0.png"));
            System.out.println("✅ Sprite sheet da porta carregado: " +
                    doorSpriteSheet.getWidth() + "x" + doorSpriteSheet.getHeight());
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar sprite sheet da porta: " + e.getMessage());
            createPlaceholderTexture();
        }
    }

    private void createPlaceholderTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(tileSize, tileSize * 2,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.0f, 1.0f, 0.0f, 1f);
        pixmap.fill();
        doorSpriteSheet = new Texture(pixmap);
        pixmap.dispose();
    }

    private void extractFrames() {
        int cols = 4;
        int rows = 4;
        int frameWidth = doorSpriteSheet.getWidth() / cols;
        int frameHeight = doorSpriteSheet.getHeight() / rows;

        TextureRegion[][] tempFrames = TextureRegion.split(doorSpriteSheet, frameWidth, frameHeight);

        // Debug completo do sprite sheet
        System.out.println("🔍 Debug do Sprite Sheet:");
        System.out.println("  Dimensões: " + doorSpriteSheet.getWidth() + "x" + doorSpriteSheet.getHeight());
        System.out.println("  Frames: " + cols + "x" + rows + " = " + (cols * rows) + " frames");
        System.out.println("  Tamanho de cada frame: " + frameWidth + "x" + frameHeight);

        frameFechada = tempFrames[0][0];

        framesAbrindo = new TextureRegion[10];
        int frameIndex = 0;

        for (int row = 0; row < 3 && frameIndex < 10; row++) {
            for (int col = 0; col < 4 && frameIndex < 10; col++) {
                framesAbrindo[frameIndex] = tempFrames[row][col];
                frameIndex++;
            }
        }

        // Extrair frames da porta aberta baseado na descrição
        framesAberta = new TextureRegion[4];

        // Seguindo a descrição: "linha 3 coluna 3 até linha 4 coluna 2"
        // Isso significa:
        // - Linha 3: colunas 2 e 3 (frames 2 e 3)
        // - Linha 4: colunas 0 e 1 (frames 0 e 1)
        framesAberta[0] = tempFrames[2][2]; // L3C3
        framesAberta[1] = tempFrames[2][3]; // L3C4
        framesAberta[2] = tempFrames[3][0]; // L4C1
        framesAberta[3] = tempFrames[3][1]; // L4C2

        System.out.println("✅ Frames da porta aberta extraídos das posições:");
        System.out.println("  [2][2], [2][3], [3][0], [3][1]");
    }

    private void setupAnimations() {
        animacaoAbrindo = new Animation<TextureRegion>(FRAME_DURATION_ABRINDO, framesAbrindo);
        animacaoAbrindo.setPlayMode(Animation.PlayMode.NORMAL);

        animacaoAberta = new Animation<TextureRegion>(FRAME_DURATION_ABERTA, framesAberta);
        animacaoAberta.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void update(float delta) {
        stateTime += delta;
        updateState();

        if (lightSphere != null) {
            lightSphere.update(delta);

            // ✅ CONTROLE MAIS PRECISO DOS ESTADOS
            if (lightEnabled) {
                lightSphere.setActive(true);
                lightSphere.setIntensity(Math.min(1f, lightSphere.getIntensity() + delta * LIGHT_TRANSITION_SPEED));

                // ✅ SE ESTÁ TOTALMENTE EXPANDIDA, INICIA PULSAÇÃO
                if (lightSphere.isFullyExpanded() && !lightSphere.isPulsating()) {
                    lightSphere.startPulsation();
                }
            } else {
                lightSphere.setIntensity(Math.max(0f, lightSphere.getIntensity() - delta * LIGHT_TRANSITION_SPEED));

                // ✅ SE A INTENSIDADE ESTÁ BAIXA, PARA A PULSAÇÃO
                if (lightSphere.getIntensity() <= 0.3f && lightSphere.isPulsating()) {
                    lightSphere.stopPulsation();
                }

                // ✅ SE CONTRAIU COMPLETAMENTE, DESATIVA
                if (lightSphere.isFullyContracted() && lightSphere.getIntensity() <= 0.01f) {
                    lightSphere.setActive(false);
                }
            }
        }
        // Fallback: verificação manual de distância
        checkPlayerDistanceFallback();
        // Debug menos frequente
        if (Math.random() < 0.003f) {
            debugState();
        }
    }

    private void updateState() {
        switch (currentState) {
            case ABRINDO:
                if (animacaoAbrindo.isAnimationFinished(stateTime)) {
                    currentState = DoorState.ABERTA;
                    stateTime = 0;
                    lightEnabled = true; // Ativa a luz quando a porta termina de abrir
                    System.out.println("🚪 Porta: Totalmente aberta - Luz da caveira ativada");
                }
                break;

            case FECHANDO:
                float reverseTime = animacaoAbrindo.getAnimationDuration() - stateTime;
                if (reverseTime <= 0) {
                    currentState = DoorState.FECHADA;
                    stateTime = 0;
                    lightEnabled = false; // Desativa a luz quando a porta fecha
                    System.out.println("🚪 Porta: Totalmente fechada - Luz da caveira desativada");
                }
                break;

            case FECHADA:
            case ABERTA:
                break;
        }
    }

    private void checkPlayerDistanceFallback() {
        if (mapa.robertinhoo == null)
            return;

        // Usar a posição do corpo do jogador no mundo
        com.badlogic.gdx.math.Vector2 playerBodyPos = mapa.robertinhoo.getBody().getPosition();

        // Converter a posição da porta para coordenadas mundiais
        Vector2 doorWorldPos = convertTileToWorldPosition((int) position.x, (int) position.y);

        float distance = Vector2.dst(doorWorldPos.x, doorWorldPos.y, playerBodyPos.x, playerBodyPos.y);

        boolean shouldBeInContact = distance <= MAX_DISTANCE_FOR_CONTACT;

        // Se o estado de contato não corresponde à distância real, corrigir
        if (shouldBeInContact && !playerInContact) {
            System.out.println("🚪 [FALLBACK] Correção: Player deveria estar em contato (distância: " + distance + ")");
            onPlayerEnter();
        } else if (!shouldBeInContact && playerInContact) {
            System.out.println("🚪 [FALLBACK] Correção: Player deveria estar fora (distância: " + distance + ")");
            onPlayerExit();
        }
    }

    // Métodos chamados pelo DoorHandler
    public void onPlayerEnter() {
        contactCount++;

        // Só processa se não estava em contato antes
        if (!playerInContact) {
            playerInContact = true;
            System.out.println("🚪 onPlayerEnter() - Contatos: " + contactCount + ", Estado: " + currentState);

            if (currentState == DoorState.FECHADA || currentState == DoorState.FECHANDO) {
                currentState = DoorState.ABRINDO;
                stateTime = 0;
                System.out.println("🚪 Porta: Iniciando abertura");
            }
        }
    }

    public void onPlayerExit() {
        contactCount = Math.max(0, contactCount - 1);

        // Só processa se não há mais contatos
        if (contactCount <= 0 && playerInContact) {
            playerInContact = false;
            contactCount = 0;
            System.out.println("🚪 onPlayerExit() - Contatos: " + contactCount + ", Estado: " + currentState);

            if (currentState == DoorState.ABERTA || currentState == DoorState.ABRINDO) {
                currentState = DoorState.FECHANDO;
                stateTime = 0;
                System.out.println("🚪 Porta: Iniciando fechamento");
            }
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY,
            com.badlogic.gdx.math.Matrix4 projectionMatrix) {
        TextureRegion currentFrame = getCurrentFrame();
        if (currentFrame == null)
            return;

        Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
        float screenX = offsetX + worldPos.x * tileSize - tileSize / 2;
        float screenY = offsetY + worldPos.y * tileSize - tileSize / 2;

        // ✅ EFEITO DE ILUMINAÇÃO DINÂMICA BASEADO NA LUZ
        if (lightSphere != null && lightSphere.isActive()) {
            // Quando a luz está ativa: usar cor CLARA baseada na intensidade
            float intensity = lightSphere.getIntensity();

            // Interpolar entre escuro (0.5f) e claro (1.0f) baseado na intensidade
            float brightness = 0.5f + (0.5f * intensity); // Varia de 0.5 a 1.0

            batch.setColor(brightness, brightness, brightness, 1f);

            // ✅ DEBUG: Log da transição (opcional)
            if (Math.random() < 0.01f) { // Apenas 1% dos frames para não sobrecarregar
                Gdx.app.log("DOOR_BRIGHTNESS",
                        "Intensidade: " + intensity +
                                " | Brilho: " + brightness +
                                " | Estado: " + currentState);
            }
        } else {
            // Quando a luz está inativa: cor ESCURA
            batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        }

        batch.draw(currentFrame, screenX, screenY, tileSize, tileSize);
        batch.setColor(1f, 1f, 1f, 1f); // ✅ SEMPRE restaurar cor normal
    }

    private TextureRegion getCurrentFrame() {
        switch (currentState) {
            case FECHADA:
                return frameFechada;
            case ABRINDO:
                return animacaoAbrindo.getKeyFrame(stateTime, false);
            case ABERTA:
                return animacaoAberta.getKeyFrame(stateTime, true);
            case FECHANDO:
                float reverseTime = Math.max(0, animacaoAbrindo.getAnimationDuration() - stateTime);
                return animacaoAbrindo.getKeyFrame(reverseTime, false);
            default:
                return frameFechada;
        }
    }

    private void debugState() {
        if (mapa.robertinhoo != null) {
            Vector2 playerPos = mapa.robertinhoo.pos;
            float distance = Vector2.dst(position.x, position.y, playerPos.x, playerPos.y);
            System.out.println("🚪 DEBUG - Estado: " + currentState +
                    " | Contato: " + playerInContact +
                    " | Contatos: " + contactCount +
                    " | Distância: " + String.format("%.2f", distance) +
                    " | Posição: " + position);
        }
    }

    public void renderLight(SistemaLuz sistemaLuz, float offsetX, float offsetY) {
        if (!lightEnabled || lightSphere == null || !lightSphere.isActive())
            return;

        Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
        float screenX = offsetX + worldPos.x * tileSize;
        float screenY = offsetY + worldPos.y * tileSize;

        float lightX = screenX;
        float lightY = screenY + tileSize * 0.4f;

        float visualIntensity = lightSphere.getIntensity();

        if (lightSphere.isPulsating()) {
            visualIntensity = 1.0f;
        }

        sistemaLuz.renderDoorSkullLight(lightX, lightY, 80f * visualIntensity, Gdx.graphics.getDeltaTime());
    }

    public void updateLightSpherePosition(float offsetX, float offsetY) {
        if (lightSphere != null) {
            // ✅ POSIÇÃO SIMPLES: usar a mesma lógica da renderização da porta
            Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
            float screenX = offsetX + worldPos.x * tileSize;
            float screenY = offsetY + worldPos.y * tileSize;

            // ✅ AJUSTE: subir um pouco a luz para ficar na caveira
            screenY += tileSize;

            lightSphere.setPosition(screenX, screenY - 35f);

            // Debug simples
            if (lightEnabled) {
                Gdx.app.log("DOOR_LIGHT", "Luz posicionada em: " + screenX + ", " + screenY);
            }
        }
    }

    public void dispose() {
        if (doorSpriteSheet != null) {
            doorSpriteSheet.dispose();
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public DoorState getCurrentState() {
        return currentState;
    }

    public boolean isPlayerInContact() {
        return playerInContact;
    }

    public EsferaDeLuz getLightSphere() {
        return lightSphere;
    }

}