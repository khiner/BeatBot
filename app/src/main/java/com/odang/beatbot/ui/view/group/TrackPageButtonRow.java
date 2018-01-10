package com.odang.beatbot.ui.view.group;

import com.odang.beatbot.event.track.TrackDestroyEvent;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;

public class TrackPageButtonRow extends PageButtonRow {
	private static final int BROWSE_PAGE_ID = 0, LEVELS_PAGE_ID = 1, NOTE_LEVELS_PAGE_ID = 2,
			EFFECTS_PAGE_ID = 3, EDIT_PAGE_ID = 4, ADSR_PAGE_ID = 5, RECORD_PAGE_ID = 6;

	private Button deleteTrackButton;

	public TrackPageButtonRow(View view) {
		super(view);
	}

	public ToggleButton getBrowseButton() {
		return pageButtons[BROWSE_PAGE_ID];
	}

	public ToggleButton getLevelsButton() {
		return pageButtons[LEVELS_PAGE_ID];
	}

	public ToggleButton getEffectsButton() {
		return pageButtons[EFFECTS_PAGE_ID];
	}

	public ToggleButton getEditButton() {
		return pageButtons[EDIT_PAGE_ID];
	}

	public ToggleButton getAdsrButton() {
		return pageButtons[ADSR_PAGE_ID];
	}

	public ToggleButton getNoteLevelsButton() {
		return pageButtons[NOTE_LEVELS_PAGE_ID];
	}

	public ToggleButton getRecordButton() {
		return pageButtons[RECORD_PAGE_ID];
	}

	public void update() {
		ToggleButton browseButton = pageButtons[BROWSE_PAGE_ID];

		// update the browse pager instrument icon
		final Track track = (Track) context.getTrackManager().getCurrTrack();
		IconResourceSet instrumentIcon = track.getIcon();

		browseButton.setResourceId(instrumentIcon);
		browseButton.setText(track.getFormattedName());
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		deleteTrackButton = new Button(this).withRoundedRect().withIcon(
				IconResourceSets.DELETE_TRACK);

		deleteTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackDestroyEvent(context.getTrackManager().getCurrTrack().getId()).execute();
			}
		});

		getEditButton().setResourceId(IconResourceSets.SAMPLE);
		getNoteLevelsButton().setResourceId(IconResourceSets.NOTE_LEVELS);
		getLevelsButton().setResourceId(IconResourceSets.LEVELS);
		getAdsrButton().setResourceId(IconResourceSets.ADSR);
		getRecordButton().setResourceId(IconResourceSets.MICROPHONE);
		getEffectsButton().setText("FX");
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();

		float labelWidth = (width - 4 * height) / 4;
		float x = addTrackButton.width;

		for (int i = 0; i < pageButtons.length; i++) {
			float w = i != BROWSE_PAGE_ID ? height : labelWidth;
			pageButtons[i].layout(this, x, 0, w, height);
			x += pageButtons[i].width;
		}

		deleteTrackButton.layout(this, width - height, 0, height, height);
	}

	@Override
	protected int getNumPages() {
		return 7;
	}
}
