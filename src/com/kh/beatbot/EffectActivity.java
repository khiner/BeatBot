package com.kh.beatbot;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.listener.Level2dListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public abstract class EffectActivity extends Activity implements LevelListener, Level2dListener {
	
	public class SampleRowAdapter extends
			ArrayAdapter<String> {
		int resourceId;
		int count = 0;
		
		public SampleRowAdapter(EffectActivity context,
				int resourceId, String[] params) {
			super(context, resourceId, params);
			this.resourceId = resourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// big hack here.  all the views are retrieved multiple times, and the
			// level bars are getting overwritten, resulting in invisible bars behind visible ones
			if (count++ < ((EffectActivity)getContext()).getNumParams()) return getLayoutInflater().inflate(resourceId, parent, false);
			View view = getLayoutInflater().inflate(resourceId, parent, false);
			TextView label = (TextView) view.findViewById(R.id.param_label);
			TronSeekbar levelBar = (TronSeekbar) view.findViewById(R.id.param_bar);
			levelBar.setTag(position);
			levelBar.addLevelListener((EffectActivity)getContext());
			if (!paramBars.containsKey(position))
				paramBars.put(position, levelBar);
			
			label.setText(getParamLabel(position));
			return view;
		}
	}
	
	protected int trackNum;
	protected TronSeekbar2d level2d = null;
	protected SampleRowAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		trackNum = getIntent().getExtras().getInt("trackNum");
		adapter = new SampleRowAdapter(
				this, R.layout.param_row, new String[getNumParams()]);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public abstract float getXValue();
	public abstract float getYValue();
	public abstract void setXValue(float x);
	public abstract void setYValue(float y);
	public abstract int getNumParams();
	public abstract String getParamLabel(int paramNum);
	
	public abstract void setEffectOn(boolean on);
	
	public abstract void setEffectDynamic(boolean dynamic);
	public Map<Integer, TronSeekbar> paramBars = new HashMap<Integer, TronSeekbar>();
	public void toggleEffect(View view) {
		setEffectOn(((ToggleButton)view).isChecked());
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {
		if (levelBar.getTag().equals(0)) {
			level2d.setViewLevelX(level);
			setXValue(level);
		} else if (levelBar.getTag().equals(1)) {
			level2d.setViewLevelY(level);
			setYValue(level);
		}
	}

	@Override
	public void setLevel(TronSeekbar2d level2d, float levelX, float levelY) {
		paramBars.get(0).setViewLevel(levelX);
		paramBars.get(1).setViewLevel(levelY);
		setXValue(levelX);
		setYValue(levelY);
	}
	
	@Override
	public void notifyChecked(TronSeekbar levelBar, boolean checked) {
		// do nothing
	}
	
	@Override
	public void notifyChecked(TronSeekbar2d level2d, boolean checked) {
		setEffectDynamic(checked);
	}
	
	@Override
	public void notifyInit(TronSeekbar levelBar) {
		if (levelBar.getTag().equals(0))
			levelBar.setViewLevel(getXValue());
		else if (levelBar.getTag().equals(1))
			levelBar.setViewLevel(getYValue());
	}
	
	@Override
	public void notifyInit(TronSeekbar2d level2d) {
		level2d.setViewLevelX(getXValue());
		level2d.setViewLevelY(getYValue());
	}
}
