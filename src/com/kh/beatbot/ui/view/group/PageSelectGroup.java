package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.AdsrPage;
import com.kh.beatbot.ui.view.page.BrowsePage;
import com.kh.beatbot.ui.view.page.MasterPage;
import com.kh.beatbot.ui.view.page.NoteLevelsPage;
import com.kh.beatbot.ui.view.page.SampleEditPage;
import com.kh.beatbot.ui.view.page.TrackPage;

public class PageSelectGroup extends TouchableView {
	public static final int BROWSE_PAGE_ID = 0, TRACK_PAGE_ID = 1,
			EDIT_PAGE_ID = 2, ADSR_PAGE_ID = 3, MASTER_PAGE_ID = 4,
			NOTE_LEVELS_PAGE_ID = 5;

	public static NoteLevelsPage levelsPage;
	public static MasterPage masterPage;
	public static TrackPage trackPage;
	public static BrowsePage browsePage;
	public static SampleEditPage editPage;
	public static AdsrPage adsrPage;
	public static ViewPager pager;

	private static ImageButton addTrackButton;
	private static ToggleButton[] pageButtons = new ToggleButton[6];

	private static ShapeGroup roundedRectGroup = new ShapeGroup();

	public void selectPage(int pageNum) {
		if (pageNum >= 0 && pageNum < pageButtons.length) {
			pageButtons[pageNum].trigger(false);
		}
	}

	public MasterPage getMasterPage() {
		return masterPage;
	}

	public synchronized void update() {
		updateInstrumentIcon();
		updateSampleText();
	}

	public void updateAdsrPage() {
		adsrPage.update();
	}

	public void updateLevelsFXPage() {
		masterPage.update();
		trackPage.update();
	}

	public void updateAll() {
		if (!initialized)
			return;
		update();
		updateLevelsFXPage();
		editPage.update();
		adsrPage.update();
	}

	@Override
	public void draw() {
		roundedRectGroup.draw(this, 1);
	}

	@Override
	protected synchronized void createChildren() {
		addTrackButton = new ImageButton();
		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i] = new ToggleButton();
		}

		addTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackCreateEvent().execute();
			}
		});

		for (int i = 0; i < pageButtons.length; i++) {
			final int id = i;
			pageButtons[i].setOnReleaseListener(new OnReleaseListener() {
				@Override
				public void onRelease(Button button) {
					pageButtons[id].setChecked(true);
					// deselect all buttons except this one.
					for (ToggleButton otherToggleButton : pageButtons) {
						if (!button.equals(otherToggleButton)) {
							otherToggleButton.setChecked(false);
						}
					}
					pager.setPage(id);
				}
			});
		}

		trackPage = new TrackPage();
		browsePage = new BrowsePage();
		editPage = new SampleEditPage();
		adsrPage = new AdsrPage();
		masterPage = new MasterPage();
		levelsPage = new NoteLevelsPage();

		masterPage.setMasterMode(true);
		trackPage.setMasterMode(false);

		pager = new ViewPager();
		pager.addPages(browsePage, trackPage, editPage, adsrPage, masterPage,
				levelsPage);

		pageButtons[TRACK_PAGE_ID].setText("Track");
		pageButtons[ADSR_PAGE_ID].setText("ADSR");
		pageButtons[MASTER_PAGE_ID].setText("Master");

		addChildren(addTrackButton, pager);
		addChildren(pageButtons);
	}

	@Override
	public synchronized void layoutChildren() {
		float labelWidth = (width - 3 * LABEL_HEIGHT) / 4;
		float labelYOffset = 2;
		addTrackButton
				.layout(this, 0, labelYOffset, LABEL_HEIGHT, LABEL_HEIGHT);

		float x = addTrackButton.width;
		for (int i = 0; i < pageButtons.length; i++) {
			float w = pageButtons[i].getText().isEmpty() && i != BROWSE_PAGE_ID ? LABEL_HEIGHT
					: labelWidth;
			pageButtons[i].layout(this, x, labelYOffset, w, LABEL_HEIGHT);
			x += pageButtons[i].width;
		}
		pager.layout(this, 0, LABEL_HEIGHT + 2 * labelYOffset, width, height
				- LABEL_HEIGHT - 2 * labelYOffset);
	}

	@Override
	protected synchronized void loadIcons() {
		addTrackButton.setIcon(new Icon(IconResources.ADD));
		addTrackButton.setBgIcon(new RoundedRectIcon(roundedRectGroup,
				Colors.labelBgColorSet, Colors.labelStrokeColorSet));

		pageButtons[EDIT_PAGE_ID].setIcon(new Icon(IconResources.SAMPLE));
		pageButtons[NOTE_LEVELS_PAGE_ID]
				.setIcon(new Icon(IconResources.LEVELS));
		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i].setBgIcon(new RoundedRectIcon(roundedRectGroup,
					Colors.labelBgColorSet, Colors.labelStrokeColorSet));
		}
	}

	private void updateInstrumentIcon() {
		// update the browse pager instrument icon
		IconResource instrumentIconResource = TrackManager.currTrack
				.getIconResource();
		ToggleButton button = pageButtons[BROWSE_PAGE_ID];
		Icon instrumentIcon = button.getIcon();
		if (instrumentIcon == null) {
			button.setIcon(new Icon(instrumentIconResource));
		} else {
			instrumentIcon.setResource(instrumentIconResource);
		}
	}

	private void updateSampleText() {
		pageButtons[BROWSE_PAGE_ID].setText(FileManager
				.formatSampleName(TrackManager.currTrack.getCurrSampleName()));
	}
}
