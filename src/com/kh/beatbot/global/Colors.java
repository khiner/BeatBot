package com.kh.beatbot.global;

import com.kh.beatbot.R;

import android.app.Activity;

public class Colors {
	public static float[] BG_COLOR, BLACK, WHITE, GREEN, YELLOW,
			RED, MIDI_VIEW_BG, MIDI_VIEW_LIGHT_BG, GRID_LINE,
			WAVEFORM, SELECT_REGION, NOTE, NOTE_SELECTED, VOLUME, PAN, PITCH, LEVEL_SELECTED, TICK_FILL, TICK_MARKER, TICKBAR, TICK_SELECTED;
	
	public static void initColors(Activity activity) {
		BLACK = colorResourceToFloats(activity, R.color.black);
		WHITE = colorResourceToFloats(activity, R.color.white);
		RED = colorResourceToFloats(activity, R.color.red);
		YELLOW = colorResourceToFloats(activity, R.color.yellow);
		GREEN = colorResourceToFloats(activity, R.color.green);
		
		BG_COLOR = colorResourceToFloats(activity, R.color.background);
		NOTE = colorResourceToFloats(activity, R.color.note);
		NOTE_SELECTED = colorResourceToFloats(activity, R.color.noteSelected);
		MIDI_VIEW_BG = colorResourceToFloats(activity, R.color.midiViewBg);
		MIDI_VIEW_LIGHT_BG = colorResourceToFloats(activity, R.color.midiViewLightBg);
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
	}
	
	public static float[] colorResourceToFloats(Activity activity, int resourceId) {
		return hexToFloats(activity.getResources().getColor(resourceId));
	}
	
	public static float[] hexToFloats(int hex) {
		return new float[] { ((hex >> 16) & 0xFF) / 255f,
				             ((hex >> 8) & 0xFF) / 255f,
				             ((hex >> 0) & 0xFF) / 255f,
				             ((hex >> 24) & 0xFF) / 255f };
	}
}
