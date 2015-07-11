package com.kh.beatbot.ui.view.page.track;

import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.BpmView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;

public class TempoPage extends TrackPage {
	private Button tapTempoButton;
	private BpmView bpmView;
	private long lastTapTime = 0;
	private final int ONE_MINUTE_MILLIS = 60000;

	public TempoPage(View view) {
		super(view);
	}

	@Override
	protected synchronized void createChildren() {
		tapTempoButton = new Button(this).withRoundedRect().withIcon(
				IconResourceSets.TAP_TEMPO_BUTTON);
		bpmView = new BpmView(this);

		tapTempoButton.setText("TAP");

		tapTempoButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				long tapTime = System.currentTimeMillis();
				float elapsedMillis = tapTime - lastTapTime;
				float bpm = ONE_MINUTE_MILLIS / elapsedMillis; // TODO average across last 3/4 taps
				if (bpm <= MidiManager.MAX_BPM + 20 && bpm >= MidiManager.MIN_BPM - 20) {
					// if we are far outside of the range, don't change the tempo.
					// otherwise, midiManager will take care of clipping the result
					MidiManager.setBPM(bpm);
				}
				lastTapTime = tapTime;
			}
		});
	}

	@Override
	public synchronized void layoutChildren() {
		float bpmHeight = 3 * height / 4;
		float bpmWidth = bpmHeight * 2;
		float tapDim = 11 * height / 16;
		float allChildrenWidth = tapDim + bpmWidth + BG_OFFSET * 2;
		tapTempoButton.layout(this, (width - allChildrenWidth) / 2, (height - tapDim) / 2, tapDim,
				tapDim);
		bpmView.layout(this, (width - allChildrenWidth) / 2 + tapDim + BG_OFFSET, height / 8,
				bpmWidth, bpmHeight);
	}
}
