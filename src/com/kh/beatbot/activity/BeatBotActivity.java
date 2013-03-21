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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.view.BpmView;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.group.GLSurfaceViewGroup;
import com.kh.beatbot.view.group.MainPage;

public class BeatBotActivity extends Activity {
	private GLSurfaceViewGroup bpmSurface, mainSurface;

	
	private static AssetManager assetManager;
	
	private long lastTapTime = 0;

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
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
			String assetPath = newDirectory.replace(
					Managers.directoryManager.getInternalDirectory(), "");
			assetPath = assetPath.substring(0, assetPath.length() - 1);
			filesToCopy = assetManager.list(assetPath);
			for (String filePath : filesToCopy) {
				if (existingFiles.contains(filePath)) {
					continue;
				}
				// copy wav file exactly from assets to sdcard
				InputStream in = assetManager.open(assetPath + "/" + filePath);
				FileOutputStream rawOut = new FileOutputStream(newDirectory
						+ filePath);
				copyFile(in, rawOut);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyAllSamplesToStorage() {
		assetManager = getAssets();
		for (int i = 0; i < DirectoryManager.drumNames.length; i++) {
			String drumPath = Managers.directoryManager.getDrumInstrument(i)
					.getPath();
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
		Colors.initColors(this);
		GlobalVars.font = Typeface.createFromAsset(getAssets(),
				"REDRING-1969-v03.ttf");
		GeneralUtils.initAndroidSettings(this);
		setContentView(R.layout.main);
		Managers.initDirectoryManager();
		copyAllSamplesToStorage();
		if (savedInstanceState == null) {
			initNativeAudio();
		}
		
		bpmSurface = ((GLSurfaceViewGroup)findViewById(R.id.bpm));
		GlobalVars.bpmView = new BpmView((TouchableSurfaceView)bpmSurface);
		bpmSurface.setBBRenderer(GlobalVars.bpmView);
		
		mainSurface = (GLSurfaceViewGroup)findViewById(R.id.mainSurface);
		GlobalVars.mainPage = new MainPage(mainSurface);
		mainSurface.setBBRenderer(GlobalVars.mainPage);
		mainSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
		Managers.init(savedInstanceState);
		
		setEditIconsEnabled(false);
		
		// were we recording and/or playing before losing the instance?
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("recording")) {
				record(findViewById(R.id.recordButton));
			} else if (savedInstanceState.getBoolean("playing")) {
				play(findViewById(R.id.playButton));
			}
		}

		Managers.trackManager.setCurrTrack(0);
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
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Closing " + getString(R.string.app_name))
	        .setMessage("Are you sure you want to exit " + getString(R.string.app_name) + "?")
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        	@Override
	        	public void onClick(DialogInterface dialog, int which) {
	        		finish();    
	        	}
	    })
	    .setNegativeButton("No", null)
	    .show();
	}

	public void onPause() {
		bpmSurface.onPause();
		mainSurface.onPause();
		super.onPause();
	}
	
	public void onResume() {
		super.onResume();
		bpmSurface.onResume();
		mainSurface.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("midiManager", Managers.midiManager);
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
			if (GlobalVars.mainPage.getMidiGroup().midiView.toggleSnapToGrid())
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
					Param param = effect.getParam(paramNum);
					if (param.beatSync) {
						param.setLevel(param.viewLevel);
						effect.setEffectParam(trackId, effect.getPosition(),
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
			// make sure the recorded instrument shows the newly recorded beat
			Managers.directoryManager.updateDirectories();

			Toast.makeText(getApplicationContext(),
					"Recorded file to " + fileName, Toast.LENGTH_SHORT).show();
		} else {
			GlobalVars.mainPage.getMidiGroup().midiView.reset();
			((ToggleButton) findViewById(R.id.playButton)).setChecked(true);
			Managers.recordManager.startRecordingNative();
			if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
				play(findViewById(R.id.playButton));
			// Managers.recordManager.startListening();
		}
	}

	public void play(View view) {
		((ToggleButton) findViewById(R.id.playButton)).setChecked(true);
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			Managers.playbackManager.reset();
			Managers.midiManager.reset();
		} else if (Managers.playbackManager.getState() == PlaybackManager.State.STOPPED) {
			Managers.playbackManager.play();
		}
	}

	public void stop(View view) {
		((ToggleButton) findViewById(R.id.playButton)).setChecked(false);
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

	public void notifyTrackAdded(int newTrackNum) {
		GlobalVars.mainPage.getMidiGroup().trackAdded(newTrackNum);
		notifyTrackChanged();
	}
	
	public void notifyTrackChanged() {
		GlobalVars.mainPage.getPageSelectGroup().notifyTrackChanged();
	}
	
	public static native boolean createAudioPlayer();

	public static native void createEngine();

	public static native void shutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("nativeaudio");
	}
}