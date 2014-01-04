package com.kh.beatbot.ui.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.NumberSegment;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class BpmView extends ClickableView {

	private static final float INC_BPM_THRESH = 15;
	private static NumberSegment[][] numberSegments = new NumberSegment[3][7];

	private static ShapeGroup shapeGroup = new ShapeGroup();

	private static float lastFrameY = -1, currYDragTotal = 0;

	private long lastTapTime = 0;

	public void setBPM(float bpm) {
		setText(String.valueOf((int) MidiManager.setBPM(bpm)));
	}

	@Override
	public synchronized void createChildren() {
		for (int i = 0; i < numberSegments.length; i++) {
			for (int j = 0; j < numberSegments[i].length; j++) {
				numberSegments[i][j] = new NumberSegment(shapeGroup, Colors.BPM_OFF, null);
			}
		}
	}

	@Override
	public synchronized void layoutChildren() {
		float longW = width / 16;
		float longH = height / 2;
		float shortW = longW * 3;
		float shortH = height / 6;

		for (int i = 0; i < numberSegments.length; i++) {
			float x = i * 4 * longW;
			numberSegments[i][0].layout(x, 0, longW, longH);
			numberSegments[i][1].layout(x, height / 2, longW, longH);
			numberSegments[i][2].layout(x + longW * 2, 0, longW, longH);
			numberSegments[i][3].layout(x + longW * 2, height / 2, longW, longH);
			numberSegments[i][4].layout(x, 0, shortW, shortH);
			numberSegments[i][4].layout(x, height / 2 - shortH / 2, shortW, shortH);
			numberSegments[i][4].layout(x, height - shortH / 2, shortW, shortH);
		}
	}

	@Override
	public void draw() {
		shapeGroup.draw();
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		lastFrameY = y;
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (id != 0)
			return; // only one pointer drags bpm
		currYDragTotal += lastFrameY - y;
		lastFrameY = y;
		if (Math.abs(currYDragTotal) > INC_BPM_THRESH) {
			if (currYDragTotal <= 0) {
				setBPM(MidiManager.getBPM() - 1);
			} else {
				setBPM(MidiManager.getBPM() + 1);
			}
			currYDragTotal %= INC_BPM_THRESH;
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		long tapTime = System.currentTimeMillis();
		float millisElapsed = tapTime - lastTapTime;
		lastTapTime = tapTime;
		float bpm = 60000 / millisElapsed;
		if (bpm <= MidiManager.MAX_BPM + 20 && bpm >= MidiManager.MIN_BPM - 20) {
			// if we are far outside of the range, don't change the tempo.
			// otherwise, midiManager will take care of clipping the result
			setBPM(bpm);
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		singleTap(id, x, y);
	}

	@Override
	protected void longPress(int id, float x, float y) {
		BeatBotActivity.mainActivity.showDialog(BeatBotActivity.BPM_DIALOG_ID);
	}

	public void setText(String text) {
		for (int i = 0; i < numberSegments.length; i++) {
			for (int j = 0; j < numberSegments[i].length; j++) {
				numberSegments[i][i].setFillColor(Colors.BPM_OFF);
			}
		}
		for (NumberSegment selectedSegment : getSelectedSegments(text)) {
			selectedSegment.setFillColor(Colors.BPM_ON);
		}
	}

	private static Set<NumberSegment> getSelectedSegments(String text) {
		Set<NumberSegment> selectedSegments = new HashSet<NumberSegment>();

		for (int i = 0; i < text.length(); i++) {
			NumberSegment[] charSegments = numberSegments[text.length() - i - 1];
			switch (text.charAt(i)) {
			case 0:
				selectedSegments.add(charSegments[0]);
				selectedSegments.add(charSegments[1]);
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[4]);
				selectedSegments.add(charSegments[6]);
				break;
			case 1:
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[3]);
				break;
			case 2:
				selectedSegments.add(charSegments[1]);
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[4]);
				selectedSegments.add(charSegments[5]);
				selectedSegments.add(charSegments[6]);
				break;
			case 3:
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[4]);
				selectedSegments.add(charSegments[5]);
				selectedSegments.add(charSegments[6]);
				break;
			case 4:
				selectedSegments.add(charSegments[0]);
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[5]);
				break;
			case 5:
				selectedSegments.add(charSegments[0]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[4]);
				selectedSegments.add(charSegments[5]);
				selectedSegments.add(charSegments[6]);
				break;
			case 6:
				selectedSegments.add(charSegments[0]);
				selectedSegments.add(charSegments[1]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[4]);
				selectedSegments.add(charSegments[5]);
				selectedSegments.add(charSegments[6]);
				break;
			case 7:
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[4]);
				break;
			case 8:
				selectedSegments.addAll(Arrays.asList(charSegments));
				break;
			case 9:
				selectedSegments.add(charSegments[0]);
				selectedSegments.add(charSegments[2]);
				selectedSegments.add(charSegments[3]);
				selectedSegments.add(charSegments[4]);
				selectedSegments.add(charSegments[5]);
				break;
			}			
		}
		return selectedSegments;
	}
}
