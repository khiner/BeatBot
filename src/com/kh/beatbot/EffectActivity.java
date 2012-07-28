package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public abstract class EffectActivity extends Activity implements LevelListener {

	public class SampleRowAdapter extends ArrayAdapter<String> {
		int resourceId;
		int count = 0;

		public SampleRowAdapter(EffectActivity context, int resourceId,
				String[] params) {
			super(context, resourceId, params);
			this.resourceId = resourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// big hack here. all the views are retrieved multiple times, and
			// the
			// level bars are getting overwritten, resulting in invisible bars
			// behind visible ones
			if (count++ < ((EffectActivity) getContext()).getNumParams())
				return getLayoutInflater().inflate(resourceId, parent, false);
			View view = getLayoutInflater().inflate(resourceId, parent, false);
			TextView label = (TextView) view.findViewById(R.id.param_label);
			TronSeekbar levelBar = (TronSeekbar) view
					.findViewById(R.id.param_bar);
			levelBar.setTag(position);
			levelBar.addLevelListener((EffectActivity) getContext());
			if (paramBars.get(position) == null)
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
		adapter = new SampleRowAdapter(this, R.layout.param_row,
				new String[getNumParams()]);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public abstract float getXValue();

	public abstract float getYValue();

	public abstract int getNumParams();

	public abstract String getParamLabel(int paramNum);

	public abstract void setXValue(float x);

	public abstract void setYValue(float y);

	public abstract void setEffectOn(boolean on);

	public SparseArray<TronSeekbar> paramBars = new SparseArray<TronSeekbar>();

	public void toggleEffect(View view) {
		setEffectOn(((ToggleButton) view).isChecked());
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		if (levelBar.getTag().equals(0)) {
			level2d.setViewLevelX(level);
			setXValue(level);
		} else if (levelBar.getTag().equals(1)) {
			level2d.setViewLevelY(level);
			setYValue(level);
		}
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		paramBars.get(0).setViewLevel(levelX);
		paramBars.get(1).setViewLevel(levelY);
		setXValue(levelX);
		setYValue(levelY);
	}

	@Override
	public void notifyChecked(LevelListenable levelBar, boolean checked) {
		// do nothing
	}

	@Override
	public void notifyInit(LevelListenable listenable) {
		if (listenable instanceof TronSeekbar) {
			if (listenable.getTag().equals(0))
				listenable.setViewLevel(getXValue());
			else if (listenable.getTag().equals(1))
				listenable.setViewLevel(getYValue());
		} else if (listenable instanceof TronSeekbar2d) {
			level2d.setViewLevelX(getXValue());
			level2d.setViewLevelY(getYValue());
		}
	}

	protected float scaleLevel(float level) {
		return (float) (Math.pow(9, level) - 1) / 8;
	}

	protected float quantizeToBeat(float level) {
		// minimum beat == 1/16 note = 1(1 << 4)
		for (int pow = 4; pow >= 0; pow--) {
			float quantized = 1f / (1 << pow);
			if (level <= quantized) {
				return quantized;
			}
		}
		return 1;
	}
}
