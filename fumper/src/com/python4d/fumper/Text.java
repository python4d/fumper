package com.python4d.fumper;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;


public class Text extends Actor {
    BitmapFont font;
    CharSequence str;  
    SpriteBatch batch;

    public Text(BitmapFont font,  CharSequence str){
        this.font=font;
        font.setColor(0.5f,0.4f,0,1);   //Brown is an underated Colour
        this.str=str;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
         font.drawMultiLine(batch, str, this.getX(), this.getY());
         //Also remember that an actor uses local coordinates for drawing within
         //itself!
    }

    
    @Override
    public Actor hit(float x, float y, boolean touchable) {
        // TODO Auto-generated method stub
        return null;
    }


	public void setText(String string) {
		
		this.str=string;
	}


}
