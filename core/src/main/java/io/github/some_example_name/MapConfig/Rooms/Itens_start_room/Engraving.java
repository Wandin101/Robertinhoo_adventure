package io.github.some_example_name.MapConfig.Rooms.Itens_start_room;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Interatibles.Interactable;
import io.github.some_example_name.Entities.Interatibles.InteractionManager;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Luz.SistemaLuz;
import io.github.some_example_name.MapConfig.Mapa;

public class Engraving implements Interactable {
    private Texture texture;
    private Vector2 tilePos;
    private Mapa mapa;
    private Body body;
    private boolean active = true;

    // Tamanho da renderização (maior que o tile)
    private static final float RENDER_SIZE = 128f; // 2x o tile (64)

    // Parâmetros da luz
    private static final float LIGHT_RADIUS = 150f;
    private static final float LIGHT_INTENSITY = 0.5f;
    private com.badlogic.gdx.graphics.Color lightColor;

    public Engraving(Mapa mapa, int tileX, int tileY, String texturePath) {
        this.mapa = mapa;
        this.tilePos = new Vector2(tileX, tileY);
        lightColor = new com.badlogic.gdx.graphics.Color(0.2f, 0.8f, 1.0f, 1f);

        try {
            texture = new Texture(Gdx.files.internal(texturePath));
            System.out.println("✅ Gravura carregada: " + texturePath);
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar gravura: " + e.getMessage());
            createPlaceholder();
        }

        createPhysicsBody();
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Vector2 worldPos = mapa.tileToWorld((int) tilePos.x, (int) tilePos.y);
        bodyDef.position.set(worldPos.x, worldPos.y);
        body = mapa.world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.4f); // área de interação

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_INTERACTABLE; // você precisa definir essa constante
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    private void createPlaceholder() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.BLUE);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        // Futuramente: animação
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, int tileSize) {
        // Calcula a posição central do tile
        float centerX = offsetX + (tilePos.x + 0.5f) * tileSize;
        float centerY = offsetY + (mapa.mapHeight - 1 - tilePos.y + 0.5f) * tileSize;

        // Posiciona a imagem centralizada
        float drawX = centerX - RENDER_SIZE / 2f;
        float drawY = centerY - RENDER_SIZE / 2f;

        batch.draw(texture, drawX, drawY, RENDER_SIZE, RENDER_SIZE);
    }

    public void renderLight(SistemaLuz sistemaLuz, float offsetX, float offsetY, int tileSize) {
        // Centro da gravura em pixels
        float centerX = offsetX + (tilePos.x + 0.5f) * tileSize;
        float centerY = offsetY + (mapa.mapHeight - 1 - tilePos.y + 0.5f) * tileSize;

        // Pequena pulsação para a luz
        float time = System.currentTimeMillis() * 0.001f;
        float pulse = (float) Math.sin(time * 2) * 0.1f + 0.9f;

        float radius = LIGHT_RADIUS * pulse;
        com.badlogic.gdx.graphics.Color color = lightColor.cpy();
        color.a = LIGHT_INTENSITY * pulse;

        sistemaLuz.renderLight(centerX, centerY, radius, color);
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void onInteract() {
        InteractionManager.getInstance().startDialogue("Bem-vindo, pequeno sonhador.\n" + //
                "\n" + //
                "Antes de morrer aqui, lembre-se:\n" + //
                "você assinou.\n" + //
                "\n" + //
                "Não sou eu quem os chama.\n" + //
                "São seus desejos que os trazem.\n" + //
                "\n" + //
                "Entre.\n" + //
                "Vamos descobrir o quanto você realmente quer isso.");
    }

    @Override
    public String getInteractionPrompt() {
        return "E";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public void dispose() {
        if (texture != null)
            texture.dispose();
    }

    @Override
    public Vector2 getTilePosition() {
        return body.getPosition(); // já é a posição em tiles
    }
}