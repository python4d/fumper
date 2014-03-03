package com.python4d.fumper;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;

public class Fumper extends Game {
	

    // a libgdx helper class that logs the current FPS each second
    private FPSLogger fpsLogger;

	public SplashScreen getSplashScreen() {
		return new SplashScreen(this);
	}
	public FumperPreferences FumperPrefs = new FumperPreferences();
	@Override
	public void create() {
		Gdx.app.log("Fumper", "Creating game");
		fpsLogger = new FPSLogger();
        Gdx.input.setCatchBackKey(true);
		setScreen(getSplashScreen());
	}

	@Override
	public void render() {
		super.render();

		// output the current FPS
		fpsLogger.log();
	}
	@Override
	public void dispose(){
		this.getScreen().dispose();
		super.dispose();
	}
	
	public void quit(Screen screen) {
		Gdx.app.exit();
	}
}
