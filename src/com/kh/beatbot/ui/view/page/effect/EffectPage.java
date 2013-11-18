package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremelo;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.Seekbar2d;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.group.ViewPager;

public class EffectPage extends TouchableView {

	private ViewPager paramsPager;
	private Seekbar2d level2d;
	private ToggleButton toggleButton;

	private EffectParamsPage chorusPage, decimatePage, delayPage, filterPage, flangerPage, reverbPage, tremeloPage;

	public Seekbar2d getLevel2d() {
		return level2d;
	}

	public void loadEffect(Effect effect) {
		toggleButton.setChecked(effect.isOn());
		toggleButton.setText(effect.getName());
		paramsPager.setPage(effect.getNum());
		setLevel2dParams(effect.getXParam(), effect.getYParam());
		((EffectParamsPage) paramsPager.getCurrPage()).setEffect(effect);
	}

	@Override
	protected synchronized void loadIcons() {
		toggleButton.setIcon(new Icon(IconResources.ON_OFF));
		toggleButton.setBgIcon(new RoundedRectIcon(null,
				Colors.labelBgColorSet, Colors.labelStrokeColorSet));
	}

	@Override
	protected synchronized void createChildren() {
		paramsPager = new ViewPager();
		level2d = new Seekbar2d();

		chorusPage = new EffectParamsPage(new Chorus(null));
		decimatePage = new EffectParamsPage(new Decimate(null));
		delayPage = new DelayParamsPage(new Delay(null));
		filterPage = new FilterParamsPage(new Filter(null));
		flangerPage = new EffectParamsPage(new Flanger(null));
		reverbPage = new EffectParamsPage(new Reverb(null));
		tremeloPage = new EffectParamsPage(new Tremelo(null));

		paramsPager.addPage(chorusPage);
		paramsPager.addPage(decimatePage);
		paramsPager.addPage(delayPage);
		paramsPager.addPage(filterPage);
		paramsPager.addPage(flangerPage);
		paramsPager.addPage(reverbPage);
		paramsPager.addPage(tremeloPage);

		toggleButton = new ToggleButton();
		toggleButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((EffectParamsPage) paramsPager.getCurrPage()).getEffect()
						.setOn(toggleButton.isChecked());
			}
		});

		addChildren(toggleButton, level2d, paramsPager);
	}

	@Override
	public synchronized void layoutChildren() {
		toggleButton.layout(this, 5, 5, (width - height) - 10,
				(width - height) / 5);
		paramsPager.layout(this, 0, (width - height) / 5 + 5, width - height,
				height - (width - height) / 5 - 5);
		level2d.layout(this, width - height, 0, height, height);
	}
	
	public void setLevel2dParams(Param xParam, Param yParam) {
		level2d.setParams(xParam, yParam);
	}
}
