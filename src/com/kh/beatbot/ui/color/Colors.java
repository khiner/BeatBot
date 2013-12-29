package com.kh.beatbot.ui.color;

import android.app.Activity;

import com.kh.beatbot.R;

public class Colors {
	public static float[] BG, BLACK, WHITE, GREEN, YELLOW, RED, MIDI_VIEW_BG,
			MIDI_VIEW_LIGHT_BG, GRID_LINE, WAVEFORM, NOTE, NOTE_SELECTED,
			VOLUME, PAN, PITCH, VOLUME_LIGHT, VOLUME_TRANS, PAN_TRANS,
			PITCH_TRANS, LEVEL_SELECTED, LEVEL_SELECTED_TRANS, TICK_FILL,
			TICK_MARKER, TICKBAR, BPM_OFF, BPM_ON, BPM_ON_SELECTED,
			BPM_OFF_SELECTED, VIEW_BG, VIEW_BG_SELECTED, LABEL_DARK, LABEL_MED,
			LABEL_LIGHT, LABEL_VERY_LIGHT, LABEL_SELECTED,
			LABEL_SELECTED_TRANS, MIDI_SELECTED_TRACK, TRANSPARENT = { 0, 0, 0,
					0 };

	public static float[][] MIDI_LINES = { { 0, 0, 0, 1 },
			{ .1f, .1f, .1f, 1 }, { .2f, .2f, .2f, 1 }, { .3f, .3f, .3f, 1 },
			{ .4f, .4f, .4f, 1 }, { .5f, .5f, .5f, 1 }, { .6f, .6f, .6f, 1 },
			{ .7f, .7f, .7f, 1 } };

	public static ColorSet labelFillColorSet, labelStrokeColorSet,
			valueLabelFillColorSet, muteButtonColorSet, soloButtonColorSet,
			buttonRowStrokeColorSet, instrumentFillColorSet, panFillColorSet,
			panStrokeColorSet, pitchFillColorSet, pitchStrokeColorSet,
			volumeFillColorSet, volumeStrokeColorSet, effectLabelFillColorSet,
			effectLabelStrokeColorSet, effectLabelTouchedFillColorSet,
			effectLabelTouchedStrokeColorSet, iconFillColorSet,
			deleteFillColorSet, deleteStrokeColorSet, menuItemFillColorSet,
			menuToggleFillColorSet, loopSelectionColorSet,
			loopSelectionStrokeColorSet, defaultBgFillColorSet,
			defaultBgStrokeColorSet;

