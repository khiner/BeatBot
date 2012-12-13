package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.view.SampleWaveformView;

public class SampleEditPage extends TrackPage {
	private SampleWaveformView sampleWaveformView;
	private ToggleButton adsrButton; 
	private ToggleButton loopButton; 
	private ToggleButton reverseButton; 
	
	public SampleEditPage(Context context) {
		super(context);
	}

	@Override
	public void inflate(Context context) {
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.track_sample_edit, this);

        sampleWaveformView = (SampleWaveformView) view.findViewById(R.id.sample_waveform_view);
        adsrButton = (ToggleButton) view.findViewById(R.id.adsr);
        loopButton = (ToggleButton) view.findViewById(R.id.loop);
        reverseButton = (ToggleButton) view.findViewById(R.id.reverse);
        
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
	protected void trackUpdated() {
		sampleWaveformView.setTrack(track);
		adsrButton.setChecked(track.isAdsrEnabled());
		loopButton.setChecked(track.isLooping());
		reverseButton.setChecked(track.isReverse());
	}
}
