package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.global.BeatBotToggleButton;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;

public class TronKnob extends LevelListenable {
	public static final float ¹ = (float) Math.PI;
	private static FloatBuffer circleVb = null;
	private static FloatBuffer selectCircleVb = null;
	private static FloatBuffer selectCircleVb2 = null;
	private static int circleWidth = 0, circleHeight = 0;

	private static float snapDistSquared;

	private int drawIndex = 0;
	private boolean levelSelected = false;
	private BeatBotToggleButton centerButton = null;
	private boolean clickable = false;

	public TronKnob(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	@Override
	public void init() {
		super.init();
		if (clickable) {
			centerButton = new BeatBotToggleButton(GlobalVars.beatSyncIcon);
			centerButton.setOn(true);
		}
	}

	private static void initCircleVbs(float width, float height) {
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
			circleVertices[i * 4 + 1] = FloatMath.sin(theta) * height / 2.3f
					+ height / 2;
			circleVertices[i * 4 + 2] = FloatMath.cos(theta) * width / 3.1f
					+ width / 2;
			circleVertices[i * 4 + 3] = FloatMath.sin(theta) * height / 3.1f
					+ height / 2;
			// two dimmer circles are shown for a "glow" effect when the user
			// touches the view
			// this first one is slightly wider...
			selectCircleVertices[i * 4] = FloatMath.cos(theta) * width / 2.2f
					+ width / 2;
			selectCircleVertices[i * 4 + 1] = FloatMath.sin(theta) * height
					/ 2.2f + height / 2;
			selectCircleVertices[i * 4 + 2] = FloatMath.cos(theta) * width
					/ 3.2f + width / 2;
			selectCircleVertices[i * 4 + 3] = FloatMath.sin(theta) * height
					/ 3.2f + height / 2;
			// and this one is even wider... use alpha channel to produce glow
			// effect
			selectCircle2Vertices[i * 4] = FloatMath.cos(theta) * width / 2.1f
					+ width / 2;
			selectCircle2Vertices[i * 4 + 1] = FloatMath.sin(theta) * height
					/ 2.1f + height / 2;
			selectCircle2Vertices[i * 4 + 2] = FloatMath.cos(theta) * width
					/ 3.3f + width / 2;
			selectCircle2Vertices[i * 4 + 3] = FloatMath.sin(theta) * height
					/ 3.3f + height / 2;
		}
		circleVb = makeFloatBuffer(circleVertices);
		selectCircleVb = makeFloatBuffer(selectCircleVertices);
		selectCircleVb2 = makeFloatBuffer(selectCircle2Vertices);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		snapDistSquared = (width / 4) * (width / 4);
		// all knobs share the same circle VBs, and they should only change when
		// width or height changes
		if (width != circleWidth || height != circleHeight) {
			initCircleVbs(width, height);
			circleWidth = width;
			circleHeight = height;
		}
	}

	@Override
	protected void drawFrame() {
		// background
		drawTriangleStrip(circleVb, BG_COLOR);
		// main selection
		drawTriangleStrip(circleVb, levelColor, drawIndex);
		if (levelSelected) { // selected glow
			drawTriangleStrip(selectCircleVb2, selectColor, drawIndex);
			drawTriangleStrip(selectCircleVb, selectColor, drawIndex);
		}
		if (centerButton != null) {
			centerButton.draw(0, 0, width, height);
		}
	}

	private void updateDrawIndex() {
		if (circleVb == null)
			return;
		drawIndex = (int) (circleVb.capacity() * level / 2);
		drawIndex += drawIndex % 2;
	}

	@Override
	public void setViewLevel(float level) {
		super.setViewLevel(level);
		updateDrawIndex();
	}

	@Override
	public void setLevel(float level) {
		super.setLevel(level);
		updateDrawIndex();
	}

	public void setBeatSync(boolean beatSync) {
		if (centerButton != null) {
			centerButton.setOn(beatSync);
		}
	}

	public boolean isBeatSync() {
		return centerButton != null && centerButton.isOn();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		if (distanceFromCenterSquared(x, y) > snapDistSquared) {
			levelSelected = true;
			setLevel(coordToLevel(x, y));
		} else if (centerButton != null) {
			centerButton.touch();
		}
		super.handleActionDown(id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		if (!levelSelected)
			return;
		float newLevel = coordToLevel(e.getX(0), e.getY(0));
		setLevel(newLevel);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		levelSelected = false;
		if (centerButton != null) {
			if (distanceFromCenterSquared(x, y) <= snapDistSquared) {
				centerButton.toggle();
				for (LevelListener listener : levelListeners) {
					listener.notifyClicked(this);
				}
			}
			centerButton.release();
		}
		super.handleActionUp(id, x, y);
	}

	public boolean isClickable() {
		return centerButton != null;
	}

	private float coordToLevel(float x, float y) {
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
}
