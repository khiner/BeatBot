package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.event.TrackCreateEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.ViewPager;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public abstract class PageButtonRow extends TouchableView {

	protected ViewPager pager;
	protected ToggleButton[] pageButtons;
	protected Button currPage, addTrackButton;

	public PageButtonRow(RenderGroup renderGroup, ViewPager pager) {
		this.pager = pager;
		shouldDraw = (null == renderGroup);
		this.renderGroup = shouldDraw ? new RenderGroup() : renderGroup;
		createChildren();
	}

	protected abstract int getNumPages();

	@Override
	protected synchronized void createChildren() {
		pageButtons = new ToggleButton[getNumPages()];

		for (int i = 0; i < getNumPages(); i++) {
			pageButtons[i] = new ToggleButton(renderGroup, false);
			pageButtons[i].setIcon(IconResourceSets.LABEL_BASE);
		}

		currPage = pageButtons[0];

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
					currPage = button;
					pager.setPage(button);
				}
			});
		}

		addTrackButton = new Button(renderGroup);
		addTrackButton.setIcon(IconResourceSets.LABEL_BASE);
		addTrackButton.setResourceId(IconResourceSets.ADD);

		addTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackCreateEvent().execute();
			}
		});

		addChildren(pageButtons);
		addChildren(addTrackButton);
	}
	
	protected synchronized void layoutChildren() {
		addTrackButton.layout(this, 0, 0, height, height);
	}
}
