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
import com.kh.beatbot.event.TempoChangeEvent;
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
import com.kh.beatbot.ui.texture.FontTextureAtlas;
import com.kh.beatbot.ui.texture.ResourceTextureAtlas;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.ViewFlipper;
import com.kh.beatbot.ui.view.group.GLSurfaceViewGroup;

public class BeatBotActivity extends Activity {
	public static final int BPM_DIALOG_ID = 0, EXIT_DIALOG_ID = 1, SAMPLE_NAME_EDIT_DIALOG_ID = 2,
			PROJECT_FILE_NAME_EDIT_DIALOG_ID = 3, MIDI_FILE_NAME_EDIT_DIALOG_ID = 4;

	private ViewFlipper activityPager;
	private EditText bpmInput, projectFileNameInput, midiFileNameInput, sampleNameInput;

	private FileManager fileManager;
	private ProjectFileManager projectFileManager;
	private MidiFileManager midiFileManager;
	private RecordManager recordManager;
	private MidiManager midiManager;
	private TrackManager trackManager;

	private FontTextureAtlas fontTextureAtlas;
	private ResourceTextureAtlas resourceTextureAtlas;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GeneralUtils.initAndroidSettings(this);

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.e("here", ex.getMessage());
			}
		});

		fontTextureAtlas = new FontTextureAtlas(getAssets(), "REDRING-1969-v03.ttf");
		resourceTextureAtlas = new ResourceTextureAtlas(getResources());

		createEngine();
		createAudioPlayer();

		Color.init(this);
		fileManager = new FileManager(getApplicationContext(), getAssets());
		projectFileManager = new ProjectFileManager(this);
		midiFileManager = new MidiFileManager(this);
		recordManager = new RecordManager(this);
		onRecordManagerInit(recordManager);

		midiManager = new MidiManager();
		trackManager = new TrackManager();
		onTrackManagerInit(trackManager);

		fileManager.addListener(trackManager);
		midiManager.addMidiNoteListener(trackManager);
		midiManager.addTempoListener(trackManager);

		activityPager = View.init(this);

		arm();
		setupDefaultProject();

		final ViewParent viewParent = ((GLSurfaceViewGroup) View.getRoot()).getParent();
		if (viewParent == null) {
			final LinearLayout layout = new LinearLayout(this);
			final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
			View.getRoot().setLayoutParams(lp);
			layout.addView(View.getRoot());
			setContentView(layout, lp);
		}

		((GLSurfaceViewGroup) View.getRoot()).setBBRenderer(activityPager);
		((GLSurfaceViewGroup) View.getRoot()).addListener(new GLSurfaceViewGroupListener() {
			@Override
			public void onGlReady(GLSurfaceViewGroup view) {
				fontTextureAtlas.loadTexture();
				resourceTextureAtlas.loadTexture();
				activityPager.initGl();
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (View.getMainPage().effectIsShowing()) {
			View.getMainPage().hideEffect();
		} else {
			showDialog(EXIT_DIALOG_ID);
		}
	}

	@Override
	public void onPause() {
		View.getRoot().setVisibility(android.view.View.GONE);
		//View.onPause();
		super.onPause();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if (hasFocus && View.getRoot().getVisibility() == android.view.View.GONE) {
	         View.getRoot().setVisibility(android.view.View.VISIBLE);
	    }
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
			bpmInput.setText(String.valueOf((int) midiManager.getBpm()));
			break;
		case SAMPLE_NAME_EDIT_DIALOG_ID:
			sampleNameInput.setText(fileToEdit.getName());
			break;
		case PROJECT_FILE_NAME_EDIT_DIALOG_ID:
			projectFileNameInput.setText(projectFileManager.getProjectName());
			break;
		case MIDI_FILE_NAME_EDIT_DIALOG_ID:
		case EXIT_DIALOG_ID:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
								new TempoChangeEvent(Integer.valueOf(bpmString)).execute();
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
							projectFileManager.saveProject(projectFileName);
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
							final String midiFileName = midiFileNameInput.getText().toString();
							midiFileManager.exportMidi(midiFileName);
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
			View.getMainPage().expandMenu();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private File fileToEdit;

	public void editFileName(File file) {
		fileToEdit = file;
		showDialog(SAMPLE_NAME_EDIT_DIALOG_ID);
	}

	public void setupDefaultProject() {
		// XXX loading a project when currently in the sampleEditView can cause segfault
		View.getPageSelectGroup().selectLevelsPage();
		EventManager.clearEvents();
		trackManager.destroy();

		for (int trackId = 0; trackId < fileManager.getDrumsDirectory().listFiles().length; trackId++) {
			new TrackCreateEvent().doExecute();
			final File sampleFile = fileManager.getDrumsDirectory().listFiles()[trackId]
					.listFiles()[0];
			trackManager.setSample(trackManager.getTrackByNoteValue(trackId), sampleFile);
		}

		trackManager.getTrackByNoteValue(0).select();
		View.getPageSelectGroup().selectLevelsPage();

		midiManager.setBpm(120);
		midiManager.setLoopTicks(0, MidiManager.TICKS_PER_NOTE * 4);
	}

	public void clearProject() {
		View.getPageSelectGroup().selectLevelsPage();
		EventManager.clearEvents();
		trackManager.destroy();

		midiManager.setBpm(120);
		midiManager.setLoopTicks(0, MidiManager.TICKS_PER_NOTE * 4);
	}

	public FontTextureAtlas getFontTextureAtlas() {
		return fontTextureAtlas;
	}

	public ResourceTextureAtlas getResourceTextureAtlas() {
		return resourceTextureAtlas;
	}

	public RecordManager getRecordManager() {
		return recordManager;
	}

	public MidiManager getMidiManager() {
		return midiManager;
	}

	public TrackManager getTrackManager() {
		return trackManager;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void importProject(final String fileName) {
		projectFileManager.importProject(this, fileName);
	}

	public void importMidi(final String fileName) {
		midiFileManager.importMidi(this, fileName);
	}

	private void shutdown() {
		nativeShutdown();
	}

	public native void onRecordManagerInit(RecordManager recordManager);

	public native void onTrackManagerInit(TrackManager trackManager);

	public native boolean createAudioPlayer();

	public native void createEngine();

	public native void arm();

	public native void nativeShutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("sndfile");
		System.loadLibrary("nativeaudio");
	}
}