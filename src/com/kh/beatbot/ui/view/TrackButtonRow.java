package com.kh.beatbot.ui.view;

import com.kh.beatbot.Track;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackButtonRow extends TouchableView {
	public ToggleButton instrumentButton, muteButton, soloButton;
	private Track track;

	public TrackButtonRow(Track track) {
		super(View.mainPage.midiTrackView.shapeGroup);
		this.track = track;
	}

	public void updateInstrumentIcon() {
		IconResource newResource = track.getIconResource();
		if (instrumentButton.getIcon() == null) {
			instrumentButton.setIcon(new Icon(newResource));
		} else {
			instrumentButton.setIconResource(newResource);
		}
	}

	@Override
	protected synchronized void initIcons() {
		updateInstrumentIcon();
		muteButton.setText("M");
		soloButton.setText("S");
		instrumentButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.instrumentFillColorSet, Colors.buttonRowStrokeColorSet));
		muteButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.muteButtonColorSet, Colors.buttonRowStrokeColorSet));
		soloButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.soloButtonColorSet, Colors.buttonRowStrokeColorSet));
	}

	@Override
	protected synchronized void createChildren() {
		instrumentButton = new ToggleButton(shapeGroup, false);
		muteButton = new ToggleButton(shapeGroup, true);
		soloButton = new ToggleButton(shapeGroup, true);
		instrumentButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				track.select();
			}
		});
		muteButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				track.mute(muteButton.isChecked());
			}
		});
		soloButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				track.solo(soloButton.isChecked());
			}
		});
		addChildren(instrumentButton, muteButton, soloButton);
	}

	@Override
	public synchronized void layoutChildren() {
		instrumentButton.layout(this, 0, 0, height, height);
		muteButton.layout(this, height, 0, height * .72f, height);
		soloButton.layout(this, height + muteButton.width, 0, height * .72f,
				height);
	}
}
