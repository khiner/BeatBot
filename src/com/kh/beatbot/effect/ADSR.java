package com.kh.beatbot.effect;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;

public class ADSR extends Effect {

	public static final String NAME = BeatBotActivity.mainActivity.getString(R.string.adsr);
	public static final int EFFECT_NUM = -1, NUM_PARAMS = 6;

	public static final int ATTACK_ID = 0, DECAY_ID = 1, SUSTAIN_ID = 2, RELEASE_ID = 3,
			START_ID = 4, PEAK_ID = 5, ATTACK_MAX_S = 20, DECAY_MAX_S = 30, RELEASE_MAX_S = 20,
			LOG_SCALE = 512;

	private int currParamId = ATTACK_ID;

	public ADSR(BaseTrack track) {
		super(track);
	}

	public void update() {
		setAttack(getAttack());
		setDecay(getDecay());
		setSustain(getSustain());
		setRelease(getRelease());
		setStart(getStart());
		setPeak(getPeak());
	}

	public int getNum() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}

	@Override
	protected void initParams() {
		params.add(new Param(0, "Attack").scale(ATTACK_MAX_S).withUnits("s").logScale(LOG_SCALE)
				.withLevel(0));
		params.add(new Param(1, "Decay").scale(DECAY_MAX_S).withUnits("s").logScale(LOG_SCALE)
				.withLevel(1));
		params.add(new Param(2, "Sustain").withLevel(1));
		params.add(new Param(3, "Release").scale(RELEASE_MAX_S).withUnits("s").logScale(LOG_SCALE)
				.withLevel(0));
		params.add(new Param(4, "Start").withLevel(0));
		params.add(new Param(5, "Peak").withLevel(1));
		position = -1; // native code understands that -1 == ADSR
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
		getParam(currParamId).setLevel(level);
	}

	public void setAttack(float attack) {
		getParam(ATTACK_ID).setLevel(attack);
	}

	public void setDecay(float decay) {
		getParam(DECAY_ID).setLevel(decay);
	}

	public void setSustain(float sustain) {
		getParam(SUSTAIN_ID).setLevel(sustain);
	}

	public void setRelease(float release) {
		getParam(RELEASE_ID).setLevel(release);
	}

	public void setStart(float start) {
		getParam(START_ID).setLevel(start);
	}

	public void setPeak(float peak) {
		getParam(PEAK_ID).setLevel(peak);
	}
}
