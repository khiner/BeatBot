package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Crush;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.ViewPager;
import com.kh.beatbot.ui.view.control.Seekbar2d;

public class EffectPage extends TouchableView {
	private ViewPager paramsPager;
	private Seekbar2d level2d;

	private EffectParamsPage chorusPage, decimatePage, delayPage, filterPage, flangerPage,
			reverbPage, tremeloPage;

	public EffectPage(View view) {
		super(view);
	}

	public Seekbar2d getLevel2d() {
		return level2d;
	}

	public Effect getEffect() {
		return ((EffectParamsPage) paramsPager.getCurrPage()).getEffect();
	}

	public void setEffect(Effect effect) {
		paramsPager.setPage(effect.getName());
		setLevel2dParams(effect.getXParam(), effect.getYParam());
		((EffectParamsPage) paramsPager.getCurrPage()).withEffect(effect);
	}

	@Override
	protected synchronized void createChildren() {
		paramsPager = new ViewPager(this);
		level2d = new Seekbar2d(this);

		chorusPage = new EffectParamsPage(paramsPager).withEffect(new Chorus());
		decimatePage = new EffectParamsPage(paramsPager).withEffect(new Crush());
		delayPage = new DelayParamsPage(paramsPager).withEffect(new Delay());
		filterPage = new FilterParamsPage(paramsPager).withEffect(new Filter());
		flangerPage = new EffectParamsPage(paramsPager).withEffect(new Flanger());
		reverbPage = new EffectParamsPage(paramsPager).withEffect(new Reverb());
		tremeloPage = new EffectParamsPage(paramsPager).withEffect(new Tremolo());

		paramsPager.addPage(Chorus.NAME, chorusPage);
		paramsPager.addPage(Crush.NAME, decimatePage);
		paramsPager.addPage(Delay.NAME, delayPage);
		paramsPager.addPage(Filter.NAME, filterPage);
		paramsPager.addPage(Flanger.NAME, flangerPage);
		paramsPager.addPage(Reverb.NAME, reverbPage);
		paramsPager.addPage(Tremolo.NAME, tremeloPage);
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
}
