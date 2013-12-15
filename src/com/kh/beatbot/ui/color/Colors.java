package com.kh.beatbot.ui.color;

import android.app.Activity;

import com.kh.beatbot.R;

public class Colors {
	public static float[] BG_COLOR, BLACK, WHITE, GREEN, YELLOW, RED,
			MIDI_VIEW_BG, MIDI_VIEW_LIGHT_BG, GRID_LINE, WAVEFORM,
			SELECT_REGION, NOTE, NOTE_SELECTED, VOLUME, PAN, PITCH,
			LEVEL_SELECTED, TICK_FILL, TICK_MARKER, TICKBAR, TICK_SELECTED,
			VOLUME_LIGHT, VOLUME_SELECTED, BPM_OFF, BPM_ON, BPM_ON_SELECTED,
			BPM_OFF_SELECTED, VIEW_BG, SAMPLE_LOOP_SELECT_OUTLINE, LABEL_DARK,
			LABEL_MED, LABEL_LIGHT, LABEL_VERY_LIGHT, LABEL_SELECTED,
			LABEL_SELECTED_TRANS, MIDI_SELECTED_TRACK, TRANSPARANT = { 0, 0, 0,
					0 };

	public static float[][] MIDI_LINES = { { 0, 0, 0, 1 },
			{ .1f, .1f, .1f, 1 }, { .2f, .2f, .2f, 1 }, { .3f, .3f, .3f, 1 },
			{ .4f, .4f, .4f, 1 }, { .5f, .5f, .5f, 1 }, { .6f, .6f, .6f, 1 },
			{ .7f, .7f, .7f, 1 } };

	public static ColorSet labelBgColorSet, labelStrokeColorSet,
			valueLabelBgColorSet, muteButtonColorSet, soloButtonColorSet,
			buttonRowStrokeColorSet, instrumentBgColorSet, panBgColorSet,
			panStrokeColorSet, pitchBgColorSet, pitchStrokeColorSet,
			volumeBgColorSet, volumeStrokeColorSet, effectLabelBgColorSet,
			effectLabelStrokeColorSet, effectLabelTouchedBgColorSet,
			effectLabelTouchedStrokeColorSet, iconFillColorSet,
			deleteFillColorSet, deleteStrokeColorSet, menuItemFillColorSet,
			menuToggleFillColorSet;

