package com.kh.beatbot.layout.page;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class SampleSelectPage extends TrackPage {
	private TextView sampleSelect;
	private ImageView instrumentSelect;
	
	private AlertDialog instrumentSelectAlert, sampleSelectAlert;
	private AlertDialog.Builder instrumentSelectAlertBuilder, sampleSelectAlertBuilder;
	
	public SampleSelectPage(Context context, View layout) {
		super(context, layout);
        sampleSelect = (TextView) layout.findViewById(R.id.sampleSelect);
        instrumentSelect = (ImageView) layout.findViewById(R.id.instrumentSelect);
        sampleSelect.setTypeface(GlobalVars.font);
        instrumentSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
        		instrumentSelectAlert.show();
            }
        });
        sampleSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	sampleSelectAlert.show();
            }
        });
        
        initBuilders(context);
	}
	
	public void inflate(Context context) {
	}
	
	protected void trackUpdated() {
		instrumentSelect.setBackgroundResource(track.getInstrument().getIconSource());
        updateSampleText();
        updateInstrumentSelectAlert();
		updateSampleSelectAlert();
	}
	
	@Override
	public void setVisibilityCode(int code) {
		// nothing to do
	}
	
	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = track.getInstrument().getCurrSampleName().replace(".raw", "").toUpperCase(); 
		sampleSelect.setText(formattedName);
	}
	
	private void setInstrument(Instrument instrument) {
		// set native sample bytes through JNI
		track.setSample(instrument.getCurrSamplePath());
		updateSampleText();
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
						instrumentSelect.setBackgroundResource(track
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
