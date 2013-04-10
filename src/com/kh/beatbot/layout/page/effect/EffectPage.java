package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.listener.Level2dListener;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.Seekbar2d;
import com.kh.beatbot.view.control.TextButton;
import com.kh.beatbot.view.group.BBViewPager;

public class EffectPage extends TouchableBBView {

	private BBViewPager paramsPager;
	private Seekbar2d level2d;
	private TextButton toggleButton;
	
	private ChorusParamsPage chorusPage;
	private DecimateParamsPage decimatePage;
	private DelayParamsPage delayPage;
	private FilterParamsPage filterPage;
	private FlangerParamsPage flangerPage;
	private ReverbParamsPage reverbPage;
	private TremeloParamsPage tremeloPage;
	
	public Seekbar2d getLevel2d() {
		return level2d;
	}
	
	public void loadEffect(Effect effect) {
		toggleButton.setChecked(effect.isOn());
		toggleButton.setText(effect.getName());
		paramsPager.setPage(effect.getNum());
		((EffectParamsPage)paramsPager.getCurrPage()).setEffect(effect);
	}
	
	@Override
	protected void loadIcons() {
		// parent
	}

	@Override
	public void init() {
		// parent
	}

	@Override
	public void draw() {
		// parent
	}
	
	@Override
	protected void createChildren() {
		paramsPager = new BBViewPager();
		level2d = new Seekbar2d();
		
		
		chorusPage = new ChorusParamsPage();
		decimatePage = new DecimateParamsPage();
		delayPage = new DelayParamsPage();
		filterPage = new FilterParamsPage();
		flangerPage = new FlangerParamsPage();
		reverbPage = new ReverbParamsPage();
		tremeloPage = new TremeloParamsPage();
		
		paramsPager.addPage(chorusPage);
		paramsPager.addPage(decimatePage);
		paramsPager.addPage(delayPage);
		paramsPager.addPage(filterPage);
		paramsPager.addPage(flangerPage);
		paramsPager.addPage(reverbPage);
		paramsPager.addPage(tremeloPage);
		
		toggleButton = new TextButton(null,
				Colors.labelBgColorSet, Colors.labelStrokeColorSet,
				R.drawable.off_icon, -1, R.drawable.on_icon);
		toggleButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				((EffectParamsPage)paramsPager.getCurrPage()).getEffect().setOn(toggleButton.isChecked());
			}
		});
		
		level2d.addLevelListener(new Level2dListener() {
			@Override
			public void onLevelChange(ControlViewBase levelListenable, float levelX,
					float levelY) {
				((EffectParamsPage)paramsPager.getCurrPage()).setXLevel(levelX);
				((EffectParamsPage)paramsPager.getCurrPage()).setYLevel(levelY);
			}
		});
		
		addChild(toggleButton);
		addChild(level2d);
		addChild(paramsPager);
	}

	@Override
	public void layoutChildren() {
		toggleButton.layout(this, 0, 0, (width - height), (width - height) / 5);
		paramsPager.layout(this, 0, (width - height) / 5, width - height, height - (width - height) / 5);
		level2d.layout(this, width - height, 0, height, height);
	}
}
