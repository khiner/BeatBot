package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class ViewPager extends TouchableView {
	private int currPageNum;
	
	public void addPage(View page) {
		addChild(page);
	}
	
	public int numPages() {
		return children.size();
	}
	
	public View getCurrPage() {
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
		for (View page : children) {
			page.layout(this, 0, 0, width, height);
		}
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
	protected View findChildAt(float x, float y) {
		return children.get(currPageNum);
	}
}
