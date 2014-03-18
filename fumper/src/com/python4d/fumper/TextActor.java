package com.python4d.fumper;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class TextActor extends Actor {
	private BitmapFont font;
	CharSequence str;
	SpriteBatch batch;

	public TextActor(BitmapFont font, CharSequence str) {
		this.font = font;
		setText(str.toString());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		font.setColor(getColor().r, getColor().g, getColor().b, getColor().a);
		font.drawMultiLine(batch, str, this.getX(),
				this.getY() + this.getHeight());
	}

	@Override
	public void setScale(float scale) {
		// on ne veut pas que le stage diminue la taille de notre textactor déjà
		// défini par setBounds
		super.setScale(1);
		font.setScale(scale);
		this.setBounds(getX(), getY(), font.getBounds(str).width,
				font.getBounds(str).height);
	}

	@Override
	public void setScale(float scalex, float scaley) {
		// on ne veut pas que le stage diminue la taille de notre textactor déjà
		// défini par setBounds
		super.setScale(1);
		font.setScale(scalex, scaley);
		this.setBounds(getX(), getY(), font.getBounds(str).width,
				font.getBounds(str).height);
	}

	/**
	 * OVERRIDE pour l'utilisation de ScaleBy de la classe Actions On va gonfler
	 * la font en utilisant le centre du text Attention! on n'utilise pas
	 * l'origine de l'acteur !
	 */
	@Override
	public void scaleBy(float scaleX, float scaleY) {
		super.scaleBy(scaleX, scaleY);
		float w = font.getBounds(str).width;
		float h = font.getBounds(str).height;
		font.scale(scaleX);
		float W = font.getBounds(str).width;
		float H = font.getBounds(str).height;
		this.setBounds(getX() - (W - w) / 2f, getY() - (H - h) / 2f, W, H);
	}

	protected void setText(String string) {
		this.str = string;
		this.setBounds(getX(), getY(), font.getBounds(str).width,
				font.getBounds(str).height);
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public BitmapFont getFont() {
		return font;
	}
	public int getStrWidth(){
		return (int) font.getBounds(str).width;
	}

	public float getStrHeight() {
		return (int) font.getBounds(str).height;
	}

}
