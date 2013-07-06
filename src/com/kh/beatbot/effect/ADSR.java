package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;

public class ADSR extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity
			.getString(R.string.adsr);
	public static final int EFFECT_NUM = -1, NUM_PARAMS = 6;

	public static final int ATTACK_ID = 0, DECAY_ID = 1, SUSTAIN_ID = 2,
			RELEASE_ID = 3, START_ID = 4, PEAK_ID = 5;

	public static final int ATTACK_MAX_S = 20, DECAY_MAX_S = 30,
			RELEASE_MAX_S = 20;

	public static final int LOG_SCALE = 512;

	public static final ParamData[] paramsData = {
			new ParamData("ATTACK", true, false, 0, ATTACK_MAX_S, LOG_SCALE,
					"s"),
			new ParamData("DECAY", true, false, 0, DECAY_MAX_S, LOG_SCALE, "s"),
			new ParamData("SUSTAIN", false, false, ""),
			new ParamData("RELEASE", true, false, 0, RELEASE_MAX_S, LOG_SCALE,
					"s"), new ParamData("START", false, false, ""),
			new ParamData("PEAK", false, false, "") };

	private int currParamId = ATTACK_ID;

	public ADSR(BaseTrack track) {
		super(track);
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

	public int getCurrParamId() {
		return currParamId;
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
