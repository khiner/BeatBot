package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.SampleEditActivity;
import com.kh.beatbot.view.bean.MidiViewBean;

public class EditLevelsView extends SurfaceViewBase {

	private int trackNum;
	private long animateCount = 0;
	private FloatBuffer levelsVB = null;
	private int[] selected = { -1, -1, -1 };
	private final float SNAP_DIST_X = 35;
	private final float SNAP_DIST_Y = 25;	

	private SampleEditActivity activity;
	
	public EditLevelsView(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setTrackNum(int trackNum) {
		this.trackNum = trackNum;
	}

	public void setActivity(SampleEditActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected void init() {
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		initLevelsVB();
	}

	private void initLevelsVB() {
		float[] levelVertices = { 0, height / 6f,
				getPrimaryVolume(trackNum) * width,
				height / 6f, // volume
				0, .5f * height, getPrimaryPan(trackNum) * width,
				.5f * height, // pan
				0, 5f * height / 6f, getPrimaryPitch(trackNum) * width,
				5f * height / 6f }; // pitch

		levelsVB = makeFloatBuffer(levelVertices);
	}

	private void drawLevels() {
		gl.glLineWidth(2f); // 2 pixels wide
		// draw each line (2*blurWidth) times, translating and changing the
		// alpha channel
		// for each line, to achieve a DIY "blur" effect
		// this blur is animated to get wider and narrower for a "pulse" effect
		float blurWidth = ((animateCount / 150)) % 2 == 0 ? (animateCount / 30f) % 5
				: 5 - (animateCount / 30f) % 5;
		blurWidth += 15;

		for (int levelNum = 0; levelNum < 3; levelNum++) {
			for (float i = -blurWidth; i < blurWidth; i++) {
				float alpha = 1 - Math.abs(i) / (float) blurWidth;
				if (selected[levelNum] != -1) {
					gl.glColor4f(MidiViewBean.SELECTED_LEVEL_R,
							MidiViewBean.SELECTED_LEVEL_G,
							MidiViewBean.SELECTED_LEVEL_B, alpha);
				} else {
					if (levelNum == 0)
						gl.glColor4f(MidiViewBean.VOLUME_R,
								MidiViewBean.VOLUME_G, MidiViewBean.VOLUME_B,
								alpha);
					else if (levelNum == 1)
						gl.glColor4f(MidiViewBean.PAN_R, MidiViewBean.PAN_G,
								MidiViewBean.PAN_B, alpha);
					else if (levelNum == 2)
						gl.glColor4f(MidiViewBean.PITCH_R,
								MidiViewBean.PITCH_G, MidiViewBean.PITCH_B,
								alpha);
				}
				// calculate color. selected bars are always red,
				// non-selected bars depend on the LevelMode type
				gl.glTranslatef(0, i, 0);
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, levelsVB);
				gl.glDrawArrays(GL10.GL_LINES, levelNum * 2, 2);
				gl.glTranslatef(0, -i, 0);
				// draw circles (big points) at top of level bars
				if (i < 1)
					continue;
				gl.glPointSize(i * 3.5f);
				gl.glDrawArrays(GL10.GL_POINTS, levelNum * 2 + 1, 1);
			}
		}
		animateCount++;
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(.2f, .2f, .2f, 1);
		drawLevels();
	}

	private float xToLevel(float x) {
		return x / width;
	}

	private float levelToX(float level) {
		return level * width;
	}

	private float getLevel(int levelNum) {
		switch (levelNum) {
		case 0:
			return getPrimaryVolume(trackNum);
		case 1:
			return getPrimaryPan(trackNum);
		case 2:
			return getPrimaryPitch(trackNum);
		default:
			return -1;
		}
	}

	private void setLevel(int levelNum, float level) {
		if (level < 0) level = 0;
		else if (level > 1) level = 1;
		switch (levelNum) {
		case 0:
			setPrimaryVolume(trackNum, level);
			break;
		case 1:
			setPrimaryPan(trackNum, level);
			break;
		case 2:
			setPrimaryPitch(trackNum, level);
			break;
		}
	}

	private void setViewChecked(int levelNum, boolean checked) {
		switch (levelNum) {
		case 0:
			((ToggleButton)activity.findViewById(R.id.volumeView)).setChecked(checked);
			break;
		case 1:
			((ToggleButton)activity.findViewById(R.id.panView)).setChecked(checked);
			break;
		case 2:
			((ToggleButton)activity.findViewById(R.id.pitchView)).setChecked(checked);
			break;
		}
	}
	
	private void handleActionDown(int id, float x, float y) {
		for (int levelNum = 0; levelNum < 3; levelNum++) {
			if (Math.abs(y - (levelNum + 1) * 2 * height / 6f) < SNAP_DIST_Y
					&& Math.abs(x - levelToX(getLevel(levelNum))) < SNAP_DIST_X) {
				selected[levelNum] = id;
				setViewChecked(levelNum, true);
				return;
			}
		}
	}

	private void handleActionMove(int id, float x) {
		for (int levelNum = 0; levelNum < 3; levelNum++) {
			if (selected[levelNum] == id) {
				setLevel(levelNum, xToLevel(x));
			}
			initLevelsVB(); // update display, since one changed
		}
	}

	private void handleActionUp(int id) {
		for (int levelNum = 0; levelNum < 3; levelNum++) {
			if (selected[levelNum] == id) {
				selected[levelNum] = -1;
				setViewChecked(levelNum, false);				
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_DOWN:
			handleActionDown(e.getPointerId(0), e.getX(0), e.getY(0));
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionDown(e.getPointerId(index), e.getX(index),
					e.getY(index));
			break;
		case MotionEvent.ACTION_MOVE:
			for (int i = 0; i < e.getPointerCount(); i++)
				handleActionMove(e.getPointerId(i), e.getX(i));
			break;
		case MotionEvent.ACTION_POINTER_UP:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionUp(e.getPointerId(index));
			break;
		case MotionEvent.ACTION_UP:
			handleActionUp(e.getPointerId(0));
			break;
		}
		return true;
	}

	public native float getPrimaryVolume(int trackNum);

	public native float getPrimaryPan(int trackNum);

	public native float getPrimaryPitch(int trackNum);

	public native void setPrimaryVolume(int trackNum, float volume);

	public native void setPrimaryPan(int trackNum, float pan);

	public native void setPrimaryPitch(int trackNum, float pitch);
}
