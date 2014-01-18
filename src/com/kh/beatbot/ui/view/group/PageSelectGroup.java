package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.Track;
import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.event.TrackDestroyEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.BpmView;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.AdsrPage;
import com.kh.beatbot.ui.view.page.BrowsePage;
import com.kh.beatbot.ui.view.page.LevelsFXPage;
import com.kh.beatbot.ui.view.page.NoteLevelsPage;
import com.kh.beatbot.ui.view.page.SampleEditPage;

public class PageSelectGroup extends TouchableView implements TrackListener {
	public static final int BROWSE_PAGE_ID = 0, LEVELS_FX_PAGE_ID = 1,
			EDIT_PAGE_ID = 2, ADSR_PAGE_ID = 3, NOTE_LEVELS_PAGE_ID = 4;

	public static NoteLevelsPage levelsPage;
	public static LevelsFXPage levelsFxPage;
	public static BrowsePage browsePage;
	public static SampleEditPage editPage;
	public static AdsrPage adsrPage;
	public static ViewPager pager;

	private static TextView bpmLabel;
	private static BpmView bpmView;

	public static ToggleButton masterButton;
	private static ImageButton addTrackButton, deleteTrackButton;
	private static ToggleButton[] pageButtons = new ToggleButton[5];

	public void setBPM(float bpm) {
		bpmView.setBPM(bpm);
	}

	public void selectPage(int pageNum) {
		if (pageNum >= 0 && pageNum < pageButtons.length) {
			pageButtons[pageNum].trigger(false);
		}
	}

	public synchronized void update() {
		updateInstrumentIcon();
		updateSampleText();
	}

	public void updateAdsrPage() {
		adsrPage.update();
	}

	public void updateLevelsFXPage() {
		levelsFxPage.update();
	}

	public void updateBrowsePage() {
		browsePage.update();
	}

	public void updateAll() {
		update();
		updateBrowsePage();
		updateLevelsFXPage();
		editPage.update();
		adsrPage.update();
		levelsPage.update();
	}

	@Override
	protected synchronized void createChildren() {
		addTrackButton = new ImageButton(shapeGroup);
		deleteTrackButton = new ImageButton(shapeGroup);
		masterButton = new ToggleButton(shapeGroup, false);

		masterButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				levelsFxPage.setMasterMode(true);
				TrackManager.currTrack.getButtonRow().instrumentButton.setChecked(false);
				updateAll();
			}
		});

		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i] = new ToggleButton(shapeGroup, false);
		}

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

		for (int i = 0; i < pageButtons.length; i++) {
			final int id = i;
			pageButtons[i].setOnReleaseListener(new OnReleaseListener() {
				@Override
				public void onRelease(Button button) {
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

		levelsFxPage = new LevelsFXPage();
		browsePage = new BrowsePage();
		editPage = new SampleEditPage();
		adsrPage = new AdsrPage();
		levelsPage = new NoteLevelsPage();

		levelsFxPage.setMasterMode(false);

		pager = new ViewPager();
		pager.addPages(browsePage, levelsFxPage, editPage, adsrPage, levelsPage);

		masterButton.setText("Master");
		pageButtons[LEVELS_FX_PAGE_ID].setText("FX");
		pageButtons[ADSR_PAGE_ID].setText("ADSR");

		bpmLabel = new TextView(shapeGroup);
		bpmView = new BpmView(shapeGroup);
		bpmLabel.setText("BPM");

		addChildren(masterButton, addTrackButton, deleteTrackButton, pager,
				bpmView, bpmLabel);
		addChildren(pageButtons);
	}

	@Override
	public synchronized void layoutChildren() {
		float labelYOffset = 2;

		masterButton.layout(this, 0, labelYOffset,
				View.mainPage.getTrackControlWidth(), LABEL_HEIGHT);

		addTrackButton.layout(this, masterButton.width, labelYOffset,
				LABEL_HEIGHT, LABEL_HEIGHT);

		float labelWidth = (width - 4 * LABEL_HEIGHT - masterButton.width) / 4;

		float x = masterButton.width + addTrackButton.width;
		for (int i = 0; i < pageButtons.length; i++) {
			float w = pageButtons[i].getText().isEmpty() && i != BROWSE_PAGE_ID ? LABEL_HEIGHT
					: labelWidth;
			pageButtons[i].layout(this, x, labelYOffset, w, LABEL_HEIGHT);
			x += pageButtons[i].width;
		}
		pager.layout(this, 0, LABEL_HEIGHT + 2 * labelYOffset, width, height
				- LABEL_HEIGHT - 2 * labelYOffset);

		bpmLabel.layout(this, width - 5 * LABEL_HEIGHT, labelYOffset,
				2 * LABEL_HEIGHT, LABEL_HEIGHT);
		bpmView.layout(this, width - 3 * LABEL_HEIGHT, labelYOffset,
				2 * LABEL_HEIGHT, LABEL_HEIGHT);

		deleteTrackButton.layout(this, width - LABEL_HEIGHT, labelYOffset,
				LABEL_HEIGHT, LABEL_HEIGHT);
	}

	@Override
	protected synchronized void initIcons() {
		addTrackButton.setIcon(new Icon(IconResources.ADD));
		addTrackButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.labelFillColorSet, Colors.labelStrokeColorSet));
		masterButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.labelFillColorSet, Colors.labelStrokeColorSet));
		deleteTrackButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.deleteFillColorSet, Colors.deleteStrokeColorSet));
		deleteTrackButton.setIcon(new Icon(IconResources.DELETE_TRACK));

		pageButtons[EDIT_PAGE_ID].setIcon(new Icon(IconResources.SAMPLE));
		pageButtons[NOTE_LEVELS_PAGE_ID]
				.setIcon(new Icon(IconResources.LEVELS));
		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i].setBgIcon(new RoundedRectIcon(shapeGroup,
					Colors.labelFillColorSet, Colors.labelStrokeColorSet));
		}

		setBPM(MidiManager.getBPM());
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

	@Override
	public void onCreate(Track track) {
		updateAll();
	}

	@Override
	public void onDestroy(Track track) {
	}

	@Override
	public void onSelect(Track track) {
		masterButton.setChecked(false);
		levelsFxPage.setMasterMode(false);
		updateAll();
	}

	@Override
	public void onSampleChange(Track track) {
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
	}
}
