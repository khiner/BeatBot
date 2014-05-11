package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.ViewPager;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.Seekbar2d;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class EffectPage extends TouchableView {

	private ViewPager paramsPager;
	private Seekbar2d level2d;
	private ToggleButton toggleButton;

	private EffectParamsPage chorusPage, decimatePage, delayPage, filterPage, flangerPage,
			reverbPage, tremeloPage;

	public Seekbar2d getLevel2d() {
		return level2d;
	}

	public void loadEffect(Effect effect) {
		toggleButton.setChecked(effect.isOn());
		toggleButton.setText(effect.getName());
		paramsPager.setPage(effect.getName());
		setLevel2dParams(effect.getXParam(), effect.getYParam());
		((EffectParamsPage) paramsPager.getCurrPage()).setEffect(effect);
	}

	@Override
	protected synchronized void createChildren() {
		paramsPager = new ViewPager(renderGroup);
		level2d = new Seekbar2d(renderGroup);

		chorusPage = new EffectParamsPage(new Chorus(null));
		decimatePage = new EffectParamsPage(new Decimate(null));
		delayPage = new DelayParamsPage(new Delay(null));
		filterPage = new FilterParamsPage(new Filter(null));
		flangerPage = new EffectParamsPage(new Flanger(null));
		reverbPage = new EffectParamsPage(new Reverb(null));
		tremeloPage = new EffectParamsPage(new Tremolo(null));

		paramsPager.addPage(Chorus.NAME, chorusPage);
		paramsPager.addPage(Decimate.NAME, decimatePage);
		paramsPager.addPage(Delay.NAME, delayPage);
		paramsPager.addPage(Filter.NAME, filterPage);
		paramsPager.addPage(Flanger.NAME, flangerPage);
		paramsPager.addPage(Reverb.NAME, reverbPage);
		paramsPager.addPage(Tremolo.NAME, tremeloPage);

		toggleButton = new ToggleButton(renderGroup).oscillating();
		toggleButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((EffectParamsPage) paramsPager.getCurrPage()).getEffect().setOn(
						toggleButton.isChecked());
			}
		});

		toggleButton.setIcon(IconResourceSets.TOGGLE);

		addChildren(toggleButton, level2d, paramsPager);
	}

	@Override
	public synchronized void layoutChildren() {
		toggleButton.layout(this, 5, 5, (width - height) - 10, (width - height) / 5);
		paramsPager.layout(this, 0, (width - height) / 5 + 5, width - height, height
				- (width - height) / 5 - 5);
		level2d.layout(this, width - height, 0, height, height);
	}

	public void setLevel2dParams(Param xParam, Param yParam) {
		level2d.setParams(xParam, yParam);
	}
}
