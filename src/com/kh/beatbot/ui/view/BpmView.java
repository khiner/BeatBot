package com.kh.beatbot.ui.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.NumberSegment;

public class BpmView extends ClickableView {

	private static final float INC_BPM_THRESH = 15;
	private static NumberSegment[][] numberSegments = new NumberSegment[3][7];

	private static float lastFrameY = -1, currYDragTotal = 0;

	private long lastTapTime = 0;

	public BpmView(View view) {
		super(view);
	}

	public void setBPM(float bpm) {
		setText(String.valueOf((int) MidiManager.setBPM(bpm)));
	}

	@Override
	public synchronized void createChildren() {
		initRoundedRect();
		for (int i = 0; i < numberSegments.length; i++) {
			for (int j = 0; j < numberSegments[i].length; j++) {
				numberSegments[i][j] = new NumberSegment(renderGroup, Color.BPM_OFF, null);
			}
			addShapes(numberSegments[i]);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		float height = this.height - BG_OFFSET * 2;
		float longW = (width - BG_OFFSET * 2) / 12;
		float longH = height / 2 - longW / 2;
		float shortW = longW * 3;
		float shortH = longW;

		for (int i = 0; i < numberSegments.length; i++) {
			float x = absoluteX + BG_OFFSET + i * 3.5f * longW + longW;
			float y = absoluteY + BG_OFFSET;
			numberSegments[i][0].layout(x, y + longW / 2, longW, longH);
			numberSegments[i][1].layout(x, y + height / 2, longW, longH);
			numberSegments[i][2].layout(x + longW * 2, y + longW / 2, longW, longH);
			numberSegments[i][3].layout(x + longW * 2, y + height / 2, longW, longH);
			numberSegments[i][4].layout(x, y, shortW, shortH);
			numberSegments[i][5].layout(x, y + height / 2 - shortH / 2, shortW, shortH);
			numberSegments[i][6].layout(x, y + height - shortH, shortW, shortH);
		}
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		lastFrameY = pos.y;
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		super.handleActionMove(id, pos);
		if (id != 0)
			return; // only one pointer drags bpm
		currYDragTotal += lastFrameY - pos.y;
		lastFrameY = pos.y;
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
	protected void singleTap(int id, Pointer pos) {
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
	protected void doubleTap(int id, Pointer pos) {
		singleTap(id, pos);
	}

	@Override
	protected void longPress(int id, Pointer pos) {
		BeatBotActivity.mainActivity.showDialog(BeatBotActivity.BPM_DIALOG_ID);
	}

	public void setText(String text) {
		for (int i = 0; i < numberSegments.length; i++) {
			for (int j = 0; j < numberSegments[i].length; j++) {
				numberSegments[i][j].setFillColor(Color.BPM_OFF);
			}
		}
		for (NumberSegment selectedSegment : getSelectedSegments(text)) {
			selectedSegment.setFillColor(Color.BPM_ON);
		}
	}

	private static Set<NumberSegment> getSelectedSegments(String text) {
		Set<NumberSegment> selectedSegments = new HashSet<NumberSegment>();

		while (text.length() < 3) {
			// prepend with 0's until the string is 3 digits long
			text = "0" + text;
		}
		for (int i = 0; i < text.length(); i++) {
			NumberSegment[] charSegments = numberSegments[i];
			switch (Character.getNumericValue(text.charAt(i))) {
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
