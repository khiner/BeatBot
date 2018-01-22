package com.odang.beatbot.track;

import com.odang.beatbot.effect.ADSR;
import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.FileListener;
import com.odang.beatbot.listener.ParamListener;
import com.odang.beatbot.manager.FileManager;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.view.TrackButtonRow;
import com.odang.beatbot.ui.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Track extends BaseTrack implements FileListener {
    public static float MIN_LOOP_WINDOW = 32f;

    private boolean reverse = false, previewing = false, muted = false, soloing = false;

    private List<MidiNote> notes = new CopyOnWriteArrayList<>();
    private File currSampleFile;
    private ADSR adsr;

    private transient Map<File, SampleParams> paramsForSample;
    private transient TrackButtonRow buttonRow;
    private transient Rectangle rectangle;

    public Track() {
        super();
        paramsForSample = new HashMap<>();
    }

    public Track(int id) {
        super(id);
        paramsForSample = new HashMap<>();
        this.adsr = new ADSR(id);
    }

    public void addNote(MidiNote note) {
        if (!notes.contains(note)) {
            notes.add(note);
        }
        updateNextNote();
    }

    public void removeNote(MidiNote note) {
        if (notes.remove(note)) {
            notifyNoteRemoved(id, note.getOnTick());
        }
    }

    public void setNoteValue(int noteValue) {
        for (MidiNote note : notes) {
            note.setNoteValue(noteValue);
        }
    }

    public MidiNote findNoteStarting(long onTick) {
        for (MidiNote note : notes) {
            if (note.getOnTick() == onTick) {
                return note;
            }
        }
        return null;
    }

    public MidiNote findNoteContaining(long tick) {
        for (MidiNote note : notes) {
            if (note.getOnTick() <= tick && note.getOffTick() >= tick) {
                return note;
            }
        }
        return null;
    }

    public List<MidiNote> getMidiNotes() {
        return notes;
    }

    private void updateADSR() {
        adsr.update();
    }

    public void setButtonRow(TrackButtonRow buttonRow) {
        this.buttonRow = buttonRow;
    }

    public TrackButtonRow getButtonRow() {
        return buttonRow;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public IconResourceSet getIcon() {
        if (currSampleFile == null) {
            return IconResourceSets.INSTRUMENT_BASE;
        } else {
            final IconResourceSet iconResourceSet = IconResourceSets.forDirectory(currSampleFile.getParentFile().getName());
            if (iconResourceSet != null) {
                return iconResourceSet;
            } else if (FileManager.isAudioFile(currSampleFile)) {
                return IconResourceSets.SAMPLE;
            } else {
                return IconResourceSets.INSTRUMENT_BASE;
            }
        }
    }

    public void selectAllNotes() {
        for (MidiNote midiNote : notes) {
            midiNote.setSelected(true);
        }
    }

    public void deselectAllNotes() {
        for (MidiNote midiNote : notes) {
            midiNote.setSelected(false);
        }
    }

    public boolean anyNotes() {
        return !notes.isEmpty();
    }

    public boolean anyNoteSelected() {
        for (MidiNote midiNote : notes) {
            if (midiNote.isSelected()) {
                return true;
            }
        }
        return false;
    }

    public void selectRegion(long leftTick, long rightTick, int topNote, int bottomNote) {
        for (MidiNote midiNote : notes) {
            boolean a = leftTick < midiNote.getOffTick();
            boolean b = rightTick > midiNote.getOffTick();
            boolean c = leftTick < midiNote.getOnTick();
            boolean d = rightTick > midiNote.getOnTick();
            boolean selected = midiNote.getNoteValue() >= topNote
                    && midiNote.getNoteValue() <= bottomNote
                    && ((a && b) || (c && d) || (!b && !c));
            midiNote.setSelected(selected);
        }
    }

    public void saveNoteTicks() {
        for (MidiNote note : notes) {
            note.saveTicks();
        }
    }

    public void updateNextNote() {
        sortCopyOnWriteMidiNotes();
        setNextNote(id, getNextMidiNote(View.context.getMidiManager().getCurrTick()));
    }

    public MidiNote getNextMidiNote(long currTick) {
        // is there another note ending or beginning between the current tick and the end of the loop?
        for (MidiNote midiNote : notes) {
            if ((midiNote.getOffTick() > currTick && midiNote.getOffTick() < View.context.getMidiManager().getLoopEndTick()) ||
                    (midiNote.getOnTick() >= currTick && midiNote.getOnTick() < View.context.getMidiManager().getLoopEndTick())) {
                return midiNote;
            }
        }
        // otherwise, get the first note that starts after loop begin
        for (MidiNote midiNote : notes) {
            if (midiNote.getOnTick() >= View.context.getMidiManager().getLoopBeginTick()) {
                return midiNote;
            }
        }
        return null;
    }

    public void setSample(final File sampleFile) throws Exception {
        if (sampleFile == null)
            return;
        final String errorMsg = setSample(id, sampleFile.getPath());
        if (errorMsg.equals("No Error.")) {
            currSampleFile = sampleFile;
            update();
        } else {
            throw new Exception(errorMsg);
        }
        select();
        View.context.getTrackManager().onSampleChange(this);
    }

    public Param getLoopBeginParam() {
        final SampleParams sampleParams = getCurrSampleParams();
        return sampleParams == null ? null : sampleParams.loopBeginParam;
    }

    public Param getLoopEndParam() {
        final SampleParams sampleParams = getCurrSampleParams();
        return sampleParams == null ? null : sampleParams.loopEndParam;
    }

    @Override
    public Effect findEffectByPosition(int position) {
        if (position == -1) {
            return adsr;
        } else {
            return super.findEffectByPosition(position);
        }
    }

    @Override
    public void addEffect(Effect effect) {
        if (!(effect instanceof ADSR)) {
            super.addEffect(effect);
        }
    }

    public Param getGainParam() {
        final SampleParams sampleParams = getCurrSampleParams();
        return sampleParams == null ? null : sampleParams.gainParam;
    }

    public Param getAdsrParam(int paramNum) {
        return adsr.getParam(paramNum);
    }

    public Param getActiveAdsrParam() {
        return adsr.getActiveParam();
    }

    public void setActiveAdsrParam(int paramId) {
        adsr.setActiveParam(paramId);
    }

    public List<Effect> getEffects() {
        final List<Effect> effects = new ArrayList<>();
        effects.addAll(effects);
        effects.add(adsr);
        return effects;
    }

    public ADSR getAdsr() {
        return adsr;
    }

    public void setSampleLoopWindow(float beginLevel, float endLevel) {
        View.context.getTrackManager().notifyLoopWindowSetEvent(this);
        getLoopBeginParam().setLevel(beginLevel);
        getLoopEndParam().setLevel(endLevel);
    }

    public SampleParams getCurrSampleParams() {
        return paramsForSample.get(currSampleFile);
    }

    public void setSampleGain(float gain) {
        getGainParam().setLevel(gain);
    }

    @Override
    public String getName() {
        return currSampleFile == null ? "Browse" : currSampleFile.getName();
    }

    public String getFormattedName() {
        return FileManager.formatSampleName(getName());
    }

    public File getCurrSampleFile() {
        return currSampleFile;
    }

    @Override
    public void onNameChange(Track track, File file, File newFile) {
        if (this != track)
            return;

        SampleParams params = paramsForSample.remove(currSampleFile);
        currSampleFile = newFile;
        paramsForSample.put(currSampleFile, params);
    }

    /**
     * Wrappers around native JNI methods
     **/

    public void stop() {
        stopTrack(id);
    }

    public void preview() {
        previewing = true;
        previewTrack(id);
    }

    public void stopPreviewing() {
        stopPreviewingTrack(id);
        previewing = false;
    }

    public void mute(boolean mute) {
        if (this.muted == mute)
            return;
        muteTrack(id, mute);
        this.muted = mute;
        View.context.getTrackManager().onMuteChange(this, mute);
    }

    public void solo(boolean solo) {
        if (this.soloing == solo)
            return;
        soloTrack(id, solo);
        soloGuiOnly(solo);
    }

    public void soloGuiOnly(boolean solo) {
        this.soloing = solo;
        View.context.getTrackManager().onSoloChange(this, solo);
    }

    public void toggleLooping() {
        toggleTrackLooping(id);
        View.context.getTrackManager().onLoopChange(this, isLooping());
    }

    public void setReverseWithoutNotify(boolean reverse) {
        setTrackReverse(id, reverse);
        this.reverse = reverse;
    }

    public void setReverse(boolean reverse) {
        setReverseWithoutNotify(reverse);
        View.context.getTrackManager().onReverseChange(this, reverse);
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isSoloing() {
        return soloing;
    }

    public boolean isSelected() {
        return this.equals(View.context.getTrackManager().getCurrTrack());
    }

    public boolean isLooping() {
        return isTrackLooping(id);
    }

    public boolean isReverse() {
        return reverse;
    }

    public boolean isSounding() {
        return previewing || isTrackPlaying(id);
    }

    private void update() {
        updateSampleParams();
        updateADSR();
    }

    private void updateLoopWindow() {
        setTrackLoopWindow(id, (long) getLoopBeginParam().level, (long) getLoopEndParam().level);
    }

    private void updateGain() {
        setTrackGain(id, getGainParam().level);
    }

    private void updateSampleParams() {
        if (!paramsForSample.containsKey(currSampleFile)) {
            paramsForSample.put(currSampleFile, new SampleParams(getFrames(id)));
        }
        updateLoopWindow();
        updateGain();
    }

    public float getNumFrames() {
        return getFrames(id);
    }

    public void fillSampleBuffer(float[] sampleBuffer, int startFrame, int endFrame, int jumpFrames) {
        fillSampleBuffer(id, sampleBuffer, startFrame, endFrame, jumpFrames);
    }

    public float getCurrentFrame() {
        return getCurrentFrame(id);
    }

    public void destroy() {
        deleteTrack(id);
        View.context.getTrackManager().onDestroy(this);
    }

    // Can't use Collections.sort(...) because copyOnWrite doesn't support Iterator.set()
    private void sortCopyOnWriteMidiNotes() {
        Object[] a = notes.toArray();
        Arrays.sort(a);
        for (int i = 0; i < a.length; i++) {
            notes.set(i, (MidiNote) a[i]);
        }
    }

    private native void deleteTrack(int trackId);

    private native void toggleTrackLooping(int trackId);

    private native boolean isTrackLooping(int trackId);

    private native boolean isTrackPlaying(int trackId);

    private native void notifyNoteRemoved(int trackId, long noteOn);

    private native void setTrackLoopWindow(int trackId, long loopBegin, long loopEnd);

    private native void stopTrack(int trackId);

    private native void previewTrack(int trackId);

    private native void stopPreviewingTrack(int trackId);

    private native void muteTrack(int trackId, boolean mute);

    private native void soloTrack(int trackId, boolean solo);

    private native void setTrackReverse(int trackId, boolean reverse);

    private native void setTrackGain(int trackId, float gain);

    private native String setSample(int trackId, String sampleName);

    private native void fillSampleBuffer(int trackId, float[] sampleBuffer, int startFrame, int endFrame, int jumpFrames);

    private native float getCurrentFrame(int trackId);

    private native float getFrames(int trackId);

    private native void setNextNote(int trackId, MidiNote midiNote);

    public class SampleParams implements ParamListener {
        public Param loopBeginParam, loopEndParam, gainParam;

        public SampleParams(float numSamples) {
            loopBeginParam = new Param(0, "Begin").scale(numSamples).withFormat("%.0f");
            loopBeginParam.setLevel(0);

            loopEndParam = new Param(1, "End").scale(numSamples).withFormat("%.0f");
            loopEndParam.setLevel(1);

            gainParam = new Param(2, "Gain").withUnits("Db").withLevel(Param.dbToView(0));

            gainParam.addListener(this);
            loopBeginParam.addListener(this);
            loopEndParam.addListener(this);
        }

        @Override
        public void onParamChange(Param param) {
            if (param.equals(gainParam)) {
                updateGain();
                View.context.getPageSelectGroup().getEditPage().sampleEditView.onParamChange(param);
            } else {
                float minLoopWindow = loopEndParam.getViewLevel(MIN_LOOP_WINDOW);
                loopBeginParam.maxViewLevel = loopEndParam.viewLevel - minLoopWindow;
                loopEndParam.minViewLevel = loopBeginParam.viewLevel + minLoopWindow;
                updateLoopWindow();
            }
        }
    }
}
