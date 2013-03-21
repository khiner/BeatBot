package com.kh.beatbot.view.group;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class BBViewPager extends TouchableBBView {
	BBView currPage;

	public BBViewPager(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void addPage(BBView page) {
		addChild(page);
	}
	
	public void setPage(int num) {
		currPage = children.get(num);
	}
	
	@Override
	public void layoutChildren() {
		for (BBView page : children)
			page.layout(this, 0, 0, width, height);
	}

	@Override
	public void draw() {
		// noop
	}

	@Override
	public void drawAll() {
		currPage.drawAll();
	}
	
	@Override
	protected void createChildren() {
		
	}

	@Override
	protected void loadIcons() {
		// Parent
	}

	@Override
	public void init() {
		// noop 
	}
	
	@Override
	protected BBView findChildAt(float x, float y) {
		return currPage;
	}
}
