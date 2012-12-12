package com.kh.beatbot.layout.page;

import android.content.Context;
import android.widget.LinearLayout;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Track;

public abstract class TrackPage extends LinearLayout {
	protected Track track;
	public static enum Type {SELECT, EDIT};
	
	private static Type[] pageOrder = {Type.SELECT, Type.EDIT};
	
	public static Type getPageType(int pageNum) {
		if (pageNum > 1) {
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
		this.track = track;
		trackUpdated();
	}
}
