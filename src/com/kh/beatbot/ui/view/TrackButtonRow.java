package com.kh.beatbot.ui.view;

import com.kh.beatbot.event.track.TrackMuteEvent;
import com.kh.beatbot.event.track.TrackSoloEvent;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackButtonRow extends TouchableView {
	public ToggleButton instrumentButton, muteButton, soloButton;
	private Track track;

	public TrackButtonRow(View view, Track track) {
		super(view);
		this.track = track;
	}
	
	public void updateInstrumentIcon() {
		instrumentButton.setResourceId(track.getIcon());
	}

	@Override
	protected void createChildren() {
		instrumentButton = new ToggleButton(this).withRoundedRect().withIcon(
				IconResourceSets.INSTRUMENT_BASE);
		muteButton = new ToggleButton(this).oscillating().withRoundedRect()
				.withIcon(IconResourceSets.MUTE);
		soloButton = new ToggleButton(this).oscillating().withRoundedRect()
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
				new TrackMuteEvent(track).execute();
			}
		});
		soloButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				new TrackSoloEvent(track).execute();
			}
		});
	}

	@Override
	public void layoutChildren() {
		instrumentButton.layout(this, 0, 0, height, height);
		muteButton.layout(this, height, 0, height * .72f, height);
		soloButton.layout(this, height + muteButton.width, 0, height * .72f, height);
	}
}
