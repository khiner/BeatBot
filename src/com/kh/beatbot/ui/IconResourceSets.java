package com.kh.beatbot.ui;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.R;
import com.kh.beatbot.ui.color.Colors;

public class IconResourceSets {
	public static IconResourceSet
			DEFAULT = s(r(Colors.VIEW_BG, Colors.VOLUME), r(Colors.VIEW_BG_SELECTED, Colors.VOLUME)),
			SAMPLE_LOOP = s(r(Colors.VOLUME_TRANS), r(Colors.VOLUME)),
			SAMPLE_BG = s(r(Colors.LABEL_LIGHT), r(Colors.LABEL_VERY_LIGHT)),
			MUTE = s(r(Colors.TRANSPARENT, Colors.WHITE), r(Colors.LABEL_SELECTED, Colors.BLACK), r(Colors.PAN, Colors.BLACK)),
			SOLO = s(r(Colors.TRANSPARENT, Colors.WHITE), r(Colors.LABEL_SELECTED, Colors.BLACK), r(Colors.PITCH, Colors.BLACK)),
			BEAT_SYNC = s(r(R.drawable.clock), null, r(R.drawable.note_icon)),
			VALUE_LABEL = s(r(Colors.LABEL_VERY_LIGHT, Colors.BLACK), r(Colors.LABEL_SELECTED, Colors.BLACK),
					null, r(Colors.LABEL_DARK, Colors.BLACK)),
			PLAY = s(r(R.drawable.play_icon, null, Colors.WHITE), r(R.drawable.play_icon_selected, null, Colors.WHITE), r(R.drawable.play_icon_selected, null, Colors.WHITE)),
			STOP = s(r(R.drawable.stop_icon, null, Colors.WHITE), r(R.drawable.stop_icon, null, Colors.LABEL_SELECTED)),
			RECORD = s(r(R.drawable.record_icon, null, Colors.WHITE), r(R.drawable.record_icon_selected, null, Colors.WHITE), r(R.drawable.record_icon_selected, null, Colors.WHITE)),
			UNDO = s(r(R.drawable.undo_icon, null, Colors.WHITE), r(R.drawable.undo_icon, null, Colors.LABEL_SELECTED), null, r(R.drawable.undo_icon, null, Colors.SEMI_TRANSPARENT)),
			REDO = s(r(R.drawable.redo_icon, null, Colors.WHITE), r(R.drawable.redo_icon, null, Colors.LABEL_SELECTED), null, r(R.drawable.redo_icon, null, Colors.SEMI_TRANSPARENT)),
			COPY = s(r(R.drawable.copy_icon, null, Colors.WHITE), r(R.drawable.copy_icon, null, Colors.VOLUME), r(R.drawable.copy_icon, null, Colors.VOLUME), r(R.drawable.copy_icon, null, Colors.SEMI_TRANSPARENT)),
			QUANTIZE = s(r(R.drawable.quantize_icon, null, Colors.WHITE), r(R.drawable.quantize_icon, null, Colors.VOLUME), null, r(R.drawable.quantize_icon, null, Colors.SEMI_TRANSPARENT)),
			DELETE_NOTE = s(r(R.drawable.delete_icon, null, Colors.WHITE), r(R.drawable.delete_icon, null, Colors.LEVEL_SELECTED), null, r(R.drawable.delete_icon, null, Colors.SEMI_TRANSPARENT)),

			INSTRUMENT_BASE = s(r(Colors.TRANSPARENT, Colors.WHITE), r(Colors.LABEL_SELECTED, Colors.BLACK), r(Colors.VOLUME, Colors.BLACK)),
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
			
			BROWSE_PAGE = s(r(Colors.LABEL_SELECTED), r(Colors.LABEL_SELECTED)),
			SLIDE_MENU = s(r(R.drawable.menu_icon, Colors.LABEL_SELECTED, null), r(R.drawable.menu_icon, Colors.LABEL_SELECTED, null)),
					
			DELETE_TRACK = s(r(R.drawable.delete_track_icon, Colors.TRANSPARENT, Colors.RED), r(R.drawable.delete_track_icon, Colors.RED, Colors.BLACK)),

