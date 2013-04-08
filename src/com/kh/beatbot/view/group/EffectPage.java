package com.kh.beatbot.view.group;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.layout.page.effect.ChorusParamsPage;
import com.kh.beatbot.layout.page.effect.DecimateParamsPage;
import com.kh.beatbot.layout.page.effect.DelayParamsPage;
import com.kh.beatbot.layout.page.effect.EffectParamsPage;
import com.kh.beatbot.layout.page.effect.FilterParamsPage;
import com.kh.beatbot.layout.page.effect.FlangerParamsPage;
import com.kh.beatbot.layout.page.effect.ReverbParamsPage;
import com.kh.beatbot.layout.page.effect.TremeloParamsPage;
import com.kh.beatbot.listener.Level2dListener;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.Seekbar2d;

public class EffectPage extends TouchableBBView {

	private BBViewPager paramsPager;
	private Seekbar2d level2d;
	
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
		
		level2d.addLevelListener(new Level2dListener() {
			@Override
			public void onLevelChange(ControlViewBase levelListenable, float levelX,
					float levelY) {
				((EffectParamsPage)paramsPager.getCurrPage()).setXLevel(levelX);
				((EffectParamsPage)paramsPager.getCurrPage()).setYLevel(levelY);
			}
		});
		
		addChild(level2d);
		addChild(paramsPager);
	}

	@Override
	public void layoutChildren() {
		paramsPager.layout(this, 0, 0, width - height, height);
		level2d.layout(this, width - height, 0, height, height);
	}
}
