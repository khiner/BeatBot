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

public class TrackPageSelect extends PageSelect {
	private TextView sampleSelect;
	private ImageView instrumentSelect;
	
	public TrackPageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		firstPageLabelIndex = 2;
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
	
	public void selectPage(final int pageNum) {
		PageManager.selectTrackPage(pageNum);
	}
	
	protected void update() {
		updateInstrumentIcon();
		updateSampleText();
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
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		onLayout(changed, l, t, r, b, 1);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		onMeasure(widthMeasureSpec, heightMeasureSpec, 1);
	}
}
