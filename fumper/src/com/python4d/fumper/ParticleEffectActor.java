package com.python4d.fumper;

import java.util.Hashtable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ParticleEffectActor extends Actor {
	ParticleEffect effect;
	Actor actor;
	Hashtable<String, Float> scaling = new Hashtable<String, Float>();

	public ParticleEffectActor(ParticleEffect effect,Actor actor) {
		this.effect = effect;
		this.actor = actor;
	    scaling.put("ScaleHighMax", effect.getEmitters().get(0).getScale().getHighMax()); 
	    scaling.put("ScaleLowMax",  effect.getEmitters().get(0).getScale().getLowMax());
	    scaling.put("VelocityHighMax",effect.getEmitters().get(0).getVelocity().getHighMax());
	    scaling.put("VelocityLowMax", effect.getEmitters().get(0).getVelocity().getLowMax());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		effect.draw(batch); 
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		effect.setPosition(actor.getX()+actor.getWidth()*actor.getScaleX()/5f, actor.getY()+actor.getHeight()*actor.getScaleY()/2.8f);
//		for (ParticleEmitter emitter : effect.getEmitters()) { //get the list of emitters - things that emit particles
			effect.getEmitters().get(0).getAngle().setLow(-actor.getRotation()+45,actor.getRotation()+45); //low is the minimum rotation
			effect.getEmitters().get(0).getAngle().setHigh(-actor.getRotation()+45,actor.getRotation()+45); //high is the max rotation		
//         }
		effect.update(delta); // update it
	}
	


	@Override
	public void setScale(float scale) {
		super.setScale(scale);
		for (ParticleEmitter emitter : effect.getEmitters()) { 
			emitter.getScale().setHigh(scaling.get("ScaleHighMax") * scale);
			emitter.getScale().setLow(scaling.get("ScaleLowMax") * scale);
			emitter.getVelocity().setHigh(scaling.get("VelocityHighMax") * scale);
			emitter.getVelocity().setLow(scaling.get("VelocityLowMax") * scale);
			
         }

	}

	public void start(){
		effect.start();
	}
	

	public ParticleEffect getEffect() {
		return effect;
	}
}
