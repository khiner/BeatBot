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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.page.MainPage;
import com.kh.beatbot.layout.page.effect.EffectPage;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.group.BBViewPager;
import com.kh.beatbot.view.group.GLSurfaceViewGroup;

public class BeatBotActivity extends Activity {

	public static final int BPM_DIALOG_ID = 0;
	public static final int EXIT_DIALOG_ID = 1;

	private GLSurfaceViewGroup mainSurface;
	private BBViewPager activityPager;
	private static AssetManager assetManager;

	private EditText bpmInput;

	private static final int MAIN_PAGE_NUM = 0;
	private static final int EFFECT_PAGE_NUM = 1;

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
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		LinearLayout layout = new LinearLayout(this);
		mainSurface = new GLSurfaceViewGroup(this);
		mainSurface.setLayoutParams(lp);
		layout.addView(mainSurface);
		setContentView(layout, lp);

		Managers.initDirectoryManager();
		copyAllSamplesToStorage();
		if (savedInstanceState == null) {
			initNativeAudio();
		}

		BBView.root = mainSurface;
		GlobalVars.mainPage = new MainPage();
		GlobalVars.effectPage = new EffectPage();

		activityPager = new BBViewPager();
		activityPager.addPage(GlobalVars.mainPage);
		activityPager.addPage(GlobalVars.effectPage);
		activityPager.setPage(0);

		mainSurface.setBBRenderer(activityPager);

		Managers.init(savedInstanceState);
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
		if (activityPager.getCurrPageNum() == MAIN_PAGE_NUM) {
			showDialog(EXIT_DIALOG_ID);
		} else if (activityPager.getCurrPageNum() == EFFECT_PAGE_NUM) {
			GlobalVars.mainPage.pageSelectGroup.updateLevelsFXPage();
			activityPager.setPage(MAIN_PAGE_NUM);
		}
	}

	@Override
	public void onPause() {
		mainSurface.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mainSurface.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("midiManager", Managers.midiManager);
		outState.putBoolean(
				"playing",
				Managers.playbackManager.getState() == PlaybackManager.State.PLAYING);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case BPM_DIALOG_ID:
			bpmInput.setText(String.valueOf((int) Managers.midiManager.getBPM()));
			break;
		case EXIT_DIALOG_ID:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (id) {
		case BPM_DIALOG_ID:
			bpmInput = new EditText(this);
			builder = new AlertDialog.Builder(GlobalVars.mainActivity);

			bpmInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
			builder.setTitle("Set BPM")
					.setView(bpmInput)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String bpmString = bpmInput.getText()
											.toString();
									if (!bpmString.isEmpty()) {
										GlobalVars.mainPage.controlButtonGroup.bpmView
												.setBPM(Integer
														.valueOf(bpmString));
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			break;
		case EXIT_DIALOG_ID:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing " + getString(R.string.app_name))
					.setMessage(
							"Are you sure you want to exit "
									+ getString(R.string.app_name) + "?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).setNegativeButton("No", null);
			break;
		}
		return builder.create();
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
			if (GlobalVars.mainPage.midiView.toggleSnapToGrid())
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

	/*
	 * Set up the project.
	 * For now, this just means setting track to #1
	 */
	public void setupProject() {
		GlobalVars.mainPage.midiTrackControl.selectTrack(0);
		GlobalVars.mainPage.pageSelectGroup.selectPage(0);
	}

	public void launchEffect(Effect effect) {
		activityPager.setPage(EFFECT_PAGE_NUM);
		GlobalVars.effectPage.loadEffect(effect);
	}

	private void initNativeAudio() {
		createEngine();
		createAudioPlayer();
	}

	public static native boolean createAudioPlayer();

	public static native void createEngine();

	public static native void shutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("nativeaudio");
	}
}