	public static void initColors(Activity activity) {
		BLACK = colorResourceToFloats(activity, R.color.black);
		WHITE = colorResourceToFloats(activity, R.color.white);
		RED = colorResourceToFloats(activity, R.color.red);
		YELLOW = colorResourceToFloats(activity, R.color.yellow);
		GREEN = colorResourceToFloats(activity, R.color.green);

		BG = colorResourceToFloats(activity, R.color.background);
		VIEW_BG = colorResourceToFloats(activity, R.color.viewBg);
		VIEW_BG_SELECTED = colorResourceToFloats(activity,
				R.color.viewBgSelected);
		NOTE = colorResourceToFloats(activity, R.color.note);
		NOTE_SELECTED = colorResourceToFloats(activity, R.color.noteSelected);
		MIDI_VIEW_BG = colorResourceToFloats(activity, R.color.midiViewBg);
		MIDI_VIEW_LIGHT_BG = colorResourceToFloats(activity,
				R.color.midiViewLightBg);
		GRID_LINE = colorResourceToFloats(activity, R.color.gridLine);
		VOLUME = colorResourceToFloats(activity, R.color.volume);
		VOLUME_TRANS = new float[] { VOLUME[0], VOLUME[1], VOLUME[2], .6f };
		VOLUME_LIGHT = colorResourceToFloats(activity, R.color.volumeLight);
		PAN = colorResourceToFloats(activity, R.color.pan);
		PAN_TRANS = new float[] { PAN[0], PAN[1], PAN[2], .6f };
		PITCH = colorResourceToFloats(activity, R.color.pitch);
		PITCH_TRANS = new float[] { PITCH[0], PITCH[1], PITCH[2], .6f };

		WAVEFORM = colorResourceToFloats(activity, R.color.waveform);
		LEVEL_SELECTED = colorResourceToFloats(activity, R.color.levelSelected);
		LEVEL_SELECTED_TRANS = new float[] { LEVEL_SELECTED[0],
				LEVEL_SELECTED[1], LEVEL_SELECTED[2], .6f };

		TICK_FILL = colorResourceToFloats(activity, R.color.tickFill);
		TICK_MARKER = colorResourceToFloats(activity, R.color.tickMarker);
		TICKBAR = colorResourceToFloats(activity, R.color.tickBar);

		BPM_ON = colorResourceToFloats(activity, R.color.bpmOn);
		BPM_OFF = colorResourceToFloats(activity, R.color.bpmOff);
		BPM_ON_SELECTED = colorResourceToFloats(activity, R.color.bpmOnSelected);
		BPM_OFF_SELECTED = colorResourceToFloats(activity,
				R.color.bpmOffSelected);
		// VIEW_BG = new float[] { 0.3275f, 0.3994f, 0.4465f, 1 };

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
		labelFillColorSet = new ColorSet(LABEL_DARK, VOLUME, LABEL_SELECTED);
		labelStrokeColorSet = new ColorSet(WHITE, BLACK, BLACK);

		valueLabelFillColorSet = new ColorSet(LABEL_VERY_LIGHT, LABEL_SELECTED,
				null, LABEL_DARK);

		buttonRowStrokeColorSet = new ColorSet(WHITE, BLACK, BLACK);

		effectLabelFillColorSet = new ColorSet(LABEL_DARK, LABEL_LIGHT, VOLUME);
		effectLabelStrokeColorSet = new ColorSet(WHITE, WHITE, WHITE);

		effectLabelTouchedFillColorSet = new ColorSet(LABEL_MED,
				LABEL_VERY_LIGHT, VOLUME_LIGHT);
		effectLabelTouchedStrokeColorSet = new ColorSet(WHITE, WHITE, WHITE);

		muteButtonColorSet = new ColorSet(null, LABEL_SELECTED, PAN);
		soloButtonColorSet = new ColorSet(null, LABEL_SELECTED, PITCH);

		instrumentFillColorSet = new ColorSet(null, LABEL_SELECTED, VOLUME);

		panFillColorSet = new ColorSet(null, LABEL_SELECTED, PAN);
		pitchFillColorSet = new ColorSet(null, LABEL_SELECTED, PITCH);
		volumeFillColorSet = new ColorSet(null, LABEL_SELECTED, VOLUME);

		panStrokeColorSet = new ColorSet(PAN, PAN, BLACK);
		pitchStrokeColorSet = new ColorSet(PITCH, PITCH, BLACK);
		volumeStrokeColorSet = new ColorSet(VOLUME, VOLUME, BLACK);

		iconFillColorSet = new ColorSet(null, LABEL_SELECTED);

		deleteFillColorSet = new ColorSet(null, RED);
		deleteStrokeColorSet = new ColorSet(RED, BLACK);

		menuItemFillColorSet = new ColorSet(null, VOLUME);
		menuToggleFillColorSet = new ColorSet(null, LABEL_LIGHT, VOLUME);

		loopSelectionColorSet = new ColorSet(VOLUME_TRANS, VOLUME);
		loopSelectionStrokeColorSet = new ColorSet(VOLUME, VOLUME);

		defaultBgFillColorSet = new ColorSet(VIEW_BG, VIEW_BG_SELECTED);
		defaultBgStrokeColorSet = new ColorSet(VOLUME);
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
