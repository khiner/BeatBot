package com.kh.beatbot.layout.page;

import android.content.Context;
import android.widget.LinearLayout;

import com.kh.beatbot.global.Track;

public abstract class TrackPage extends LinearLayout {
	public static final int NUM_TRACK_PAGES = 3;
	protected static Track track;
	
	public static enum Type {SELECT, EDIT, LEVELS};
	
	private static Type[] pageOrder = {Type.SELECT, Type.EDIT, Type.LEVELS};
	
	public static Type getPageType(int pageNum) {
		if (pageNum >= pageOrder.length) {
			return Type.SELECT;
		}
		return pageOrder[pageNum];
	}
	
	public TrackPage(Context context) {
		super(context);
		inflate(context);
	}
	
	protected abstract void inflate(Context context);
	protected abstract void trackUpdated();
	
	public void setTrack(Track track) {
		TrackPage.track = track;
		trackUpdated();
	}
}
