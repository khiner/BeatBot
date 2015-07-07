package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class EditGroup extends TouchableView {
	public MidiViewGroup midiViewGroup;
	public PageSelectGroup pageSelectGroup;

	public EditGroup(View view) {
		super(view);
	}

	@Override
	protected synchronized void createChildren() {
		midiViewGroup = new MidiViewGroup(this);
		pageSelectGroup = new PageSelectGroup(this);
	}

	@Override
	public synchronized void layoutChildren() {
		float midiHeight = 3 * height / 5;
		float pageSelectGroupHeight = height - midiHeight;
		midiViewGroup.layout(this, 0, 0, width - 15, midiHeight);
		pageSelectGroup.layout(this, 0, midiHeight, width, pageSelectGroupHeight);
	}
}
