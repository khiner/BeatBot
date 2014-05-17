package com.kh.beatbot.ui.view;

import com.kh.beatbot.Track;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackButtonRow extends TouchableView {
	public ToggleButton instrumentButton, muteButton, soloButton;
	private Track track;

	public TrackButtonRow(View view, RenderGroup renderGroup, Track track) {
		super(view, renderGroup);
		this.track = track;
	}

	public void updateInstrumentIcon() {
		instrumentButton.setResourceId(track.getIcon());
	}

	@Override
	protected synchronized void createChildren() {
		instrumentButton = new ToggleButton(this, renderGroup).withRoundedRect().withIcon(
				IconResourceSets.INSTRUMENT_BASE);
		muteButton = new ToggleButton(this, renderGroup).oscillating().withRoundedRect()
				.withIcon(IconResourceSets.MUTE);
		soloButton = new ToggleButton(this, renderGroup).oscillating().withRoundedRect()
				.withIcon(IconResourceSets.SOLO);

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
	}

	@Override
	public synchronized void layoutChildren() {
		instrumentButton.layout(this, 0, 0, height, height);
		muteButton.layout(this, height, 0, height * .72f, height);
		soloButton.layout(this, height + muteButton.width, 0, height * .72f, height);
	}
}
