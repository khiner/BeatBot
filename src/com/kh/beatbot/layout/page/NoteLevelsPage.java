package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.R;
import com.kh.beatbot.view.LevelsView;

public class NoteLevelsPage extends Page {

	LevelsView levelsView;
	public NoteLevelsPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		levelsView = (LevelsView)findViewById(R.id.levelsView);
	}

	@Override
	public void update() {
	}

	@Override
	public void setVisibilityCode(int code) {
		levelsView.setVisibility(code);
	}

}
