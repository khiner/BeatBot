package com.kh.beatbot.layout.page;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.ViewGroup;

import com.kh.beatbot.global.Track;

public final class TrackPageFactory {
	private static Map<TrackPage.Type, TrackPage> instances = new HashMap<TrackPage.Type, TrackPage>();
	
	private TrackPageFactory() {}

	public static void setTrack(Track track) {
		for (TrackPage trackPage : instances.values()) {
			trackPage.setTrack(track);
		}
	}
	
	public static TrackPage getTrackPage(TrackPage.Type type) {
		return instances.get(type);
	}
	
	public static TrackPage createPage(Context context, ViewGroup parent, TrackPage.Type pageType) {
		if (!instances.containsKey(pageType)) {
			TrackPage newPage = createPageInstance(context, pageType);
			instances.put(pageType, newPage);
			parent.addView(newPage);
			parent.refreshDrawableState();
		}
		return instances.get(pageType);
	}
	
	private static TrackPage createPageInstance(Context context, TrackPage.Type pageType) {
		switch (pageType) {
		case EDIT:
			return new SampleEditPage(context);
		case SELECT:
			return new SampleSelectPage(context);
		case LEVELS:
			return new LevelsPage(context);
		}
		return null;
	}
}
