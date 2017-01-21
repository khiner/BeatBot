package com.odang.beatbot.ui.view.page.main;

import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.group.MidiViewGroup;
import com.odang.beatbot.ui.view.group.PageSelectGroup;

public class EditPage extends TouchableView {
	public MidiViewGroup midiViewGroup;
	public PageSelectGroup pageSelectGroup;

	public EditPage(View view) {
		super(view);
	}

	@Override
	protected void createChildren() {
		midiViewGroup = new MidiViewGroup(this);
		pageSelectGroup = new PageSelectGroup(this);
	}

	@Override
	public void layoutChildren() {
		float midiHeight = 3 * height / 5;
		float pageSelectGroupHeight = height - midiHeight;
		midiViewGroup.layout(this, 0, 0, width - 15, midiHeight);
		pageSelectGroup.layout(this, 0, midiHeight, width, pageSelectGroupHeight);
	}
}
