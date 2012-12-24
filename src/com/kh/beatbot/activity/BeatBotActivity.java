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
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.EffectParam;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.page.TrackPage;
import com.kh.beatbot.layout.page.TrackPageFactory;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.helper.LevelsViewHelper;

public class BeatBotActivity extends Activity implements LevelListener {

	private int pageNum = 0;
	private Context cxt = this;
	private Animation fadeIn, fadeOut;
	// these are used as variables for convenience, since they are reference
	// frequently
	private ToggleButton volume, pan, pitch;
	private TronSeekbar levelBar;

	private ViewFlipper trackPager;
	private LinearLayout trackPageSelect;
	private static AssetManager assetManager;

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

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
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
	private static void convertWavToBB(InputStream wavIn, FileOutputStream bbOut)
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
			bbOut.write(floatBuffer.array());
			if (channels == 1) {
				// if mono, left and right copies should be identical
				bbOut.write(floatBuffer.array());
			}
		}
		// clean up
		wavIn.close();
		wavIn = null;
		bbOut.flush();
		bbOut.close();
		bbOut = null;
	}
	
	private static void copyFromAssetsToExternal(String newDirectory) {
		List<String> existingFiles = new ArrayList<String>();
		String[] filesToCopy = null;
		String[] existingFilesAry = new File(newDirectory).list();
		if (existingFilesAry != null) {
			existingFiles = Arrays.asList(existingFilesAry);
		}
		try {
			String assetPath = newDirectory.replace(Managers.directoryManager.getInternalDirectory(), "");
			assetPath = assetPath.substring(0, assetPath.length() - 1);
			filesToCopy = assetManager.list(assetPath);
			for (String filePath : filesToCopy) {
				if (existingFiles.contains(filePath)) {
					continue;
				}
				// copy wav file exactly from assets to sdcard
				InputStream in = assetManager.open(assetPath + "/" + filePath);
				FileOutputStream rawOut = new FileOutputStream(newDirectory + filePath);
				copyFile(in, rawOut);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyAllSamplesToStorage() {
		assetManager = getAssets();
		for (int i = 0; i < DirectoryManager.drumNames.length; i++) {
			String drumPath = Managers.directoryManager.getDrumInstrument(i).getPath();
			// the sample folder for this sample type does not yet exist.
			// create it and write all assets of this type to the folder
			copyFromAssetsToExternal(drumPath);
		}
		Managers.directoryManager.updateDirectories();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GlobalVars.mainActivity = this;
		GeneralUtils.initAndroidSettings(this);
		SurfaceViewBase.setResources(getResources());
		setContentView(R.layout.main);
		Managers.initDirectoryManager();
		copyAllSamplesToStorage();
		if (savedInstanceState == null) {
			initNativeAudio();
		}
		Managers.init(savedInstanceState);
		initLevelsIconGroup();
		GlobalVars.font = Typeface.createFromAsset(getAssets(),
				"REDRING-1969-v03.ttf");
		trackPageSelect = (LinearLayout) findViewById(R.id.trackPageSelect);
		for (int i = 1; i < trackPageSelect.getChildCount(); i++) {
			TextView pageText = null;
			// TODO clean up
			if (i == 1) // view 1 is a linear layout containing a sample select text view
				pageText = (TextView) ((LinearLayout)trackPageSelect.getChildAt(1)).getChildAt(0);
			else
				pageText = (TextView) trackPageSelect.getChildAt(i);
			pageText.setTypeface(GlobalVars.font);
			pageText.setGravity(Gravity.CENTER);
			if (i <= 1) {
				continue;
			}
			final int pageNum = i - 1;
			pageText.setTag(pageNum);
			pageText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int j = 2; j < trackPageSelect.getChildCount(); j++) {
						TextView pageText = (TextView) trackPageSelect
								.getChildAt(j);
						pageText.setSelected(pageNum + 1 == j);
					}
					selectTrackPage(v);
				}
			});
		}
		trackPager = (ViewFlipper) findViewById(R.id.trackFlipper);
		for (int i = 0; i < TrackPage.NUM_TRACK_PAGES; i++) {
			TrackPageFactory.createPage(cxt, this, TrackPage.getPageType(i));
		}
		setEditIconsEnabled(false);
		GlobalVars.midiView = ((MidiView) findViewById(R.id.midiView));
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
		Managers.trackManager.trackClicked(0);
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
	}

	public void quantizeEffectParams() {
		for (int trackId = 0; trackId < Managers.trackManager.getNumTracks(); trackId++) {
			for (Effect effect : Managers.trackManager.getTrack(trackId).effects) {
				for (int paramNum = 0; paramNum < effect.getNumParams(); paramNum++) {
					EffectParam param = effect.getParam(paramNum);
					if (param.beatSync) {
						effect.setParamLevel(param, param.viewLevel);
						effect.setEffectParam(trackId, effect.getId(),
								paramNum, param.level);
					}
				}
			}
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

	public void record(View view) {
		if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING) {
			// Managers.recordManager.stopListening();
			String fileName = Managers.recordManager.stopRecordingAndWriteWav();
			// make sure the recorded instrument shows the newly recorded "song"
			Managers.directoryManager.updateRecordDirectory();
			TrackPageFactory.updatePages();
			
			Toast.makeText(getApplicationContext(),
					"Recorded file to " + fileName, Toast.LENGTH_SHORT).show();
		} else {
			GlobalVars.midiView.reset();
			((ToggleButton) findViewById(R.id.playButton)).setChecked(true);
			if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
				play(findViewById(R.id.playButton));
			Managers.recordManager.startRecordingNative();
			// Managers.recordManager.startListening();
		}
	}

	public void selectTrackPage(View view) {
		int prevPageNum = pageNum;
		pageNum = (Integer) view.getTag();
		if (prevPageNum == pageNum)
			return;
		TrackPageFactory.getTrackPage(TrackPage.getPageType(prevPageNum))
			.setVisible(false);
		trackPager.setDisplayedChild(pageNum);
		TrackPageFactory.getTrackPage(TrackPage.getPageType(pageNum))
			.setVisible(true);
	}

	public void play(View view) {
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
			recordButton.setChecked(false);
			record(recordButton);
		}
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			((ToggleButton) findViewById(R.id.playButton)).setChecked(false);
			Managers.playbackManager.stop();
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
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
		Managers.midiManager.setBPM(bpm);
	}

	public void addTrack(View view) {
		Managers.directoryManager.showAddTrackAlert();
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