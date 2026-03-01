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
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Luz.EsferaDeLuz;
import io.github.some_example_name.Luz.SistemaLuz;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.MapConfig.RoomTransitionManager;

public class Room0Door {
    private Mapa mapa;
    private Vector2 position;
    private Texture doorSpriteSheet;
    private int tileSize;

    private boolean lightEnabled = false;
    private EsferaDeLuz lightSphere;

    private static final float LIGHT_SPHERE_RADIUS = 200f;
    private static final float LIGHT_TRANSITION_SPEED = 2f;
    private boolean playerInteracting = false;

    private float interactionCooldown = 0;
    private static final float INTERACTION_DELAY = 0.5f;
    private boolean interactive;
    private boolean waitingForPlayerExit = false;
    private boolean exitTransitionPending = false;

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

    private boolean waitingForPlayerEnter = false;
    private boolean transitionPending = false;

    public Room0Door(Mapa mapa, int tileX, int tileY, boolean interactive) {
        this.mapa = mapa;
        this.tileSize = 64;
        this.position = new Vector2(tileX, tileY);
        this.interactive = interactive;
        this.currentState = DoorState.FECHADA;
        this.stateTime = 0;
        this.playerInContact = false;
        this.contactCount = 0;
        this.lightEnabled = false;

        loadSpriteSheet();
        extractFrames();
        setupAnimations();
        if (interactive) {
            createPhysicsBody();
            Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
            float sphereX = worldPos.x * tileSize;
            float sphereY = worldPos.y * tileSize;
            this.lightSphere = new EsferaDeLuz(sphereX, sphereY, LIGHT_SPHERE_RADIUS,
                    new Color(1f, 1f, 1f, 1f));
            this.lightSphere.setActive(false);
        } else {
            // Porta não interativa: começa aberta e aguarda a animação de saída
            currentState = DoorState.ABERTA;
            lightEnabled = false;
            waitingForPlayerExit = true;
        }
        System.out.println("🚪 Porta criada em: " + position + (interactive ? " (interativa)" : " (estática)"));
    }

