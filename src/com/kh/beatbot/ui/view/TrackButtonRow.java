package com.kh.beatbot.ui.view;

import com.kh.beatbot.Track;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.ShapeIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class TrackButtonRow extends TouchableView {
	public static ShapeGroup roundedRectGroup = new ShapeGroup();

	public ToggleButton instrumentButton, muteButton, soloButton;
	private Track track;

	public TrackButtonRow(Track track) {
		super();
		this.track = track;
	}

	public void updateInstrumentIcon() {
		IconResource newResource = track.getInstrument().getIconResource();
		Icon instrumentIcon = instrumentButton.getIcon();
		if (instrumentIcon == null) {
			instrumentButton.setIcon(new Icon(newResource));
		} else {
			instrumentIcon.setResource(newResource);
		}
	}

	@Override
	protected void loadIcons() {
		updateInstrumentIcon();
		muteButton.setText("M");
		soloButton.setText("S");
		instrumentButton.setBgIcon(new RoundedRectIcon(roundedRectGroup,
				Colors.instrumentBgColorSet, Colors.buttonRowStrokeColorSet));
		muteButton.setBgIcon(new RoundedRectIcon(roundedRectGroup,
				Colors.muteButtonColorSet, Colors.buttonRowStrokeColorSet));
		soloButton.setBgIcon(new RoundedRectIcon(roundedRectGroup,
				Colors.soloButtonColorSet, Colors.buttonRowStrokeColorSet));
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
				TrackManager.setTrack(track);
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
				if (soloButton.isChecked()) {
					// if this track is soloing, set all other solo icons to
					// inactive.
					TrackManager.selectSoloButton(soloButton);
				}
				track.solo(soloButton.isChecked());
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
		soloButton.layout(this, height + muteButton.width, 0, height * .72f,
				height);
	}

	public void destroy() {
		((ShapeIcon) muteButton.getBgIcon()).destroy();
		((ShapeIcon) soloButton.getBgIcon()).destroy();
		((ShapeIcon) instrumentButton.getBgIcon()).destroy();
	}
}
