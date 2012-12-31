package com.kh.beatbot.layout.page;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class SampleSelectPage extends TrackPage {
	private TextView sampleSelect;
	private ImageView instrumentSelect;

	public SampleSelectPage(Context context, View layout) {
		super(context, layout);
		sampleSelect = (TextView) layout.findViewById(R.id.sampleSelect);
		instrumentSelect = (ImageView) layout
				.findViewById(R.id.instrumentSelect);
		sampleSelect.setTypeface(GlobalVars.font);
		instrumentSelect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Managers.directoryManager.showInstrumentSelectAlert();
			}
		});
		sampleSelect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Managers.directoryManager.showSampleSelectAlert();
			}
		});
	}

	public void inflate(Context context) {
	}

	protected void update() {
		updateInstrumentIcon();
		updateSampleText();
	}

	@Override
	public void setVisibilityCode(int code) {
		// nothing to do
	}

	private void updateInstrumentIcon() {
		// update the track pager instrument icon
		if (track.getInstrument().getBBIconSource().defaultIcon == null) {
			return;
		}
		instrumentSelect
				.setImageResource(track.getInstrument().getBBIconSource().defaultIcon.resourceId);
	}

	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = track.getSampleName().replace(".raw", "")
				.toUpperCase();
		sampleSelect.setText(formattedName);
	}
}
