package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.View;

public abstract class TrackPage {
	protected Context context;
	protected View layout;

	public static enum Type {
		LEVELS, EDIT, EFFECTS
	};

	private static Type[] pageOrder = { Type.LEVELS, Type.EDIT, Type.EFFECTS };
	public static final int NUM_TRACK_PAGES = pageOrder.length;

	TrackPage(Context context, View layout) {
		this.context = context;
		this.layout = layout;
	}

	public static Type getPageType(int pageNum) {
		return pageOrder[pageNum];
	}

	protected abstract void update();

	protected abstract void setVisibilityCode(int code);

	public void setVisible(boolean visible) {
		int code = visible ? View.VISIBLE : View.INVISIBLE;
		setVisibilityCode(code);
	}
}
