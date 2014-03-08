package com.python4d.fumper;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import java.awt.Event;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.python4d.fumper.Toast.TEXT_POS;

public class PlayScreen extends AbstractScreen {
	private enum GamePart {
		DEBUT, FIN, EN_COURS, LEVEL_END, NEW_LEVEL, NEW_FRUIT, QUIT, GAMEOVER
	};

	private GamePart examGamePart = GamePart.DEBUT;
	private Image bgImage, startActor, tapandplayActor;
	private Bascule bascule;
	private Body camion, oiseau;
	private Array<Fruit> fruits = new Array<Fruit>();
	private Array<Image> imageFruits = null;

	public Array<Fruit> getFruits() {
		return fruits;
	}

	private boolean start_ok = false;
	private boolean quit = false;
	private int level = 1;
	private int nb_fruits = 10;

	private int score = 0;
	private int lastlevel_score = 0;
	private int highscore = 0, highscoreWEB = -1;
	private String highscore_name, highscoreWEB_name;

	private Panier panier = null;
	private TextActor textScore, textHighScore, textGameOver, textStart;

	public PlayScreen(Fumper game) {
		super(game);
		highscore = game.FumperPrefs.getHighScore();
		highscore_name = new String(game.FumperPrefs.getHighScoreName());

		TestHighScoreWeb(
				HighScoreWebBox,
				"Your Local HighScore Kills WebScore's ! What's Your Name Farmer?",
				"");
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
			myToast.makeText(result, font_berlin,10f);
			if (!result.startsWith("!"))
				highscoreWEB = highscore;
		}

