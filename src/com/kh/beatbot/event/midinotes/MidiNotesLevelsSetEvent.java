package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.group.PageSelectGroup;

public class MidiNotesLevelsSetEvent extends MidiNotesGroupEvent {

	private Track track;

	public MidiNotesLevelsSetEvent(Track track) {
		this.track = track;
	}

	@Override
	public void updateUi() {
		super.updateUi();
		View.mainPage.pageSelectGroup
				.selectPage(PageSelectGroup.NOTE_LEVELS_PAGE_ID);
		if (track != null) {
			TrackManager.setTrack(track);
		}
	}
}
