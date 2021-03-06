package com.odang.beatbot.ui.view;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.listener.TrackListener;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.shape.RenderGroup;

public class MidiTrackView extends TouchableView implements TrackListener {
    public MidiTrackView(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
    }

    @Override
    public void layoutChildren() {
        for (int i = 0; i < context.getTrackManager().getNumTracks(); i++) {
            final Track track = context.getTrackManager().getTrackByNoteValue(i);
            final TrackButtonRow buttonRow = track.getButtonRow();
            buttonRow.layout(this, 0, MidiView.trackHeight * i, width, MidiView.trackHeight);
        }
    }

    @Override
    public float getYTouchTranslate() {
        return context.getMainPage().getMidiView().getYOffset();
    }

    @Override
    public void onCreate(Track track) {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            TrackButtonRow buttonRow = new TrackButtonRow(this, track);
            track.setButtonRow(buttonRow);
            layoutChildren();
        }
    }

    @Override
    public void onDestroy(Track track) {
        synchronized (context.getMainPage().getMidiViewGroup()) {
            TrackButtonRow buttonRow = track.getButtonRow();
            removeChild(buttonRow);
            layoutChildren();
            track.setButtonRow(null);
        }
    }

    @Override
    public void onSelect(BaseTrack track) {
    }

    @Override
    public void onSampleChange(Track track) {
        track.getButtonRow().updateInstrumentIcon();
    }

    @Override
    public void onMuteChange(Track track, boolean mute) {
        track.getButtonRow().muteButton.setChecked(mute);
    }

    @Override
    public void onSoloChange(Track track, boolean solo) {
        track.getButtonRow().soloButton.setChecked(solo);
    }

    @Override
    public void onReverseChange(Track track, boolean reverse) {
    }

    @Override
    public void onLoopChange(Track track, boolean loop) {
    }

    @Override
    public void onEffectCreate(BaseTrack track, Effect effect) {
    }

    @Override
    public void onEffectDestroy(BaseTrack track, Effect effect) {
    }

    @Override
    public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
                                    int endEffectPosition) {
    }
}
