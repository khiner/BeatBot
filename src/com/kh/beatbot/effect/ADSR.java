package com.kh.beatbot.effect;

public class ADSR extends Effect {

	public static final int ATTACK_ID = 0;
	public static final int DECAY_ID = 1;
	public static final int SUSTAIN_ID = 2;
	public static final int RELEASE_ID = 3;
	public static final int START_ID = 4;
	public static final int PEAK_ID = 5;

	public static final int ATTACK_MAX_S = 20; // 20s
	public static final int DECAY_MAX_S = 30; // 30s
	public static final int RELEASE_MAX_S = 20; // 30s
	
	public static final int LOG_SCALE = 512;
	
	private int currParamId = ATTACK_ID;
	
	public ADSR(String name, int trackNum) {
		super(name, trackNum);
	}
	
	@Override
	protected void initParams() {
		position = -1; // -1 native code understands that -1 == ADSR
		params.add(new Param("ATTACK", true, false, ATTACK_MAX_S, LOG_SCALE, "s"));
		params.add(new Param("DECAY", true, false, DECAY_MAX_S, LOG_SCALE, "s"));
		params.add(new Param("SUSTAIN", false, false, ""));
		params.add(new Param("RELEASE", true, false, RELEASE_MAX_S, LOG_SCALE, "s"));
		params.add(new Param("START", false, false, ""));
		params.add(new Param("PEAK", false, false, ""));
		setAttack(0);
		setDecay(1);
		setSustain(1);
		setRelease(0);
		setStart(0);
		setPeak(1);
	}

	@Override
	public int getOnDrawableId() {
		return 0;
	}

	@Override
	public int getOffDrawableId() {
		return 0;
	}
	
	public void setCurrParam(int paramId) {
		currParamId = paramId;
	}
	
	public Param getCurrParam() {
		return getParam(currParamId);
	}
	
	public float getAttack() {
		return params.get(ATTACK_ID).viewLevel;
	}
	
	public float getDecay() {
		return params.get(DECAY_ID).viewLevel;
	}
	
	public float getSustain() {
		return params.get(SUSTAIN_ID).viewLevel;
	}
	
	public float getRelease() {
		return params.get(RELEASE_ID).viewLevel;
	}
	
	public float getStart() {
		return params.get(START_ID).viewLevel;
	}
	
	public float getPeak() {
		return params.get(PEAK_ID).viewLevel;
	}
	
	public void setCurrParamLevel(float level) {
		setParamLevel(currParamId, level);
	}
	
	public void setAttack(float attack) {
		setParamLevel(ATTACK_ID, attack);
	}
	
	public void setDecay(float decay) {
		setParamLevel(DECAY_ID, decay);
	}

	public void setSustain(float sustain) {
		setParamLevel(SUSTAIN_ID, sustain);
	}
	
	public void setRelease(float release) {
		setParamLevel(RELEASE_ID, release);
	}
	
	public void setStart(float start) {
		setParamLevel(START_ID, start);
	}
	
	public void setPeak(float peak) {
		setParamLevel(PEAK_ID, peak);
	}
}