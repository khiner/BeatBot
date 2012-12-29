package com.kh.beatbot.layout.page;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Track;

public final class TrackPageFactory {
	private static Map<TrackPage.Type, TrackPage> instances = new HashMap<TrackPage.Type, TrackPage>();

	private TrackPageFactory() {
	}

	public static void setTrack(Track track) {
		if (TrackPage.track == track)
			return;
		for (TrackPage trackPage : instances.values()) {
			trackPage.setTrack(track);
		}
	}

	public static void updatePages() {
		for (TrackPage trackPage : instances.values()) {
			trackPage.update();
		}
	}

	public static TrackPage getTrackPage(TrackPage.Type type) {
		return instances.get(type);
	}

	public static TrackPage createPage(Context context, Activity parent,
			TrackPage.Type pageType) {
		if (!instances.containsKey(pageType)) {
			TrackPage newPage = createPageInstance(context, parent, pageType);
			instances.put(pageType, newPage);
		}
		return instances.get(pageType);
	}

	private static TrackPage createPageInstance(Context context,
			Activity parent, TrackPage.Type pageType) {
		switch (pageType) {
		case SELECT:
			return new SampleSelectPage(context,
					parent.findViewById(R.id.trackPageSelect));
		case LEVELS:
			return new LevelsPage(context, parent.findViewById(R.id.levelsPage));
		case EDIT:
			return new SampleEditPage(context,
					parent.findViewById(R.id.sampleEditPage));
		case EFFECTS:
			return new EffectsPage(context,
					parent.findViewById(R.id.effectsPage));
		}
		return null;
	}
}
