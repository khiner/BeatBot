package com.kh.beatbot.ui.view;

import java.util.HashMap;
import java.util.Map;

public class ViewFlipper extends TouchableView {
	private Map<Object, View> pageMap = new HashMap<Object, View>();
	private View currPage;

	public ViewFlipper(View view) {
		super(view);
		shouldDraw = false;
	}

	public void addPage(Object key, View page) {
		pageMap.put(key, page);
		children.add(page);
	}

	public View getCurrPage() {
		return currPage;
	}

	public synchronized void setPage(Object key) {
		if (null == key || !pageMap.containsKey(key))
			return;
		currPage = pageMap.get(key);
	}

	@Override
	public synchronized void layoutChildren() {
		for (View child : children) {
			child.layout(this, 0, 0, width, height);
		}
	}
	
	@Override
	public synchronized void drawChildren() {
		if (null != currPage) {
			currPage.drawAll();
		}
	}
	
	@Override
	public synchronized View findChildAt(float x, float y) {
		return currPage;
	}
}
