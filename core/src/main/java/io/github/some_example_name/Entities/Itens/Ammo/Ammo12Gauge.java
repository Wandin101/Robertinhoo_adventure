package io.github.some_example_name.Entities.Itens.Ammo;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.MapConfig.Mapa;

import com.badlogic.gdx.graphics.Texture;

public class Ammo12Gauge extends Ammo {
    private Body body;

    protected Vector2 setWorldPosition;

    public Ammo12Gauge(Mapa mapa, float x, float y) {
        super("12gauge", 15, 25, new TextureRegion(new Texture("ITENS/Ammo/12-gauge.png")), 2, 1);
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        createBody(this.position);
    }

    public Ammo12Gauge() {
        super("12gauge", 15, 25, new TextureRegion(new Texture("ITENS/Ammo/12-gauge.png")), 2, 1);
        this.position = new Vector2();
        this.mapa = null;
        this.body = null;
    }

    public void createBody(Vector2 position) {
        if (mapa == null) {
            throw new IllegalStateException("Mapa não definido para 12Gauge");
        }
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(position.x + 0.5f, position.y + 0.5f);

        body = mapa.world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.4f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = Constants.BIT_ITEM;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public void destroyBody() {
        mapa.world.destroyBody(body);
    }

    @Override
    public Item copy() {
        Ammo12Gauge copy = new Ammo12Gauge();
        copy.setQuantity(this.quantity);
        copy.setMapa(this.mapa);
        copy.setPosition(this.position.cpy());
        return copy;
    }

    @Override
    public String getName() {
        return "12 Gauge";
    }

    @Override
    public Body getBody() {

        return body;
    }
}