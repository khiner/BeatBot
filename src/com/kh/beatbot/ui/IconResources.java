package com.kh.beatbot.ui;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.R;

public class IconResources {
	public static IconResource BANDPASS_FILTER, HIGHPASS_FILTER,
			LOWPASS_FILTER, ADSR, ATTACK, DECAY, SUSTAIN, RELEASE, BEAT_SYNC,
			LINK, ON_OFF, PLAY, STOP, RECORD, ADD, COPY, DELETE_NOTE,
			DELETE_TRACK, UNDO, REDO, QUANTIZE, LEVELS, NOTE_LEVELS, PREVIEW,
			LOOP, REVERSE, BROWSE, DRUMS, KICK, SNARE, HH_CLOSED, HH_OPEN,
			RIMSHOT, SAMPLE, MICROPHONE, BEAT, FILE, MENU, SETTINGS,
			SNAP_TO_GRID, MIDI_IMPORT, MIDI_EXPORT;

	private static Map<String, IconResource> DIRECTORY_ICON_RESOURCES;

	public static final IconResource forDirectory(String directoryName) {
		return DIRECTORY_ICON_RESOURCES.get(directoryName);
	}

	public static void init() {
		BANDPASS_FILTER = new IconResource(R.drawable.bandpass_filter_icon);
		HIGHPASS_FILTER = new IconResource(R.drawable.highpass_filter_icon);
		LOWPASS_FILTER = new IconResource(R.drawable.lowpass_filter_icon);

		ADSR = new IconResource(R.drawable.adsr_icon, -1,
				R.drawable.adsr_icon_selected);

		ATTACK = new IconResource(R.drawable.attack_icon, -1,
				R.drawable.attack_icon_selected);
		DECAY = new IconResource(R.drawable.decay_icon, -1,
				R.drawable.decay_icon_selected);
		SUSTAIN = new IconResource(R.drawable.sustain_icon, -1,
				R.drawable.sustain_icon_selected);
		RELEASE = new IconResource(R.drawable.release_icon, -1,
				R.drawable.release_icon_selected);

		BEAT_SYNC = new IconResource(R.drawable.clock, -1, R.drawable.note_icon);
		LINK = new IconResource(R.drawable.link_icon, -1, R.drawable.link_icon_selected);
		ON_OFF = new IconResource(R.drawable.off_icon, -1, R.drawable.on_icon);

		PLAY = new IconResource(R.drawable.play_icon,
				R.drawable.play_icon_selected, R.drawable.play_icon_selected);
		RECORD = new IconResource(R.drawable.record_icon,
				R.drawable.record_icon_selected,
				R.drawable.record_icon_selected);
		STOP = new IconResource(R.drawable.stop_icon,
				R.drawable.stop_icon_selected);

		ADD = new IconResource(R.drawable.plus_outline);
		COPY = new IconResource(R.drawable.copy_icon,
				R.drawable.copy_icon_selected, -1,
				R.drawable.copy_icon_disabled);
		DELETE_NOTE = new IconResource(R.drawable.delete_icon,
				R.drawable.delete_icon_selected, -1,
				R.drawable.delete_icon_disabled);
		DELETE_TRACK = new IconResource(R.drawable.delete_track_icon,
				R.drawable.delete_track_icon_selected);
		UNDO = new IconResource(R.drawable.undo_icon,
				R.drawable.undo_icon_selected, -1,
				R.drawable.undo_icon_disabled);
		REDO = new IconResource(R.drawable.redo_icon,
				R.drawable.redo_icon_selected, -1,
				R.drawable.redo_icon_disabled);
		QUANTIZE = new IconResource(R.drawable.quantize_icon,
				R.drawable.quantize_icon_selected, -1,
				R.drawable.quantize_icon_disabled);

		LEVELS = new IconResource(R.drawable.levels_icon);
		NOTE_LEVELS = new IconResource(R.drawable.note_levels_icon);
		LOOP = new IconResource(R.drawable.loop_icon, -1,
				R.drawable.loop_icon_selected);
		PREVIEW = new IconResource(R.drawable.preview_icon,
				R.drawable.preview_icon_selected);
		REVERSE = new IconResource(R.drawable.reverse_icon, -1,
				R.drawable.reverse_icon_selected);
		BROWSE = new IconResource(R.drawable.browse_icon,
				R.drawable.browse_icon_selected);

		DRUMS = new IconResource(R.drawable.drums_icon, -1,
				R.drawable.drums_icon_selected);
		KICK = new IconResource(R.drawable.kick_icon, -1,
				R.drawable.kick_icon_selected);
		SNARE = new IconResource(R.drawable.snare_icon, -1,
				R.drawable.snare_icon_selected);
		HH_CLOSED = new IconResource(R.drawable.hh_closed_icon, -1,
				R.drawable.hh_closed_icon_selected);
		HH_OPEN = new IconResource(R.drawable.hh_open_icon, -1,
				R.drawable.hh_open_icon_selected);
		RIMSHOT = new IconResource(R.drawable.rimshot_icon, -1,
				R.drawable.rimshot_icon_selected);

		MICROPHONE = new IconResource(R.drawable.microphone_icon, -1,
				R.drawable.microphone_icon_selected);
		BEAT = new IconResource(R.drawable.beat_icon, -1,
				R.drawable.beat_icon_selected);
		SAMPLE = new IconResource(R.drawable.sample_icon, -1,
				R.drawable.sample_icon_selected);

		FILE = new IconResource(R.drawable.browse_icon_selected, -1,
				R.drawable.browse_icon_menu_selected);
		MENU = new IconResource(R.drawable.menu_icon);
		SETTINGS = new IconResource(R.drawable.settings_icon, -1,
				R.drawable.settings_icon_selected);
		SNAP_TO_GRID = new IconResource(R.drawable.snap_to_grid_icon);

		MIDI_IMPORT = new IconResource(R.drawable.midi_import_icon);
		MIDI_EXPORT = new IconResource(R.drawable.midi_export_icon);

		DIRECTORY_ICON_RESOURCES = new HashMap<String, IconResource>() {
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
	}
}
