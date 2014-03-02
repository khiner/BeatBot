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
			PLAY = s(r(R.drawable.play_icon), r(R.drawable.play_icon_selected), r(R.drawable.play_icon_selected)),
			STOP = s(r(R.drawable.stop_icon), r(R.drawable.stop_icon_selected)),
			RECORD = s(r(R.drawable.record_icon), r(R.drawable.record_icon_selected), r(R.drawable.record_icon_selected)),
			UNDO = s(r(R.drawable.undo_icon), r(R.drawable.undo_icon_selected), null, r(R.drawable.undo_icon_disabled)),
			REDO = s(r(R.drawable.redo_icon), r(R.drawable.redo_icon_selected), null, r(R.drawable.redo_icon_disabled)),
			COPY = s(r(R.drawable.copy_icon), r(R.drawable.copy_icon_selected), null, r(R.drawable.copy_icon_disabled)),
			QUANTIZE = s(r(R.drawable.quantize_icon), r(R.drawable.quantize_icon_selected), null, r(R.drawable.quantize_icon_disabled)),
			DELETE_NOTE = s(r(R.drawable.delete_icon), r(R.drawable.delete_icon_selected), null, r(R.drawable.delete_icon_disabled)),

			INSTRUMENT_BASE = s(r(Colors.TRANSPARENT, Colors.WHITE), r(Colors.LABEL_SELECTED, Colors.BLACK), r(Colors.VOLUME, Colors.BLACK)),
			BROWSE = s(r(R.drawable.browse_icon), r(R.drawable.browse_icon_selected), r(R.drawable.browse_icon_selected)),
			DRUMS= s(r(R.drawable.drums_icon), r(R.drawable.drums_icon_selected), r(R.drawable.drums_icon_selected)),
			KICK= s(r(R.drawable.kick_icon), r(R.drawable.kick_icon_selected), r(R.drawable.kick_icon_selected)),
			SNARE= s(r(R.drawable.snare_icon), r(R.drawable.snare_icon_selected), r(R.drawable.snare_icon_selected)),
			HH_CLOSED = s(r(R.drawable.hh_closed_icon), r(R.drawable.hh_closed_icon_selected), r(R.drawable.hh_closed_icon_selected)),
			HH_OPEN = s(r(R.drawable.hh_open_icon), r(R.drawable.hh_open_icon_selected), r(R.drawable.hh_open_icon_selected)),
			RIMSHOT = s(r(R.drawable.rimshot_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.rimshot_icon_selected, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.rimshot_icon_selected, Colors.VOLUME, Colors.BLACK)),
			MICROPHONE = s(r(R.drawable.microphone_icon), r(R.drawable.microphone_icon_selected), r(R.drawable.microphone_icon_selected)),
			BEAT = s(r(R.drawable.beat_icon), r(R.drawable.beat_icon_selected), r(R.drawable.beat_icon_selected)),
			SAMPLE = s(r(R.drawable.sample_icon), r(R.drawable.sample_icon_selected), r(R.drawable.sample_icon_selected)),
			
			BROWSE_PAGE = s(r(Colors.LABEL_SELECTED), r(Colors.LABEL_SELECTED)),

			DELETE_TRACK = s(r(R.drawable.delete_track_icon, Colors.TRANSPARENT, Colors.RED), r(R.drawable.delete_track_icon_selected, Colors.RED, Colors.BLACK)),

			LABEL_BASE = s(r(Colors.LABEL_DARK, Colors.WHITE), r(Colors.VOLUME, Colors.BLACK), r(Colors.LABEL_SELECTED, Colors.BLACK)),
			ADD = s(r(R.drawable.plus_outline), r(R.drawable.plus_outline), r(R.drawable.plus_outline)),
			NOTE_LEVELS = s(r(R.drawable.note_levels_icon), r(R.drawable.note_levels_icon), r(R.drawable.note_levels_icon)),
			ADSR = s(r(R.drawable.adsr_icon), r(R.drawable.adsr_icon_selected), r(R.drawable.adsr_icon_selected)),
			LEVELS = s(r(R.drawable.levels_icon), r(R.drawable.levels_icon), r(R.drawable.levels_icon)),

			MENU_ITEM = s(r(Colors.TRANSPARENT), r(Colors.LABEL_LIGHT), r(Colors.VOLUME)),
			
			FILE = s(r(R.drawable.browse_icon_selected), r(R.drawable.browse_icon_menu_selected)),
	        MENU = s(r(R.drawable.menu_icon)),
			SETTINGS = s(r(R.drawable.settings_icon), r(R.drawable.settings_icon_selected)),
			SNAP_TO_GRID = s(r(R.drawable.snap_to_grid_icon)),
			MIDI_IMPORT = s(r(R.drawable.midi_import_icon)),
			MIDI_EXPORT = s(r(R.drawable.midi_export_icon)),

			ATTACK = s(r(R.drawable.attack_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.attack_icon_selected, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.attack_icon_selected, Colors.VOLUME, Colors.BLACK)),
			DECAY = s(r(R.drawable.decay_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.decay_icon_selected, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.decay_icon_selected, Colors.VOLUME, Colors.BLACK)),
			SUSTAIN = s(r(R.drawable.sustain_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.sustain_icon_selected, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.sustain_icon_selected, Colors.VOLUME, Colors.BLACK)),
			RELEASE = s(r(R.drawable.release_icon, Colors.TRANSPARENT, Colors.WHITE), r(R.drawable.release_icon_selected, Colors.LABEL_SELECTED, Colors.BLACK), r(R.drawable.release_icon_selected, Colors.VOLUME, Colors.BLACK)),
			
			VOLUME = s(r(Colors.TRANSPARENT, Colors.VOLUME), r(Colors.VOLUME, Colors.BLACK), r(Colors.VOLUME, Colors.BLACK), r(Colors.TRANSPARENT, Colors.VOLUME)),
			PAN = s(r(Colors.TRANSPARENT, Colors.PAN), r(Colors.PAN, Colors.BLACK), r(Colors.PAN, Colors.BLACK), r(Colors.TRANSPARENT, Colors.PAN)),
			PITCH = s(r(Colors.TRANSPARENT, Colors.PITCH), r(Colors.PITCH, Colors.BLACK), r(Colors.PITCH, Colors.BLACK), r(Colors.TRANSPARENT, Colors.PITCH)),

			LOOP = s(r(R.drawable.loop_icon), r(R.drawable.loop_icon_selected)),
			PREVIEW = s(r(R.drawable.preview_icon), r(R.drawable.preview_icon_selected)),
			REVERSE = s(r(R.drawable.reverse_icon), r(R.drawable.reverse_icon_selected)),

			LINK = s(r(R.drawable.link_icon), r(R.drawable.link_icon_selected)),

			TOGGLE = s(r(R.drawable.off_icon, Colors.LABEL_DARK, Colors.WHITE), r(R.drawable.on_icon, Colors.VOLUME, Colors.BLACK), r(R.drawable.on_icon, Colors.LABEL_SELECTED, Colors.BLACK)),

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
