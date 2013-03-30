package com.kh.beatbot.effect;


import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;

public class ADSR extends Effect {

	public static final String NAME = GlobalVars.mainActivity.getString(R.string.adsr);
	public static final int EFFECT_NUM = -1;
	public static final int NUM_PARAMS = 6;
	
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
	
	public static final ParamData[] paramsData = {
		new ParamData("ATTACK", true, false, ATTACK_MAX_S, LOG_SCALE, "s"),
		new ParamData("DECAY", true, false, DECAY_MAX_S, LOG_SCALE, "s"),
		new ParamData("SUSTAIN", false, false, ""),
		new ParamData("RELEASE", true, false, RELEASE_MAX_S, LOG_SCALE, "s"),
		new ParamData("START", false, false, ""),
		new ParamData("PEAK", false, false, "")
	};
	
	private int currParamId = ATTACK_ID;
	
	public ADSR(int trackNum) {
		super(trackNum);
	}
	
	public int getNum() {
		return EFFECT_NUM;
	}
	
	public String getName() {
		return NAME;
	}
	
	public int numParams() {
		return NUM_PARAMS;
	}
	
	public ParamData[] getParamsData() {
		return paramsData;
	}
	
	@Override
	protected void initParams() {
		super.initParams();
		position = -1; // native code understands that -1 == ADSR
		setAttack(0);
		setDecay(1);
		setSustain(1);
		setRelease(0);
		setStart(0);
		setPeak(1);
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
