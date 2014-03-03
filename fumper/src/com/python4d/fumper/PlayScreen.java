package com.python4d.fumper;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class PlayScreen extends AbstractScreen {
	private enum GamePart {
		DEBUT, FIN, EN_COURS, LEVEL_END,NEW_LEVEL, NEW_FRUIT, QUIT
	};

	private GamePart examGamePart = GamePart.DEBUT;
	private Image bgImage;
	private Bascule bascule;
	private Body camion, oiseau;
	private Array<Fruit> fruits = new Array<Fruit>();
	private Array<Image> imageFruits = null;

	public Array<Fruit> getFruits() {
		return fruits;
	}

	private boolean quit = false;
	private int level = 1;
	private int nb_fruits = 10;

	private int score = 0;
	private int score_total = 0;
	private int highscore = 0;
	private String highscore_name; 

	private Panier panier = null;
	private Text textActor;

	public PlayScreen(Fumper game) {
		super(game);
		highscore=game.FumperPrefs.getHighScore();
		highscore_name=new String(game.FumperPrefs.getHighScoreName());

	}
	
	//
	//Boite de dialog de base pour enregistrer le nom du farmer vainqueur
	//
	private TextInputListener listener = new TextInputListener(){

		@Override
		public void input(String text) {
			highscore_name=text.toString();
			game.FumperPrefs.setHighScore(score, highscore_name);
		}

		@Override
		public void canceled() {
			game.FumperPrefs.setHighScore(score, highscore_name);
		}
		
	};
	private Image tapandplayActor;
	@Override
	public void show() {
		super.show();
		//Mettre le fond d'écran - valade pendant tout le jeu
		AtlasRegion bgRegion = getAtlas().findRegion("background/background");
		Drawable bgDrawable = new TextureRegionDrawable(bgRegion);
		bgImage = new Image(bgDrawable);
		bgImage.setFillParent(true);
		stage.addActor(bgImage);
		
		tapandplayActor=new Image(new TextureRegionDrawable(getAtlas().findRegion(TypeOfObject.tapandplay.getNameImage())));
		stage.addActor(tapandplayActor);

		Gdx.input.setInputProcessor(stage);
		stage.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.log("Fumper/PlayScreen/CliscListener/", "button clicked");
			}

		});

		stage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
					examGamePart = GamePart.QUIT;
				}
				return super.keyDown(event, keycode);
			}

			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Fumper/PlayScreen/All stage", "touch started at ("
						+ x + ", " + y + ")");
				getBascule().push(level);
				try{
					Gdx.input.vibrate(50);
				}finally{
					
				}
				return false;
			}

			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Fumper/Bascule/All stage", "touch done at (" + x
						+ ", " + y + ")");
			}
		});
	}

	/**
	 * Permet de supprimer les fruits hors champ et controler ceux qui sont dans le panier
	 * @param fruits
	 * @return la taille de Array des fruits
	 */
	private int UpdateFruits(Array<Fruit> fruits){
		
		int index = 0;
		for (Fruit i : fruits) {
			//Quels fruits détruire?
			if (i.getBody().getPosition().x
					* AbstractScreen.BOX_TO_WORLD < -100
					|| i.getBody().getPosition().y
							* AbstractScreen.BOX_TO_WORLD < -100
					|| i.getColor().a == 0.5f && !worldbox.isLocked()) {
				if (i.getBody().getUserData().equals("in"))
					score++;
				worldbox.destroyBody(i.getBody());
				i.remove();
				fruits.removeIndex(index);
			} else {
				i.setPosition(i.getBody().getPosition().x
						* AbstractScreen.BOX_TO_WORLD, i.getBody()
						.getPosition().y * AbstractScreen.BOX_TO_WORLD);
				i.setRotation(i.getBody().getAngle()
						* MathUtils.radiansToDegrees);
			}
	
			index++;
		}
		return fruits.size;
	}
	@Override
	public void render(float delta) {
		super.render(delta);
		getBascule().render();

		switch (examGamePart) {


		case DEBUT:
			examGamePart = GamePart.EN_COURS;
		case EN_COURS:
			if (wait-- < 0)
				examGamePart = GamePart.NEW_FRUIT;
			else {
				UpdateFruits(fruits);
				textActor
						.setText("\n\nScore="+ score
								+ "\nLevel=" + level
								+ "\nHighScore=" + highscore
								+ "\nFarmer=" + highscore_name);
				
			}
			
			
			float xx = panier.getImgPanier().getX()
					* AbstractScreen.WORLD_TO_BOX;
			float yx = panier.getImgPanier().getY()
					* AbstractScreen.WORLD_TO_BOX;
			float s = panier.getImgPanier().getWidth()
					* panier.getImgPanier().getScaleX()
					* AbstractScreen.WORLD_TO_BOX;
			// panier.nb_fruits_in(fruits);
			// getWorldbox().QueryAABB(panier.callback, xx,yx,xx+s,yx+s);
			ImmediateModeRenderer10 renderer = new ImmediateModeRenderer10();
			renderer.begin(GL10.GL_LINE_LOOP);
			renderer.color(1, 1, 1, 1);
			renderer.vertex(xx, yx, 0);
			renderer.vertex(xx + s, yx, 0);
			renderer.vertex(xx + s, yx + s, 0);
			renderer.vertex(xx, yx + s, 0);
			renderer.end();
			break;

		case NEW_FRUIT:
			panier.check_fruits_in();
			textActor.addAction(sequence(alpha(0.0f, 1), alpha(1.0f, 1)));
			wait = NB_CYCLE;
			if (--nb_fruits < 0)
				examGamePart = GamePart.LEVEL_END;
			else {
				fruits.add(new Fruit(this, tofTable[(level-1)%TypeOfObject.getNbFruits()],
						0.1f));
				fruits.peek().getBody().setUserData(new String("out"));
				fruits.peek().addAction(
						sequence(delay(10.0f), alpha(0.5f, 5.0f),
								run(new Runnable() {
									public void run() {
										System.out.println("Action complete!");
									}
								})));
				//laché de pomme: toujours au même endroit par rapport à la bascule 
				//et y toujours à la même hauteur par rapport au panier
				int x = (int) (getBascule().getImgPlanche().getX());
				int y =  (int) (panier.getImgPanier().getY()*1.5);
				if (debug)
					x = (int) (panier.getImgPanier().getX() + 10);
				int angle = 0;// new Random().nextInt(360);
				fruits.peek().ResetPosition(x, y, angle);
				stage.addActor(fruits.peek());
				imageFruits.removeIndex(imageFruits.size-1).remove();
				panier.getImgPanier_front().setZIndex(99);
				Gdx.app.log("Fumper/playScreen/Render/NEW_FRUIT/", "nb fruits="
						+ fruits.size + "/zindex=" + fruits.peek().getZIndex()
						+ "/zindex panier=" + panier.getImgPanier().getZIndex());
				examGamePart = GamePart.EN_COURS;
			}

			break;

		case LEVEL_END:
			if (UpdateFruits(fruits)==0) 
				examGamePart = GamePart.NEW_LEVEL;
			break;
			
		case NEW_LEVEL:
//TODO GAME OVER SCORE PAS PROGRESSER DEPUIS LE DERNIER LEVEL
			level++;
			nb_fruits = 10;
			if (level>TypeOfObject.getNbFruits()){
				//TODO Bonus level? SplashBonus? SplashNexLevel?
			}

			//On monte le panier
			if (panier != null)
				panier.RemovePanier();
			panier = new Panier(this, Gdx.graphics.getWidth() / 1.5f,
					(Gdx.graphics.getWidth() / 3f * (1.0f+((level-1)%TypeOfObject.getNbFruits()) / 10f)), 0.2f
							* Gdx.graphics.getWidth()
							/ AbstractScreen.GAME_VIEWPORT_WIDTH);
			panier.getImgPanier_front().setZIndex(99);

			if (score > highscore){
				highscore = score;
				highscore_name="UnFamousKnown";
			}
			examGamePart = GamePart.EN_COURS;
			FruitsDansArbre();
			break;
			
			
		case QUIT:
			if (game.FumperPrefs.getHighScore() < score){
				Gdx.input.getTextInput(listener, "Nice ! Farmer !", "What's Your Name?");
			}
			game.setScreen(new SplashScreen(game));
			dispose();
			break;
		default:
			break;
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		
		// Recréation de la Bascule 
		if (getBascule() != null)
			getBascule().RemoveCatapult();
		setBascule(new Bascule(this, width / 4,  width / 10f, 0.08f * width
				/ AbstractScreen.GAME_VIEWPORT_WIDTH));
		
		//Recréation du Panier
		if (panier != null)
			panier.RemovePanier();
		panier = new Panier(this, width / 1.5f, width / 3f
				* (0.9f + level / 10f), 0.2f * width
				/ AbstractScreen.GAME_VIEWPORT_WIDTH);
		
		//resize Fruits
		if (fruits != null) {
			for (Fruit i : fruits) {
				i.setPosition(i.getBody().getPosition().x
						* AbstractScreen.BOX_TO_WORLD, i.getBody()
						.getPosition().y * AbstractScreen.BOX_TO_WORLD);
				i.setRotation(i.getBody().getAngle()
						* MathUtils.radiansToDegrees);
			}
		}
		//Mettre les pommes dans l'arbre
		if (imageFruits!=null)
			while(imageFruits.size>0){
				imageFruits.removeIndex(imageFruits.size-1).remove();
			}
		FruitsDansArbre();
		
		//Resize the tapandplay
		tapandplayActor.setScale(width/1000.0f);
		tapandplayActor.setPosition(Gdx.graphics.getWidth()/1.5f,
				Gdx.graphics.getHeight()/50.0f);
		
		
		//Resize Font
		if (textActor != null) {
			textActor.remove();
		}
		float sizefont=(float)width / (float)AbstractScreen.GAME_VIEWPORT_WIDTH/1.7f ;
		font_berlin.setScale(sizefont);
		textActor = new Text(font_berlin, "");
		textActor.setPosition(10, Gdx.graphics.getHeight());
		stage.addActor(textActor);

	}

	private void FruitsDansArbre(){

		imageFruits=new Array<Image>();
		AtlasRegion ImgRegion = getAtlas().findRegion(tofTable[(level-1)%TypeOfObject.getNbFruits()].getNameImage());
		Drawable ImgDrawable = new TextureRegionDrawable(ImgRegion);
		for (int i=0;i<nb_fruits;i++){
			imageFruits.add(new Image(ImgDrawable));
			imageFruits.peek().setScale(Gdx.graphics.getWidth()*0.0005f);
			imageFruits.peek().setPosition(Gdx.graphics.getWidth()/11+new Random().nextInt(Gdx.graphics.getWidth()/6), 
					Gdx.graphics.getHeight()/2.2f+new Random().nextInt(Gdx.graphics.getHeight()/10));
			stage.addActor(imageFruits.peek());
		}
	}
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * @return the bascule
	 */
	public Bascule getBascule() {
		return bascule;
	}

	/**
	 * @param bascule
	 *            the bascule to set
	 */
	public void setBascule(Bascule bascule) {
		this.bascule = bascule;
	}
}
