package com.python4d.fumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class FumperPreferences {
	 private String PREFS_NAME= new String("fumper"); ;
	 private String PREFS_HIGHSCORE= new String("high-score");
	 private String PREFS_HIGHSCORE_NAME = new String("high-score-name");;

	public FumperPreferences (){

    }
   
   public int getHighScore()
   {

	   //il faut ABSOLUMENT mettre dans une variable LOCAL l'objet Preference pour ANDROID...??
	   Preferences getPrefs= Gdx.app.getPreferences( PREFS_NAME );
       return getPrefs.getInteger( PREFS_HIGHSCORE,0 );
   }
   public String getHighScoreName()
   {

	   //il faut ABSOLUMENT mettre dans une variable LOCAL l'objet Preference pour ANDROID...??
	   Preferences getPrefs= Gdx.app.getPreferences( PREFS_NAME );
       return getPrefs.getString( PREFS_HIGHSCORE_NAME,"" );
   }
   public void setHighScore(int highscore,String name)
   {
	   //il faut ABSOLUMENT mettre dans une variable LOCAL l'objet Preference pour ANDROID...??
	    Preferences getPrefs= Gdx.app.getPreferences( PREFS_NAME );
	    getPrefs.putInteger ( PREFS_HIGHSCORE, highscore );
	    getPrefs.putString ( PREFS_HIGHSCORE_NAME, name );
        getPrefs.flush();
   }
   
}