    private void createPhysicsBody() {
        try {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
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
        float worldX = tileX + 0.5f;
        float worldY = (mapa.mapHeight - 1 - tileY) + 0.3f;
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

        frameFechada = tempFrames[0][0];

        framesAbrindo = new TextureRegion[10];
        int frameIndex = 0;
        for (int row = 0; row < 3 && frameIndex < 10; row++) {
            for (int col = 0; col < 4 && frameIndex < 10; col++) {
                framesAbrindo[frameIndex] = tempFrames[row][col];
                frameIndex++;
            }
        }

        framesAberta = new TextureRegion[4];
        framesAberta[0] = tempFrames[2][2];
        framesAberta[1] = tempFrames[2][3];
        framesAberta[2] = tempFrames[3][0];
        framesAberta[3] = tempFrames[3][1];

        System.out.println("✅ Frames da porta aberta extraídos das posições: [2][2], [2][3], [3][0], [3][1]");
    }

    private void setupAnimations() {
        animacaoAbrindo = new Animation<TextureRegion>(FRAME_DURATION_ABRINDO, framesAbrindo);
        animacaoAbrindo.setPlayMode(Animation.PlayMode.NORMAL);
        animacaoAberta = new Animation<TextureRegion>(FRAME_DURATION_ABERTA, framesAberta);
        animacaoAberta.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void update(float delta) {
        // Atualiza o tempo do estado sempre
        stateTime += delta;
        updateState();

        if (interactive) {
            // Lógica interativa (sala 0)
            if (interactionCooldown > 0) {
                interactionCooldown -= delta;
            }
            checkDoorInteraction();

            if (waitingForPlayerEnter) {
                if (mapa.robertinhoo != null) {
                    PlayerRenderer renderer = mapa.robertinhoo.getRenderer();
                    boolean playing = renderer.isEnterAnimationPlaying();
                    boolean complete = renderer.isEnterAnimationComplete();
                    if (playing && complete) {
                        waitingForPlayerEnter = false;
                        if (currentState == DoorState.ABERTA) {
                            currentState = DoorState.FECHANDO;
                            stateTime = 0;
                            transitionPending = true;
                        }
                    }
                }
            }

            if (lightSphere != null) {
                lightSphere.update(delta);
                if (lightEnabled) {
                    lightSphere.setActive(true);
                    lightSphere.setIntensity(Math.min(1f, lightSphere.getIntensity() + delta * LIGHT_TRANSITION_SPEED));
                    if (lightSphere.isFullyExpanded() && !lightSphere.isPulsating()) {
                        lightSphere.startPulsation();
                    }
                } else {
                    lightSphere.setIntensity(Math.max(0f, lightSphere.getIntensity() - delta * LIGHT_TRANSITION_SPEED));
                    if (lightSphere.getIntensity() <= 0.3f && lightSphere.isPulsating()) {
                        lightSphere.stopPulsation();
                    }
                    if (lightSphere.isFullyContracted() && lightSphere.getIntensity() <= 0.01f) {
                        lightSphere.setActive(false);
                    }
                }
            }
            // Fallback de contato
            checkPlayerDistanceFallback();
            if (Math.random() < 0.003f) {
                debugState();
            }
        } else {
            if (waitingForPlayerExit) {
                if (mapa.robertinhoo != null) {
                    boolean complete = mapa.robertinhoo.getRenderer().isBackAnimationComplete();
                    if (complete) {
                        waitingForPlayerExit = false;
                        mapa.robertinhoo.setSensor(false);
                        mapa.robertinhoo.setState(Robertinhoo.IDLE);
                        mapa.robertinhoo.lastDir = Robertinhoo.DOWN;
                        mapa.robertinhoo.getRenderer().resetBackAnimation();
                        if (currentState == DoorState.ABERTA) {
                            currentState = DoorState.FECHANDO;
                            stateTime = 0;
                            exitTransitionPending = true;
                        }
                    }
                }
            }

        }
    }

    private void updateState() {
        switch (currentState) {
            case ABRINDO:
                if (animacaoAbrindo.isAnimationFinished(stateTime)) {
                    currentState = DoorState.ABERTA;
                    stateTime = 0;
                    lightEnabled = true;
                    System.out.println("🚪 Porta: Totalmente aberta");
                }
                break;
            case FECHANDO:
                float reverseTime = animacaoAbrindo.getAnimationDuration() - stateTime;
                if (reverseTime <= 0) {
                    currentState = DoorState.FECHADA;
                    stateTime = 0;
                    lightEnabled = false;
                    System.out.println("🚪 Porta: Totalmente fechada");

                    if (transitionPending) {
                        transitionPending = false;
                        triggerRoomTransition();
                    }
                    if (exitTransitionPending) {
                        exitTransitionPending = false;
                        // Fim da sequência de saída
                    }
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
        com.badlogic.gdx.math.Vector2 playerBodyPos = mapa.robertinhoo.getBody().getPosition();
        Vector2 doorWorldPos = convertTileToWorldPosition((int) position.x, (int) position.y);
        float distance = Vector2.dst(doorWorldPos.x, doorWorldPos.y, playerBodyPos.x, playerBodyPos.y);
        boolean shouldBeInContact = distance <= MAX_DISTANCE_FOR_CONTACT;
        if (shouldBeInContact && !playerInContact) {
            System.out.println("🚪 [FALLBACK] Correção: Player deveria estar em contato");
            onPlayerEnter();
        } else if (!shouldBeInContact && playerInContact) {
            System.out.println("🚪 [FALLBACK] Correção: Player deveria estar fora");
            onPlayerExit();
        }
    }

    public void onPlayerEnter() {
        if (!interactive)
            return;
        contactCount++;
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
        if (!interactive)
            return;
        contactCount = Math.max(0, contactCount - 1);
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

        if (lightSphere != null && lightSphere.isActive()) {
            float intensity = lightSphere.getIntensity();
            float brightness = 0.5f + (0.5f * intensity);
            batch.setColor(brightness, brightness, brightness, 1f);
        } else {
            batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        }

        batch.draw(currentFrame, screenX, screenY, tileSize, tileSize);
        batch.setColor(1f, 1f, 1f, 1f);
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
        if (lightSphere.isPulsating())
            visualIntensity = 1.0f;
        sistemaLuz.renderDoorSkullLight(lightX, lightY, 80f * visualIntensity, Gdx.graphics.getDeltaTime());
    }

    public void updateLightSpherePosition(float offsetX, float offsetY) {
        if (lightSphere != null) {
            Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
            float screenX = offsetX + worldPos.x * tileSize;
            float screenY = offsetY + worldPos.y * tileSize + tileSize;
            lightSphere.setPosition(screenX, screenY - 35f);
        }
    }

    private void startPlayerEntering() {
        if (mapa.robertinhoo == null)
            return;
        Vector2 doorWorldPos = convertTileToWorldPosition((int) position.x, (int) position.y);
        mapa.robertinhoo.getBody().setTransform(doorWorldPos, 0);
        mapa.robertinhoo.getBody().setLinearVelocity(0, 0);
        mapa.robertinhoo.getBody().setAngularVelocity(0);
        mapa.robertinhoo.state = Robertinhoo.ENTERING_DOOR;
        waitingForPlayerEnter = true;
        mapa.robertinhoo.setSensor(true);
    }

    private void checkDoorInteraction() {
        if (waitingForPlayerEnter || transitionPending || !interactive)
            return;
        if (currentState == DoorState.ABERTA && playerInContact && interactionCooldown <= 0) {
            if (isInteractKeyPressed()) {
                playerInteracting = true;
                startPlayerEntering();
                interactionCooldown = INTERACTION_DELAY;
            }
        } else {
            playerInteracting = false;
        }
    }

    private boolean isInteractKeyPressed() {
        return com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.E);
    }

    private void triggerRoomTransition() {
        if (mapa instanceof RoomTransitionManager) {
            ((RoomTransitionManager) mapa).transitionToRoom1();
        }
    }

    public void dispose() {
        if (doorSpriteSheet != null)
            doorSpriteSheet.dispose();
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

    public boolean isPlayerInteracting() {
        return playerInteracting;
    }

    public Vector2 getSpawnPosition() {
        Vector2 worldPos = mapa.tileToWorld((int) position.x, (int) position.y);
        float offsetY = -0.5f; // ajuste fino
        return new Vector2(worldPos.x, worldPos.y + offsetY);
    }
}