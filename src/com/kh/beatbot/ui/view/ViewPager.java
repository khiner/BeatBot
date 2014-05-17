package com.kh.beatbot.ui.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kh.beatbot.listener.PagerListener;
import com.kh.beatbot.ui.shape.RenderGroup;

public class ViewPager extends TouchableView {
	private Map<Object, View> pageMap = new HashMap<Object, View>();
	private Object currPageId;
	private Set<PagerListener> listeners = new HashSet<PagerListener>();

	public ViewPager(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public void addPage(Object key, View page) {
		pageMap.put(key, page);
	}

	public void addListener(PagerListener listener) {
		listeners.add(listener);
	}

	public View getCurrPage() {
		return pageMap.get(currPageId);
	}

	public Object getCurrPageId() {
		return currPageId;
	}

	public void setPage(Object key) {
		if (null == key || key.equals(currPageId) || !pageMap.containsKey(key))
			return;

		removeChild(getCurrPage());
		currPageId = key;
		View currPage = getCurrPage();
		addChild(currPage);
		layoutChildren();
		for (PagerListener listener : listeners) {
			listener.onPageChange(this, currPage);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		View currPage = getCurrPage();
		if (null != currPage) {
			currPage.layout(this, 0, 0, width, height);
		}
	}
	
	@Override
	public synchronized void addChild(View child) {
		if (pageMap.containsValue(child)) {
			super.addChild(child);
		}
	}
}
