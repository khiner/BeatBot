package com.kh.beatbot.ui.icon;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.R;
import com.kh.beatbot.ui.color.Color;

public class IconResourceSets {
	public static IconResourceSet
			DEFAULT = s(r(Color.VIEW_BG, Color.TRON_BLUE), r(Color.VIEW_BG_SELECTED, Color.TRON_BLUE), r(Color.VIEW_BG_SELECTED, Color.TRON_BLUE), r(Color.VIEW_BG, Color.TRON_BLUE)),
			SAMPLE_LOOP = s(r(Color.TRON_BLUE_TRANS), r(Color.TRON_BLUE)),
			SAMPLE_BG = s(r(Color.LABEL_LIGHT), r(Color.LABEL_VERY_LIGHT)),
			MUTE = s(r(Color.TRANSPARENT, Color.WHITE), r(Color.LABEL_SELECTED, Color.BLACK), r(Color.PAN, Color.BLACK)),
			SOLO = s(r(Color.TRANSPARENT, Color.WHITE), r(Color.LABEL_SELECTED, Color.BLACK), r(Color.PITCH, Color.BLACK)),
			BEAT_SYNC = s(r(R.drawable.clock, null, null, null, Color.WHITE), null, r(R.drawable.note_icon, null, null, null, Color.WHITE)),
			VALUE_LABEL = s(r(Color.LABEL_VERY_LIGHT), r(Color.LABEL_SELECTED), null, r(Color.LABEL_DARK)),
			PLAY = s(r(R.drawable.play_icon, null, Color.WHITE), r(R.drawable.play_icon_selected, null, Color.WHITE), r(R.drawable.play_icon_selected, null, Color.WHITE)),
			STOP = s(r(R.drawable.stop_icon, null, Color.WHITE), r(R.drawable.stop_icon, null, Color.LABEL_SELECTED)),
			RECORD = s(r(R.drawable.record_icon, null, Color.WHITE), r(R.drawable.record_icon_selected, null, Color.WHITE), r(R.drawable.record_icon_selected, null, Color.WHITE)),
			UNDO = s(r(R.drawable.undo_icon, null, Color.WHITE), r(R.drawable.undo_icon, null, Color.LABEL_SELECTED), null, r(R.drawable.undo_icon, null, Color.SEMI_TRANSPARENT)),
			REDO = s(r(R.drawable.redo_icon, null, Color.WHITE), r(R.drawable.redo_icon, null, Color.LABEL_SELECTED), null, r(R.drawable.redo_icon, null, Color.SEMI_TRANSPARENT)),
			COPY = s(r(R.drawable.copy_icon, null, Color.WHITE), r(R.drawable.copy_icon, null, Color.TRON_BLUE), r(R.drawable.copy_icon, null, Color.TRON_BLUE), r(R.drawable.copy_icon, null, Color.SEMI_TRANSPARENT)),
			QUANTIZE = s(r(R.drawable.quantize_icon, null, Color.WHITE), r(R.drawable.quantize_icon, null, Color.TRON_BLUE), null, r(R.drawable.quantize_icon, null, Color.SEMI_TRANSPARENT)),
			DELETE_NOTE = s(r(R.drawable.delete_icon, null, Color.WHITE), r(R.drawable.delete_icon, null, Color.LEVEL_SELECTED), null, r(R.drawable.delete_icon, null, Color.SEMI_TRANSPARENT)),

			INSTRUMENT_BASE = s(r(Color.TRANSPARENT, Color.WHITE), r(Color.LABEL_SELECTED, Color.BLACK), r(Color.TRON_BLUE, Color.BLACK)),
			BROWSE = s(r(R.drawable.browse_icon), r(R.drawable.browse_icon), r(R.drawable.browse_icon)),
			DRUMS= s(r(R.drawable.drums_icon), r(R.drawable.drums_icon), r(R.drawable.drums_icon)),
			KICK= s(r(R.drawable.kick_icon), r(R.drawable.kick_icon), r(R.drawable.kick_icon)),
			SNARE= s(r(R.drawable.snare_icon), r(R.drawable.snare_icon), r(R.drawable.snare_icon)),
			HH_CLOSED = s(r(R.drawable.hh_closed_icon), r(R.drawable.hh_closed_icon), r(R.drawable.hh_closed_icon)),
			HH_OPEN = s(r(R.drawable.hh_open_icon), r(R.drawable.hh_open_icon), r(R.drawable.hh_open_icon)),
			RIMSHOT = s(r(R.drawable.rimshot_icon), r(R.drawable.rimshot_icon), r(R.drawable.rimshot_icon)),
			MICROPHONE = s(r(R.drawable.microphone_icon), r(R.drawable.microphone_icon), r(R.drawable.microphone_icon)),
			BEAT = s(r(R.drawable.beat_icon), r(R.drawable.beat_icon), r(R.drawable.beat_icon)),
			SAMPLE = s(r(R.drawable.sample_icon), r(R.drawable.sample_icon), r(R.drawable.sample_icon)),
			
