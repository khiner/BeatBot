package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PageManager;
import com.kh.beatbot.manager.TrackManager;

public class MainPageSelect extends PageSelect {
	private TextView sampleSelect;
	private ImageView instrumentSelect;
	
	public MainPageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		fixedWidthIndices = new int[] {0, 3}; // + icon and track icon positions
	}

	public void init() {
		super.init();
		sampleSelect = (TextView) findViewById(R.id.sampleSelect);
		sampleSelect.setTypeface(GlobalVars.font);
		sampleSelect.setGravity(Gravity.CENTER);
		instrumentSelect = (ImageView) findViewById(R.id.instrumentSelect);
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
	
	public void update() {
		updateInstrumentIcon();
		updateSampleText();
	}
	
	public void selectPage(final int pageNum) {
		PageManager.selectPage(pageNum);
	}
	
	private void updateInstrumentIcon() {
		// update the track pager instrument icon
		if (TrackManager.currTrack.getInstrument().getBBIconSource().defaultIcon == null) {
			return;
		}
		instrumentSelect
				.setImageResource(TrackManager.currTrack.getInstrument().getBBIconSource().defaultIcon.resourceId);
	}

	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = TrackManager.currTrack.getSampleName().replace(".raw", "")
				.toUpperCase();
		sampleSelect.setText(formattedName);
	}
}
