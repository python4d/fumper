package com.python4d.fumper;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class PlayScreen extends AbstractScreen {
	private enum GamePart {
		DEBUT, FIN, EN_COURS, LEVEL_END, NEW_LEVEL, NEW_FRUIT, QUIT, GAMEOVER, BONUS_TIME, BONUS_TIME_END
	};
	
	private static String copyright="(c) BacoLand/Python4D - 9/2014 - v1.05";
	private boolean superman=false;
	
	ParticleEffectActor effectActor;
	private GamePart examGamePart = GamePart.DEBUT;
	private Image bgImage, tapandplayActor,splash;
	private Bascule bascule;
	private Array<Fruit> fruits = new Array<Fruit>();
	private Array<Image> imageFruits = null;

	public Array<Fruit> getFruits() {
		return fruits;
	}

	private boolean start_ok = false;
	private int level = 1;
	private int nb_fruits = 10;

	private int score = 0;
	private int lastlevel_score = 0;
	private int highscore = 0, highscoreWEB = -1;
	private String highscore_name = new String("!wait for local info!");
	private String highscoreWEB_name = new String("!wait for web info!");
	private boolean bonus_flag=false;
	
	private AnimatedActor fusee, oiseau;
	private Panier panier = null;
	private TextActor textCopyright,textScore, textHighScore, textGameOver, textStart,textBonus;
	public PlayScreen(Fumper game) {
		super(game);
		highscore = game.FumperPrefs.getHighScore();
		highscore_name = new String(game.FumperPrefs.getHighScoreName());

		// Utilisation d'un timer pour lancer un thread FTP toutes les 30
		// secondes
		new Timer().scheduleTask(new Task() {
			@Override
			public void run() {

				new Thread(new Runnable() {
					@Override
					public void run() {
						TestHighScoreWeb(
								HighScoreWebBox,
								"Your Local HighScore Kills WebScore's ! What's Your Name Farmer?",
								"");
					}
				}).start();

			}

		}, 0, 30);
	}

	/**
	 * Boite de dialog de base pour enregistrer le nom WEB du farmer vainqueur
	 */
	private TextInputListener HighScoreWebBox = new TextInputListener() {

		@Override
		public void input(String text) {
			highscoreWEB_name = text.toString();

			String result = new String(new FTPConnect().writefile("fumper.txt",
					new String[] { "farmer", highscoreWEB_name }, new String[] {
							"hs", Integer.toString(highscore) }));
			myToast.makeText(result, font_berlin, 10f);
			if (!result.startsWith("!"))
				highscoreWEB = highscore;
		}

		@Override
		public void canceled() {
			myToast.makeText("Canceled? What a Strange Farmer...", font_berlin,
					10f);
		}

	};

	/**
	 * Boite de dialog de base pour enregistrer le nom du farmer vainqueur
	 */

	@SuppressWarnings("unused")
	private TextInputListener listener = new TextInputListener() {

		@Override
		public void input(String text) {
			highscore_name = text.toString();
			game.FumperPrefs.setHighScore(score, highscore_name);
		}

		@Override
		public void canceled() {
			game.FumperPrefs.setHighScore(score, highscore_name);
		}

	};

	@Override
	public void show() {
		super.show();
		FumperSound.bascule.load();
		FumperSound.fruit_bascule.load();
		FumperSound.spaceship.load();
		FumperSound.bird.load();

		// Mettre le fond d'écran - valade pendant tout le jeu
		AtlasRegion bgRegion = getAtlas().findRegion("background/background");
		Drawable bgDrawable = new TextureRegionDrawable(bgRegion);
		bgImage = new Image(bgDrawable);
		bgImage.setFillParent(true);
		stage.addActor(bgImage);
		
		//copyright
		textCopyright=new TextActor(font_berlin_copyright,copyright);

		stage.addActor(textCopyright);
		// Splash
		splash = new Image(new TextureRegionDrawable(getAtlas()
				.findRegion(TypeOfObject.splash.getNameImage())));
		splash.setOrigin(splash.getWidth()/2f, splash.getHeight()/2f);
		stage.addActor(splash);
		// Petite main instructive
		tapandplayActor = new Image(new TextureRegionDrawable(getAtlas()
				.findRegion(TypeOfObject.tapandplay.getNameImage())));
		stage.addActor(tapandplayActor);
		//oiseau
		Array<AtlasRegion> arrayOiseau = new Array<AtlasRegion>();
		for (int i = 0; i < TypeOfObject.oiseau.getNbImages(); i++) {
			arrayOiseau.add(getAtlas().findRegion(
					TypeOfObject.oiseau.getAllImages()[i]));
		}
		Animation animationOiseau = new Animation(0.15f, arrayOiseau,
				Animation.LOOP);
		oiseau = new AnimatedActor(animationOiseau);

		stage.addActor(oiseau);
		// fusée animée
		Array<AtlasRegion> arrayFusee = new Array<AtlasRegion>();
		for (int i = 0; i < TypeOfObject.fusee.getNbImages(); i++) {
			arrayFusee.add(getAtlas().findRegion(
					TypeOfObject.fusee.getAllImages()[i]));
		}
		Animation animationFusee = new Animation(0.05f, arrayFusee,
				Animation.LOOP);
		fusee = new AnimatedActor(animationFusee);
		fusee.setPosition(-1000, -1000);
		stage.addActor(fusee);
		// la fusee qui brule
		ParticleEffect effect = new ParticleEffect();
		effect.load(Gdx.files.internal("effects/ParticleEffects.p"),
				Gdx.files.internal("effects"));
		effectActor = new ParticleEffectActor(effect, fusee);
		stage.addActor(effectActor);

		// Animation du text GameOver & text Start ou Replay
		textBonus = new TextActor(font_goodgirl3, "Message Bonus!");
		textBonus.addAction(fadeOut(0f));
		textBonus.setScale(Gdx.graphics.getWidth()/500f);
		stage.addActor(textBonus);
		// Animation du text GameOver & text Start ou Replay
		textGameOver = new TextActor(font_goodgirl2, "GAME OVER");
		textGameOver.setVisible(false);
		textGameOver.addAction(forever(parallel(
				sequence(moveBy(5f, 0, 0.5f),
						moveBy(-5f, 0, 0.5f)),
				sequence(scaleBy(0.1f, 0.1f, 0.5f),
						scaleBy(-0.1f, -0.1f, 0.5f)))));
		stage.addActor(textGameOver);
		textStart = new TextActor(font_goodgirl, "START FUMPER");
		textStart.setTouchable(Touchable.enabled);
		textStart.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.log(
						"Fumper/PlayScreen/textStart/clicked(x,y)" + "(" + x
								+ "," + y + ")" + "(getX,getY)=",
						"(" + textStart.getX() + "," + textStart.getY()
								+ ") et (textStart.width,height)=("
								+ textStart.getWidth() + ","
								+ textStart.getHeight() + ")");
				start_ok = true;
			}
		});
		textStart.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Example", "touch started at (" + x + ", " + y
						+ ")");
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Example", "touch done at (" + x + ", " + y + ")");
			}
		});

		stage.addActor(textStart);

		// HUD=Score et Highscore
		textScore = new TextActor(font_hennypenny, "");
		stage.addActor(textScore);
		textHighScore = new TextActor(font_hennypenny2, "");
		stage.addActor(textHighScore);
		Gdx.input.setInputProcessor(stage);
		stage.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.log("Fumper/PlayScreen/ClickListener of stage/",
						"button clicked");
			}

		});

		stage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
					if (!myToast
							.makeText("Back Again to Quit", font_berlin, 1f))
						examGamePart = GamePart.QUIT;
				}
				return super.keyDown(event, keycode);
			}

			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Fumper/PlayScreen/InputListenerStage",
						"touch started at (" + x + ", " + y + ")");
				getBascule().push(level+(bonus_flag?100:0)+(superman?100:0));
				Gdx.app.log("PlayScreen/touchDown=>", "bonus="+bonus_flag+", level="+level+", force="+(level+(bonus_flag?100:0)));
				FumperSound.bascule.play(0.5f);

				Gdx.app.log(
						"Fumper/PlayScreen/InputListenerStage/touchDown/Hit Actor=",
						"" + stage.hit(x, y, false).getName());
				try {
					Gdx.input.vibrate(50);
				} finally {

				}
				return true;
			}

			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Fumper/PlayScreen/InputListenerStage/touchUp",
						"touch done at (" + x + ", " + y + ")");
			}
		});
	}

	/**
	 * Permet de supprimer les fruits hors champ et controler ceux qui sont dans
	 * le panier
	 * 
	 * @param fruits
	 * @return la taille de Array des fruits
	 */
	private int UpdateFruits(Array<Fruit> fruits, boolean immediat) {

		int index = 0;
		for (Fruit i : fruits) {
			// cas spécial du fruit qui va dans les airs => le Bonus time permet de lancer très fort...
			if (i.getY() > Gdx.graphics.getWidth()*3f
					&& fusee.getActions().size == 0) {
				score+=5;
				textScore.addAction(sequence(scaleBy(0.5f,0.5f, 0.5f), scaleBy(-0.5f,-0.5f, 0.5f)));
				// On positionne en dehors de l'écran la fusee
				fusee.setZIndex(oiseau.getZIndex()-1);
				fusee.setScale(Gdx.graphics.getWidth()/500f );
				fusee.setPosition(Gdx.graphics.getWidth(),
						Gdx.graphics.getHeight() -100);
				fusee.setRotation(80);
				effectActor.setScale(fusee.getScaleX());
				fusee.addAction(sequence(
						parallel(
								moveTo(-500, Gdx.graphics.getHeight()/3, 5f),
								rotateTo(100f,5f)
								)));
				FumperSound.spaceship.play(0.3f);
				effectActor.start();
			}
			//cas spécial de l'oiseau => le Bonus time permet de lancer très fort...
			float so=Gdx.graphics.getWidth()/200.0f;
			float x=i.getBody().getWorldCenter().x * AbstractScreen.BOX_TO_WORLD;
			float xo=oiseau.getX()+oiseau.getWidth()/2.0f*so;
			float yo=oiseau.getY()+oiseau.getHeight()/2.0f*so;
			float y=i.getBody().getWorldCenter().y * AbstractScreen.BOX_TO_WORLD;
			
			if ((y>(yo-oiseau.getHeight()*so/2f))&&
				(y<(yo+oiseau.getHeight()*so/2f))&&
				(x>(xo-oiseau.getWidth()*so/2f))&&
				(x<(xo+oiseau.getWidth()*so/2f))&&
				splash.getActions().size == 0){
				resizeOiseau();
				score+=2;
				splash.setPosition(xo-splash.getOriginX(), yo-splash.getOriginY());
				splash.toFront();
				splash.addAction((sequence(
											fadeIn(0f),
											parallel(
													rotateBy(90f, 0.2f),
													scaleTo(0.5f, 0.5f, 0.2f)),
											delay(1f),
											fadeOut(0f),
											rotateBy(-90f),
											scaleTo(Gdx.graphics.getWidth()/200, Gdx.graphics.getWidth()/200))));
				
				Gdx.app.log("Playscreen/UpdateFruits=>", "Oiseau touché!");
				FumperSound.bird.getSnd().play(1f, 0.5f, 0f);
				//FumperSound.bird.play();
			}
			// Quels fruits détruire?
			if (i.getBody().getPosition().x * AbstractScreen.BOX_TO_WORLD < -100
					|| i.getBody().getPosition().x
							* AbstractScreen.BOX_TO_WORLD > Gdx.graphics
							.getWidth() + 100
					|| i.getBody().getPosition().y
							* AbstractScreen.BOX_TO_WORLD < -100
					|| i.getColor().a == 0.5f
					|| (i.getBody().getUserData().equals("in") && immediat)
					&& !worldbox.isLocked()) {
				if (i.getBody().getUserData().equals("in")) {
					score++;
					textScore.addAction(sequence(scaleBy(0.5f,0.5f, 0.5f), scaleBy(-0.5f,-0.5f, 0.5f)));
					FumperSound.fruit_bascule.play();
				}
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

	private void UpdateHUD() {
		String txt= new String("Score=" + score + "\nLevel=" + level);
		textScore.setText(txt);
		StringBuffer chaine = new StringBuffer("Local HighScore=" + highscore);
		if (highscoreWEB < 0 || highscoreWEB_name.startsWith("!"))
			textHighScore.setText(chaine
					+ "\n Web HighScore=<Internet not available>");
		else
			textHighScore.setText("Local HighScore=" + highscore
					+ "\nWeb HighScore=" + highscoreWEB + "\t by "
					+ highscoreWEB_name);

	}

	/**
	 * State machine générale / Création de la frame via super() (cf
	 * AbsctractScreen)
	 */
	@Override
	public void render(float delta) {
		super.render(delta);

		UpdateHUD();
		getBascule().update();
		getPanier().update();
		switch (examGamePart) {

		case DEBUT:
			if (start_ok) {
				textGameOver.setVisible(false);
				textStart.setVisible(false);
				score = 0;
				level = superman?9:1;
				nb_fruits = 10;
				resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				examGamePart = GamePart.EN_COURS;
			}
			break;
		case EN_COURS:
			// quand doit-on laisser un nouveau fruit? soit plus de body fruits dans le jeu wait-- < 0 
			if (fruits.size-panier.check_fruits_in()<= 0 || (bonus_flag? wait--<0:false))
				examGamePart = GamePart.NEW_FRUIT;
			else
				UpdateFruits(fruits, false);
			break;

		case NEW_FRUIT:
			panier.check_fruits_in();
			textScore.addAction(sequence(alpha(0.0f, 0.2f), alpha(1.0f,0.2f)));
			wait = bonus_flag?NB_CYCLE_FOR_5S:NB_CYCLE_FOR_15S;
			wait = debug?NB_CYCLE_FOR_1S:wait;
			if (--nb_fruits < 0)
				examGamePart = GamePart.LEVEL_END;
			else {
				fruits.add(new Fruit(this, tofTable[(level - 1)
						% TypeOfObject.getNbFruits()], 0.1f*(1+(level-1)/100f)));
				fruits.peek().addAction(
						sequence(delay(10.0f), alpha(0.5f, 5.0f),
								run(new Runnable() {
									public void run() {
										System.out.println("Action complete!");
									}
								})));
				fruits.peek().getBody().setUserData(new String("out"));
				// laché de pomme: toujours au même endroit par rapport à la
				// bascule
				// et y toujours à la même hauteur par rapport au panier
				int x = (int) (getBascule().getImgBuche().getX()-Gdx.graphics.getWidth()/6f);
				int y = (int) (panier.getImgPanier().getY() * 1.5);
				if (debug)
					x = (int) (panier.getImgPanier().getX() + 5);
				int angle = 0;// new Random().nextInt(360);
				fruits.peek().ResetPosition(x, y, angle);
				stage.addActor(fruits.peek());
				imageFruits.removeIndex(imageFruits.size - 1).remove();
				panier.getImgPanier_front().setZIndex(50);
				Gdx.app.log("Fumper/playScreen/Render/NEW_FRUIT/", "nb fruits="
						+ fruits.size + "/zindex=" + fruits.peek().getZIndex()
						+ "/zindex panier=" + panier.getImgPanier().getZIndex());
				examGamePart = GamePart.EN_COURS;
			}

			break;

		case LEVEL_END:
			if (UpdateFruits(fruits, true) == 0) // vérifie qu'il n'y a plus de
													// fruit en mouvement ou sur
													// l'écran
				if (score > lastlevel_score || bonus_flag) {
					examGamePart = GamePart.NEW_LEVEL;
					lastlevel_score = score;
				} else {
					examGamePart = GamePart.GAMEOVER;
					lastlevel_score = 0;
				}
			else
				panier.check_fruits_in();
			break;

		case NEW_LEVEL:
			if (bonus_flag) {
				examGamePart = GamePart.BONUS_TIME;
				break;
			}
			level++;
			if (level % TypeOfObject.getNbFruits() == 0) {
				textBonus.setText("Bonus Time !");	
				textBonus.toFront();
				textBonus.setPosition(Gdx.graphics.getWidth()/2-textBonus.getWidth()/2, Gdx.graphics.getHeight()/2);
				textBonus.addAction(parallel(
						fadeIn(0f),
								moveBy(0f, Gdx.graphics.getHeight()/3, 3f),
								scaleBy(1f, 1f, 3f),
								sequence(
										delay(2f),
										fadeOut(1f),
										scaleBy(-1f, -1f, 0f))));
				bonus_flag=true;
				nb_fruits = 50;
			}
			else
				nb_fruits = 10;
			
			// On monte le panier
			if (panier != null)
				panier.RemovePanier();
			panier = new Panier(
					this,
					Gdx.graphics.getWidth() / 1.5f,
					(Gdx.graphics.getWidth() / 3f * (1.0f + ((level - 1) % TypeOfObject
							.getNbFruits()) / 10f)), 0.2f * Gdx.graphics
							.getWidth() / 400);
			panier.getImgPanier_front().toFront();

			if (score > highscore) {
				highscore = score;
				highscore_name = "Still UnFamousKnown";
			}
			examGamePart = GamePart.EN_COURS;
			FruitsDansArbre();
			break;
		case BONUS_TIME:
			wait=NB_CYCLE_FOR_20S;
			examGamePart = GamePart.BONUS_TIME_END;
			bonus_flag=false;
			textBonus.setText("End of Bonus Time...");
			textBonus.toFront();
			textBonus.setPosition(Gdx.graphics.getWidth()/2-textBonus.getWidth()/2, Gdx.graphics.getHeight()/2);
			textBonus.addAction(parallel(fadeIn(0f),
							moveBy(0f, Gdx.graphics.getHeight()/3, 3f),
							scaleBy(0.5f, 0.5f, 3f),
							sequence(
									delay(2f),
									fadeOut(1f)),
							scaleBy(-0.5f, -0.5f, 0f)));
			
			break;
		case BONUS_TIME_END:
			if (wait--<0) 
				examGamePart = GamePart.NEW_LEVEL;
			break;
		case GAMEOVER:
			textGameOver.setVisible(true);
			textStart.setText("Play Again");
			resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			textStart.setVisible(true);
			examGamePart = GamePart.DEBUT;
			start_ok = false;
			break;
		case QUIT:
			start_ok = false;
			if (game.FumperPrefs.getHighScore() < score) {
				// Gdx.input.getTextInput(listener,
				// "Nice ! Farmer !\nWhat's Your Name?", "");
				game.FumperPrefs.setHighScore(score, "local");
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					TestHighScoreWeb(
							HighScoreWebBox,
							"Nice ! You kill WebScore ! \nWhat's Your Name Farmer?",
							"");
				}
			}).start();
			dispose();
			game.setScreen(new SplashScreen(game));
			break;
		default:
			break;
		}
	}

	/**
	 * Connection FTP pour récupérer et vérifier que le WebScore a été ou non
	 * battu
	 * 
	 * @param ListenerBoiteDialog
	 * @param LibBoiteDialog
	 * @param ValeurDefaut
	 */
	private void TestHighScoreWeb(TextInputListener ListenerBoiteDialog,
			String LibBoiteDialog, String ValeurDefaut) {

		try {
			highscoreWEB = Integer.parseInt(new FTPConnect().readfile(
					"fumper.txt", "hs"));
		} catch (NumberFormatException e) {
			highscoreWEB = -1;
		}
		highscoreWEB_name = new FTPConnect().readfile("fumper.txt", "farmer");
		if (highscoreWEB_name.startsWith("!")) {
			//myToast.makeText(highscoreWEB_name, font_berlin, 10f);
		} else {
			if (highscore > highscoreWEB && highscoreWEB > 0 && !start_ok)
				Gdx.input.getTextInput(ListenerBoiteDialog, LibBoiteDialog,
						ValeurDefaut);

		}
	}

	void resizeOiseau(){
		//oiseau
		oiseau.clearActions();
		oiseau.setScale(Gdx.graphics.getWidth()/200f);
		oiseau.setPosition(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()/1.2f);
		oiseau.addAction(forever(parallel(
								sequence(
										moveTo(-Gdx.graphics.getWidth()/10, oiseau.getY(), 10f),
										moveTo(Gdx.graphics.getWidth(), oiseau.getY(),0f))
								)));
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);

		// Recréation du Panier
		if (panier != null)
			panier.RemovePanier();
		panier = new Panier(
				this,
				Gdx.graphics.getWidth() / 1.5f,
				(Gdx.graphics.getWidth() / 3f * (1.0f + ((level - 1) % TypeOfObject
						.getNbFruits()) / 10f)), 0.2f * Gdx.graphics
						.getWidth() / 400);
		// Recréation de la Bascule
		if (bascule != null) {
			bascule.RemoveCatapult();
			bascule = null;
		}
		bascule = new Bascule(this, width / 4, width / 10f, 0.08f * width / 400);
		// Mettre les pommes dans l'arbre
		if (imageFruits != null)
			while (imageFruits.size > 0) {
				imageFruits.removeIndex(imageFruits.size - 1).remove();
			}
		FruitsDansArbre();
		
		//Resize Oiseau et Splash pour l'oiseau mort
		splash.setScale(Gdx.graphics.getWidth()/200);
		splash.getColor().a=0;
		resizeOiseau();			

		
		// Resize the tapandplay & start button
		tapandplayActor.setScale(width / 1000.0f);
		tapandplayActor.setPosition(Gdx.graphics.getWidth() / 1.5f,
				Gdx.graphics.getHeight() / 50.0f);

		// Resize Font
		if (textStart != null) {
			float sizefont = (float) width / 500f;
			textStart.setScale(sizefont);
			textStart.setPosition(width / 2 - textStart.getWidth() / 2, height);
			textStart.toFront();
			textStart.addAction(sequence(
					delay(1f),
					moveTo(width / 2 - textStart.getWidth() / 2f,
							height / 2f, 1f, Interpolation.bounceOut),
					forever(sequence(delay(3f), moveBy(0f, 10f,
							1f), moveBy(0f, -10f, 1f,
							Interpolation.bounceOut)))));
		}

		if (textGameOver != null) {
			float sizefont = (float) width / 300f;
			textGameOver.setScale(sizefont);
			textGameOver.setPosition(width / 2 - textGameOver.getWidth() / 2,
					height / 5);
			textGameOver.toFront();
		}
		if (textHighScore != null) {
			float sizefont = (float) width / 800f;
			textHighScore.setScale(sizefont);
			textHighScore.setPosition(10, Gdx.graphics.getHeight() - 100);

		}
		if (textScore != null) {
			float sizefont = (float) width / 400f;
			textScore.setScale(sizefont);
			textScore.setPosition(10,
					textHighScore.getY() - textHighScore.getHeight() * 4);
		}
		if (textCopyright != null) {
			textCopyright.toFront();
			textCopyright.setScale(Gdx.graphics.getWidth()/1500f);
			textCopyright.setPosition(Gdx.graphics.getWidth()-textCopyright.getStrWidth(),textCopyright.getStrHeight()/2f);
		}
	}

	private void FruitsDansArbre() {
		FruitsDansArbre(nb_fruits, level);
	}

	private void FruitsDansArbre(int nb_fruits, int level) {

		imageFruits = new Array<Image>();
		AtlasRegion ImgRegion = getAtlas().findRegion(
				tofTable[(level - 1) % TypeOfObject.getNbFruits()]
						.getNameImage());
		Drawable ImgDrawable = new TextureRegionDrawable(ImgRegion);
		for (int i = 0; i < nb_fruits; i++) {
			imageFruits.add(new Image(ImgDrawable));
			imageFruits.peek().setScale(Gdx.graphics.getWidth() * 0.0005f);
			imageFruits.peek()
					.setPosition(
							Gdx.graphics.getWidth()
									/ 11
									+ new Random().nextInt(Gdx.graphics
											.getWidth() / 6),
							Gdx.graphics.getHeight()
									/ 2.2f
									+ new Random().nextInt(Gdx.graphics
											.getHeight() / 10));
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
	 * @return the panier
	 */
	public Panier getPanier() {
		return panier;
	}
	/**
	 * @param bascule
	 *            the bascule to set
	 */
	public void setBascule(Bascule bascule) {
		this.bascule = bascule;
	}
}
