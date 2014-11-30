package com.kh.beatbot.ui.view.page;

import java.io.File;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.SampleView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class RecordPage extends TrackPage {
	private Button recordSourceSelectButton;
	private SampleView sampleView;
	private ToggleButton recordButton;

	public RecordPage(View view) {
		super(view);
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
		sampleView.setText("Ready to record");

		recordSourceSelectButton = new Button(this).withIcon(IconResourceSets.VALUE_LABEL)
				.withRoundedRect();
		recordButton = new ToggleButton(this).oscillating().withIcon(IconResourceSets.RECORD);
		recordSourceSelectButton.setText(RecordManager.GLOBAL_RECORD_SOURCE);
		recordSourceSelectButton.setOnReleaseListener(RecordManager.getRecordSourceListener());

		recordButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				if (((ToggleButton) button).isChecked()) {
					RecordManager.startRecording();
					sampleView.setText("Recording...");
				} else {
					File sampleFile = RecordManager.stopRecording();
					try {
						sampleView.setText("");
						TrackManager.currTrack.setSample(sampleFile);
					} catch (Exception e) {
						sampleView.setText("Error saving file");
					}
				}
			}
		});
	}

	@Override
	public synchronized void layoutChildren() {
		float topBarH = height * .29f;
		float fillH = height - topBarH;
		recordSourceSelectButton.layout(this, 0, 0, topBarH * 4, topBarH);
		recordButton.layout(this, 0, topBarH, fillH, fillH);
		sampleView.layout(this, fillH, topBarH, width - fillH, fillH);
	}
}
