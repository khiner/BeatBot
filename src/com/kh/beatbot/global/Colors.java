package com.kh.beatbot.global;

public class Colors {
	public static final float[] BG_COLOR = { 0.12549f, 0.188235f, 0.227451f, 1 };
	public static final float[] BLACK = { 0, 0, 0, 1 };
	public static final float[] WHITE = { 1, 1, 1, 1 };
	public static final float[] GREEN = { 0, 1, 0, 1 };
	public static final float[] YELLOW = { 1, 1, 0, 1 };
	public static final float[] RED = { 1, 0, 0, 1 };

	public final static float[] MIDI_VIEW_DEFAULT_BG_COLOR = { .5f, .5f, .5f, 1};
	public final static float[] MIDI_VIEW_LIGHT_BG_COLOR = { .7f, .7f, .7f, 1};
	
	// select region (rectangle) color
	public final static float[] SELECT_REGION_COLOR = { .6f, .6f, 1, .7f };
	// main midi grid lines
	public final static float[] GRID_LINE_COLOR = { 0, 0, 0, 1 };
	// waveform lines in midi view
	public final static float[] WAVEFORM_COLOR = { 0, 0, 0, 1 };
	// non-selected notes
	public final static float[] NOTE_COLOR = { 1, 0, 0, 1 };
	// selected notes
	public final static float[] NOTE_SELECTED_COLOR = { 0, 0, 1, 1 };
	// volume bars
	public final static float[] VOLUME_COLOR = { .412f, .788f, 1, 1 };
	// pan bars
	public final static float[] PAN_COLOR = { 1, .788f, .392f, 1 };
	// pitch bars
	public final static float[] PITCH_COLOR = { .443f, 1, .533f, 1 };
	// selected bars
	public final static float[] LEVEL_SELECTED_COLOR = { .9f, 0, .1f, 1 };
	public final static float[] TICK_FILL_COLOR = { .3f, .3f, .3f, 1 };
	public final static float[] TICK_MARKER_COLOR = { .8f, .8f, .8f, 1 };
	public final static float[] TICKBAR_COLOR = { .6f, .6f, .6f, 1 };
	public final static float[] TICK_SELECTED_COLOR = VOLUME_COLOR;
}