			LABEL_BASE = s(r(Colors.LABEL_DARK, Colors.WHITE), r(Colors.VOLUME, Colors.BLACK), r(Colors.LABEL_SELECTED, Colors.BLACK)),
			ADD = s(r(R.drawable.plus_outline), r(R.drawable.plus_outline), r(R.drawable.plus_outline)),
			NOTE_LEVELS = s(r(R.drawable.note_levels_icon), r(R.drawable.note_levels_icon), r(R.drawable.note_levels_icon)),
			ADSR = s(r(R.drawable.adsr_icon, null, Colors.WHITE), r(R.drawable.adsr_icon, null, Colors.BLACK), r(R.drawable.adsr_icon, null, Colors.BLACK)),
			LEVELS = s(r(R.drawable.levels_icon), r(R.drawable.levels_icon), r(R.drawable.levels_icon)),

			MENU_ITEM = s(r(null), r(Colors.LABEL_LIGHT), r(Colors.VOLUME)),
			
			FILE = s(r(R.drawable.browse_icon, null, Colors.BLACK), r(R.drawable.browse_icon, null, Colors.VOLUME)),
			SETTINGS = s(r(R.drawable.settings_icon, null, Colors.BLACK), r(R.drawable.settings_icon, null, Colors.VOLUME)),
			SNAP_TO_GRID = s(r(R.drawable.snap_to_grid_icon), null, r(R.drawable.snap_to_grid_icon)),
			MIDI_IMPORT = s(r(R.drawable.midi_import_icon), null, r(R.drawable.midi_import_icon)),
			MIDI_EXPORT = s(r(R.drawable.midi_export_icon)),

			ATTACK = s(r(R.drawable.attack_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.attack_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.attack_icon, Colors.VOLUME, Colors.BLACK)),
			DECAY = s(r(R.drawable.decay_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.decay_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.decay_icon, Colors.VOLUME, Colors.BLACK)),
			SUSTAIN = s(r(R.drawable.sustain_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.sustain_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.sustain_icon, Colors.VOLUME, Colors.BLACK)),
			RELEASE = s(r(R.drawable.release_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.release_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.release_icon, Colors.VOLUME, Colors.BLACK)),
			
			VOLUME = s(r(Colors.TRANSPARENT, Colors.VOLUME), r(Colors.VOLUME, Colors.BLACK), r(Colors.VOLUME, Colors.BLACK), r(Colors.TRANSPARENT, Colors.VOLUME)),
			PAN = s(r(Colors.TRANSPARENT, Colors.PAN), r(Colors.PAN, Colors.BLACK), r(Colors.PAN, Colors.BLACK), r(Colors.TRANSPARENT, Colors.PAN)),
			PITCH = s(r(Colors.TRANSPARENT, Colors.PITCH), r(Colors.PITCH, Colors.BLACK), r(Colors.PITCH, Colors.BLACK), r(Colors.TRANSPARENT, Colors.PITCH)),

			LOOP = s(r(R.drawable.loop_icon, null, Colors.WHITE), r(R.drawable.loop_icon, null, Colors.VOLUME)),
			PREVIEW = s(r(R.drawable.preview_icon, null, Colors.WHITE), r(R.drawable.preview_icon_selected, null, Colors.WHITE)),
			REVERSE = s(r(R.drawable.reverse_icon, null, Colors.WHITE), r(R.drawable.reverse_icon, null, Colors.VOLUME)),

			LINK = s(r(R.drawable.link_icon, null, Colors.WHITE), r(R.drawable.link_icon, null, Colors.VOLUME)),

			TOGGLE = s(r(R.drawable.toggle_icon, Colors.LABEL_DARK, Colors.LABEL_LIGHT), r(R.drawable.toggle_icon, Colors.VOLUME, Colors.VOLUME), r(R.drawable.toggle_icon, Colors.LABEL_SELECTED, Colors.VOLUME)),

			BANDPASS_FILTER = s(r(R.drawable.bandpass_filter_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.bandpass_filter_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.bandpass_filter_icon, Colors.VOLUME, Colors.BLACK)),
			HIGHPASS_FILTER = s(r(R.drawable.highpass_filter_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.highpass_filter_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.highpass_filter_icon, Colors.VOLUME, Colors.BLACK)),
			LOWPASS_FILTER = s(r(R.drawable.lowpass_filter_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.lowpass_filter_icon, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.lowpass_filter_icon, Colors.VOLUME, Colors.BLACK));

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

	private static IconResourceSet s(IconResource defaultIconResource) {
		return new IconResourceSet(defaultIconResource, null, null, null);
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
