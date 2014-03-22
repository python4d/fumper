package com.python4d.fumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.python4d.fumper.AbstractScreen.TypeOfObject;

public class Fruit extends ImageAndBody {

	private Drawable drawable;
	private Body body = null;

	public Fruit(AbstractScreen screen, TypeOfObject tof,float size) {
		super(screen);
		AtlasRegion region = screen.getAtlas().findRegion(tof.getNameImage());
		drawable = new TextureRegionDrawable(region);
		this.setDrawable(drawable);
		this.setSize(Gdx.graphics.getWidth()*size, Gdx.graphics.getWidth()*size);
		this.setOrigin(0,0);

		this.body = CreateBody(tof.getNameBody());
	}

	public Body getBody() {
		return body;
	}

	public void ResetPosition(float x, float y, float angle) {
		super.setPosition(x, y);
		this.setRotation(angle);
		body.setTransform(x * AbstractScreen.WORLD_TO_BOX, y
				* AbstractScreen.WORLD_TO_BOX, angle / 360.0f * 2.0f
				* MathUtils.PI);
	}


	private Body CreateBody(String namebody) {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DynamicBody;

		FixtureDef fd = new FixtureDef();
		fd.density = 1f;
		fd.friction = 0.5f;
		fd.restitution = 0.3f;

		// 3. Create a Body, as usual.
		Body body = screen.getWorldbox().createBody(bd);

		// 4. Create the body fixture automatically by using the loader.
		screen.getBodyLoader().attachFixture(body, namebody, fd,
				getWidth() * getScaleX() * AbstractScreen.WORLD_TO_BOX);
		return body;
	}

}
