package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.SwappingViewPager;
import com.odang.beatbot.ui.view.View;

public interface PagerListener {
    public void onPageChange(SwappingViewPager pager, View prevPage, View newPage);
}
