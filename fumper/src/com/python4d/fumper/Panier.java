package com.python4d.fumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.python4d.fumper.AbstractScreen.TypeOfObject;

public class Panier {

	private AbstractScreen screen;
	private int nb_fruits_dans_panier = 0;

	private Image imgPanier, imgPanier_front;
	private Body bodyPanier, bodyPlanche;
	private TypeOfObject panier = TypeOfObject.panier;
	private TypeOfObject panier_front = TypeOfObject.panier_front;

	public Panier(AbstractScreen screen, float Posx, float Posy, float size) {
		this.screen = screen;
		AtlasRegion bgRegion = screen.getAtlas().findRegion(
				panier.getNameImage());
		Drawable bgDrawable = new TextureRegionDrawable(bgRegion);
		imgPanier = new Image(bgDrawable);
		imgPanier.setScale(size);
		imgPanier.setPosition(Posx, Posy);
		bgRegion = screen.getAtlas().findRegion(panier_front.getNameImage());
		bgDrawable = new TextureRegionDrawable(bgRegion);
		imgPanier_front = new Image(bgDrawable);
		imgPanier_front.setScale(size);
		imgPanier_front.setPosition(Posx, Posy);
		screen.stage.addActor(imgPanier);
		screen.stage.addActor(imgPanier_front);
		float PosxW = (imgPanier.getX()) * AbstractScreen.WORLD_TO_BOX;
		float PosyW = (imgPanier.getY()) * AbstractScreen.WORLD_TO_BOX;
		// Create our body definition
		BodyDef bdPanier = new BodyDef();
		// Set its world position
		bdPanier.position.set(new Vector2(PosxW, PosyW));
		bdPanier.type = BodyType.StaticBody;
		// Create a body from the definition and add it to the world
		bodyPanier = screen.getWorldbox().createBody(bdPanier);
		FixtureDef fdPanier = new FixtureDef();
		fdPanier.friction = 0.50f;
		fdPanier.restitution = 0.3f;
		fdPanier.density = 1.0f;
		screen.getBodyLoader().attachFixture(
				bodyPanier,
				panier.getNameBody(),
				fdPanier,
				imgPanier.getWidth() * imgPanier.getScaleX()
						* AbstractScreen.WORLD_TO_BOX);
		bodyPanier.createFixture(fdPanier);
		// add radar sensor to ship
		CircleShape circleShape = new CircleShape();
		circleShape.setPosition(new Vector2(imgPanier.getWidth()
				* imgPanier.getScaleX() * AbstractScreen.WORLD_TO_BOX / 2,
				imgPanier.getHeight() * imgPanier.getScaleX()
						* AbstractScreen.WORLD_TO_BOX / 2));
		circleShape.setRadius(imgPanier.getWidth() * imgPanier.getScaleX()
				* AbstractScreen.WORLD_TO_BOX / 2);
		FixtureDef myFixtureDef = new FixtureDef();
		myFixtureDef.shape = circleShape;
		myFixtureDef.isSensor = true;
		bodyPanier.createFixture(myFixtureDef);

		bodyPanier.setUserData(imgPanier);
		bodyPlanche = ((PlayScreen) screen).getBascule().getBodyPlanche();

		circleShape.dispose();
	}
//Tentative d'utiliser la fonction QUERYAABB... cf nb_fruits_in()
	public QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Gdx.app.log("Fumper/panier/callback", 
					"\t/nb fruits_dans_panier="+nb_fruits_dans_panier+
					"\tVAngulaire="+fixture.getBody().getAngularVelocity()+
					"\tVx,y="+fixture.getBody().getLinearVelocity()+
					"\t/Body="+fixture.getBody());
			// ne doit pas être le panier lui-même...
			if (fixture.getBody() == bodyPanier)
				return true;

			if (fixture.getBody().getUserData()!=null)
				if ( fixture.getBody().getUserData().toString().equals("in"))
					return true;
				
			// Récupérartion du body dans le panier, doit être sans vitesse angulaire ou verticale
			if (fixture.getBody().getAngularVelocity()!= 0.0f || Math.abs(fixture.getBody().getLinearVelocity().y)>=0.0001f)
				return true;
				
			nb_fruits_dans_panier++;
			fixture.getBody().setUserData(new String("in"));
			return true;

		}
	};

	public Image getImgPanier_front() {
		return imgPanier_front;
	}

	public Image getImgPanier() {
		return imgPanier;
	}

	public Body getBodyPanier() {
		return bodyPanier;
	}

	public void RemovePanier() {
		imgPanier.remove();
		imgPanier_front.remove();
		screen.getWorldbox().destroyBody(bodyPanier);

	}
	public int check_fruits_in() {
		float x=imgPanier.getX()* AbstractScreen.WORLD_TO_BOX;
		float y=imgPanier.getY()* AbstractScreen.WORLD_TO_BOX;
		float s=imgPanier.getWidth() * imgPanier.getScaleX()* AbstractScreen.WORLD_TO_BOX;	

		int nb=0;
		for (Fruit f: ((PlayScreen) screen).getFruits()){
			boolean in=(f.getBody().getWorldCenter().x>x 
					&& f.getBody().getWorldCenter().y>y
					&& f.getBody().getWorldCenter().x<s+x
					&& f.getBody().getWorldCenter().y<s+y);
			//déjà dedans
			if (f.getBody().getUserData().toString().equals("in")){
				//y-est-il toujours même s'il il bouge?
				if (in) 
					nb++;
				else
					f.getBody().setUserData(new String("out"));
			}
			else
				//nouveau dedans et sans vie?
				if (in && f.getBody().getAngularVelocity()== 0.0f 
						&& Math.abs(f.getBody().getLinearVelocity().y)<=0.0001f){
					f.getBody().setUserData(new String("in"));
					nb++;
				}
			
		}
		Gdx.app.log("Fumper/panier/nb_fruits_in()",""+nb);
		return nb;
	}
	public int nb_fruits_in(Array<Fruit> fruits) {

		return screen.getWorldbox().getContactCount();
	}
}
