package com.odang.beatbot.manager;

import android.app.AlertDialog;
import android.content.Context;

import com.odang.beatbot.activity.BeatBotActivity;
import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.listener.FileListener;
import com.odang.beatbot.listener.MidiNoteListener;
import com.odang.beatbot.listener.TempoListener;
import com.odang.beatbot.listener.TrackLevelsEventListener;
import com.odang.beatbot.listener.TrackListener;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrackManager implements TrackListener, FileListener, MidiNoteListener, TempoListener {
    public static final int MASTER_TRACK_ID = -1;

    private final BaseTrack masterTrack = new BaseTrack(MASTER_TRACK_ID);
    private final List<Track> tracks = new ArrayList<>();
    private final Set<TrackListener> trackListeners = new HashSet<>();
    private final Set<TrackLevelsEventListener> trackLevelsEventListeners = new HashSet<>();

    private AlertDialog.Builder sampleSaveErrorAlert;

    private Track currTrack;

    public TrackManager(final BeatBotActivity context) {
        context.getFileManager().addListener(this);
        context.getMidiManager().addMidiNoteListener(this);
        context.getMidiManager().addTempoListener(this);
        context.onTrackManagerInit(this); // notify native
    }

    public BaseTrack getMasterTrack() {
        return masterTrack;
    }

    public List<Track> getTracks() {
        return tracks; // XXX shouldn't provide full list - concurrency threat
    }

    public void addTrackListener(TrackListener trackListener) {
        trackListeners.add(trackListener);
    }

    public void addTrackLevelsEventListener(TrackLevelsEventListener listener) {
        trackLevelsEventListeners.add(listener);
    }

    public void notifyTrackLevelsSetEvent(BaseTrack track) {
        if (!track.equals(currTrack)) {
            track.select();
        }
        for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
            listener.onTrackLevelsChange(track);
        }
    }

    public void notifyLoopWindowSetEvent(Track track) {
        if (!track.equals(currTrack)) {
            track.select();
        }
        for (TrackLevelsEventListener listener : trackLevelsEventListeners) {
            listener.onSampleLoopWindowChange(track);
        }
    }

    public void init(Context context) {
        sampleSaveErrorAlert = new AlertDialog.Builder(context);
        sampleSaveErrorAlert.setPositiveButton("OK", null);
    }

    public void setSample(Track track, File sampleFile) {
        try {
            track.setSample(sampleFile);
        } catch (Exception e) {
            sampleSaveErrorAlert.setTitle("Error loading " + sampleFile.getName() + ".")
                    .setMessage(e.getMessage()).create().show();
        }
    }

    public void destroy() {
        List<Track> tracksToDestroy = new ArrayList<Track>();
        tracksToDestroy.addAll(tracks);
        for (Track track : tracksToDestroy) {
            track.destroy();
        }
    }

    public void selectTrackNotesExclusive(Track track) {
        deselectAllNotes();
        track.selectAllNotes();
    }

    public void deselectAllNotes() {
        for (Track track : tracks) {
            track.deselectAllNotes();
        }
    }

    public List<MidiNote> copySelectedNotes() {
        List<MidiNote> selectedNotesCopy = new ArrayList<MidiNote>();
        for (Track track : tracks) {
            for (MidiNote note : track.getMidiNotes()) {
                if (note.isSelected()) {
                    selectedNotesCopy.add(note.getCopy());
                }
            }
        }
        return selectedNotesCopy;
    }

    public List<MidiNote> getSelectedNotes() {
        final List<MidiNote> selectedNotes = new ArrayList<MidiNote>();
        for (Track track : tracks) {
            for (MidiNote note : track.getMidiNotes()) {
                if (note.isSelected()) {
                    selectedNotes.add(note);
                }
            }
        }
        return selectedNotes;
    }

    public float getAdjustedTickDiff(float tickDiff, long startOnTick, MidiNote singleNote) {
        if (tickDiff == 0)
            return 0;
        float adjustedTickDiff = tickDiff;
        for (Track track : tracks) {
            for (MidiNote note : track.getMidiNotes()) {
                if (note.isSelected()) {
                    if (null != singleNote && !note.equals(singleNote))
                        continue;
                    if (Math.abs(startOnTick - note.getOnTick()) + Math.abs(tickDiff) <= 10) {
                        // inside threshold distance - set to original position
                        return startOnTick - note.getOnTick();
                    }
                    if (note.getOnTick() < -adjustedTickDiff) {
                        adjustedTickDiff = -note.getOnTick();
                    } else if (MidiManager.MAX_TICKS - note.getOffTick() < adjustedTickDiff) {
                        adjustedTickDiff = MidiManager.MAX_TICKS - note.getOffTick();
                    }
                }
            }
        }
        return adjustedTickDiff;
    }

    public int getAdjustedNoteDiff(int noteDiff, MidiNote singleNote) {
        int adjustedNoteDiff = noteDiff;
        for (Track track : tracks) {
            for (MidiNote note : track.getMidiNotes()) {
                if (note.isSelected()) {
                    if (singleNote != null && !note.equals(singleNote))
                        continue;
                    if (note.getNoteValue() < -adjustedNoteDiff) {
                        adjustedNoteDiff = -note.getNoteValue();
                    } else if (tracks.size() - 1 - note.getNoteValue() < adjustedNoteDiff) {
                        adjustedNoteDiff = tracks.size() - 1 - note.getNoteValue();
                    }
                }
            }
        }
        return adjustedNoteDiff;
    }

    public void selectRegion(long leftTick, long rightTick, int topNote, int bottomNote) {
        for (Track track : tracks) {
            track.selectRegion(leftTick, rightTick, topNote, bottomNote);
        }
    }

    public void saveNoteTicks() {
        for (Track track : tracks) {
            track.saveNoteTicks();
        }
    }

    // return true if any Midi note exists
    public boolean anyNotes() {
        for (Track track : tracks) {
            if (track.anyNotes()) {
                return true;
            }
        }
        return false;
    }

    // return true if any Midi note is selected
    public boolean anyNoteSelected() {
        for (Track track : tracks) {
            if (track.anyNoteSelected()) {
                return true;
            }
        }
        return false;
    }

    public long[] getSelectedNoteTickWindow() {
        long[] selectedNoteTickWindow = {Long.MAX_VALUE, Long.MIN_VALUE};
        for (Track track : tracks) {
            for (MidiNote note : track.getMidiNotes()) {
                if (note.isSelected() && note.getOnTick() < selectedNoteTickWindow[0]) {
                    selectedNoteTickWindow[0] = note.getOnTick();
                }
                if (note.isSelected() && note.getOffTick() > selectedNoteTickWindow[1]) {
                    selectedNoteTickWindow[1] = note.getOffTick();
                }
            }
        }
        return selectedNoteTickWindow;
    }

    public Track getTrackByNoteValue(int noteValue) {
        return noteValue < tracks.size() ? tracks.get(noteValue) : null;
    }

    public BaseTrack getCurrTrack() {
        return currTrack == null ? masterTrack : currTrack;
    }

    public Track getTrackById(int trackId) {
        for (Track track : tracks) {
            if (track.getId() == trackId)
                return track;
        }

        return null;
    }

    public Track getTrack(MidiNote note) {
        return getTrackByNoteValue(note.getNoteValue());
    }

    public BaseTrack getBaseTrackById(int trackId) {
        return trackId == MASTER_TRACK_ID ? masterTrack : getTrackById(trackId);
    }

    public Track getSoloingTrack() {
        for (Track track : tracks) {
            if (track.isSoloing())
                return track;
        }
        return null;
    }

    public int getNumTracks() {
        return tracks.size();
    }

    public Track createTrack() {
        int id = tracks.isEmpty() ? 0 : tracks.get(tracks.size() - 1).getId() + 1;
        return createTrack(id, tracks.size());
    }

    public Track createTrack(int trackId, int position) {
        createTrackNative(trackId);
        final Track newTrack = new Track(trackId);
        tracks.add(position, newTrack);
        onCreate(newTrack);
        return newTrack;
    }

    private void quantizeEffectParams() {
        for (Track track : tracks) {
            track.quantizeEffectParams();
        }
    }

    // Don't delete! Called from C through JNI
    public MidiNote getNextMidiNote(int trackId, long currTick) {
        return getTrackById(trackId).getNextMidiNote(currTick);
    }

    public void updateAllTrackNextNotes() {
        for (Track track : tracks) {
            track.updateNextNote();
        }
    }

    @Override
    public void onCreate(Track track) {
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setNoteValue(i);
        }
        for (TrackListener trackListener : trackListeners) {
            trackListener.onCreate(track);
        }
        track.updateNextNote();
        track.select();
    }

    @Override
    public void onDestroy(Track track) {
        tracks.remove(track);
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setNoteValue(i);
        }
        if (!tracks.isEmpty()) {
            tracks.get(Math.min(track.getId(), tracks.size() - 1)).select();
        }

        for (TrackListener trackListener : trackListeners) {
            trackListener.onDestroy(track);
        }
    }

    @Override
    public void onSelect(BaseTrack track) {
        if ((currTrack != null && currTrack.equals(track)) ||
                (currTrack == null && masterTrack.equals(track)))
            return; // already selected

        if (track instanceof Track) {
            currTrack = (Track) track;
            currTrack.getButtonRow().instrumentButton.setChecked(true);
        } else {
            currTrack = null;
        }

        for (Track otherTrack : tracks) {
            if (!track.equals(otherTrack)) {
                otherTrack.getButtonRow().instrumentButton.setChecked(false);
            }
        }
        for (TrackListener trackListener : trackListeners) {
            trackListener.onSelect(track);
        }
    }

    @Override
    public void onEffectCreate(BaseTrack track, Effect effect) {
        track.select();
        for (TrackListener trackListener : trackListeners) {
            trackListener.onEffectCreate(track, effect);
        }
    }

    @Override
    public void onEffectDestroy(BaseTrack track, Effect effect) {
        track.select();
        for (TrackListener trackListener : trackListeners) {
            trackListener.onEffectDestroy(track, effect);
        }
    }

    @Override
    public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
                                    int endEffectPosition) {
        track.select();
        for (TrackListener trackListener : trackListeners) {
            trackListener.onEffectOrderChange(track, initialEffectPosition, endEffectPosition);
        }
    }

    @Override
    public void onSampleChange(Track track) {
        for (TrackListener trackListener : trackListeners) {
            trackListener.onSampleChange(track);
        }
    }

    @Override
    public void onMuteChange(Track track, boolean mute) {
        for (TrackListener trackListener : trackListeners) {
            trackListener.onMuteChange(track, mute);
        }
    }

    @Override
    public void onSoloChange(Track track, boolean solo) {
        if (solo) {
            for (Track otherTrack : tracks) {
                if (!otherTrack.equals(track) && otherTrack.isSoloing()) {
                    otherTrack.soloGuiOnly(false);
                }
            }
        }
        for (TrackListener trackListener : trackListeners) {
            trackListener.onSoloChange(track, solo);
        }
    }

    @Override
    public void onReverseChange(Track track, boolean reverse) {
        for (TrackListener trackListener : trackListeners) {
            trackListener.onReverseChange(track, reverse);
        }
    }

    @Override
    public void onLoopChange(Track track, boolean loop) {
        for (TrackListener trackListener : trackListeners) {
            trackListener.onLoopChange(track, loop);
        }
    }

    @Override
    public void onNameChange(File file, File newFile) {
        for (Track track : tracks) {
            if (track.getCurrSampleFile().equals(file)) {
                track.onNameChange(file, newFile);
            }
        }
    }

    @Override
    public void onCreate(MidiNote note) {
        Track track = getTrack(note);
        if (null != track) {
            track.addNote(note);
        }
    }

    @Override
    public void onDestroy(MidiNote note) {
        Track track = getTrack(note);
        if (null != track) {
            track.removeNote(note);
        }
    }

    @Override
    public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
                       int endNoteValue, long endOnTick, long endOffTick) {
        if (beginNoteValue == endNoteValue) {
            Track track = getTrackByNoteValue(beginNoteValue);
            // if we're changing the stop tick on a note that's already playing to a
            // note before the current tick, or moving the start tick to after the playhead
            // stop the track
            final long currTick = View.context.getMidiManager().getCurrTick();
            final boolean playing = beginOnTick <= currTick && beginOffTick >= currTick;
            if (playing && (endOffTick < currTick || endOnTick > currTick)) {
                track.stop();
            } else {
                track.updateNextNote();
            }
        } else {
            Track oldTrack = getTrackByNoteValue(beginNoteValue);
            Track newTrack = getTrackByNoteValue(endNoteValue);
            if (null != oldTrack)
                oldTrack.removeNote(note);
            if (null != newTrack)
                newTrack.addNote(note);
        }
    }

    @Override
    public void onSelectStateChange(MidiNote note) {
        // no-op
    }

    @Override
    public void beforeLevelChange(MidiNote note) {
        // no-op
    }

    @Override
    public void onLevelChange(MidiNote note, LevelType type) {
        getTrack(note).updateNextNote();
    }

    @Override
    public void onTempoChange(float bpm) {
        quantizeEffectParams();
    }

    private native void createTrackNative(int trackId);
}
