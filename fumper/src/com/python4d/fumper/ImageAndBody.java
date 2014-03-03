package com.python4d.fumper;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

public abstract class ImageAndBody extends Image {

	protected AbstractScreen screen;

	public ImageAndBody(AbstractScreen screen) {
		super();
		this.screen = screen;
	}
	

}
