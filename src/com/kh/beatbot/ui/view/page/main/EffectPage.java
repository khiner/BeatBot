package com.kh.beatbot.ui.view.page.main;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Crush;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.event.effect.EffectParamsChangeEvent;
import com.kh.beatbot.listener.MultiViewTouchTracker;
import com.kh.beatbot.listener.TouchableViewsListener;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.SwappingViewPager;
import com.kh.beatbot.ui.view.control.Seekbar2d;
import com.kh.beatbot.ui.view.group.effect.DelayParamsGroup;
import com.kh.beatbot.ui.view.group.effect.EffectParamsGroup;
import com.kh.beatbot.ui.view.group.effect.FilterParamsGroup;

public class EffectPage extends TouchableView implements TouchableViewsListener {
	private SwappingViewPager paramsPager;
	private Seekbar2d level2d;

	private EffectParamsGroup chorusGroup, decimateGroup, delayGroup, filterGroup, flangerGroup,
			reverbGroup, tremeloGroup;

	public EffectPage(View view) {
		super(view);
	}

	public Seekbar2d getLevel2d() {
		return level2d;
	}

	public Effect getEffect() {
		return ((EffectParamsGroup) paramsPager.getCurrPage()).getEffect();
	}

	public void setEffect(Effect effect) {
		paramsPager.setPage(effect.getName());
		setLevel2dParams(effect.getXParam(), effect.getYParam());
		((EffectParamsGroup) paramsPager.getCurrPage()).withEffect(effect);
	}

	@Override
	protected synchronized void createChildren() {
		paramsPager = new SwappingViewPager(this);
		level2d = new Seekbar2d(this);

		chorusGroup = new EffectParamsGroup(paramsPager).withEffect(new Chorus());
		decimateGroup = new EffectParamsGroup(paramsPager).withEffect(new Crush());
		delayGroup = new DelayParamsGroup(paramsPager).withEffect(new Delay());
		filterGroup = new FilterParamsGroup(paramsPager).withEffect(new Filter());
		flangerGroup = new EffectParamsGroup(paramsPager).withEffect(new Flanger());
		reverbGroup = new EffectParamsGroup(paramsPager).withEffect(new Reverb());
		tremeloGroup = new EffectParamsGroup(paramsPager).withEffect(new Tremolo());

		paramsPager.addPage(Chorus.NAME, chorusGroup);
		paramsPager.addPage(Crush.NAME, decimateGroup);
		paramsPager.addPage(Delay.NAME, delayGroup);
		paramsPager.addPage(Filter.NAME, filterGroup);
		paramsPager.addPage(Flanger.NAME, flangerGroup);
		paramsPager.addPage(Reverb.NAME, reverbGroup);
		paramsPager.addPage(Tremolo.NAME, tremeloGroup);

		new MultiViewTouchTracker(this).monitorViews(level2d, paramsPager);
	}

	@Override
	public synchronized void layoutChildren() {
		float paramsPageWidth = width - height - BG_OFFSET * 2;
		paramsPager.layout(this, BG_OFFSET, BG_OFFSET, paramsPageWidth, height - BG_OFFSET * 2);
		level2d.layout(this, paramsPageWidth + BG_OFFSET * 4, BG_OFFSET * 2,
				height - BG_OFFSET * 4, height - BG_OFFSET * 4);
	}

	public void setLevel2dParams(Param xParam, Param yParam) {
		level2d.setParams(xParam, yParam);
	}

	private EffectParamsChangeEvent effectParamsChangeEvent = null;

	@Override
	public void onFirstPress() {
		Effect effect = getEffect();
		effectParamsChangeEvent = new EffectParamsChangeEvent(effect.getTrackId(),
				effect.getPosition());
		effectParamsChangeEvent.begin();
	}

	@Override
	public void onLastRelease() {
		effectParamsChangeEvent.end();
	}
}