		@Override
		public void canceled() {
			myToast.makeText("Canceled? Strange Farmer...", font_berlin, 5f);
		}

	};

	/**
	 * Boite de dialog de base pour enregistrer le nom du farmer vainqueur
	 */

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
		// Mettre le fond d'écran - valade pendant tout le jeu
		AtlasRegion bgRegion = getAtlas().findRegion("background/background");
		Drawable bgDrawable = new TextureRegionDrawable(bgRegion);
		bgImage = new Image(bgDrawable);
		bgImage.setFillParent(true);
		stage.addActor(bgImage);
		// Animation du bouton start ou TextActor Start
		startActor = new Image(new TextureRegionDrawable(getAtlas().findRegion(
				TypeOfObject.start.getNameImage())));
		startActor.setOrigin(startActor.getWidth() / 2,
				startActor.getHeight() / 2);
		startActor.addAction(forever(sequence(
				Actions.scaleBy(0.2f, 0.01f, 0.5f, Interpolation.exp5In),
				Actions.scaleBy(-0.2f, -0.01f, 0.5f, Interpolation.exp5Out))));

		boolean b = startActor.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.log("Fumper/PlayScreen/startActor addlistener/",
						"Actor button Start clicked");
				start_ok = true;
			}

		});
		// stage.addActor(startActor);
		textStart = new TextActor(font_goodgirl, "START FUMPER");
		b = textStart.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.log("Fumper/PlayScreen/textStart ClickListener/(getX,getY)=",	"("+textStart.getX()+","+textStart.getY()+") et (textStart.width,height)=("+textStart.getWidth()+","+textStart.getHeight()+")");
				start_ok = true;
			}

		});
		stage.addActor(textStart);

		tapandplayActor = new Image(new TextureRegionDrawable(getAtlas()
				.findRegion(TypeOfObject.tapandplay.getNameImage())));
		stage.addActor(tapandplayActor);

		// Animation du text GameOver
		textGameOver = new TextActor(font_goodgirl, "GAME OVER");
		textGameOver.setVisible(false);
		textGameOver.setOrigin(startActor.getWidth() / 2,
				startActor.getHeight() / 2);
		textGameOver.addAction(forever(sequence(Actions.moveBy(5f, 0, 0.5f),
				Actions.moveBy(-5f, 0, 0.5f))));
		stage.addActor(textGameOver);

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
					if (!myToast.makeText("Back Again to Quit", font_berlin, 1f))
						examGamePart = GamePart.QUIT;
				}
				return super.keyDown(event, keycode);
			}

			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				Gdx.app.log("Fumper/PlayScreen/All stage", "touch started at ("
						+ x + ", " + y + ")");
				getBascule().push(level);
				FumperSound.bascule.play();

				Gdx.app.log("Fumper/PlayScreen/InputListener of stage/touchDown/Hit Actor=",""+stage.hit( x,  y,false).getName()) ;
				try {
					Gdx.input.vibrate(50);
				} finally {

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
	 * Permet de supprimer les fruits hors champ et controler ceux qui sont dans
	 * le panier
	 * 
	 * @param fruits
	 * @return la taille de Array des fruits
	 */
	private int UpdateFruits(Array<Fruit> fruits, boolean immediat) {

		int index = 0;
		for (Fruit i : fruits) {
			// Quels fruits détruire?
			if (i.getBody().getPosition().x * AbstractScreen.BOX_TO_WORLD < -100
					|| i.getBody().getPosition().y
							* AbstractScreen.BOX_TO_WORLD < -100
					|| i.getColor().a == 0.5f
					|| (i.getBody().getUserData().equals("in") && immediat)
					&& !worldbox.isLocked()) {
				if (i.getBody().getUserData().equals("in")) {
					score++;
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
		textScore.setText("Score=" + score + "\nLevel=" + level);
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
		getBascule().render();

		switch (examGamePart) {

		case DEBUT:
			UpdateHUD();
			if (start_ok) {
				textGameOver.setVisible(false);
				textStart.setVisible(false);
				start_ok = false;
				score=0;
				level=1;
				nb_fruits = 10;
				resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
				examGamePart = GamePart.EN_COURS;
			}
			break;
		case EN_COURS:
			if (wait-- < 0)
				examGamePart = GamePart.NEW_FRUIT;
			else {
				UpdateFruits(fruits, false);
				UpdateHUD();
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
			textScore.addAction(sequence(alpha(0.0f, 1), alpha(1.0f, 1)));
			wait = NB_CYCLE;
			if (--nb_fruits < 0)
				examGamePart = GamePart.LEVEL_END;
			else {
				fruits.add(new Fruit(this, tofTable[(level - 1)
						% TypeOfObject.getNbFruits()], 0.1f));
				fruits.peek().getBody().setUserData(new String("out"));
				fruits.peek().addAction(
						sequence(delay(10.0f), alpha(0.5f, 5.0f),
								run(new Runnable() {
									public void run() {
										System.out.println("Action complete!");
									}
								})));
				// laché de pomme: toujours au même endroit par rapport à la
				// bascule
				// et y toujours à la même hauteur par rapport au panier
				int x = (int) (getBascule().getImgPlanche().getX());
				int y = (int) (panier.getImgPanier().getY() * 1.5);
				if (debug)
					x = (int) (panier.getImgPanier().getX() + 10);
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
				if (score > lastlevel_score) {
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
			// TODO GAME OVER SCORE PAS PROGRESSER DEPUIS LE DERNIER LEVEL
			level++;
			nb_fruits = 10;
			if (level > TypeOfObject.getNbFruits()) {
				// TODO Bonus level? SplashBonus? SplashNexLevel?
			}

			// On monte le panier
			if (panier != null)
				panier.RemovePanier();
			panier = new Panier(
					this,
					Gdx.graphics.getWidth() / 1.5f,
					(Gdx.graphics.getWidth() / 3f * (1.0f + ((level - 1) % TypeOfObject
							.getNbFruits()) / 10f)), 0.2f
							* Gdx.graphics.getWidth()
							/ AbstractScreen.GAME_VIEWPORT_WIDTH);
			panier.getImgPanier_front().setZIndex(50);

			if (score > highscore) {
				highscore = score;
				highscore_name = "Still UnFamousKnown";
			}
			examGamePart = GamePart.EN_COURS;
			FruitsDansArbre();
			break;

		case GAMEOVER:
			textGameOver.setVisible(true);
			textStart.setText("Play Again");
			resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
			textStart.setVisible(true);
			examGamePart = GamePart.DEBUT;
			break;
		case QUIT:
			if (game.FumperPrefs.getHighScore() < score) {
				// Gdx.input.getTextInput(listener,
				// "Nice ! Farmer !\nWhat's Your Name?", "");
				game.FumperPrefs.setHighScore(score, "local");
			}
			TestHighScoreWeb(HighScoreWebBox,
					"Nice ! You kill WebScore ! \nWhat's Your Name Farmer?", "");
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
			myToast.makeText(highscoreWEB_name, font_berlin,10f);
		} else {
			if (highscore > highscoreWEB)
				Gdx.input.getTextInput(ListenerBoiteDialog, LibBoiteDialog,
						ValeurDefaut);

		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);

		// Recréation de la Bascule
		if (getBascule() != null)
			getBascule().RemoveCatapult();
		setBascule(new Bascule(this, width / 4, width / 10f, 0.08f * width
				/ AbstractScreen.GAME_VIEWPORT_WIDTH));

		// Recréation du Panier
		if (panier != null)
			panier.RemovePanier();
		panier = new Panier(this, width / 1.5f, width / 3f
				* (0.9f + level / 10f), 0.2f * width
				/ AbstractScreen.GAME_VIEWPORT_WIDTH);

		// resize Fruits
		if (fruits != null) {
			for (Fruit i : fruits) {
				i.setPosition(i.getBody().getPosition().x
						* AbstractScreen.BOX_TO_WORLD, i.getBody()
						.getPosition().y * AbstractScreen.BOX_TO_WORLD);
				i.setRotation(i.getBody().getAngle()
						* MathUtils.radiansToDegrees);
			}
		}
		// Mettre les pommes dans l'arbre
		if (imageFruits != null)
			while (imageFruits.size > 0) {
				imageFruits.removeIndex(imageFruits.size - 1).remove();
			}
		FruitsDansArbre();

		// Resize the tapandplay & start button
		tapandplayActor.setScale(width / 1000.0f);
		tapandplayActor.setPosition(Gdx.graphics.getWidth() / 1.5f,
				Gdx.graphics.getHeight() / 50.0f);
		startActor.setScale(width / 500.0f);
		startActor.setPosition(width / 2 - startActor.getWidth() / 2, height
				/ 2 - startActor.getHeight() / 2);
		startActor.setZIndex(100);

		// Resize Font
		if (textStart != null) {
			float sizefont = (float) width
					/ (float) AbstractScreen.GAME_VIEWPORT_WIDTH;
			textStart.setScale(sizefont);
			textStart.setPosition(width / 2 - textStart.getWidth() / 2f, height);
			textStart.toFront();
			textStart
					.addAction(sequence(
							Actions.delay(1f),
							Actions.moveTo(width / 2 - textStart.getWidth()
									/ 2f, height / 2f, 1f,
									Interpolation.bounceOut),
							forever(sequence(Actions.delay(3f), Actions.moveBy(
									0f, 10f, 1f, Interpolation.bounceIn),
									Actions.moveBy(0f, -10f, 1f,
											Interpolation.bounceOut)))));

		}
		if (textGameOver != null) {
			float sizefont = (float) width
					/ (float) AbstractScreen.GAME_VIEWPORT_WIDTH;
			textGameOver.setScale(sizefont);
			textGameOver.setPosition(width / 7, height / 5);
			textGameOver.toFront();
		}
		if (textHighScore != null) {
			float sizefont = (float) width
					/ (float) AbstractScreen.GAME_VIEWPORT_WIDTH / 2f;
			textHighScore.setScale(sizefont);
			textHighScore.setPosition(10, Gdx.graphics.getHeight() - 100);

		}
		if (textScore != null) {
			float sizefont = (float) width
					/ (float) AbstractScreen.GAME_VIEWPORT_WIDTH;
			textScore.setScale(sizefont);
			textScore.setPosition(10, textHighScore.getY()-textHighScore.getHeight()*4);
		}
	}


	private void FruitsDansArbre() {
		FruitsDansArbre(nb_fruits,level);
	}
	private void FruitsDansArbre(int nb_fruits,int level) {

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
	 * @param bascule
	 *            the bascule to set
	 */
	public void setBascule(Bascule bascule) {
		this.bascule = bascule;
	}
}
