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
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kh.beatbot.R;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.SampleRenameEvent;
import com.kh.beatbot.event.track.TrackCreateEvent;
import com.kh.beatbot.listener.GLSurfaceViewGroupListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.ProjectFileManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.ViewFlipper;
import com.kh.beatbot.ui.view.group.GLSurfaceViewGroup;

public class BeatBotActivity extends Activity {
	public static final int BPM_DIALOG_ID = 0, EXIT_DIALOG_ID = 1, SAMPLE_NAME_EDIT_DIALOG_ID = 2,
			PROJECT_FILE_NAME_EDIT_DIALOG_ID = 3, MIDI_FILE_NAME_EDIT_DIALOG_ID = 4;

	private static boolean initialized = false;

	private static ViewFlipper activityPager;
	private static EditText bpmInput, projectFileNameInput, midiFileNameInput, sampleNameInput;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GeneralUtils.initAndroidSettings(this);

		TextureAtlas.font.load(this, "REDRING-1969-v03.ttf");
		TextureAtlas.resource.load(this);

		if (!initialized) {
			createEngine();
			createAudioPlayer();

			Color.init(this);
			FileManager.init(this);
			ProjectFileManager.init(this);
			MidiFileManager.init(this);
			RecordManager.init(this);
			activityPager = View.init(this);

			MidiManager.init();
			TrackManager.init(this);

			arm();

			setupDefaultProject();

			((GLSurfaceViewGroup) View.root).setBBRenderer(activityPager);
			((GLSurfaceViewGroup) View.root).addListener(new GLSurfaceViewGroupListener() {
				@Override
				public void onGlReady(GLSurfaceViewGroup view) {
					TextureAtlas.font.loadTexture();
					TextureAtlas.resource.loadTexture();
					activityPager.initGl();
				}
			});
		}

		ViewParent viewParent = ((GLSurfaceViewGroup) View.root).getParent();
		if (null == viewParent) {
			LinearLayout layout = new LinearLayout(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
			View.root.setLayoutParams(lp);
			layout.addView(View.root);
			setContentView(layout, lp);
		}

		initialized = true;
	}

	@Override
	public void onBackPressed() {
		if (View.mainPage.effectIsShowing()) {
			View.mainPage.hideEffect();
		} else {
			showDialog(EXIT_DIALOG_ID);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		View.root.onResume();
	}

	@Override
	public void onPause() {
		View.root.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (isFinishing()) {
			Log.d("Janitor", "shutting down");
			shutdown();
		}
		super.onDestroy();
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
		case PROJECT_FILE_NAME_EDIT_DIALOG_ID:
			projectFileNameInput.setText(ProjectFileManager.getProjectName());
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
								View.mainPage.getPageSelectGroup().setBPM(
										Integer.valueOf(bpmString));
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
		case PROJECT_FILE_NAME_EDIT_DIALOG_ID:
			projectFileNameInput = new EditText(this);

			builder.setTitle("Save project as:").setView(projectFileNameInput)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String projectFileName = projectFileNameInput.getText().toString();
							ProjectFileManager.saveProject(projectFileName);
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

	public static void setupDefaultProject() {
		// XXX loading a project when currently in the sampleEditView can cause segfault
		View.mainPage.getPageSelectGroup().selectLevelsPage();
		EventManager.clearEvents();
		TrackManager.destroy();

		for (int trackId = 0; trackId < FileManager.drumsDirectory.listFiles().length; trackId++) {
			new TrackCreateEvent().doExecute();
			final File sampleFile = FileManager.drumsDirectory.listFiles()[trackId].listFiles()[0];
			TrackManager.setSample(TrackManager.getTrackByNoteValue(trackId), sampleFile);
		}

		TrackManager.getTrackByNoteValue(0).select();
		View.mainPage.getPageSelectGroup().selectLevelsPage();

		MidiManager.setBPM(120);
		MidiManager.setLoopBeginTick(0);
		MidiManager.setLoopEndTick(MidiManager.TICKS_PER_NOTE * 4);
	}

	public static void clearProject() {
		View.mainPage.getPageSelectGroup().selectLevelsPage();
		EventManager.clearEvents();
		TrackManager.destroy();

		MidiManager.setBPM(120);
		MidiManager.setLoopBeginTick(0);
		MidiManager.setLoopEndTick(MidiManager.TICKS_PER_NOTE * 4);
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