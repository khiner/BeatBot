package com.kh.beatbot.ui.view;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.ui.shape.RenderGroup;


public class ViewPager extends TouchableView {
	private Map<Object, View> pageMap = new HashMap<Object, View>();
	private Object currPageId;

	public ViewPager(RenderGroup renderGroup) {
		super(renderGroup);
	}

	public void addPage(Object key, View page) {
		pageMap.put(key, page);
	}

	public int pageCount() {
		return pageMap.size();
	}

	public View getCurrPage() {
		return pageMap.get(currPageId);
	}

	public Object getCurrPageId() {
		return currPageId;
	}

	public void setPage(Object key) {
		View currPage = getCurrPage();
		removeChild(currPage);
		View nextPage = pageMap.get(key);
		if (null != nextPage) {
			currPageId = key;
			addChild(nextPage);
		}
		layoutChildren();
	}

	@Override
	public synchronized void layoutChildren() {
		View currPage = getCurrPage();
		if (null != currPage) {
			currPage.layout(this, 0, 0, width, height);
		}
	}

	@Override
	public synchronized void drawAll() {
		View currPage = getCurrPage();
		if (null != currPage) {
			currPage.drawAll();
		}
	}
}
