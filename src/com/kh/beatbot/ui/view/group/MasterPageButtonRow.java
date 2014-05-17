package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.BpmView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class MasterPageButtonRow extends PageButtonRow {

	private static final int LEVELS_PAGE_ID = 0, EFFECTS_PAGE_ID = 1;

	private View bpmLabel;
	private BpmView bpmView;

	public MasterPageButtonRow(View view) {
		super(view);
	}

	public void setBPM(float bpm) {
		bpmView.setBPM(bpm);
	}

	public ToggleButton getLevelsButton() {
		return pageButtons[LEVELS_PAGE_ID];
	}

	public ToggleButton getEffectsButton() {
		return pageButtons[EFFECTS_PAGE_ID];
	}

	@Override
	protected synchronized void createChildren() {
		super.createChildren();

		bpmLabel = new View(this);
		bpmView = new BpmView(this);

		getLevelsButton().setResourceId(IconResourceSets.LEVELS);
		getEffectsButton().setText("FX");

		bpmLabel.setText("BPM");
		setBPM(MidiManager.getBPM());
	}

	@Override
	public synchronized void layoutChildren() {
		super.layoutChildren();

		float x = addTrackButton.width;
		for (int i = 0; i < pageButtons.length; i++) {
			pageButtons[i].layout(this, x, 0, height, height);
			x += pageButtons[i].width;
		}

		bpmLabel.layout(this, width - 5 * height, 0, 2 * height, height);
		bpmView.layout(this, width - 3 * height, 0, 2 * height, height);
	}

	@Override
	protected int getNumPages() {
		return 2;
	}
}
