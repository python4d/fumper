package com.python4d.fumper;


import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class Main {
	public static void main(String[] args) {

        Settings settings = new Settings();
        settings.maxWidth = 1024;
        settings.maxHeight = 1024;
        TexturePacker.processIfModified(settings, "./images", "../fumper-android/assets/texture", "Fumper.atlas");

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "fumper";
		cfg.useGL20 = true;
		cfg.width = 320;
		cfg.height = 480;

		
		new LwjglApplication(new Fumper(), cfg);
	}
}