			BROWSE_PAGE = s(r(Color.LABEL_SELECTED), r(Color.LABEL_SELECTED)),
			SLIDE_MENU = s(r(R.drawable.menu_icon, Color.LABEL_SELECTED, null), r(R.drawable.menu_icon, Color.LABEL_SELECTED, null)),
					
			DELETE_TRACK = s(r(R.drawable.delete_track_icon, Color.TRANSPARENT, Color.RED), r(R.drawable.delete_track_icon, Color.RED, Color.BLACK)),

			LABEL_BASE = s(r(Color.LABEL_DARK, Color.WHITE), r(Color.TRON_BLUE, Color.BLACK), r(Color.LABEL_SELECTED, Color.BLACK)),
			ADD = s(r(R.drawable.plus_outline), r(R.drawable.plus_outline), r(R.drawable.plus_outline)),
			NOTE_LEVELS = s(r(R.drawable.note_levels_icon), r(R.drawable.note_levels_icon), r(R.drawable.note_levels_icon)),
			ADSR = s(r(R.drawable.adsr_icon, null, Color.WHITE), r(R.drawable.adsr_icon, null, Color.BLACK), r(R.drawable.adsr_icon, null, Color.BLACK)),
			LEVELS = s(r(R.drawable.levels_icon), r(R.drawable.levels_icon), r(R.drawable.levels_icon)),

			MENU_ITEM = s(r(null), r(Color.LABEL_LIGHT), r(Color.TRON_BLUE)),

			FILE = s(r(R.drawable.browse_icon, null, null, null, Color.BLACK), r(R.drawable.browse_icon, null, null, null, Color.TRON_BLUE)),
			SETTINGS = s(r(R.drawable.settings_icon, null, null, null, Color.BLACK), r(R.drawable.settings_icon, null, null, null, Color.TRON_BLUE)),
			SNAP_TO_GRID = s(r(R.drawable.snap_to_grid_icon), r(R.drawable.snap_to_grid_icon)),
			MIDI_IMPORT = s(r(R.drawable.midi_import_icon), r(R.drawable.midi_import_icon)),
			MIDI_EXPORT = s(r(R.drawable.midi_export_icon), r(R.drawable.midi_export_icon)),

			ATTACK = s(r(R.drawable.attack_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.attack_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.attack_icon, Color.TRON_BLUE, Color.BLACK)),
			DECAY = s(r(R.drawable.decay_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.decay_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.decay_icon, Color.TRON_BLUE, Color.BLACK)),
			SUSTAIN = s(r(R.drawable.sustain_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.sustain_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.sustain_icon, Color.TRON_BLUE, Color.BLACK)),
			RELEASE = s(r(R.drawable.release_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.release_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.release_icon, Color.TRON_BLUE, Color.BLACK)),
			
			VOLUME = s(r(Color.TRANSPARENT, Color.TRON_BLUE), r(Color.TRON_BLUE, Color.BLACK), r(Color.TRON_BLUE, Color.BLACK), r(Color.TRANSPARENT, Color.TRON_BLUE)),
			PAN = s(r(Color.TRANSPARENT, Color.PAN), r(Color.PAN, Color.BLACK), r(Color.PAN, Color.BLACK), r(Color.TRANSPARENT, Color.PAN)),
			PITCH = s(r(Color.TRANSPARENT, Color.PITCH), r(Color.PITCH, Color.BLACK), r(Color.PITCH, Color.BLACK), r(Color.TRANSPARENT, Color.PITCH)),

