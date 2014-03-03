package com.python4d.fumper;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.RemoteInput;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.python4d.fumper.AbstractScreen.TypeOfObject;
import com.python4d.fumper.Toast.TEXT_POS;

public class SplashScreen extends AbstractScreen {

	private AnimatedActor tapFingerActor;
	private Image splashImage, bgImage;
	private Body splashBody;
	private Array<Fruit> pommes = new Array<Fruit>();
	private boolean quit = false;
	private Text textActor;
	private int rotation;

	public SplashScreen(Fumper game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();
		FumperSound.INTRO.play(0.0f, true);
		AtlasRegion bgRegion = getAtlas().findRegion("background/background");
		Drawable bgDrawable = new TextureRegionDrawable(bgRegion);
		bgImage = new Image(bgDrawable);
		bgImage.setFillParent(true);
		bgImage.getColor().a = 0.5f;
		stage.addActor(bgImage);

		AtlasRegion splashRegion = getAtlas().findRegion(
				"splash-screen/splash-image");
		Drawable splashDrawable = new TextureRegionDrawable(splashRegion);
		splashImage = new Image(splashDrawable);
		splashImage.getColor().a = 0f;
		splashImage.addAction(sequence(fadeIn(0.75f), delay(3.0f)));
		splashImage.setOrigin(0, 0);
		splashImage.setScale(1f);
		stage.addActor(splashImage);

		textActor = new Text(font_berlin, "\n\n Fruit\n J\n u\n m\n p\n e\n r");
		textActor.setZIndex(9);
		stage.addActor(textActor);

		Array<AtlasRegion> arrayTapFinger=new Array<AtlasRegion>();
		for (int i=0;i<TypeOfObject.tap_finger.getNbImages();i++){
			arrayTapFinger.add(getAtlas().findRegion(TypeOfObject.tap_finger.getAllImages()[i]));
		}
		Animation tapFinger=new Animation(0.5f,arrayTapFinger,Animation.LOOP);
		tapFingerActor=new AnimatedActor(new AnimationDrawable(tapFinger));
		stage.addActor(tapFingerActor);
		
		Gdx.input.setInputProcessor(stage);
		stage.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.log("Fumper/ClickListener/", "button clicked");
				worldbox.setGravity(new Vector2(0, -9.8f));
				game.setScreen(new PlayScreen(game));
				dispose();
			}

		});
		stage.addListener(new InputListener() {

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
					if (!myToast.makeText("Back Again to Quit", font_berlin,
							Toast.COLOR_PREF.GREEN, Toast.STYLE.NORMAL, 0,
							TEXT_POS.down, 1f))
						quit = true;
				}
				return super.keyDown(event, keycode);
			}

		});

	}

	// Utilisation de render pour repositionner les images si issues du monde
	// world
	@Override
	public void render(float delta) {
		super.render(delta);
		if (pommes.size < 10) {
			pommes.add(new Fruit(this, tofTable[new Random().nextInt(TypeOfObject.getNbFruits())], 0.1f));
			int x = new Random().nextInt(Gdx.graphics.getWidth());
			int y = new Random().nextInt(100) + Gdx.graphics.getHeight();
			int angle = new Random().nextInt(360);
			pommes.peek().ResetPosition(x, y, angle);
			pommes.peek().setZIndex(99);
			stage.addActor(pommes.peek());
		}
		for (Fruit i : pommes) {
			i.setPosition(i.getBody().getPosition().x
					* AbstractScreen.BOX_TO_WORLD, i.getBody().getPosition().y
					* AbstractScreen.BOX_TO_WORLD);
			i.setRotation(i.getBody().getAngle() * MathUtils.radiansToDegrees);
		}
		splashImage.setPosition(splashBody.getPosition().x
				* AbstractScreen.BOX_TO_WORLD, splashBody.getPosition().y
				* AbstractScreen.BOX_TO_WORLD);
		splashImage.setRotation(splashBody.getAngle()
				* MathUtils.radiansToDegrees);
		if (wait1s-- < 0) {
			int index = 0;
			for (Fruit i : pommes) {
				// Quels fruits détruire?
				if (i.getBody().getPosition().x * AbstractScreen.BOX_TO_WORLD < -100
						|| i.getBody().getPosition().y
								* AbstractScreen.BOX_TO_WORLD < -100
						&& !worldbox.isLocked()) {
					worldbox.destroyBody(i.getBody());
					i.remove();
					pommes.removeIndex(index);
				}

				index++;
			}
			wait1s = NB_CYCLE / 10;
			Gdx.app.log("Fumper/SplashScreen/Render:",
					"AccX=" + Gdx.input.getAccelerometerX() + "AccY="
							+ Gdx.input.getAccelerometerY() + "AccZ="
							+ Gdx.input.getAccelerometerZ() + "/pitch="
							+ (int) Gdx.input.getPitch() + "/azimut="
							+ (int) Gdx.input.getAzimuth() + "/roll="
							+ (int) Gdx.input.getRoll());
			// worldbox.setGravity(new Vector2(-Gdx.input.getAccelerometerX(),
			// -Gdx.input.getAccelerometerY()));
		}
		if (quit)
			game.quit(game.getScreen());
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		splashImage.setSize(width - 200,
				(width - 200) * (splashImage.getDrawable().getMinHeight())
						/ splashImage.getDrawable().getMinWidth());
		splashImage.setPosition((width - splashImage.getWidth()) / 2.0f,
				(height - splashImage.getHeight()) / 4.0f);

		if (splashBody != null) {
			worldbox.destroyBody(splashBody);
		}
		// Body de la cage
		splashBody = CreateSplashBody((width - splashImage.getWidth()) / 2.0f,
				(height - splashImage.getHeight()) / 4.0f);

		textActor.setPosition(0, Gdx.graphics.getHeight());

		tapFingerActor.setScale(width/500.0f);
		tapFingerActor.setPosition(Gdx.graphics.getWidth()/1.5f,
				Gdx.graphics.getHeight()/2.0f);
	}

	protected Body CreateSplashBody(float Posx, float Posy) {
		Posx = Posx * AbstractScreen.WORLD_TO_BOX;
		Posy = Posy * AbstractScreen.WORLD_TO_BOX;
		final TypeOfObject too = TypeOfObject.SPLASH;

		// Create our body definition
		BodyDef bd = new BodyDef();
		// Set its world position
		bd.position.set(new Vector2(Posx, Posy));
		bd.type = BodyType.DynamicBody;
		// Create a body from the definition and add it to the world
		Body body = getWorldbox().createBody(bd);
		FixtureDef fd = new FixtureDef();
		fd.friction = 0.90f;
		fd.restitution = 0.6f;
		fd.density = 1f;
		getBodyLoader().attachFixture(body, too.getNameBody(), fd,
				splashImage.getWidth() * AbstractScreen.WORLD_TO_BOX);
		body.createFixture(fd);
		Body groundbody = worldbox.createBody(new BodyDef());
		RevoluteJointDef jd = new RevoluteJointDef();
		jd.enableLimit = true;
		jd.lowerAngle = -30 * MathUtils.degreesToRadians;
		jd.upperAngle = 30 * MathUtils.degreesToRadians;
		jd.bodyA = groundbody;
		jd.bodyB = body;
		jd.collideConnected = false;

		float instable = 1.0f;
		jd.localAnchorA.add(Posx + body.getLocalCenter().x,
				Posy + body.getLocalCenter().y * instable);
		jd.localAnchorB.add(body.getLocalCenter().x, body.getLocalCenter().y
				* instable);
		worldbox.createJoint(jd);

		return body;
	}

	@Override
	public void dispose() {
		if (FumperSound.INTRO != null)
			FumperSound.INTRO.dispose();
		super.dispose();
	}
}