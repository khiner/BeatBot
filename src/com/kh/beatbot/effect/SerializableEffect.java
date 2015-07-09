package com.kh.beatbot.effect;

// Contains all the info needed to store/recreate an effect on save/load
public class SerializableEffect {
	public int position;
	public String name;
	public float[] levels;
	public boolean on;
	
	public SerializableEffect(Effect effect) {
		this.position = effect.getPosition();
		this.name = effect.getName();
		this.levels = effect.getLevels();
		this.on = effect.isOn();
	}
}
