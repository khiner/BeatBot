package com.kh.beatbot.ui.view;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.TempoListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.NumberSegment;

public class BpmView extends LongPressableView implements TempoListener {
	private final static int NUM_DIGITS = 3, NUM_SEGMENTS = 7;
	private final static float BPM_INCREMENT_THRESHOLD = 15f;
	private NumberSegment[][] allNumberSegments; // number segments for all digits

	private float lastFrameY = -1, yDragTotal = 0;

	public BpmView(View view) {
		super(view);
		MidiManager.setTempoListener(this);
	}

	@Override
	public void onTempoChange(float bpm) {
		setText(String.valueOf((int) bpm));
	}

	@Override
	public synchronized void createChildren() {
		allNumberSegments = new NumberSegment[NUM_DIGITS][NUM_SEGMENTS];
		setIcon(IconResourceSets.SEVEN_SEGMENT_BG);
		initRoundedRect();
		for (int i = 0; i < allNumberSegments.length; i++) {
			for (int j = 0; j < allNumberSegments[i].length; j++) {
				allNumberSegments[i][j] = new NumberSegment(renderGroup, Color.SEVEN_SEGMENT_OFF, null);
			}
			addShapes(allNumberSegments[i]);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		float height = this.height - BG_OFFSET * 2;
		float longW = (width - BG_OFFSET * 2) / 12;
		float longH = (height - longW) / 2;
		float shortW = longW * 3;
		float shortH = longW;

		for (int i = 0; i < allNumberSegments.length; i++) {
			float x = absoluteX + BG_OFFSET + i * 3.5f * longW + longW;
			float y = absoluteY + BG_OFFSET;
			allNumberSegments[i][0].layout(x, y + longW / 2, longW, longH);
			allNumberSegments[i][1].layout(x, y + height / 2, longW, longH);
			allNumberSegments[i][2].layout(x + longW * 2, y + longW / 2, longW, longH);
			allNumberSegments[i][3].layout(x + longW * 2, y + height / 2, longW, longH);
			allNumberSegments[i][4].layout(x, y, shortW, shortH);
			allNumberSegments[i][5].layout(x, y + (height - shortH) / 2, shortW, shortH);
			allNumberSegments[i][6].layout(x, y + height - shortH, shortW, shortH);
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
		yDragTotal += lastFrameY - pos.y;
		lastFrameY = pos.y;
		if (Math.abs(yDragTotal) > BPM_INCREMENT_THRESHOLD) {
			if (yDragTotal <= 0) {
				MidiManager.setBPM(MidiManager.getBPM() - 1);
			} else {
				MidiManager.setBPM(MidiManager.getBPM() + 1);
			}
			yDragTotal %= BPM_INCREMENT_THRESHOLD;
		}
	}

	@Override
	protected void longPress(int id, Pointer pos) {
		context.showDialog(BeatBotActivity.BPM_DIALOG_ID);
	}

	private final int[][] SEGMENT_INDICES_FOR_DIGIT = {
		{ 0, 1, 2, 3, 4, 6 }, // 0
		{ 2, 3 }, // 1
		{ 1, 2, 4, 5, 6 }, // 2
		{ 2, 3, 4, 5, 6 }, // 3
		{ 0, 2, 3, 5 }, // 4
		{ 0, 3, 4, 5, 6 }, // 5
		{ 0, 1, 3, 4, 5, 6 }, // 6
		{ 2, 3, 4 }, // 7
		{ 0, 1, 2, 3, 4, 5, 6 }, // 8
		{ 0, 2, 3, 4, 5 } // 9
	};

	public void setText(String text) {
		// prepend with 0's until the string is 3 digits long
		while (text.length() < NUM_DIGITS)
			text = "0" + text;

		for (NumberSegment[] numberSegments : allNumberSegments) {
			for (NumberSegment numberSegment : numberSegments) {
				numberSegment.setFillColor(Color.SEVEN_SEGMENT_OFF);
			}
		}

		for (int i = 0; i < text.length(); i++) {
			NumberSegment[] numberSegments = allNumberSegments[i];
			int digit = Character.getNumericValue(text.charAt(i));
			int[] enabledSegmentIndices = SEGMENT_INDICES_FOR_DIGIT[digit];
			for (int enabledSegmentIndex : enabledSegmentIndices) {
				numberSegments[enabledSegmentIndex].setFillColor(Color.SEVEN_SEGMENT_ON);
			}
		}
	}
}
