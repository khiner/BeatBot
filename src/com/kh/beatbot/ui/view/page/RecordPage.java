package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.SampleView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;

public class RecordPage extends TrackPage {
	private SampleView sampleView;
	private Button recordButton;

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
		sampleView = new SampleView(this, null);
		sampleView.setClip(true);
		recordButton = new Button(this).withIcon(IconResourceSets.RECORD);

		recordButton.setOnPressListener(new OnPressListener() {
			@Override
			public void onPress(Button button) {
				RecordManager.startRecording();
			}
		});

		recordButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				RecordManager.stopRecording();
			}
		});
	}

	@Override
	public synchronized void layoutChildren() {
		float topBarH = height * .29f;
		float fillH = height - topBarH;
		float margin = width * .02f;
		recordButton.layout(this, 0, topBarH, fillH, fillH);
		sampleView.layout(this, fillH, topBarH, width - fillH / 2 - fillH - margin * 2, fillH);
	}
}
