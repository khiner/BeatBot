package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.Track;
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
		View.mainPage.pageSelectGroup.selectNoteLevelsPage();
		if (track != null) {
			track.select();
		}
	}
}
