package com.kh.beatbot.ui.view;

import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.shape.WaveformShape;

public class SampleView extends View {
	// min distance for pointer to select loop markers
	private WaveformShape waveformShape;

	// Zooming/scrolling will change the view window of the samples.
	// Keep track of that with offset and width.
	private float waveformWidth = 0, loopButtonW = 0;

	public SampleView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public synchronized void update() {
		loopButtonW = height / 3;
		waveformWidth = width - loopButtonW;

		if (waveformShape != null) {
			waveformShape.layout(absoluteX, absoluteY, waveformWidth, height);
			waveformShape.update(0, width, 0, Long.MAX_VALUE, loopButtonW / 2);
			waveformShape.resample();
		}
	}

	/*
	 * Sample views *either* show sample waveform *or* text, never both simultaneously
	 */
	@Override
	public void setText(final String text) {
		if (null != waveformShape) {
			removeShape(waveformShape);
			waveformShape = null;
		}
		if (text.isEmpty()) { // no text, create empty waveform shape
			if (null == waveformShape) {
				waveformShape = new WaveformShape(renderGroup, waveformWidth, Color.LABEL_SELECTED,
						Color.BLACK);
				addShapes(waveformShape);
			}
		}
		super.setText(text);
	}

	@Override
	public void createChildren() {
		setIcon(IconResourceSets.SAMPLE_BG);
		initRoundedRect();
	}
}
