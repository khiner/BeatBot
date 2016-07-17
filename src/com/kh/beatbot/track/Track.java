package com.kh.beatbot.track;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.FileListener;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.view.TrackButtonRow;
import com.kh.beatbot.ui.view.View;

public class Track extends BaseTrack implements FileListener {
	public static float MIN_LOOP_WINDOW = 32f;

	private boolean adsrEnabled = false, reverse = false, previewing = false, muted = false,
			soloing = false;

	private List<MidiNote> notes = Collections.synchronizedList(new ArrayList<MidiNote>());
	private File currSampleFile;
	private ADSR adsr;

	private transient Map<File, SampleParams> paramsForSample;
	private transient TrackButtonRow buttonRow;
	private transient Rectangle rectangle;

	public Track() {
		super();
		paramsForSample = new HashMap<File, SampleParams>();
	}

	public Track(int id) {
		super(id);
		paramsForSample = new HashMap<File, SampleParams>();
		this.adsr = new ADSR(id);
	}

	public void addNote(MidiNote note) {
		synchronized (notes) {
			if (!notes.contains(note)) {
				notes.add(note);
			}
		}
		updateNextNote();
	}

	public void removeNote(MidiNote note) {
		synchronized (notes) {
			if (notes.remove(note)) {
				notifyNoteRemoved(id, note.getOnTick());
			}
		}
	}

	public void setNoteValue(int noteValue) {
		for (MidiNote note : notes) {
			note.setNoteWithoutNotify(noteValue);
		}
	}

	public boolean containsNote(MidiNote note) {
		return notes.contains(note);
	}

	public MidiNote findNoteStarting(long onTick) {
		synchronized (notes) {
			for (MidiNote note : notes) {
				if (note.getOnTick() == onTick) {
					return note;
				}
			}
			return null;
		}
	}

	public MidiNote findNoteContaining(long tick) {
		synchronized (notes) {
			for (MidiNote note : notes) {
				if (note.getOnTick() <= tick && note.getOffTick() >= tick) {
					return note;
				}
			}
			return null;
		}
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
		return null == currSampleFile ? IconResourceSets.INSTRUMENT_BASE : IconResourceSets
				.forDirectory(currSampleFile.getParentFile().getName());
	}

	public void checkInstrumentButton() {
		buttonRow.instrumentButton.setChecked(true);
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
			// conditions for region selection
			boolean a = leftTick < midiNote.getOffTick();
			boolean b = rightTick > midiNote.getOffTick();
			boolean c = leftTick < midiNote.getOnTick();
			boolean d = rightTick > midiNote.getOnTick();
			boolean selected = id >= topNote && id <= bottomNote
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
		synchronized (notes) {
			Collections.sort(notes);
			setNextNote(id, getNextMidiNote(View.context.getMidiManager().getCurrTick()));
		}
	}

	public MidiNote getNextMidiNote(long currTick) {
		// is there another note starting between the current tick and the end of the loop?
		for (MidiNote midiNote : notes) {
			if (midiNote.getOnTick() >= currTick
					&& midiNote.getOnTick() < View.context.getMidiManager().getLoopEndTick()) {
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
	public void onNameChange(File file, File newFile) {
		SampleParams params = paramsForSample.remove(currSampleFile);
		currSampleFile = newFile;
		paramsForSample.put(currSampleFile, params);
	}

	/** Wrappers around native JNI methods **/

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
		muteTrack(id, mute);
		this.muted = mute;
		View.context.getTrackManager().onMuteChange(this, mute);
	}

	public void solo(boolean solo) {
		soloTrack(id, solo);
		this.soloing = solo;
		View.context.getTrackManager().onSoloChange(this, solo);
	}

	public void toggleLooping() {
		toggleTrackLooping(id);
		View.context.getTrackManager().onLoopChange(this, isLooping());
	}

	// set play mode to reverse
	public void setReverse(boolean reverse) {
		setTrackReverse(id, reverse);
		this.reverse = reverse;
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

	public boolean isPreviewing() {
		return previewing;
	}

	public boolean isPlaying() {
		return isTrackPlaying(id);
	}

	public boolean isSounding() {
		return isPreviewing() || isPlaying();
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

	public float getSample(long sampleIndex, int channel) {
		return getSample(id, sampleIndex, channel);
	}

	public float getCurrentFrame() {
		return getCurrentFrame(id);
	}

	public void destroy() {
		deleteTrack(id);
		View.context.getTrackManager().onDestroy(this);
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

	private native float getSample(int trackId, long sampleIndex, int channel);

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
				View.context.getPageSelectGroup().getEditPage().sampleEdit.onParamChange(param);
			} else {
				float minLoopWindow = loopEndParam.getViewLevel(MIN_LOOP_WINDOW);
				loopBeginParam.maxViewLevel = loopEndParam.viewLevel - minLoopWindow;
				loopEndParam.minViewLevel = loopBeginParam.viewLevel + minLoopWindow;
				updateLoopWindow();
			}
		}
	}
}
