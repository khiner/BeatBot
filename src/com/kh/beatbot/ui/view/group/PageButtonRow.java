package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.event.track.TrackCreateEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.SwappingViewPager;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public abstract class PageButtonRow extends TouchableView {
	protected ToggleButton[] pageButtons;
	protected Button currPage, addTrackButton;

	protected abstract int getNumPages();

	public PageButtonRow(View view) {
		super(view);
	}

	public abstract ToggleButton getLevelsButton();

	@Override
	protected synchronized void createChildren() {
		pageButtons = new ToggleButton[getNumPages()];

		for (int i = 0; i < getNumPages(); i++) {
			pageButtons[i] = new ToggleButton(this).withRoundedRect().withIcon(
					IconResourceSets.LABEL_BASE);
		}

		currPage = pageButtons[0];

		addTrackButton = new Button(this).withRoundedRect().withIcon(IconResourceSets.LABEL_BASE);
		addTrackButton.setResourceId(IconResourceSets.ADD);

		addTrackButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackCreateEvent().execute();
			}
		});
	}

	public void setPager(final SwappingViewPager pager) {
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
