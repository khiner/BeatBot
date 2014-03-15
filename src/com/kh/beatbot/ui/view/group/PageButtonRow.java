package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public abstract class PageButtonRow extends TouchableView {

	protected ViewPager pager;
	protected ToggleButton[] pageButtons;

	public PageButtonRow(ShapeGroup shapeGroup, ViewPager pager) {
		this.pager = pager;
		shouldDraw = (null == shapeGroup);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		createChildren();
	}

	protected abstract int getNumPages();

	@Override
	protected synchronized void createChildren() {
		pageButtons = new ToggleButton[getNumPages()];

		for (int i = 0; i < getNumPages(); i++) {
			pageButtons[i] = new ToggleButton(shapeGroup, false);
			pageButtons[i].setIcon(IconResourceSets.LABEL_BASE);
		}

		for (ToggleButton pageButton : pageButtons) {
			pageButton.setOnReleaseListener(new OnReleaseListener() {
				@Override
				public void onRelease(Button button) {
					// deselect all buttons except this one.
					for (ToggleButton otherToggleButton : pageButtons) {
						if (!button.equals(otherToggleButton)) {
							otherToggleButton.setChecked(false);
						}
					}
					pager.setPage(button);
				}
			});
		}

		addChildren(pageButtons);
	}
}
