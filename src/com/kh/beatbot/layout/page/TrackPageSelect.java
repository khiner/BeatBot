package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PageManager;

public class TrackPageSelect extends LinearLayout {
	private TextView sampleSelect;
	private ImageView instrumentSelect;

	public TrackPageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		sampleSelect = (TextView) findViewById(R.id.sampleSelect);
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
		for (int i = 1; i < getChildCount(); i++) {
			TextView pageText = null;
			// TODO clean up
			if (i == 1) // view 1 is a linear layout containing a sample select
						// text view
				pageText = (TextView) ((LinearLayout) getChildAt(1))
						.getChildAt(0);
			else
				pageText = (TextView) getChildAt(i);
			pageText.setTypeface(GlobalVars.font);
			pageText.setGravity(Gravity.CENTER);
			if (i <= 1) {
				continue;
			}
			final int pageNum = i - 2;
			pageText.setTag(pageNum);
			pageText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int j = 2; j < getChildCount(); j++) {
						TextView pageText = (TextView) getChildAt(j);
						pageText.setSelected(pageNum + 2 == j);
					}
					PageManager.selectTrackPage((Integer)v.getTag());
				}
			});
		}
	}
	
	protected void update() {
		updateInstrumentIcon();
		updateSampleText();
	}

	private void updateInstrumentIcon() {
		// update the track pager instrument icon
		if (GlobalVars.currTrack.getInstrument().getBBIconSource().defaultIcon == null) {
			return;
		}
		instrumentSelect
				.setImageResource(GlobalVars.currTrack.getInstrument().getBBIconSource().defaultIcon.resourceId);
	}

	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = GlobalVars.currTrack.getSampleName().replace(".raw", "")
				.toUpperCase();
		sampleSelect.setText(formattedName);
	}
}
