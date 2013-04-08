package com.kh.beatbot.view.group;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.TouchableBBView;

public class BBViewPager extends TouchableBBView {
	private int currPageNum;
	
	public void addPage(BBView page) {
		addChild(page);
	}
	
	public int numPages() {
		return children.size();
	}
	
	public BBView getCurrPage() {
		return children.get(currPageNum);
	}
	
	public int getCurrPageNum() {
		return currPageNum;
	}
	
	public void setPage(int num) {
		currPageNum = num;
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
		getCurrPage().drawAll();
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
		return children.get(currPageNum);
	}
}
