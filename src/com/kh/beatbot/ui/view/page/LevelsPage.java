package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.Seekbar;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class LevelsPage extends TouchableView {
	// levels attrs
	protected Seekbar levelBar;
	protected ToggleButton volumeToggle, panToggle, pitchToggle;
	protected boolean masterMode = false;

	@Override
	public synchronized void update() {
		updateLevels();
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}

	public BaseTrack getCurrTrack() {
		return masterMode ? TrackManager.masterTrack : TrackManager.currTrack;
	}

	private void updateLevels() {
		Param currParam = getCurrTrack().getCurrentLevelParam();
		levelBar.setParam(currParam);
		levelBar.setLevelColor(getLevelColor(currParam),
				getLevelColorTrans(currParam));
		deselectAll();
		selectLevel(currParam);
	}

	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectLevel(Param currParam) {
		if (currParam.equals(getCurrTrack().volumeParam)) {
			volumeToggle.setChecked(true);
		} else if (currParam.equals(getCurrTrack().panParam)) {
			panToggle.setChecked(true);
		} else if (currParam.equals(getCurrTrack().pitchParam)) {
			pitchToggle.setChecked(true);
		}
	}

	private float[] getLevelColor(Param currParam) {
		if (currParam.equals(getCurrTrack().volumeParam)) {
			return Colors.VOLUME;
		} else if (currParam.equals(getCurrTrack().panParam)) {
			return Colors.PAN;
		} else if (currParam.equals(getCurrTrack().pitchParam)) {
			return Colors.PITCH;
		}
		return Colors.VOLUME;
	}

	private float[] getLevelColorTrans(Param currParam) {
		if (currParam.equals(getCurrTrack().volumeParam)) {
			return Colors.VOLUME_TRANS;
		} else if (currParam.equals(getCurrTrack().panParam)) {
			return Colors.PAN_TRANS;
		} else if (currParam.equals(getCurrTrack().pitchParam)) {
			return Colors.PITCH_TRANS;
		}
		return Colors.VOLUME_TRANS;
	}

	@Override
	protected synchronized void initIcons() {
		volumeToggle.setText("Vol");
		panToggle.setText("Pan");
		pitchToggle.setText("Pit");
		volumeToggle.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.volumeFillColorSet, Colors.volumeStrokeColorSet));
		panToggle.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.panFillColorSet, Colors.panStrokeColorSet));
		pitchToggle.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.pitchFillColorSet, Colors.pitchStrokeColorSet));
	}

	@Override
	protected synchronized void createChildren() {
		levelBar = new Seekbar(shapeGroup);
		volumeToggle = new ToggleButton(shapeGroup, false);
		panToggle = new ToggleButton(shapeGroup, false);
		pitchToggle = new ToggleButton(shapeGroup, false);
		volumeToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().setLevelType(Effect.LevelType.VOLUME);
				updateLevels();
			}
		});
		panToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().setLevelType(Effect.LevelType.PAN);
				updateLevels();
			}
		});
		pitchToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().setLevelType(Effect.LevelType.PITCH);
				updateLevels();
			}
		});
		addChildren(levelBar, volumeToggle, panToggle, pitchToggle);
	}

	@Override
	public synchronized void layoutChildren() {
		float thirdHeight = height / 3;
		float topRowY = height / 12;

		volumeToggle.layout(this, 0, topRowY, 2 * thirdHeight, thirdHeight);
		panToggle.layout(this, 2 * thirdHeight, topRowY, 2 * thirdHeight,
				thirdHeight);
		pitchToggle.layout(this, 4 * thirdHeight, topRowY, 2 * thirdHeight,
				thirdHeight);

		float levelX = 6 * thirdHeight;
		levelBar.layout(this, levelX, topRowY, width - levelX, thirdHeight);
	}
}