	public static void initColors(Activity activity) {
		BLACK = colorResourceToFloats(activity, R.color.black);
		WHITE = colorResourceToFloats(activity, R.color.white);
		RED = colorResourceToFloats(activity, R.color.red);
		YELLOW = colorResourceToFloats(activity, R.color.yellow);
		GREEN = colorResourceToFloats(activity, R.color.green);

		BG_COLOR = colorResourceToFloats(activity, R.color.background);
		VIEW_BG = colorResourceToFloats(activity, R.color.viewBg);

		NOTE = colorResourceToFloats(activity, R.color.note);
		NOTE_SELECTED = colorResourceToFloats(activity, R.color.noteSelected);
		MIDI_VIEW_BG = colorResourceToFloats(activity, R.color.midiViewBg);
		MIDI_VIEW_LIGHT_BG = colorResourceToFloats(activity,
				R.color.midiViewLightBg);
		GRID_LINE = colorResourceToFloats(activity, R.color.gridLine);
		SELECT_REGION = colorResourceToFloats(activity, R.color.selectRegion);
		VOLUME = colorResourceToFloats(activity, R.color.volume);
		WAVEFORM = colorResourceToFloats(activity, R.color.waveform);
		PAN = colorResourceToFloats(activity, R.color.pan);
		PITCH = colorResourceToFloats(activity, R.color.pitch);
		LEVEL_SELECTED = colorResourceToFloats(activity, R.color.levelSelected);
		TICK_FILL = colorResourceToFloats(activity, R.color.tickFill);
		TICK_MARKER = colorResourceToFloats(activity, R.color.tickMarker);
		TICKBAR = colorResourceToFloats(activity, R.color.tickBar);
		TICK_SELECTED = colorResourceToFloats(activity, R.color.tickSelected);

		VOLUME_SELECTED = new float[] { VOLUME[0], VOLUME[1], VOLUME[2], .6f };
		VOLUME_LIGHT = colorResourceToFloats(activity, R.color.volumeLight);

		BPM_ON = colorResourceToFloats(activity, R.color.bpmOn);
		BPM_OFF = colorResourceToFloats(activity, R.color.bpmOff);
		BPM_ON_SELECTED = colorResourceToFloats(activity, R.color.bpmOnSelected);
		BPM_OFF_SELECTED = colorResourceToFloats(activity,
				R.color.bpmOffSelected);
		// VIEW_BG = new float[] { 0.3275f, 0.3994f, 0.4465f, 1 };

		SAMPLE_LOOP_SELECT_OUTLINE = colorResourceToFloats(activity,
				R.color.sampleLoopSelectOutline);

		LABEL_DARK = colorResourceToFloats(activity, R.color.labelDark);
		LABEL_MED = colorResourceToFloats(activity, R.color.labelMed);
		LABEL_LIGHT = colorResourceToFloats(activity, R.color.labelLight);
		LABEL_VERY_LIGHT = colorResourceToFloats(activity,
				R.color.labelVeryLight);
		LABEL_SELECTED = colorResourceToFloats(activity, R.color.labelSelected);
		LABEL_SELECTED_TRANS = new float[] { LABEL_SELECTED[0],
				LABEL_SELECTED[1], LABEL_SELECTED[2], .38f };

		MIDI_SELECTED_TRACK = new float[] { YELLOW[0], YELLOW[1], YELLOW[2],
				.38f };
		labelBgColorSet = new ColorSet(LABEL_DARK, VOLUME, LABEL_SELECTED);
		labelStrokeColorSet = new ColorSet(WHITE, BLACK, BLACK);

		valueLabelBgColorSet = new ColorSet(LABEL_VERY_LIGHT, LABEL_SELECTED,
				null, LABEL_DARK);

		buttonRowStrokeColorSet = new ColorSet(WHITE, BLACK, BLACK);

		effectLabelBgColorSet = new ColorSet(LABEL_DARK, LABEL_LIGHT, VOLUME);
		effectLabelStrokeColorSet = new ColorSet(WHITE, WHITE, WHITE);

		effectLabelTouchedBgColorSet = new ColorSet(LABEL_MED,
				LABEL_VERY_LIGHT, VOLUME_LIGHT);
		effectLabelTouchedStrokeColorSet = new ColorSet(WHITE, WHITE, WHITE);

		muteButtonColorSet = new ColorSet(null, LABEL_SELECTED, PAN);
		soloButtonColorSet = new ColorSet(null, LABEL_SELECTED, PITCH);

		instrumentBgColorSet = new ColorSet(null, LABEL_SELECTED, VOLUME);

		panBgColorSet = new ColorSet(null, LABEL_SELECTED, PAN);
		pitchBgColorSet = new ColorSet(null, LABEL_SELECTED, PITCH);
		volumeBgColorSet = new ColorSet(null, LABEL_SELECTED, VOLUME);

		panStrokeColorSet = new ColorSet(PAN, PAN, BLACK);
		pitchStrokeColorSet = new ColorSet(PITCH, PITCH, BLACK);
		volumeStrokeColorSet = new ColorSet(VOLUME, VOLUME, BLACK);

		iconFillColorSet = new ColorSet(null, LABEL_SELECTED);

		deleteFillColorSet = new ColorSet(null, RED);
		deleteStrokeColorSet = new ColorSet(RED, BLACK);

		menuItemFillColorSet = new ColorSet(null, VOLUME);
		menuToggleFillColorSet = new ColorSet(null, LABEL_LIGHT, VOLUME);
	}

	public static float[] colorResourceToFloats(Activity activity,
			int resourceId) {
		return hexToFloats(activity.getResources().getColor(resourceId));
	}

	public static float[] hexToFloats(int hex) {
		return new float[] { ((hex >> 16) & 0xFF) / 255f,
				((hex >> 8) & 0xFF) / 255f, ((hex >> 0) & 0xFF) / 255f,
				((hex >> 24) & 0xFF) / 255f };
	}
}
