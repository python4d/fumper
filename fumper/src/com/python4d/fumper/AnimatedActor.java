package com.python4d.fumper;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Redéfini la fonction act de la classe Actor avec une animation récupérer via
 * la Class {@link AnimationDrawable}
 * 
 * @author {@link <a href=
 *         "http://stackoverflow.com/questions/16059578/libgdx-is-there-an-actor-that-is-animated"
 *         >stackoverflow<a/>}
 */
public class AnimatedActor extends Image {
	private final AnimationDrawable drawable;

	public AnimatedActor(AnimationDrawable drawable) {
		super(drawable);
		this.drawable = drawable;
	}

	@Override
	public void act(float delta) {
		drawable.act(delta);
		super.act(delta);
	}
}