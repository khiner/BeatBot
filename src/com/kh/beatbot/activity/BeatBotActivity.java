package com.kh.beatbot.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.DirectionalViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.layout.page.TrackPage;
import com.kh.beatbot.layout.page.TrackPageFactory;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.listener.MidiTrackControlListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.helper.LevelsViewHelper;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class BeatBotActivity extends Activity implements
		MidiTrackControlListener, LevelListener {
	
	private Context cxt = this;
	private Animation fadeIn, fadeOut;
	// these are used as variables for convenience, since they are reference
	// frequently
	private ToggleButton volume, pan, pitch;
	private TronSeekbar levelBar;

	private DirectionalViewPager trackPager;
	
	private static AssetManager assetManager;

	private static AlertDialog instrumentSelectAlert = null;

	private long lastTapTime = 0;

	private void initLevelsIconGroup() {
		volume = (ToggleButton) findViewById(R.id.masterVolumeToggle);
		pan = (ToggleButton) findViewById(R.id.masterPanToggle);
		pitch = (ToggleButton) findViewById(R.id.masterPitchToggle);
		levelBar = (TronSeekbar) findViewById(R.id.masterLevelBar);
		levelBar.addLevelListener(this);
		setMasterVolume(GlobalVars.MASTER_VOL_LEVEL);
		setMasterPan(GlobalVars.MASTER_PAN_LEVEL);
		setMasterPitch(GlobalVars.MASTER_PIT_LEVEL);
	}

	/**
	 * The Select Instrument Alert is shown when adding a new track
	 */
	private void initInstrumentSelectAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Instrument");
		builder.setAdapter(GlobalVars.instrumentSelectAdapter,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						addTrack(item);
					}
				});
		instrumentSelectAlert = builder.create();
		instrumentSelectAlert
				.setOnShowListener(GlobalVars.instrumentSelectOnShowListener);
	}

	private void initManagers(Bundle savedInstanceState) {
		Managers.init(savedInstanceState);
		Managers.midiManager.setActivity(this);
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		in.close();
		in = null;
		out.flush();
		out.close();
		out = null;
	}

	/**
	 * Copy the wavFile into a file with raw PCM Wav bytes. Works with
	 * mono/stereo (for mono, duplicate each sample to the left/right channels)
	 * Method for detecting/adapting to stereo/mono adapted from
	 * http://stackoverflow
	 * .com/questions/8754111/how-to-read-the-data-in-a-wav-file-to-an-array
	 */
	private void convertWavToRaw(InputStream wavIn, FileOutputStream rawOut)
			throws IOException {
		byte[] headerBytes = new byte[44];
		wavIn.read(headerBytes);
		// Determine if mono or stereo
		int channels = headerBytes[22]; // Forget byte 23 as 99.999% of WAVs are
										// 1 or 2 channels

		byte[] inBytes = new byte[2];
		ByteBuffer floatBuffer = ByteBuffer.allocate(4);
		floatBuffer.order(ByteOrder.LITTLE_ENDIAN);
		while (wavIn.read(inBytes) != -1) {
			// convert two bytes to a float (little endian)
			short s = (short) (((inBytes[1] & 0xff) << 8) | (inBytes[0] & 0xff));
			floatBuffer.putFloat(0, s / 32768.0f);
			rawOut.write(floatBuffer.array());
			if (channels == 1) {
				// if mono, left and right copies should be identical
				rawOut.write(floatBuffer.array());
			}
		}
		// clean up
		wavIn.close();
		wavIn = null;
		rawOut.flush();
		rawOut.close();
		rawOut = null;
	}

	private void copyFromAssetsToExternal(String folderName) {
		String newDirectory = GlobalVars.appDirectory + "/" + folderName + "/";
		File newSampleFolder = new File(newDirectory);
		newSampleFolder.mkdir();
		List<String> existingFiles = new ArrayList<String>();
		String[] filesToCopy = null;
		String[] existingFilesAry = newSampleFolder.list();
		if (existingFilesAry != null) {
			existingFiles = Arrays.asList(existingFilesAry);
		}
		try {
			filesToCopy = assetManager.list(folderName);
			for (String file : filesToCopy) {
				// copy wav file exactly from assets to sdcard
				if (!existingFiles.contains(file.replace(".wav", ".raw"))) {
					InputStream in = assetManager.open(folderName + file);
					FileOutputStream rawOut = new FileOutputStream(newDirectory
							+ file.replace(".wav", ".raw"));
					convertWavToRaw(in, rawOut);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyAllSamplesToStorage() {
		assetManager = getAssets();
		for (String instrumentName : GlobalVars.allInstrumentTypes) {
			// the sample folder for this sample type does not yet exist.
			// create it and write all assets of this type to the folder
			if (!instrumentName.equals("recorded")) {
				copyFromAssetsToExternal(instrumentName);
			}
		}
	}

	private void initDataDir() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// we can read and write to external storage
			String extStorageDir = Environment.getExternalStorageDirectory()
					.toString();
			GlobalVars.appDirectory = extStorageDir + "/BeatBot/";
			File appDirectoryFile = new File(GlobalVars.appDirectory);
			// build the directory structure, if needed
			appDirectoryFile.mkdirs();
		} else { // we need read AND write access for this app - default to
					// internal storage
			GlobalVars.appDirectory = getFilesDir().toString() + "/";
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GeneralUtils.initAndroidSettings(this);
		initDataDir();
		copyAllSamplesToStorage();
		SurfaceViewBase.setResources(getResources());
		setContentView(R.layout.main);
		GlobalVars.initTracks();
		GlobalVars.initInstrumentSelect(this);
		if (savedInstanceState == null) {
			initNativeAudio();
		}
		initLevelsIconGroup();
		initInstrumentSelectAlert();
		GlobalVars.font = Typeface.createFromAsset(getAssets(),
				"REDRING-1969-v03.ttf");
		initManagers(savedInstanceState);
        trackPager = (DirectionalViewPager) findViewById(R.id.trackPager);
		for (int i = 0; i < TrackPage.NUM_TRACK_PAGES; i++) {
			trackPager.addNewItem(i, TrackPageFactory.createPage(cxt, trackPager, TrackPage.getPageType(i)));
		}
		setEditIconsEnabled(false);
		GlobalVars.midiView = ((MidiView) findViewById(R.id.midiView));
		GlobalVars.midiView.initMeFirst();
		MidiTrackControlHelper.addListener(this);
		if (savedInstanceState != null) {
			GlobalVars.midiView.readFromBundle(savedInstanceState);

		}

		// were we recording and/or playing before losing the instance?
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("recording")) {
				record(findViewById(R.id.recordButton));
			} else if (savedInstanceState.getBoolean("playing")) {
				play(findViewById(R.id.playButton));
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			Managers.recordManager.release();
			shutdown();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("midiManager", Managers.midiManager);
		GlobalVars.midiView.writeToBundle(outState);
		outState.putBoolean(
				"playing",
				Managers.playbackManager.getState() == PlaybackManager.State.PLAYING);
		outState.putBoolean(
				"recording",
				Managers.recordManager.getState() != RecordManager.State.INITIALIZING);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		Intent midiFileMenuIntent = new Intent(this, MidiFileMenuActivity.class);
		menu.findItem(R.id.midi_menu_item).setIntent(midiFileMenuIntent);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.snap:
			if (GlobalVars.midiView.toggleSnapToGrid())
				item.setIcon(R.drawable.btn_check_buttonless_on);
			else
				item.setIcon(R.drawable.btn_check_buttonless_off);
			return true;
		case R.id.quantize_current:
			Managers.midiManager.quantize(GlobalVars.currBeatDivision);
			return true;
		case R.id.quantize_quarter:
			Managers.midiManager.quantize(1);
		case R.id.quantize_eighth:
			Managers.midiManager.quantize(2);
			return true;
		case R.id.quantize_sixteenth:
			Managers.midiManager.quantize(4);
			return true;
		case R.id.quantize_thirty_second:
			Managers.midiManager.quantize(8);
			return true;
		case R.id.save_wav:
			return true;
			// midi import/export menu item is handled as an intent -
			// MidiFileMenuActivity.class
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initNativeAudio() {
		createEngine();
		createAudioPlayer();
		for (int trackId = 0; trackId < GlobalVars.tracks.size(); trackId++) {
			Instrument instrument = GlobalVars.tracks.get(trackId)
					.getInstrument();
			addTrack(instrument.getCurrSamplePath());
			GlobalVars.tracks.get(trackId).arm();
		}
	}

	public void setEditIconsEnabled(final boolean enabled) {
		setDeleteIconEnabled(enabled);
		setCopyIconEnabled(enabled);
	}
	
	private void setDeleteIconEnabled(final boolean enabled) {
		// need to change UI stuff on the UI thread.
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				((ImageButton) findViewById(R.id.delete)).setEnabled(enabled);
			}
		});
	}
	
	private void setCopyIconEnabled(final boolean enabled) {
		// need to change UI stuff on the UI thread.
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				((ToggleButton) findViewById(R.id.copy)).setEnabled(enabled);
			}
		});
	}

	/**
	 * Open new intent for sample edit view, passing the track number to the
	 * intent as an extra.
	 */
	private void launchSampleEditActivity(int trackId) {
		Intent intent = new Intent();
		intent.setClass(this, SampleEditActivity.class);
		intent.putExtra("trackId", trackId);
		startActivity(intent);
	}

	public void record(View view) {
		if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING) {
			// Managers.recordManager.stopListening();
			String fileName = Managers.recordManager.stopRecordingAndWriteWav();
			Toast.makeText(getApplicationContext(),
					"Recorded file to " + fileName, Toast.LENGTH_SHORT).show();
		} else {
			GlobalVars.midiView.reset();
			// if we're already playing, midiManager is already ticking away.
			if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
				play(findViewById(R.id.playButton));
			Managers.recordManager.startRecordingNative();
			// Managers.recordManager.startListening();
		}
	}

	public void play(View view) {
		((ToggleButton) view).setChecked(true);
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			Managers.playbackManager.reset();
			Managers.midiManager.reset();
		} else if (Managers.playbackManager.getState() == PlaybackManager.State.STOPPED) {
			Managers.playbackManager.play();
		}
	}

	public void stop(View view) {
		if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING) {
			ToggleButton recordButton = (ToggleButton) findViewById(R.id.recordButton);
			record(recordButton);
			recordButton.setChecked(false);
		}
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			Managers.playbackManager.stop();
			((ToggleButton) findViewById(R.id.playButton)).setChecked(false);
			Managers.midiManager.reset();
		}
	}

	public void undo(View view) {
		Managers.midiManager.undo();
		GlobalVars.midiView.handleUndo();
	}

	public void levels(View view) {
		GlobalVars.midiView.toggleLevelsView();
	}

	public void copy(View view) {
		String msg = null;
		if (((ToggleButton) view).isChecked()) {
			Managers.midiManager.copy();
			msg = "Tap To Paste";
		} else {
			Managers.midiManager.cancelCopy();
			msg = "Copy Cancelled";
		}
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
		.show();
	}

	public void uncheckCopyButton() {
		((ToggleButton) findViewById(R.id.copy)).setChecked(false);
	}
	
	public void delete(View view) {
		Managers.midiManager.deleteSelectedNotes();
	}

	public void volume(View view) {
		volume.setChecked(true);
		pan.setChecked(false);
		pitch.setChecked(false);
		GlobalVars.midiView.setLevelMode(LevelsViewHelper.LevelMode.VOLUME);
		updateLevelBar();
	}

	public void pan(View view) {
		volume.setChecked(false);
		pan.setChecked(true);
		pitch.setChecked(false);
		GlobalVars.midiView.setLevelMode(LevelsViewHelper.LevelMode.PAN);
		updateLevelBar();
	}

	public void pitch(View view) {
		volume.setChecked(false);
		pan.setChecked(false);
		pitch.setChecked(true);
		GlobalVars.midiView.setLevelMode(LevelsViewHelper.LevelMode.PITCH);
		updateLevelBar();
	}

	private void updateLevelBar() {
		switch (GlobalVars.midiView.getLevelMode()) {
		case VOLUME:
			levelBar.setLevelColor(Colors.VOLUME_COLOR);
			levelBar.setLevel(GlobalVars.MASTER_VOL_LEVEL);
			break;
		case PAN:
			levelBar.setLevelColor(Colors.PAN_COLOR);
			levelBar.setLevel(GlobalVars.MASTER_PAN_LEVEL);
			break;
		case PITCH:
			levelBar.setLevelColor(Colors.PITCH_COLOR);
			levelBar.setLevel(GlobalVars.MASTER_PIT_LEVEL);
			break;
		}
	}

	public void bpmTap(View view) {
		long tapTime = System.currentTimeMillis();
		float millisElapsed = tapTime - lastTapTime;
		lastTapTime = tapTime;
		float bpm = 60000 / millisElapsed;
		// if bpm is far below MIN limit, consider this the first tap,
		// otherwise,
		// if it's under but close, set to MIN_BPM
		if (bpm < MidiManager.MIN_BPM - 10)
			return;
		MidiManager.setBPM(bpm);
	}

	public void addTrack(View view) {
		instrumentSelectAlert.show();
	}

	@Override
	public void muteToggled(int track, boolean mute) {
		GlobalVars.tracks.get(track).mute(mute);
	}

	@Override
	public void soloToggled(int track, boolean solo) {
		GlobalVars.tracks.get(track).solo(solo);
	}

	@Override
	public void trackLongPressed(int track) {
		Managers.midiManager.selectRow(track);
	}

	@Override
	public void trackClicked(int track) {
		TrackPageFactory.setTrack(GlobalVars.tracks.get(track));
	}

	@Override
	public void notifyInit(LevelListenable levelBar) {
		updateLevelBar();
	}

	@Override
	public void notifyPressed(LevelListenable levelListenable, boolean pressed) {
	}

	@Override
	public void notifyClicked(LevelListenable levelListenable) {
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		switch (GlobalVars.midiView.getLevelMode()) {
		case VOLUME:
			GlobalVars.MASTER_VOL_LEVEL = level;
			setMasterVolume(level);
			break;
		case PAN:
			GlobalVars.MASTER_PAN_LEVEL = level;
			setMasterPan(level);
			break;
		case PITCH:
			GlobalVars.MASTER_PIT_LEVEL = level;
			setMasterPitch(level);
			break;
		}
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
	}

	private void addTrack(int instrumentType) {
		Instrument newInstrument = GlobalVars.getInstrument(instrumentType);
		addTrack(newInstrument.getCurrSamplePath());
		GlobalVars.tracks
				.add(new Track(GlobalVars.tracks.size(), newInstrument));
		GlobalVars.midiView.updateTracks();
		// launch sample edit activity for the newly added track
		launchSampleEditActivity(GlobalVars.tracks.size() - 1);
	}

	public static native void addTrack(String sampleFileName);

	public static native void setMasterVolume(float level);

	public static native void setMasterPan(float level);

	public static native void setMasterPitch(float level);

	public static native boolean createAudioPlayer();

	public static native void createEngine();

	public static native void shutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("nativeaudio");
	}
}