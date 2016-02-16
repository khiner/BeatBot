package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.SwappingViewPager;

public interface PagerListener {
	public void onPageChange(SwappingViewPager pager, View prevPage, View newPage);
}
