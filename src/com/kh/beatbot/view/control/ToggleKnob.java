package com.kh.beatbot.view.control;

import com.kh.beatbot.R;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.ToggleButton;

public class ToggleKnob extends Knob {

	private ToggleButton centerButton;
	private float snapDistSquared;
	private boolean centerButtonTouched = false;

	@Override
	public void setId(int id) {
		super.setId(id);
		centerButton.setId(id);
	}
	
	public void setBeatSync(boolean beatSync) {
		centerButton.setChecked(beatSync);
	}

	public boolean isBeatSync() {
		return centerButton.isChecked();
	}

	@Override
	public void draw() {
		super.draw();
		centerButton.draw();
	}

	protected void loadIcons() {
		super.loadIcons();
		centerButton.setIconSource(new ImageIconSource(R.drawable.clock, R.drawable.note_icon));
		centerButton.setChecked(true);
	}

	@Override
	protected void createChildren() {
		super.createChildren();
		centerButton = new ToggleButton();
		centerButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				for (Level1dListener listener : levelListeners) {
					((BBOnClickListener)listener).onClick(centerButton);
				}
			}
		});
		// not adding center button as child, instead manually drawing and handling touch events
	}
	
	@Override
	public void layoutChildren() {
		super.layoutChildren();
		centerButton.layout(this, 0, 0, width, height);
		snapDistSquared = (width / 3) * (width / 3);
	}
	
	@Override
	public void handleActionDown(int id, float x, float y) {
		if (distanceFromCenterSquared(x, y) < snapDistSquared) {
			centerButton.handleActionDown(id, x, y);
			centerButtonTouched = true;
		} else {
			super.handleActionDown(id, x, y);
		}
	}
	
	@Override
	public void handleActionMove(int id, float x, float y) {
		if (centerButtonTouched) {
			centerButton.handleActionMove(id, x, y);
		} else {
			super.handleActionMove(id, x, y);
		}
	}
	
	@Override
	public void handleActionUp(int id, float x, float y) {
		if (centerButtonTouched) {
			centerButton.handleActionUp(id, x, y);
		} else {
			super.handleActionUp(id, x, y);
		}
		centerButtonTouched = false;
	}
}
