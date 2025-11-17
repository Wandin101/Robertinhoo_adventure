package io.github.some_example_name.MapConfig.Rooms.Items_sala_0;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Sounds.AudioManager;
import io.github.some_example_name.Sounds.GameGameSoundsPaths;
import io.github.some_example_name.Entities.Itens.Contact.Constants;

public class CampFire {
    private Vector2 position;
    private Texture spriteSheet;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private float frameDuration = 0.1f;
    private Body body;
    private Mapa mapa;
    private AudioManager audioManager;

    public CampFire(Mapa mapa, float tileX, float tileY) {
        this.mapa = mapa;
        this.position = new Vector2(tileX, tileY);
        this.stateTime = 0f;
        this.audioManager = AudioManager.getInstance();

        System.out.println("🔥 Fogueira COM COLISÃO criada em Tile: " + position);

        loadSpriteSheet();
        createPhysicsBody();
        startAmbientSound();
         testAudioFile();
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = new Texture(Gdx.files.internal("sala_0/fogueiraPrincipal.png"));

            int frameWidth = spriteSheet.getWidth() / 8;
            int frameHeight = spriteSheet.getHeight();

            TextureRegion[] frames = new TextureRegion[8];
            for (int i = 0; i < 8; i++) {
                frames[i] = new TextureRegion(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
            }

            animation = new Animation<TextureRegion>(frameDuration, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);

        } catch (Exception e) {
            System.err.println("❌ Erro no sprite sheet: " + e.getMessage());
            createPlaceholder();
        }
    }

    private void createPlaceholder() {
        // Placeholder simples
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.ORANGE);
        pixmap.fill();
        spriteSheet = new Texture(pixmap);
        pixmap.dispose();

        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spriteSheet);
        animation = new Animation<TextureRegion>(frameDuration, frames);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
       
    }

    public void render(SpriteBatch batch, float screenX, float screenY) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

            float renderSize = 128;
            float centeredX = screenX - (renderSize - 64) / 2f;
            float centeredY = screenY - (renderSize - 64) / 2f;

            batch.draw(currentFrame, centeredX, centeredY, renderSize, renderSize);
        }
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        // ✅ POSIÇÃO CENTRALIZADA
        float bodyX = position.x + 0.5f;
        float bodyY = position.y + 0.5f;
        bodyDef.position.set(bodyX, bodyY - 0.4f);

        body = mapa.world.createBody(bodyDef);

        PolygonShape rectShape = new PolygonShape();

        rectShape.setAsBox(0.8f, 0.3f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = Constants.BIT_OBJECT;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_PLAYER_ATTACK;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        rectShape.dispose();

        body.setUserData(this);

        System.out.println("✅ Body da fogueira criado com raio " + 0.4f + " em: " + bodyDef.position);
    }

    public Vector2 getBodyPosition() {
        return body.getPosition();
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return position;
    }

private void startAmbientSound() {
    // ✅ USAR postRunnable PARA GARANTIR CONTEXTO
    Gdx.app.postRunnable(new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("🎵 Iniciando som da fogueira no thread principal...");
                
                // Pequeno delay para garantir que o áudio está pronto
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
                
                audioManager.playAmbient(GameGameSoundsPaths.Ambient.FOGUEIRA_SOUND);
                System.out.println("🔊 Som ambiente da fogueira - Chamada completada");
                
            } catch (Exception e) {
                System.err.println("❌ Erro crítico no som: " + e.getMessage());
            }
        }
    });
}

      private void testAudioFile() {
        // ✅ VERIFICAR SE O ARQUIVO EXISTE
        boolean exists = Gdx.files.internal("Sounds/fogueiraSound.ogg").exists();
        System.out.println("🔊 Arquivo fogueiraSound.ogg existe: " + exists);
        
        if (exists) {
            try {
                long fileSize = Gdx.files.internal("Sounds/fogueiraSound.ogg").length();
                System.out.println("🔊 Tamanho do arquivo: teste wandin " + fileSize + " bytes");
            } catch (Exception e) {
                System.err.println("❌ Erro ao verificar arquivo: " + e.getMessage());
            }
        }
    }

    private void stopAmbientSound() {
        try {
            audioManager.stopAmbient(GameGameSoundsPaths.Ambient.FOGUEIRA_SOUND);
            System.out.println("🔇 Som ambiente da fogueira parado");
        } catch (Exception e) {
            System.err.println("❌ Erro ao parar som ambiente da fogueira: " + e.getMessage());
        }
    }

    public void pauseAmbientSound() {
        try {
            audioManager.pauseAmbient(GameGameSoundsPaths.Ambient.FOGUEIRA_SOUND);
            System.out.println("⏸️ Som ambiente da fogueira pausado");
        } catch (Exception e) {
            System.err.println("❌ Erro ao pausar som ambiente da fogueira: " + e.getMessage());
        }
    }

    public void resumeAmbientSound() {
        try {
            audioManager.resumeAmbient(GameGameSoundsPaths.Ambient.FOGUEIRA_SOUND);
            System.out.println("▶️ Som ambiente da fogueira retomado");
        } catch (Exception e) {
            System.err.println("❌ Erro ao retomar som ambiente da fogueira: " + e.getMessage());
        }
    }

    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
        stopAmbientSound();
    }
}