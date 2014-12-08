package com.kh.beatbot.ui.view.page;

import java.io.File;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.listener.RecordStateListener;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.SampleView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.ThresholdParamControl;

public class RecordPage extends TrackPage implements RecordStateListener {
	private Button recordSourceSelectButton;
	private SampleView sampleView;
	private ToggleButton recordButton;
	private ThresholdParamControl thresholdParamControl;

	public RecordPage(View view) {
		super(view);
		RecordManager.setListener(this);
	}

	@Override
	public void onSelect(BaseTrack track) {
		if (null != sampleView) {
			sampleView.update();
		}
	}

	@Override
	protected synchronized void createChildren() {
		sampleView = new SampleView(this, renderGroup);
		sampleView.setClip(true);

		recordSourceSelectButton = new Button(this).withIcon(IconResourceSets.VALUE_LABEL)
				.withRoundedRect();
		recordButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.RECORD);
		recordSourceSelectButton.setText(RecordManager.GLOBAL_RECORD_SOURCE);
		recordSourceSelectButton
				.setOnReleaseListener(RecordManager.getRecordSourceButtonListener());

		recordButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				if (((ToggleButton) button).isChecked()) {
					RecordManager.arm();
				} else {
					RecordManager.stopRecording();
				}
			}
		});

		thresholdParamControl = new ThresholdParamControl(this);
		Param thresholdParam = new Param(0, "Threshold").withUnits("db");
		thresholdParamControl.setParam(thresholdParam);
		thresholdParam.addListener(new ParamListener() {
			@Override
			public void onParamChanged(Param param) {
				RecordManager.setThresholdLevel(param.viewLevel);
			}
		});
	}

	@Override
	public synchronized void layoutChildren() {
		float topBarH = height * .29f;
		float fillH = height - topBarH;
		recordSourceSelectButton.layout(this, 0, 0, topBarH * 4, topBarH);
		thresholdParamControl.layout(this, topBarH * 4, 0, width - topBarH * 4, topBarH);

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
			TrackManager.currTrack.setSample(recordedSampleFile);
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
		RecordManager.startListening(); // listen to RecordSource to start populating ThresholdBar
	}

	@Override
	public synchronized void hide() {
		RecordManager.stopListening();
		super.hide();
	}
}
