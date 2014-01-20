package com.kh.beatbot.ui.view.group;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class ViewPager extends TouchableView {
	private Map<Object, View> pageMap = new HashMap<Object, View>();
	private Object currPageId;

	public void addPage(Object key, View page) {
		pageMap.put(key, page);
		addChildren(page);
	}

	public int numPages() {
		return children.size();
	}

	public View getCurrPage() {
		return pageMap.get(currPageId);
	}

	public Object getCurrPageId() {
		return currPageId;
	}

	public void setPage(Object key) {
		if (pageMap.containsKey(key)) {
			currPageId = key;
		}
	}

	@Override
	public synchronized void layoutChildren() {
		for (View page : children) {
			page.layout(this, 0, 0, width, height);
		}
	}

	@Override
	public synchronized void drawAll() {
		getCurrPage().drawAll();
	}

	@Override
	protected synchronized View findChildAt(float x, float y) {
		return getCurrPage();
	}
}
