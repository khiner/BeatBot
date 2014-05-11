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
	protected ToggleButton[] pageButtons;
	protected Button currPage, addTrackButton;

	protected abstract int getNumPages();

	public PageButtonRow(RenderGroup renderGroup) {
		super(renderGroup);
	}

	@Override
	protected synchronized void createChildren() {
		pageButtons = new ToggleButton[getNumPages()];

		for (int i = 0; i < getNumPages(); i++) {
			pageButtons[i] = new ToggleButton(renderGroup);
			pageButtons[i].setIcon(IconResourceSets.LABEL_BASE);
		}

		currPage = pageButtons[0];

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

	public void setPager(final ViewPager pager) {
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
	}

	public synchronized void layoutChildren() {
		addTrackButton.layout(this, 0, 0, height, height);
	}
}
