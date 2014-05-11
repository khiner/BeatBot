package com.kh.beatbot.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.event.SampleRenameEvent;
import com.kh.beatbot.listener.GLSurfaceViewGroupListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.ViewPager;
import com.kh.beatbot.ui.view.group.GLSurfaceViewGroup;
import com.kh.beatbot.ui.view.page.MainPage;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public class BeatBotActivity extends Activity {
	public static final int BPM_DIALOG_ID = 0, EXIT_DIALOG_ID = 1, SAMPLE_NAME_EDIT_DIALOG_ID = 2,
			MIDI_FILE_NAME_EDIT_DIALOG_ID = 3;

	private static final String MAIN_PAGE_ID = "main", EFFECT_PAGE_ID = "effect";

	private static ViewPager activityPager;
	private static EditText bpmInput, midiFileNameInput, sampleNameInput;

	public static BeatBotActivity mainActivity;

	private Thread janitor;

	private final Runnable cleanup = new Runnable() {
		@Override
		public void run() {
			Log.d("Janitor", "shutting down");
			shutdown();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainActivity = this;

		GeneralUtils.initAndroidSettings(this);
		Color.init(this);
		// load font file once, with static height
		// to change height, simply use gl.scale()
		TextureAtlas.font.load(this, "REDRING-1969-v03.ttf");
		TextureAtlas.resource.load(this);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

		FileManager.init();
		MidiFileManager.init();

		View.root = new GLSurfaceViewGroup(this);
		View.root.setLayoutParams(lp);

		LinearLayout layout = new LinearLayout(this);
		layout.addView(View.root);
		setContentView(layout, lp);

		View.mainPage = new MainPage();
		View.effectPage = new EffectPage();

		activityPager = new ViewPager(null);
		activityPager.addPage(MAIN_PAGE_ID, View.mainPage);
		activityPager.addPage(EFFECT_PAGE_ID, View.effectPage);
		activityPager.setPage(MAIN_PAGE_ID);

		((GLSurfaceViewGroup) View.root).setBBRenderer(activityPager);

		((GLSurfaceViewGroup) View.root).addListener(new GLSurfaceViewGroupListener() {

			@Override
			public void onGlReady(GLSurfaceViewGroup view) {
				TextureAtlas.font.loadTexture();
				TextureAtlas.resource.loadTexture();

				TrackManager.init();
				MidiManager.init();
				activityPager.initGl();

				arm();

				setupProject();
			}
		});

		initNativeAudio();
	}

	@Override
	public void onDestroy() {
		if (janitor == null) {
			janitor = new Thread(cleanup);
		}
		janitor.start();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (activityPager.getCurrPageId() == MAIN_PAGE_ID) {
			showDialog(EXIT_DIALOG_ID);
		} else if (activityPager.getCurrPageId() == EFFECT_PAGE_ID) {
			activityPager.setPage(MAIN_PAGE_ID);
		}
	}

	@Override
	public void onPause() {
		View.root.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		View.root.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("playing", PlaybackManager.getState() == PlaybackManager.State.PLAYING);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case BPM_DIALOG_ID:
			bpmInput.setText(String.valueOf((int) MidiManager.getBPM()));
			break;
		case SAMPLE_NAME_EDIT_DIALOG_ID:
			sampleNameInput.setText(fileToEdit.getName());
			break;
		case MIDI_FILE_NAME_EDIT_DIALOG_ID:

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

			bpmInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
			builder.setTitle("Set BPM").setView(bpmInput)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String bpmString = bpmInput.getText().toString();
							if (!bpmString.isEmpty()) {
								View.mainPage.pageSelectGroup.setBPM(Integer.valueOf(bpmString));
							}
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			break;
		case SAMPLE_NAME_EDIT_DIALOG_ID:
			sampleNameInput = new EditText(this);

			builder.setTitle("Edit Sample Name").setView(sampleNameInput)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String sampleName = sampleNameInput.getText().toString();
							new SampleRenameEvent(fileToEdit, sampleName).execute();
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			break;
		case MIDI_FILE_NAME_EDIT_DIALOG_ID:
			midiFileNameInput = new EditText(this);

			builder.setTitle("Save MIDI as:").setView(midiFileNameInput)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String midiFileName = midiFileNameInput.getText().toString();
							MidiFileManager.exportMidi(midiFileName);
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			break;

		case EXIT_DIALOG_ID:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing " + getString(R.string.app_name))
					.setMessage(
							"Are you sure you want to exit " + getString(R.string.app_name) + "?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton("No", null);
			break;
		}

		return builder.create();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			View.mainPage.expandMenu();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private File fileToEdit;

	public void editFileName(File file) {
		fileToEdit = file;
		showDialog(SAMPLE_NAME_EDIT_DIALOG_ID);
	}

	/*
	 * Set up the project. For now, this just means setting track 0, page 0 view
	 */
	public void setupProject() {
		if (TrackManager.getNumTracks() <= 0)
			return;
		TrackManager.masterTrack.select();
		View.mainPage.pageSelectGroup.selectLevelsPage();
	}

	public void launchEffect(Effect effect) {
		View.effectPage.loadEffect(effect);
		activityPager.setPage(EFFECT_PAGE_ID);
	}

	private void initNativeAudio() {
		createEngine();
		createAudioPlayer();
	}

	private void shutdown() {
		nativeShutdown();
	}

	public static native boolean createAudioPlayer();

	public static native void createEngine();

	public static native void arm();

	public static native void nativeShutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("sndfile");
		System.loadLibrary("nativeaudio");
	}
}