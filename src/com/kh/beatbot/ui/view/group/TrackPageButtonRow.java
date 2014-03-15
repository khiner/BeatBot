package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.event.TrackDestroyEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackPageButtonRow extends PageButtonRow {
	private static final int BROWSE_PAGE_ID = 0, LEVELS_PAGE_ID = 1, NOTE_LEVELS_PAGE_ID = 2,
			EFFECTS_PAGE_ID = 3, EDIT_PAGE_ID = 4, ADSR_PAGE_ID = 5;

	private Button addTrackButton, deleteTrackButton;

	public TrackPageButtonRow(ShapeGroup shapeGroup, ViewPager pager) {
		super(shapeGroup, pager);
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

	@Override
	public synchronized void update() {
		ToggleButton browseButton = pageButtons[BROWSE_PAGE_ID];

		// update the browse pager instrument icon
		IconResourceSet instrumentIcon = TrackManager.currTrack.getIcon();

		browseButton.setResourceId(instrumentIcon);
		browseButton.setText(FileManager.formatSampleName(TrackManager.currTrack
				.getCurrSampleName()));
	}

	@Override
	protected synchronized void createChildren() {
		super.createChildren();

		addTrackButton = new Button(shapeGroup);
		deleteTrackButton = new Button(shapeGroup);

		addTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackCreateEvent().execute();
			}
		});

		deleteTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackDestroyEvent(TrackManager.currTrack).execute();
			}
		});

		deleteTrackButton.setIcon(IconResourceSets.DELETE_TRACK);
		addTrackButton.setIcon(IconResourceSets.LABEL_BASE);
		addTrackButton.setResourceId(IconResourceSets.ADD);
		getEditButton().setResourceId(IconResourceSets.SAMPLE);
		getNoteLevelsButton().setResourceId(IconResourceSets.NOTE_LEVELS);
		getLevelsButton().setResourceId(IconResourceSets.LEVELS);
		getAdsrButton().setResourceId(IconResourceSets.ADSR);
		getEffectsButton().setText("FX");

		addChildren(addTrackButton, deleteTrackButton);
	}

	@Override
	public synchronized void layoutChildren() {
		addTrackButton.layout(this, 0, 0, height, height);

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
		return 6;
	}
}
