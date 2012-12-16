package com.kh.beatbot.layout.page;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class SampleSelectPage extends TrackPage {
	private Button sampleSelectButton;
	private ImageButton instrumentSelectButton;
	
	private AlertDialog instrumentSelectAlert, sampleSelectAlert;
	private AlertDialog.Builder instrumentSelectAlertBuilder, sampleSelectAlertBuilder;
	
	public SampleSelectPage(Context context, View layout) {
		super(context, layout);
        sampleSelectButton = (Button) layout.findViewById(R.id.sampleSelect);
        instrumentSelectButton = (ImageButton) layout.findViewById(R.id.instrumentSelect);
        sampleSelectButton.setTypeface(GlobalVars.font);
        instrumentSelectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
        		instrumentSelectAlert.show();
            }
        });
        sampleSelectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	sampleSelectAlert.show();
            }
        });
        
        initBuilders(context);
	}
	
	public void inflate(Context context) {
	}
	
	protected void trackUpdated() {
		instrumentSelectButton.setBackgroundResource(track.getInstrument().getIconSource());
        sampleSelectButton.setText(track.getInstrument().getCurrSampleName());
        updateInstrumentSelectAlert();
		updateSampleSelectAlert();
	}
	
	@Override
	public void setVisibilityCode(int code) {
		// nothing to do
	}
	
	private void setInstrument(Instrument instrument) {
		// set native sample bytes through JNI
		track.setSample(instrument.getCurrSamplePath());
		// update sample label text
		sampleSelectButton.setText(instrument.getCurrSampleName());
		// update the midi view instrument icon for this track
		MidiTrackControlHelper.updateInstrumentIcon(track.getId());
	}
	
	private void updateInstrumentSelectAlert() {
		instrumentSelectAlertBuilder.setAdapter(GlobalVars.instrumentSelectAdapter,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						track.setInstrument(GlobalVars.getInstrument(item));
						track.getInstrument().setCurrSampleNum(0);
						track.getInstrument().getBBIconSource();
						setInstrument(track.getInstrument());
						// update instrument icon to reflect the change
						instrumentSelectButton.setBackgroundResource(track
										.getInstrument().getIconSource());
						// update the sample select alert names with the new
						// instrument samples
						updateSampleSelectAlert();
					}
				});
		instrumentSelectAlert = instrumentSelectAlertBuilder.create();
		instrumentSelectAlert
				.setOnShowListener(GlobalVars.instrumentSelectOnShowListener);
	}

	private void updateSampleSelectAlert() {
		sampleSelectAlertBuilder.setItems(track.getInstrument().getSampleNames(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						track.getInstrument().setCurrSampleNum(item);
						setInstrument(track.getInstrument());
					}
				});
		sampleSelectAlert = sampleSelectAlertBuilder.create();
	}
	
	private void initBuilders(Context context) {
		instrumentSelectAlertBuilder = new AlertDialog.Builder(context);
		instrumentSelectAlertBuilder.setTitle("Choose Instrument");
		sampleSelectAlertBuilder = new AlertDialog.Builder(context);
		sampleSelectAlertBuilder.setTitle("Choose Sample");
	}
}