			PREVIEW = s(r(R.drawable.preview_icon, null, Color.WHITE), r(R.drawable.preview_icon_selected, null, Color.WHITE)),
			LOOP = s(r(R.drawable.loop_icon, null, Color.LABEL_VERY_LIGHT), r(R.drawable.loop_icon, null, Color.LABEL_SELECTED)),
			REVERSE = s(r(R.drawable.reverse_icon, null, Color.LABEL_VERY_LIGHT), r(R.drawable.reverse_icon, null, Color.LABEL_SELECTED)),

			LINK = s(r(R.drawable.link_icon, null, Color.WHITE), r(R.drawable.link_icon, null, Color.TRON_BLUE)),

			CONTROL_LABEL = s(r(-1, null, null, Color.TRON_BLUE, null), null),
			TOGGLE = s(r(R.drawable.toggle_icon, Color.LABEL_DARK, Color.LABEL_VERY_LIGHT), r(R.drawable.toggle_icon, Color.TRON_BLUE, Color.BLACK), r(R.drawable.toggle_icon, Color.LABEL_SELECTED, Color.BLACK, Color.BLACK, Color.TRON_BLUE)),

			BANDPASS_FILTER = s(r(R.drawable.bandpass_filter_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.bandpass_filter_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.bandpass_filter_icon, Color.TRON_BLUE, Color.BLACK)),
			HIGHPASS_FILTER = s(r(R.drawable.highpass_filter_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.highpass_filter_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.highpass_filter_icon, Color.TRON_BLUE, Color.BLACK)),
			LOWPASS_FILTER = s(r(R.drawable.lowpass_filter_icon, Color.TRANSPARENT, Color.WHITE), r(R.drawable.lowpass_filter_icon, Color.LABEL_SELECTED, Color.BLACK), r(R.drawable.lowpass_filter_icon, Color.TRON_BLUE, Color.BLACK)),
			
			MIDI_TICK_BAR = s(r(Color.TICK_FILL), r(Color.TICK_FILL)),
			MIDI_LOOP_BAR = s(r(Color.TICKBAR), r(Color.TRON_BLUE)),
			MIDI_LOOP_BUTTONS = s(r(Color.TRANSPARENT, Color.RED), r(Color.TRANSPARENT, Color.RED));

	private static Map<String, IconResourceSet> DIRECTORY_ICON_RESOURCES = new HashMap<String, IconResourceSet>() {
		{
			put("/", BROWSE);
			put("drums", DRUMS);
			put("recorded", MICROPHONE);
			put("beats", BEAT);
			put("samples", SAMPLE);
			put("kick", KICK);
			put("snare", SNARE);
			put("hh_closed", HH_CLOSED);
			put("hh_open", HH_OPEN);
			put("rim", RIMSHOT);
		}
	};

	public static final IconResourceSet forDirectory(String directoryName) {
		return DIRECTORY_ICON_RESOURCES.get(directoryName);
	}

	private static IconResource r(int resourceId) {
		return new IconResource(resourceId, null, null);
	}

	private static IconResource r(float[] fillColor) {
		return new IconResource(-1, fillColor, null);
	}
	
	private static IconResource r(float[] fillColor, float[] strokeColor) {
		return new IconResource(-1, fillColor, strokeColor);
	}

	private static IconResource r(int resourceId, float[] fillColor, float[] strokeColor) {
		return new IconResource(resourceId, fillColor, strokeColor);
	}

	private static IconResource r(int resourceId, float[] fillColor, float[] strokeColor, float[] textColor, float[] iconColor) {
		return new IconResource(resourceId, fillColor, strokeColor, textColor, iconColor);
	}

	private static IconResourceSet s(IconResource defaultIconResource, IconResource pressedIconResource) {
		return new IconResourceSet(defaultIconResource, pressedIconResource, null, null);
	}

	private static IconResourceSet s(IconResource defaultIconResource, IconResource pressedIconResource, IconResource selectedIconResource) {
		return new IconResourceSet(defaultIconResource, pressedIconResource, selectedIconResource, null);
	}

	private static IconResourceSet s(IconResource defaultIconResource, IconResource pressedIconResource, IconResource selectedIconResource, IconResource disabledIconResource) {
		return new IconResourceSet(defaultIconResource, pressedIconResource, selectedIconResource, disabledIconResource);
	}
}
