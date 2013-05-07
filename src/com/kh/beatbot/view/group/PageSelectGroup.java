package com.kh.beatbot.view.group;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.layout.page.AdsrPage;
import com.kh.beatbot.layout.page.LevelsFXPage;
import com.kh.beatbot.layout.page.NoteLevelsPage;
import com.kh.beatbot.layout.page.SampleEditPage;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.TextButton;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class PageSelectGroup extends TouchableBBView {
	private static final int LEVELS_FX_PAGE_ID = 0;
	private static final int EDIT_PAGE_ID = 1;
	private static final int ADSR_PAGE_ID = 2;
	private static final int MASTER_PAGE_ID = 3;
	private static final int NOTE_LEVELS_PAGE_ID = 4;
	
	private static NoteLevelsPage levelsPage;
	private static LevelsFXPage masterLevelsFxPage, trackLevelsFxPage;
	private static SampleEditPage sampleEditPage;
	private static AdsrPage adsrPage;
	private static BBViewPager pager;

	private static TextButton addTrackButton;
	private static TextButton instrumentSelectButton;
	private static TextButton sampleSelectButton;
	private static TextButton[] textButtons = new TextButton[5];

	private static ShapeGroup roundedRectGroup = new ShapeGroup();

	public void update() {
		updateInstrumentIcon();
		updateSampleText();
	}

	public void updateAdsrPage() {
		adsrPage.updateLevelBar();
		adsrPage.updateLabels();
	}

	public void updateLevelsFXPage() {
		masterLevelsFxPage.update();
		trackLevelsFxPage.update();
	}

	public void notifyTrackChanged() {
		if (!initialized)
			return;
		update();
		trackLevelsFxPage.update();
		sampleEditPage.update();
		adsrPage.update();
	}

	@Override
	public void draw() {
		translate(-absoluteX, -absoluteY);
		roundedRectGroup.draw((GL11) BBView.gl, 1);
		translate(absoluteX, absoluteY);
	}

	@Override
	protected void createChildren() {
		addTrackButton = new TextButton();
		instrumentSelectButton = new TextButton();
		sampleSelectButton = new TextButton();
		for (int i = 0; i < textButtons.length; i++) {
			textButtons[i] = new TextButton();
		}

		textButtons[NOTE_LEVELS_PAGE_ID] = new TextButton();

		addTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				// add track should not behave as a toggle
				addTrackButton.setChecked(false);
				Managers.directoryManager.showAddTrackAlert();
			}
		});

		instrumentSelectButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				// instr select should not behave as a toggle
				instrumentSelectButton.setChecked(false);
				Managers.directoryManager.showInstrumentSelectAlert();
			}
		});

		sampleSelectButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				// sample select should not behave as a toggle
				sampleSelectButton.setChecked(false);
				Managers.directoryManager.showSampleSelectAlert();
			}
		});

		for (int i = 0; i < textButtons.length; i++) {
			final int id = i;
			textButtons[i].setOnReleaseListener(new OnReleaseListener() {
				@Override
				public void onRelease(Button button) {
					textButtons[id].setChecked(true);
					// deselect all buttons except this one.
					for (TextButton otherTextButton : textButtons) {
						if (!button.equals(otherTextButton)) {
							otherTextButton.setChecked(false);
						}
					}
					pager.setPage(id);
				}
			});
		}

		trackLevelsFxPage = new LevelsFXPage();
		sampleEditPage = new SampleEditPage();
		adsrPage = new AdsrPage();
		masterLevelsFxPage = new LevelsFXPage();
		levelsPage = new NoteLevelsPage();

		masterLevelsFxPage.setMasterMode(true);
		trackLevelsFxPage.setMasterMode(false);

		pager = new BBViewPager();
		pager.addPage(trackLevelsFxPage);
		pager.addPage(sampleEditPage);
		pager.addPage(adsrPage);
		pager.addPage(masterLevelsFxPage);
		pager.addPage(levelsPage);
		pager.setPage(LEVELS_FX_PAGE_ID);

		addChild(addTrackButton);
		addChild(instrumentSelectButton);
		addChild(sampleSelectButton);
		for (TextButton textButton : textButtons) {
			addChild(textButton);
		}
		addChild(pager);
	}

	@Override
	public void layoutChildren() {
		float buttonHeight = height / 5;
		float labelWidth = (width - 3 * buttonHeight) / 5;
		float labelYOffset = 2;
		addTrackButton
				.layout(this, 0, labelYOffset, buttonHeight, buttonHeight);
		instrumentSelectButton.layout(this, buttonHeight, labelYOffset,
				buttonHeight, buttonHeight);
		sampleSelectButton.layout(this, buttonHeight * 2, labelYOffset,
				labelWidth, buttonHeight);
		for (int i = 0; i < textButtons.length - 1; i++) {
			textButtons[i].layout(this,
					buttonHeight * 2 + (i + 1) * labelWidth, labelYOffset,
					labelWidth, buttonHeight);
		}
		textButtons[NOTE_LEVELS_PAGE_ID].layout(this, buttonHeight * 2 + 5
				* labelWidth, labelYOffset, buttonHeight, buttonHeight);
		pager.layout(this, 0, buttonHeight + 2 * labelYOffset, width, height
				- buttonHeight - 2 * labelYOffset);
	}

	@Override
	protected void loadIcons() {
		addTrackButton.setIconSource(new ImageIconSource(
				R.drawable.plus_outline, R.drawable.plus_outline));
		textButtons[NOTE_LEVELS_PAGE_ID].setIconSource(new ImageIconSource(
				R.drawable.levels_icon, -1, R.drawable.levels_icon_selected));
		addTrackButton.setBgIconSource(new RoundedRectIconSource(
				roundedRectGroup, Colors.labelBgColorSet,
				Colors.labelStrokeColorSet));
		instrumentSelectButton.setBgIconSource(new RoundedRectIconSource(
				roundedRectGroup, Colors.labelBgColorSet,
				Colors.labelStrokeColorSet));
		sampleSelectButton.setBgIconSource(new RoundedRectIconSource(
				roundedRectGroup, Colors.labelBgColorSet,
				Colors.labelStrokeColorSet));
		for (int i = 0; i < textButtons.length; i++) {
			textButtons[i].setBgIconSource(new RoundedRectIconSource(
					roundedRectGroup, Colors.labelBgColorSet,
					Colors.labelStrokeColorSet));
		}
		textButtons[LEVELS_FX_PAGE_ID].setText("FX");
		textButtons[EDIT_PAGE_ID].setText("EDIT");
		textButtons[ADSR_PAGE_ID].setText("ADSR");
		textButtons[MASTER_PAGE_ID].setText("MASTER");
	}

	@Override
	public void init() {
		// Parent
	}

	private void updateInstrumentIcon() {
		// update the track pager instrument icon
		instrumentSelectButton.setIconSource(TrackManager.currTrack.getInstrument()
				.getIconSource());
	}

	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = TrackManager.currTrack.getSampleName()
				.replace(".bb", "").toUpperCase();
		sampleSelectButton.setText(formattedName);
	}
}
