package com.python4d.fumper;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AndroidApplication {
    private  AdView adView; 
    private final static AdRequest adr=new AdRequest.Builder().build();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        //
        //admob
        //https://github.com/libgdx/libgdx/wiki/Admob-in-libgdx
        //
		View gameView = initializeForView(new Fumper(), false);
		RelativeLayout layout = new RelativeLayout(this);
	    layout.addView(gameView);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		AdView adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-1008481061910472/5992717317");
	    adView.setAdSize(AdSize.BANNER); 
	    //adView.setAdUnitId("ca-app-pub-1008481061910472/2701710119");
	    //adView.setAdSize(AdSize.BANNER); 
	    RelativeLayout.LayoutParams adParams = 
	            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
	                    RelativeLayout.LayoutParams.WRAP_CONTENT);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layout.addView(adView, adParams);
        adView.loadAd(adr );
        setContentView(layout);
	   
        
    }

    @Override
    public void onDestroy() {
    if (adView!=null)
    	adView.destroy();
    	super.onDestroy();
    }
}