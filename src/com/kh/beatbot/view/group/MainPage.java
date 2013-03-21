package com.kh.beatbot.view.group;

import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class MainPage extends TouchableBBView {
	
	private MidiGroup midiGroup;
	private PageSelectGroup pageSelectGroup;
	
	public MainPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public MidiGroup getMidiGroup() {
		return midiGroup;
	}
	
	public PageSelectGroup getPageSelectGroup() {
		return pageSelectGroup;
	}
	
	@Override
	public void init() {
		// nothing to do.
	}

	@Override
	public void draw() {
		// parent group
	}

	@Override
	protected void createChildren() {
		midiGroup = new MidiGroup((TouchableSurfaceView)root);
		pageSelectGroup = new PageSelectGroup((TouchableSurfaceView)root);
		
		addChild(midiGroup);
		addChild(pageSelectGroup);
	}

	@Override
	public void layoutChildren() {
		midiGroup.layout(this, 0, 0, width, 3 * height / 4);
		pageSelectGroup.layout(this, 0, 3 * height / 4, width, 1 * height / 4);
	}

	@Override
	protected void loadIcons() {
		// parent group
	}
}
