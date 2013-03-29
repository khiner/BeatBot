package com.kh.beatbot.view.control;

import java.nio.FloatBuffer;

import android.util.FloatMath;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.listener.KnobListener;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.ToggleButton;
import com.kh.beatbot.view.TouchableSurfaceView;

public class Knob extends ControlView1dBase implements BBOnClickListener {

	private FloatBuffer circleVb;
	private FloatBuffer selectCircleVb;
	private FloatBuffer selectCircleVb2;

	private ToggleButton centerButton;
	private float snapDistSquared;

	private int drawIndex = 0;

	private boolean clickable = false;
	
	public Knob(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	protected void loadIcons() {
		centerButton.setIconSource(new BBIconSource(R.drawable.clock, R.drawable.note_icon));
	}
	
	private void initCircleVbs(float width, float height) {
		float[] circleVertices = new float[128];
		float[] selectCircleVertices = new float[128];
		float[] selectCircle2Vertices = new float[128];
		float theta = 3 * ¹ / 4; // start at 1/8 around the circle
		for (int i = 0; i < circleVertices.length / 4; i++) {
			// theta will range from ¹/4 to 7¹/8,
			// with the ¹/8 gap at the "bottom" of the view
			theta += 6 * ¹ / circleVertices.length;
			// main circles will show when user is not touching
			circleVertices[i * 4] = FloatMath.cos(theta) * width / 2.3f + width
					/ 2;
			circleVertices[i * 4 + 1] = FloatMath.sin(theta) * width / 2.3f
					+ width / 2;
			circleVertices[i * 4 + 2] = FloatMath.cos(theta) * width / 3.1f
					+ width / 2;
			circleVertices[i * 4 + 3] = FloatMath.sin(theta) * width / 3.1f
					+ width / 2;
			// two dimmer circles are shown for a "glow" effect when the user
			// touches the view
			// this first one is slightly wider...
			selectCircleVertices[i * 4] = FloatMath.cos(theta) * width / 2.2f
					+ width / 2;
			selectCircleVertices[i * 4 + 1] = FloatMath.sin(theta) * width
					/ 2.2f + width / 2;
			selectCircleVertices[i * 4 + 2] = FloatMath.cos(theta) * width
					/ 3.2f + width / 2;
			selectCircleVertices[i * 4 + 3] = FloatMath.sin(theta) * width
					/ 3.2f + width / 2;
			// and this one is even wider... use alpha channel to produce glow
			// effect
			selectCircle2Vertices[i * 4] = FloatMath.cos(theta) * width / 2.1f
					+ width / 2;
			selectCircle2Vertices[i * 4 + 1] = FloatMath.sin(theta) * width
					/ 2.1f + width / 2;
			selectCircle2Vertices[i * 4 + 2] = FloatMath.cos(theta) * width
					/ 3.3f + width / 2;
			selectCircle2Vertices[i * 4 + 3] = FloatMath.sin(theta) * width
					/ 3.3f + width / 2;
		}
		circleVb = makeFloatBuffer(circleVertices);
		selectCircleVb = makeFloatBuffer(selectCircleVertices);
		selectCircleVb2 = makeFloatBuffer(selectCircle2Vertices);
	}

	@Override
	public void draw() {
		// level background
		drawTriangleStrip(circleVb, Colors.VIEW_BG);
		// main selection
		drawTriangleStrip(circleVb, levelColor, drawIndex);
		if (selected) { // selected glow
			drawTriangleStrip(selectCircleVb2, selectColor, drawIndex);
			drawTriangleStrip(selectCircleVb, selectColor, drawIndex);
		}
		if (clickable) {
			centerButton.draw();
		}
	}

	private void updateDrawIndex() {
		if (circleVb == null)
			return;
		drawIndex = (int) (circleVb.capacity() * level / 2);
		drawIndex += drawIndex % 2;
	}

	public void setBeatSync(boolean beatSync) {
		if (centerButton != null) {
			centerButton.setChecked(beatSync);
		}
	}

	public boolean isBeatSync() {
		return centerButton != null && centerButton.isChecked();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		if (distanceFromCenterSquared(x, y) > snapDistSquared) {
			super.handleActionDown(id, x, y);
		}
	}

	public boolean isClickable() {
		return centerButton != null;
	}

	@Override
	public void setViewLevel(float level) {
		super.setViewLevel(level);
		updateDrawIndex();
	}
	
	@Override
	protected float posToLevel(float x, float y) {
		float unitX = (x - width / 2) / width;
		float unitY = (y - height / 2) / height;
		float theta = (float) Math.atan(unitY / unitX) + ¹ / 2;
		// atan ranges from 0 to ¹, and produces symmetric results around the y
		// axis.
		// we need 0 to 2*¹, so ad ¹ if right of x axis.
		if (unitX > 0)
			theta += ¹;
		// convert to level - remember, min theta is ¹/4, max is 7¹/8
		float level = (4 * theta / ¹ - 1) / 6;
		return level > 0 ? (level < 1 ? level : 1) : 0;
	}

	@Override
	protected void createChildren() {
		centerButton = new ToggleButton((TouchableSurfaceView)root);
		centerButton.setOnClickListener(this);
	}
	
	@Override
	public void layoutChildren() {
		centerButton.layout(this, 0, 0, width, height);
		snapDistSquared = (width / 4) * (width / 4);
		initCircleVbs(width, height);
		if (clickable) {
			centerButton.setChecked(true);
		}
	}
	
	@Override
	public void onClick(Button button) {
		if (distanceFromCenterSquared(x, y) <= snapDistSquared) {
			for (Level1dListener listener : levelListeners) {
				((KnobListener)listener).onClick(this);
			}
		}
	}
}
