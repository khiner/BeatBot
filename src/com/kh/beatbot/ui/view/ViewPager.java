package com.kh.beatbot.ui.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kh.beatbot.listener.PagerListener;

public class ViewPager extends TouchableView {
	private Map<Object, View> pageMap = new HashMap<Object, View>();
	private Object currPageId;
	private Set<PagerListener> listeners = new HashSet<PagerListener>();

	public void addPage(Object key, View page) {
		pageMap.put(key, page);
		addChildren(page);
	}

	public void addListener(PagerListener listener) {
		listeners.add(listener);
	}

	public int pageCount() {
		return children.size();
	}

	public View getCurrPage() {
		return pageMap.get(currPageId);
	}

	public Object getCurrPageId() {
		return currPageId;
	}

	public void setPage(Object key) {
		if (!pageMap.containsKey(key))
			return;
		currPageId = key;
		for (PagerListener listener : listeners) {
			listener.onPageChange(this, getCurrPage());
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
		View currPage = getCurrPage();
		if (null != currPage) {
			currPage.drawAll();
		}
	}

	@Override
	protected synchronized View findChildAt(float x, float y) {
		return getCurrPage();
	}
}
