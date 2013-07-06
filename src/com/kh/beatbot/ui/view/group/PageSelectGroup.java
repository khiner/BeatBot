package com.kh.beatbot.ui.view.group;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.layout.page.AdsrPage;
import com.kh.beatbot.layout.page.MasterPage;
import com.kh.beatbot.layout.page.NoteLevelsPage;
import com.kh.beatbot.layout.page.SampleEditPage;
import com.kh.beatbot.layout.page.TrackPage;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class PageSelectGroup extends TouchableView {
	public static final int TRACK_PAGE_ID = 0;
	public static final int EDIT_PAGE_ID = 1;
	public static final int ADSR_PAGE_ID = 2;
	public static final int MASTER_PAGE_ID = 3;
	public static final int NOTE_LEVELS_PAGE_ID = 4;

	private static NoteLevelsPage levelsPage;
	private static MasterPage masterPage;
	private static TrackPage trackPage;
	private static SampleEditPage sampleEditPage;
	private static AdsrPage adsrPage;
	private static ViewPager pager;

	private static ImageButton addTrackButton;
	private static ToggleButton[] pageButtons = new ToggleButton[5];

	private static ShapeGroup roundedRectGroup = new ShapeGroup();

	public void selectPage(int pageNum) {
		if (pageNum < 0 || pageNum >= pageButtons.length) {
			return;
		}
		pageButtons[pageNum].trigger(false);
	}

	public MasterPage getMasterPage() {
		return masterPage;
	}

	public void update() {
		updateInstrumentIcon();
		updateSampleText();
	}

	public void updateAdsrPage() {
		adsrPage.updateLevelBar();
		adsrPage.updateLabels();
	}

	public void updateLevelsFXPage() {
		masterPage.update();
		trackPage.update();
	}

	public void notifyTrackChanged() {
		if (!initialized)
			return;
		update();
		trackPage.update();
		sampleEditPage.update();
		adsrPage.update();
	}

	@Override
	public void draw() {
		translate(-absoluteX, -absoluteY);
		roundedRectGroup.draw((GL11) View.gl, 1);
		translate(absoluteX, absoluteY);
	}

	@Override
	protected void createChildren() {
		addTrackButton = new ImageButton();
		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i] = new ToggleButton();
		}

		pageButtons[NOTE_LEVELS_PAGE_ID] = new ToggleButton();

		addTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				Managers.directoryManager.showAddTrackAlert();
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
		sampleEditPage = new SampleEditPage();
		adsrPage = new AdsrPage();
		masterPage = new MasterPage();
		levelsPage = new NoteLevelsPage();

		masterPage.setMasterMode(true);
		trackPage.setMasterMode(false);

		pager = new ViewPager();
		pager.addPage(trackPage);
		pager.addPage(sampleEditPage);
		pager.addPage(adsrPage);
		pager.addPage(masterPage);
		pager.addPage(levelsPage);
		pager.setPage(TRACK_PAGE_ID);

		addChild(addTrackButton);
		for (ToggleButton ToggleButton : pageButtons) {
			addChild(ToggleButton);
		}
		addChild(pager);
	}

	@Override
	public void layoutChildren() {
		float labelHeight = height / 5;
		float labelWidth = (width - 2 * labelHeight) / 4;
		float labelYOffset = 2;
		addTrackButton.layout(this, 0, labelYOffset, labelHeight, labelHeight);
		for (int i = 0; i < pageButtons.length - 1; i++) {
			float x = labelHeight + i * labelWidth;
			pageButtons[i].layout(this, x, labelYOffset, labelWidth,
					labelHeight);
		}
		pageButtons[NOTE_LEVELS_PAGE_ID].layout(this, labelHeight + 4
				* labelWidth, labelYOffset, labelHeight, labelHeight);
		pager.layout(this, 0, labelHeight + 2 * labelYOffset, width, height
				- labelHeight - 2 * labelYOffset);
	}

	@Override
	protected void loadIcons() {
		addTrackButton.setIcon(new Icon(IconResources.ADD));
		addTrackButton.setBgIcon(new RoundedRectIcon(roundedRectGroup,
				Colors.labelBgColorSet, Colors.labelStrokeColorSet));

		pageButtons[NOTE_LEVELS_PAGE_ID]
				.setIcon(new Icon(IconResources.LEVELS));
		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i].setBgIcon(new RoundedRectIcon(roundedRectGroup,
					Colors.labelBgColorSet, Colors.labelStrokeColorSet));
		}
		pageButtons[TRACK_PAGE_ID].setText("TRACK");
		pageButtons[ADSR_PAGE_ID].setText("ADSR");
		pageButtons[MASTER_PAGE_ID].setText("MASTER");
	}

	@Override
	public void init() {
		// Parent
	}

	private void updateInstrumentIcon() {
		// update the track pager instrument icon
		IconResource instrumentIconResource = TrackManager.currTrack.getInstrument().getIconResource();
		Icon instrumentIcon = pageButtons[TRACK_PAGE_ID].getIcon();
		if (instrumentIcon == null) {
			pageButtons[TRACK_PAGE_ID].setIcon(new Icon(instrumentIconResource));
		} else {
			instrumentIcon.setResource(instrumentIconResource);
		}
	}

	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = TrackManager.currTrack.getCurrSampleName()
				.toUpperCase();
		pageButtons[EDIT_PAGE_ID].setText(formattedName);
	}
}
