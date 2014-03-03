package com.python4d.fumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class AbstractScreen implements Screen {

	protected boolean debug=false;
	
	// the fixed viewport dimensions (ratio: 1.6)
	public static int GAME_VIEWPORT_WIDTH = 400,
			GAME_VIEWPORT_HEIGHT = 240;
	public static float WORLD_TO_BOX = GAME_VIEWPORT_HEIGHT/20000.0f;//0.01f;
	public static float BOX_TO_WORLD = 1.0f/WORLD_TO_BOX;//100f;
	protected int NB_CYCLE = (int) (10000.0f / 60.0f);
	protected int wait = NB_CYCLE;
	private float fdt =0;

	protected int wait1s = NB_CYCLE/10;
	protected Fumper game;
	
	protected BitmapFont font_berlin;
	protected SpriteBatch batch;
	public SpriteBatch getBatch() {
		return batch;
	}

	protected  Stage stage;
	protected TypeOfObject[] tofTable = TypeOfObject.values();
	private TextureAtlas atlas;

	protected World worldbox;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera camera;
	private Matrix4 MatZoom;
	private BodyEditorLoader bodyloader = new BodyEditorLoader(
			Gdx.files.internal("texture/body/fumperbody.bd"));

	protected Toast myToast=new Toast(1, 0);
	
	//definition des objets
	public enum TypeOfObject {

		pomme_verte(1,"fruits/pomme_verte","pomme_verte"),
		citron(1,"fruits/citron","citron"),
		cerises(1,"fruits/cerises","cerises"),
		orange(1,"fruits/orange","orange"),
		pomme(1,"fruits/pomme-dessin", "pomme-dessin"),
		pomme2(1,"fruits/pomme-dessin2", "pomme-dessin2"),
		pomme3(1,"fruits/pomme-photo", "pomme-photo"),
		SPLASH(1,"splash-screen/splash-image", "splash-image"),
		buche(1,"bascule/buche_bois","buche"),
		planche(1,"bascule/planche","planche"),
		panier(1,"objets/panier","panier"),
		panier_front(1,"objets/panier-front",""),
		tap_finger(2,"autres/tap_finger1","autres/tap_finger2"),
		tapandplay(1,"autres/tapandplay","");
		
		String[] st;
		private int nb_images;
		private static int NB_FRUITS=7;

		TypeOfObject(int nb_images,String ... st) {
			this.st = st;
			this.nb_images=nb_images;

		}

		public String getNameImage() {
			return st[0];
		}
		public String[] getAllImages() {
			return st;
		}
		public int getNbImages(){
			return nb_images;
		}
		
		public String getNameBody() {
			return st[nb_images];
		}

		/**
		 * @return le nb de fruits possible dans la banque d'image
		 */
		public static int getNbFruits() {
			return NB_FRUITS;
		}

	}
	//definition des sons
	public enum FumperSound
	{	//  Name in ASSETS and use it as music or sound(false)
	    INTRO( "sons/intro.mp3",true);

	    private String fileName;
	    private boolean music;
	    private Sound snd=null;
		private Music msc=null;
	    private long id;
		
	    private FumperSound(
	        String fileName, boolean music)
	    {
	        this.fileName = fileName;
	        this.music=music;
	    }

	    public String getFileName()
	    {
	        return fileName;
	    }
	    
	    public void load(){
	    	if (music)
	    		msc=Gdx.audio.newMusic(Gdx.files.internal(fileName));
	    	else
	    		snd=Gdx.audio.newSound(Gdx.files.internal(fileName));

	    }
	    public void play(float volume,boolean looping){
	    	if (music){
	    		if (msc==null)
	    			load();
	    		msc.setLooping(looping);
	    		msc.setVolume(volume);
	    		msc.play();
	    	}
	    	else
	    	{
		    	if (snd==null)
		    		load();
		    	snd.setLooping(id, looping);
		    	snd.loop(volume);
		    	id=snd.play(volume);
	    	}
	    }
	    public void dispose(){
	    	if (music)
		    	msc.dispose();
	    	else
	    		snd.dispose();
	    }
	}
	public BodyEditorLoader getBodyLoader() {
		return bodyloader;
	}

	public AbstractScreen(Fumper game) {
		this.game = game;

		this.font_berlin = new BitmapFont(Gdx.files.internal("font/berlin.fnt"),
		         Gdx.files.internal("font/berlin.png"), false);
		this.batch = new SpriteBatch();
		this.stage = new Stage(0, 0, true);
	}

	protected String getName() {
		return getClass().getSimpleName();
	}

	public World getWorldbox() {
		if (worldbox == null) {
			worldbox = new World(new Vector2(0, -9.8f), true);
			 debugRenderer = new Box2DDebugRenderer(false, false, false,
			 false,false, false);
			 debugRenderer = new Box2DDebugRenderer(true, true, true, true,
			 		true, true);
		}
		return worldbox;
	}

	public TextureAtlas getAtlas() {
		if (atlas == null) {
			atlas = new TextureAtlas(Gdx.files.internal("texture/Fumper.atlas"));
		}
		return atlas;
	}

	@Override
	public void render(float delta) {
		// the following code clears the screen with the given RGB color (black)
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	
		
		// update and draw the stage actors
		stage.act(delta);
		 stage.draw();

		if (myToast!=null)
			myToast.toaster();
		//calcul du monde Box
		if (worldbox != null) {
			//filter the step world?
			//http://giderosmobile.com/forum/discussion/383/box-2d-worldstep-fixed-time-steps/p1
			//http://bitsquid.blogspot.fr/2010/10/time-step-smoothing.html
			//fdt = Gdx.graphics.getDeltaTime() * 0.4f + fdt * (1 -  0.4f);
			//worldbox.step(fdt, 6, 3);
			worldbox.step(1.0f / 60.0f, 1, 1);
			MatZoom=camera.combined.cpy();
			if (debug==true)
				debugRenderer.render(this.worldbox, MatZoom.scl(BOX_TO_WORLD));
		}
	}

	@Override
	public void resize(int width, int height) {
		Gdx.app.log("Fumper/AbstractScreen/resize/", "Resizing screen: " + getName() + " to: "
				+ width + " x " + height);

		// resize the stage
		stage.setViewport(width, height, true);
		
		// Le monde Box2D ne doit pas changer
		WORLD_TO_BOX = GAME_VIEWPORT_HEIGHT/20000.0f*GAME_VIEWPORT_WIDTH/(float)width;
		BOX_TO_WORLD=1.0f/WORLD_TO_BOX;
		
		MatZoom = new Matrix4();			
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		camera.setToOrtho(false,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
	}

	@Override
	public void show() {
		Gdx.app.log("Fumper/show/", "Showing screen: " + getName());

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		Gdx.app.log("Fumper/abstracScreen/dispose", "Disposing screen: " + getName());

		// dispose the collaborators
		if (atlas!=null) atlas.dispose();
		if (worldbox!=null) worldbox.dispose();
		if (stage!=null) stage.dispose();
		if (font_berlin!=null) font_berlin.dispose();
		if (batch!=null) batch.dispose();
		

	}

}
