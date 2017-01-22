package com.odang.beatbot.ui.view.page.track;

import java.io.File;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.listener.ParamListener;
import com.odang.beatbot.listener.RecordStateListener;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.SampleView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;
import com.odang.beatbot.ui.view.control.param.ThresholdParamControl;

public class RecordPage extends TrackPage implements RecordStateListener {
	private View recordSourceSelectLabel;
	private Button recordSourceSelectButton;
	private SampleView sampleView;
	private ToggleButton recordButton;
	private ThresholdParamControl thresholdParamControl;

	public RecordPage(View view) {
		super(view);
		context.getRecordManager().setListener(this);
	}

	@Override
	public void onSelect(BaseTrack track) {
		if (null != sampleView) {
			sampleView.update();
		}
	}

	@Override
	protected void createChildren() {
		sampleView = new SampleView(this, renderGroup);
		sampleView.setClip(true);

		recordSourceSelectLabel = new View(this).withIcon(IconResourceSets.CONTROL_LABEL);
		recordSourceSelectLabel.setText("Source");
		recordSourceSelectButton = new Button(this).withIcon(IconResourceSets.VALUE_LABEL)
				.withRoundedRect();
		recordButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.RECORD);
		recordSourceSelectButton.setText(context.getTrackManager().getMasterTrack().getFormattedName());
		recordSourceSelectButton.setOnReleaseListener(context.getRecordManager()
				.getRecordSourceButtonListener());

		recordButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				if (((ToggleButton) button).isChecked()) {
					context.getRecordManager().arm();
				} else {
					context.getRecordManager().stopRecording();
				}
			}
		});

		thresholdParamControl = new ThresholdParamControl(this);
		final Param thresholdParam = new Param(0, "Threshold").withUnits("db");
		thresholdParamControl.setParam(thresholdParam);
		thresholdParam.addListener(new ParamListener() {
			@Override
			public void onParamChange(Param param) {
				View.context.getRecordManager().setThresholdLevel(param.viewLevel);
			}
		});
	}

	@Override
	public void layoutChildren() {
		final float topBarH = height * .29f;
		final float fillH = height - topBarH;
		recordSourceSelectLabel.layout(this, 0, 0, topBarH * 2, topBarH);
		recordSourceSelectButton.layout(this, topBarH * 2, 0, topBarH * 4, topBarH);
		thresholdParamControl.layout(this, topBarH * 6, 0, width - topBarH * 6, topBarH);

		recordButton.layout(this, 0, topBarH, fillH, fillH);
		sampleView.layout(this, fillH, topBarH, width - fillH, fillH);
	}

	@Override
	public void onListenStart() {
		sampleView.setText("Ready to record");
	}

	@Override
	public void onRecordArmed() {
		sampleView.setText("Waiting for threshold...");
	}

	@Override
	public void onRecordDisarmed() {
		sampleView.setText("Ready to record");
	}

	@Override
	public void onListenStop() {
		sampleView.setText("Select record source");
	}

	@Override
	public void onRecordStart() {
		sampleView.setText("Recording...");
	}

	@Override
	public void onRecordStop(File recordedSampleFile) {
		try {
			sampleView.setText("");
			((Track) context.getTrackManager().getCurrTrack()).setSample(recordedSampleFile);
			sampleView.update();
		} catch (Exception e) {
			sampleView.setText("Error saving file");
		}
	}

	@Override
	public void onRecordSourceBufferFilled(float maxFrame) {
		thresholdParamControl.setLevel(maxFrame);
	}

	@Override
	public synchronized void show() {
		super.show();
		// listen to RecordSource to start populating ThresholdBar
		context.getRecordManager().startListening();
	}

	@Override
	public synchronized void hide() {
		context.getRecordManager().stopListening();
		super.hide();
	}
}
