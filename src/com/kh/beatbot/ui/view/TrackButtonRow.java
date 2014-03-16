package com.kh.beatbot.ui.view;

import com.kh.beatbot.Track;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackButtonRow extends TouchableView {
	public ToggleButton instrumentButton, muteButton, soloButton;
	private Track track;

	public TrackButtonRow(ShapeGroup shapeGroup, Track track) {
		super(shapeGroup);
		this.track = track;
	}

	public void updateInstrumentIcon() {
		instrumentButton.setResourceId(track.getIcon());
	}

	@Override
	protected synchronized void createChildren() {
		instrumentButton = new ToggleButton(shapeGroup, false);
		muteButton = new ToggleButton(shapeGroup, true);
		soloButton = new ToggleButton(shapeGroup, true);

		instrumentButton.setIcon(IconResourceSets.INSTRUMENT_BASE);
		muteButton.setIcon(IconResourceSets.MUTE);
		soloButton.setIcon(IconResourceSets.SOLO);
		muteButton.setText("M");
		soloButton.setText("S");

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
		soloButton.layout(this, height + muteButton.width, 0, height * .72f, height);
	}
}
