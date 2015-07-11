package com.kh.beatbot.ui.color;

import android.app.Activity;

import com.kh.beatbot.R;

public class Color {
	public static float[] BG, BLACK, WHITE, GREEN, YELLOW, RED, DARK_TRANS, MIDI_VIEW_DARK_BG,
			MIDI_VIEW_BG, MIDI_VIEW_LIGHT_BG, GRID_LINE, WAVEFORM, NOTE, NOTE_LIGHT, NOTE_SELECTED,
			NOTE_SELECTED_LIGHT, TRON_BLUE, PAN, PITCH, TRON_BLUE_LIGHT, TRON_BLUE_TRANS,
			PAN_TRANS, PITCH_TRANS, LEVEL_SELECTED, LEVEL_SELECTED_TRANS, TICK_FILL, TICK_MARKER,
			TICKBAR, SEVEN_SEGMENT_OFF, SEVEN_SEGMENT_ON, VIEW_BG, VIEW_BG_SELECTED, LABEL_DARK,
			LABEL_MED, LABEL_LIGHT, LABEL_VERY_LIGHT, LABEL_SELECTED, LABEL_TRANS,
			LABEL_SELECTED_TRANS, MIDI_SELECTED_TRACK, TRANSPARENT = { 0, 0, 0, 0 },
			SEMI_TRANSPARENT = { 1, 1, 1, .4f };

	public static float[][] MIDI_LINES = { { 0, 0, 0, 1 }, { .05f, .05f, .05f, 1 },
			{ .1f, .1f, .1f, 1 }, { .15f, .15f, .15f, 1 }, { .2f, .2f, .2f, 1 },
			{ .25f, .25f, .25f, 1 }, { .3f, .3f, .3f, 1 }, { .35f, .35f, .35f, 1 } };

	public static void init(Activity activity) {
		BLACK = colorResourceToFloats(activity, R.color.black);
		WHITE = colorResourceToFloats(activity, R.color.white);
		RED = colorResourceToFloats(activity, R.color.red);
		YELLOW = colorResourceToFloats(activity, R.color.yellow);
		GREEN = colorResourceToFloats(activity, R.color.green);

		DARK_TRANS = new float[] { 0, 0, 0, .2f };
		MIDI_VIEW_DARK_BG = colorResourceToFloats(activity, R.color.midiViewDarkBg);
		BG = colorResourceToFloats(activity, R.color.background);
		VIEW_BG = colorResourceToFloats(activity, R.color.viewBg);
		VIEW_BG_SELECTED = colorResourceToFloats(activity, R.color.viewBgSelected);
		NOTE_LIGHT = lighten(colorResourceToFloats(activity, R.color.note), .1f);
		NOTE = darken(NOTE_LIGHT, .25f);
		NOTE_SELECTED_LIGHT = lighten(colorResourceToFloats(activity, R.color.noteSelected), .1f);
		NOTE_SELECTED = darken(NOTE_SELECTED_LIGHT, .25f);

		MIDI_VIEW_BG = colorResourceToFloats(activity, R.color.midiViewBg);
		MIDI_VIEW_LIGHT_BG = colorResourceToFloats(activity, R.color.midiViewLightBg);
		MIDI_VIEW_LIGHT_BG[3] = .5f;
		GRID_LINE = colorResourceToFloats(activity, R.color.gridLine);
		TRON_BLUE = colorResourceToFloats(activity, R.color.tronBlue);
		TRON_BLUE_TRANS = transparentize(TRON_BLUE, .4f);
		TRON_BLUE_LIGHT = colorResourceToFloats(activity, R.color.tronBlueLight);
		PAN = colorResourceToFloats(activity, R.color.pan);
		PAN_TRANS = transparentize(PAN, .4f);
		PITCH = colorResourceToFloats(activity, R.color.pitch);
		PITCH_TRANS = transparentize(PITCH, .4f);

		WAVEFORM = colorResourceToFloats(activity, R.color.waveform);
		LEVEL_SELECTED = colorResourceToFloats(activity, R.color.levelSelected);
		LEVEL_SELECTED_TRANS = transparentize(LEVEL_SELECTED, .4f);

		TICK_FILL = colorResourceToFloats(activity, R.color.tickFill);
		TICK_MARKER = colorResourceToFloats(activity, R.color.tickMarker);
		TICKBAR = colorResourceToFloats(activity, R.color.tickBar);

		SEVEN_SEGMENT_ON = colorResourceToFloats(activity, R.color.sevenSegmentOn);
		SEVEN_SEGMENT_OFF = colorResourceToFloats(activity, R.color.sevenSegmentOff);
		// VIEW_BG = new float[] { 0.3275f, 0.3994f, 0.4465f, 1 };

		LABEL_DARK = colorResourceToFloats(activity, R.color.labelDark);
		LABEL_MED = colorResourceToFloats(activity, R.color.labelMed);
		LABEL_LIGHT = colorResourceToFloats(activity, R.color.labelLight);
		LABEL_VERY_LIGHT = colorResourceToFloats(activity, R.color.labelVeryLight);
		LABEL_SELECTED = colorResourceToFloats(activity, R.color.labelSelected);
		LABEL_TRANS = transparentize(LABEL_DARK, .52f);
		LABEL_SELECTED_TRANS = transparentize(LABEL_SELECTED, .72f);

		MIDI_SELECTED_TRACK = transparentize(YELLOW, .72f);
	}

	public static float[] colorResourceToFloats(Activity activity, int resourceId) {
		return hexToFloats(activity.getResources().getColor(resourceId));
	}

	public static float[] hexToFloats(int hex) {
		return new float[] { ((hex >> 16) & 0xFF) / 255f, ((hex >> 8) & 0xFF) / 255f,
				((hex >> 0) & 0xFF) / 255f, ((hex >> 24) & 0xFF) / 255f };
	}

	public static float[] lighten(float[] color, float amount) {
		float scale = 1 + amount;
		return new float[] { color[0] * scale, color[1] * scale, color[2] * scale, color[3] };
	}

	public static float[] darken(float[] color, float amount) {
		return lighten(color, -amount);
	}

	public static float[] transparentize(float[] color, float amount) {
		return new float[] { color[0], color[1], color[2], 1 - amount };
	}
}
