package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.SampleWaveformView;

public class SampleEditPage extends Page {
	
	public SampleEditPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private SampleWaveformView sampleWaveformView;
	private ToggleButton adsrButton;
	private ToggleButton loopButton;
	private ToggleButton reverseButton;


	public void init() {
		sampleWaveformView = (SampleWaveformView) findViewById(R.id.sample_waveform_view);
		adsrButton = (ToggleButton) findViewById(R.id.adsr);
		loopButton = (ToggleButton) findViewById(R.id.loop);
		reverseButton = (ToggleButton) findViewById(R.id.reverse);
		adsrButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				GlobalVars.currTrack.setAdsrOn(adsrButton.isChecked());
			}
		});
		loopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				GlobalVars.currTrack.toggleLooping();
			}
		});
		reverseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				GlobalVars.currTrack.setReverse(reverseButton.isChecked());
			}
		});
	}
	
	@Override
	public void update() {
		sampleWaveformView.update();
		adsrButton.setChecked(GlobalVars.currTrack.isAdsrEnabled());
		loopButton.setChecked(GlobalVars.currTrack.isLooping());
		reverseButton.setChecked(GlobalVars.currTrack.isReverse());
	}

	@Override
	public void setVisibilityCode(int code) {
		sampleWaveformView.setVisibility(code);
	}
}
