package com.python4d.fumper;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;


public class TextActor extends Actor {
    private BitmapFont font;
	CharSequence str;  
    SpriteBatch batch;

    public TextActor(BitmapFont font,  CharSequence str){
        this.font=font;
        setText(str.toString());
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
         font.drawMultiLine(batch, str, this.getX(), this.getY()+this.getHeight());
         //Also remember that an actor uses local coordinates for drawing within
         //itself!
    }



	@Override
	protected void sizeChanged() {
		super.sizeChanged();
	}


	@Override
	public void setScale(float scale) {
		super.setScale(scale);
		font.setScale(scale);
        this.setSize(font.getBounds(str).width, font.getBounds(str).height);
	}


	protected void setText(String string) {
		this.str=string;
        this.setSize(font.getBounds(str).width, font.getBounds(str).height);
	}

	/**
	 * Getter
	 * @return
	 */
    public BitmapFont getFont() {
		return font;
	}


}
