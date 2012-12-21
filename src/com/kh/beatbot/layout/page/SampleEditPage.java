package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.view.SampleWaveformView;

public class SampleEditPage extends TrackPage {
	private SampleWaveformView sampleWaveformView;
	private ToggleButton adsrButton; 
	private ToggleButton loopButton; 
	private ToggleButton reverseButton; 
	
	public SampleEditPage(Context context, View layout) {
		super(context, layout);
        sampleWaveformView = (SampleWaveformView) layout.findViewById(R.id.sample_waveform_view);
        adsrButton = (ToggleButton) layout.findViewById(R.id.adsr);
        loopButton = (ToggleButton) layout.findViewById(R.id.loop);
        reverseButton = (ToggleButton) layout.findViewById(R.id.reverse);
        adsrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	track.setAdsrOn(adsrButton.isChecked());
            }
        });
        loopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	track.toggleLooping();
            }
        });
        reverseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	track.setReverse(reverseButton.isChecked());
            }
        });
	}

	@Override
	protected void update() {
		sampleWaveformView.setTrack(track);
		adsrButton.setChecked(track.isAdsrEnabled());
		loopButton.setChecked(track.isLooping());
		reverseButton.setChecked(track.isReverse());
	}
	
	@Override
	public void setVisibilityCode(int code) {
		sampleWaveformView.setVisibility(code);
	}
}
