package com.kh.beatbot.ui;

import com.kh.beatbot.R;

public class IconResources {
	public static final IconResource
		BANDPASS_FILTER = new IconResource(R.drawable.bandpass_filter_icon),
		HIGHPASS_FILTER = new IconResource(R.drawable.highpass_filter_icon),
		LOWPASS_FILTER = new IconResource(R.drawable.lowpass_filter_icon),
		
		ATTACK = new IconResource(R.drawable.attack_icon, R.drawable.attack_icon_selected, R.drawable.attack_icon_selected),
		DECAY = new IconResource(R.drawable.decay_icon, R.drawable.decay_icon_selected, R.drawable.decay_icon_selected),
		SUSTAIN = new IconResource(R.drawable.sustain_icon, R.drawable.sustain_icon_selected, R.drawable.sustain_icon_selected),
		RELEASE = new IconResource(R.drawable.release_icon, R.drawable.release_icon_selected, R.drawable.release_icon_selected),
		
		BEAT_SYNC = new IconResource(R.drawable.clock, -1, R.drawable.note_icon),
		LINK = new IconResource(R.drawable.link_broken, -1, R.drawable.link),
		ON_OFF = new IconResource(R.drawable.off_icon, -1, R.drawable.on_icon),
		
		PLAY = new IconResource(R.drawable.play_icon, R.drawable.play_icon_pressed, R.drawable.play_icon_selected),
		RECORD = new IconResource(R.drawable.rec_off_icon, R.drawable.rec_icon_pressed, R.drawable.rec_on_icon_selected),
		STOP = new IconResource(R.drawable.stop_icon, R.drawable.stop_icon_pressed),
		
		ADD = new IconResource(R.drawable.plus_outline),
		COPY = new IconResource(R.drawable.copy_icon, R.drawable.copy_icon_pressed, R.drawable.copy_icon_pressed, R.drawable.copy_icon_inactive),
		DELETE_NOTE = new IconResource(R.drawable.delete_icon, R.drawable.delete_icon_pressed, -1, R.drawable.delete_icon_inactive),
		DELETE_TRACK = new IconResource(R.drawable.delete_track_icon, R.drawable.delete_track_icon_selected),
		UNDO = new IconResource(R.drawable.undo_icon, R.drawable.undo_icon_pressed, -1, R.drawable.undo_icon_disabled),
		
		LEVELS = new IconResource(R.drawable.levels_icon, -1, R.drawable.levels_icon_selected),
		LOOP = new IconResource(R.drawable.loop_icon, -1, R.drawable.loop_icon_selected),
		PREVIEW = new IconResource(R.drawable.preview_icon, R.drawable.preview_icon_selected),
		REVERSE = new IconResource(R.drawable.reverse_icon, -1, R.drawable.reverse_icon_selected),
		
		BROWSE = new IconResource(R.drawable.browse_icon, R.drawable.browse_icon_selected),
		EDIT = new IconResource(R.drawable.edit_icon, R.drawable.edit_icon_selected),
		
		DRUMS = new IconResource(R.drawable.drums_icon, R.drawable.drums_icon_selected, R.drawable.drums_icon_selected, -1, R.drawable.drums_icon_selected, R.drawable.drums_icon),
		KICK = new IconResource(R.drawable.kick_icon, R.drawable.kick_icon_selected, R.drawable.kick_icon_selected, -1, R.drawable.kick_icon_selected, R.drawable.kick_icon),
		SNARE = new IconResource(R.drawable.snare_icon, R.drawable.snare_icon_selected, R.drawable.snare_icon_selected, -1, R.drawable.snare_icon_selected, R.drawable.snare_icon),
		HH_CLOSED = new IconResource(R.drawable.hh_closed_icon, R.drawable.hh_closed_icon_selected, R.drawable.hh_closed_icon_selected, -1, R.drawable.hh_closed_icon_selected, R.drawable.hh_closed_icon),
		HH_OPEN = new IconResource(R.drawable.hh_open_icon, R.drawable.hh_open_icon_selected, R.drawable.hh_open_icon_selected, -1, R.drawable.hh_open_icon_selected, R.drawable.hh_open_icon),
		RIMSHOT = new IconResource(R.drawable.rimshot_icon, R.drawable.rimshot_icon_selected, R.drawable.rimshot_icon_selected, -1, R.drawable.rimshot_icon_selected, R.drawable.rimshot_icon),
		
		MICROPHONE = new IconResource(R.drawable.microphone_icon, R.drawable.microphone_icon_selected, R.drawable.microphone_icon_selected, -1, R.drawable.microphone_icon_selected, R.drawable.microphone_icon),
		BEAT = new IconResource(R.drawable.beat_icon, R.drawable.beat_icon_selected, R.drawable.beat_icon_selected, -1, R.drawable.beat_icon_selected, R.drawable.beat_icon),
		SAMPLE = new IconResource(R.drawable.sample_icon, R.drawable.sample_icon_selected, R.drawable.sample_icon_selected, -1, R.drawable.sample_icon_selected, R.drawable.sample_icon);
}
