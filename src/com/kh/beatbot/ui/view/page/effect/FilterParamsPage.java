package com.kh.beatbot.ui.view.page.effect;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class FilterParamsPage extends EffectParamsPage {

	private class FilterToggleListener implements OnReleaseListener {
		@Override
		public void onRelease(Button button) {
			for (int i = 0; i < filterToggles.length; i++) {
				if (button.equals(filterToggles[i])) {
					((Filter) effect).setMode(i);
					filterToggles[i].setChecked(true);
				} else {
					filterToggles[i].setChecked(false);
				}
			}
		}
	}

	private ToggleButton[] filterToggles;
	private ShapeGroup iconGroup;
	private FilterToggleListener filterToggleListener;

	@Override
	protected int getNumParams() {
		return Filter.NUM_PARAMS;
	}

	@Override
	public void draw() {
		super.draw();
		push();
		translate(-absoluteX, -absoluteY);
		iconGroup.draw((GL11) gl, 2);
		pop();
	}

	@Override
	public void createChildren() {
		super.createChildren();
		iconGroup = new ShapeGroup();
		filterToggles = new ToggleButton[3];
		filterToggleListener = new FilterToggleListener();
		for (int i = 0; i < filterToggles.length; i++) {
			filterToggles[i] = new ToggleButton();
			filterToggles[i].setOnReleaseListener(filterToggleListener);
			addChild(filterToggles[i]);
		}
	}

	@Override
	protected void loadIcons() {
		for (ToggleButton filterToggle : filterToggles) {
			filterToggle.setBgIcon(new RoundedRectIcon(iconGroup,
					Colors.instrumentBgColorSet,
					Colors.buttonRowStrokeColorSet));
		}
		filterToggles[0].setIcon(new Icon(IconResources.LOWPASS_FILTER));
		filterToggles[1].setIcon(new Icon(IconResources.BANDPASS_FILTER));
		filterToggles[2].setIcon(new Icon(IconResources.HIGHPASS_FILTER));
		filterToggles[0].setChecked(true);
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
		paramControls[0].layout(this, width / 2 - paramW - 10, paramY, paramW,
				paramH);
		paramControls[1].layout(this, width / 2 + 10, paramY, paramW, paramH);
		paramControls[2].layout(this, width / 2 - paramW - 10, paramY + paramH,
				paramW, paramH);
		paramControls[3].layout(this, width / 2 + 10, paramY + paramH, paramW,
				paramH);
	}
}