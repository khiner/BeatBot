package com.kh.beatbot.layout.page.effect;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.TextButton;
import com.kh.beatbot.view.mesh.ShapeGroup;


public class FilterParamsPage extends EffectParamsPage {

	private TextButton[] filterToggles;
	private ShapeGroup iconGroup;
	
	@Override
	protected int getNumParams() {
		return Filter.NUM_PARAMS;
	}

	@Override
	public void draw() {
		super.draw();
		push();
		translate(-absoluteX, -absoluteY);
		iconGroup.draw((GL11)gl, 2);
		pop();
	}
	
	@Override
	public void createChildren() {
		super.createChildren();
		iconGroup = new ShapeGroup();
		filterToggles = new TextButton[3];
		for (int i = 0; i < filterToggles.length; i++) {
			filterToggles[i] = new TextButton(iconGroup, Colors.instrumentBgColorSet, Colors.instrumentStrokeColorSet);
			filterToggles[i].setOnClickListener(this);
			addChild(filterToggles[i]);
		}
	}

	@Override
	protected void loadIcons() {
		filterToggles[0].setForegroundIconSource(new ImageIconSource(R.drawable.lowpass_filter_icon));
		filterToggles[1].setForegroundIconSource(new ImageIconSource(R.drawable.bandpass_filter_icon));
		filterToggles[2].setForegroundIconSource(new ImageIconSource(R.drawable.highpass_filter_icon));
		filterToggles[0].setChecked(true);
	}
	
	public void onClick(Button button) {
		for (int i = 0; i < filterToggles.length; i++) {
			if (button.equals(filterToggles[i])) {
				((Filter) effect).setMode(i);
				filterToggles[i].setChecked(true);
			} else
				filterToggles[i].setChecked(false);
		}
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Filter.PARAMS_DATA;
	}
	
	public void layoutChildren() {
		float toggleY = 5;
		float toggleW = width / 6;
		float paramH = (height - toggleY - toggleW) / 2 - 10;
		float paramW = 2 * paramH / 3;
		float x = width / 2 - toggleW * 2;
		for (Button toggle : filterToggles) {
			toggle.layout(this, x, toggleY, toggleW, toggleW);
			x += 3 * toggleW / 2;
		}
		
		float paramY = toggleY + toggleW;
		paramControls[0].layout(this, width / 2 - paramW - 10, paramY, paramW, paramH);
		paramControls[1].layout(this, width / 2 + 10, paramY, paramW, paramH);
		paramControls[2].layout(this, width / 2 - paramW - 10, paramY + paramH, paramW, paramH);
		paramControls[3].layout(this, width / 2 + 10, paramY + paramH, paramW, paramH);
	}
}
