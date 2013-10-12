package com.kh.beatbot.event;

import com.kh.beatbot.Track;
import com.kh.beatbot.ui.view.group.PageSelectGroup;
import com.kh.beatbot.ui.view.page.Page;

public class SetMidiNotesLevelsEvent extends MidiNotesGroupEvent {

	private Track track;

	public SetMidiNotesLevelsEvent(Track track) {
		this.track = track;
	}

	@Override
	protected void updateUi() {
		super.updateUi();
		Page.mainPage.pageSelectGroup
				.selectPage(PageSelectGroup.NOTE_LEVELS_PAGE_ID);
		if (track != null) {
			track.select();
		}
	}
}
