package com.kh.beatbot.view;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.global.ShapeIconSource;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ToggleButton;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class ButtonRow extends TouchableBBView {
	public static ShapeGroup roundedRectGroup = new ShapeGroup();
	
	Track track;
	public ToggleButton instrumentButton, muteButton, soloButton;

	public ButtonRow(Track track) {
		super();
		this.track = track;
	}

	public void updateInstrumentIcon() {
		instrumentButton.setIconSource(track.getInstrument().getIconSource());
	}

	@Override
	protected void loadIcons() {
		updateInstrumentIcon();
		muteButton.setText("M");
		soloButton.setText("S");
		instrumentButton.setBgIconSource(new RoundedRectIconSource(
				roundedRectGroup, Colors.instrumentBgColorSet,
				Colors.instrumentStrokeColorSet));
		muteButton.setBgIconSource(new RoundedRectIconSource(
				roundedRectGroup, Colors.muteButtonColorSet,
				Colors.labelStrokeColorSet));
		soloButton.setBgIconSource(new RoundedRectIconSource(
				roundedRectGroup, Colors.soloButtonColorSet,
				Colors.labelStrokeColorSet));
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void draw() {
		// parent view, no drawing
	}

	@Override
	protected void createChildren() {
		instrumentButton = new ToggleButton();
		muteButton = new ToggleButton();
		soloButton = new ToggleButton();
		instrumentButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				TrackManager.selectInstrumentButton(instrumentButton);
				TrackManager.setTrack(track.getId());
			}
		});
		muteButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				TrackManager.getTrack(track.getId()).mute(
						muteButton.isChecked());
			}
		});
		soloButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				if (soloButton.isChecked()) {
					// if this track is soloing, set all other solo icons to
					// inactive.
					TrackManager.selectSoloButton(soloButton);
				}
				TrackManager.getTrack(track.getId()).solo(
						soloButton.isChecked());
			}
		});
		addChild(instrumentButton);
		addChild(muteButton);
		addChild(soloButton);
	}

	@Override
	public void layoutChildren() {
		instrumentButton.layout(this, 0, 0, height, height);
		muteButton.layout(this, height, 0, height * .72f, height);
		soloButton.layout(this, height + muteButton.width, 0,
				height * .72f, height);
	}
	
	public void destroy() {
		((ShapeIconSource)muteButton.getBgIconSource()).destroy();
		((ShapeIconSource)soloButton.getBgIconSource()).destroy();
		((ShapeIconSource)instrumentButton.getBgIconSource()).destroy();
	}
}
