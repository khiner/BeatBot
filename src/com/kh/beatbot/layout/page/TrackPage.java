package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.View;

import com.kh.beatbot.global.Track;

public abstract class TrackPage {
	protected Context context;
	protected View layout;
	public static enum Type {SELECT, LEVELS, EDIT, EFFECTS};
	private static Type[] pageOrder = {Type.SELECT, Type.LEVELS, Type.EDIT, Type.EFFECTS};
	public static final int NUM_TRACK_PAGES = pageOrder.length;
	protected static Track track;
	
	TrackPage(Context context, View layout) {
		this.context = context;
		this.layout = layout;
	}
	
	public static Type getPageType(int pageNum) {
		if (pageNum >= pageOrder.length) {
			return Type.SELECT;
		}
		return pageOrder[pageNum];
	}
	
	protected abstract void update();
	protected abstract void setVisibilityCode(int code);
	public void setVisible(boolean visible) {
		int code = visible ? View.VISIBLE : View.INVISIBLE;
		setVisibilityCode(code);
	}
	
	public void setTrack(Track track) {
		TrackPage.track = track;
		update();
	}
}
