package com.odang.beatbot.listener;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;

public interface TrackListener {
	void onCreate(Track track);

	void onDestroy(Track track);

	void onSelect(BaseTrack track);

	void onSampleChange(Track track);

	void onMuteChange(Track track, boolean mute);

	void onSoloChange(Track track, boolean solo);

	void onReverseChange(Track track, boolean reverse);
	
	void onLoopChange(Track track, boolean loop);

	void onEffectCreate(BaseTrack track, Effect effect);

	void onEffectDestroy(BaseTrack track, Effect effect);

	void onEffectOrderChange(BaseTrack track, int initialEffectPosition, int endEffectPosition);
}
