package com.kh.beatbot.ui.view;


public class ViewPager extends TouchableView {
	private View currPage;

	public ViewPager(View view) {
		super(view);
		shouldDraw = false;
	}

	public void addPage(View page) {
		children.add(page);
		if (children.size() == 1)
			setPage(page);
	}

	public View getCurrPage() {
		return currPage;
	}

	public synchronized void setPage(View view) {
		if (!children.contains(view))
			return;
		currPage = view;
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
