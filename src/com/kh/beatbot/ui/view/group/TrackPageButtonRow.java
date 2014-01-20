package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.event.TrackDestroyEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackPageButtonRow extends PageButtonRow {
	private static final int BROWSE_PAGE_ID = 0, LEVELS_FX_PAGE_ID = 1,
			EDIT_PAGE_ID = 2, ADSR_PAGE_ID = 3, NOTE_LEVELS_PAGE_ID = 4;

	private ImageButton addTrackButton, deleteTrackButton;

	public TrackPageButtonRow(ShapeGroup shapeGroup, ViewPager pager) {
		super(shapeGroup, pager);
	}

	public ToggleButton getBrowseButton() {
		return pageButtons[BROWSE_PAGE_ID];
	}

	public ToggleButton getLevelsFxButton() {
		return pageButtons[LEVELS_FX_PAGE_ID];
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
		browseButton.setText(FileManager
				.formatSampleName(TrackManager.currTrack.getCurrSampleName()));

		// update the browse pager instrument icon
		IconResource instrumentIconResource = TrackManager.currTrack
				.getIconResource();
		Icon instrumentIcon = browseButton.getIcon();
		if (instrumentIcon == null) {
			browseButton.setIcon(new Icon(instrumentIconResource));
		} else {
			instrumentIcon.setResource(instrumentIconResource);
		}
	}

	@Override
	protected synchronized void createChildren() {
		super.createChildren();

		addTrackButton = new ImageButton(shapeGroup);
		deleteTrackButton = new ImageButton(shapeGroup);

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

		addChildren(addTrackButton, deleteTrackButton);
	}

	@Override
	public synchronized void layoutChildren() {
		addTrackButton.layout(this, 0, 0, height, height);

		float labelWidth = (width - 4 * height) / 4;
		float x = addTrackButton.width;

		for (int i = 0; i < pageButtons.length; i++) {
			float w = pageButtons[i].getText().isEmpty() && i != BROWSE_PAGE_ID ? height
					: labelWidth;
			pageButtons[i].layout(this, x, 0, w, height);
			x += pageButtons[i].width;
		}

		deleteTrackButton.layout(this, width - height, 0, height, height);
	}

	@Override
	protected synchronized void initIcons() {
		super.initIcons();
		addTrackButton.setIcon(new Icon(IconResources.ADD));
		addTrackButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.labelFillColorSet, Colors.labelStrokeColorSet));

		deleteTrackButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.deleteFillColorSet, Colors.deleteStrokeColorSet));
		deleteTrackButton.setIcon(new Icon(IconResources.DELETE_TRACK));

		pageButtons[EDIT_PAGE_ID].setIcon(new Icon(IconResources.SAMPLE));
		pageButtons[NOTE_LEVELS_PAGE_ID]
				.setIcon(new Icon(IconResources.LEVELS));

		pageButtons[LEVELS_FX_PAGE_ID].setText("FX");
		pageButtons[ADSR_PAGE_ID].setText("ADSR");
	}

	@Override
	protected int getNumPages() {
		return 5;
	}
}
