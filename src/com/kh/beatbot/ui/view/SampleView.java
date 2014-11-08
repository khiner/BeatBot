package com.kh.beatbot.ui.view;

import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.shape.WaveformShape;

public class SampleView extends View {
	private static final String NO_SAMPLE_MESSAGE = "Tap to load a sample.";

	// min distance for pointer to select loop markers
	private static WaveformShape waveformShape;

	// Zooming/scrolling will change the view window of the samples.
	// Keep track of that with offset and width.
	private float waveformWidth = 0, loopButtonW = 0;

	public SampleView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public synchronized void update() {
		loopButtonW = height / 3;
		waveformWidth = width - loopButtonW;

		if (true/*hasSample()*/) {
			if (null == waveformShape) {
				waveformShape = new WaveformShape(renderGroup, waveformWidth, Color.LABEL_SELECTED,
						Color.BLACK);
				addShapes(waveformShape);
			}
			waveformShape.layout(absoluteX, absoluteY, waveformWidth, height);
			waveformShape.resample();
			setText("");
		} else {
			if (null != waveformShape) {
				removeShape(waveformShape);
				waveformShape = null;
			}
			setText(NO_SAMPLE_MESSAGE);
		}
	}

	@Override
	public synchronized void createChildren() {
		setIcon(IconResourceSets.SAMPLE_BG);
		initRect();
	}
}
