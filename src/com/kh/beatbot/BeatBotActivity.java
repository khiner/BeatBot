package com.kh.beatbot;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.AudioClassificationManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.view.BpmView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.ThresholdBarView;
import com.kh.beatbot.view.helper.LevelsViewHelper;

public class BeatBotActivity extends Activity {
	private class FadeListener implements AnimationListener {
		boolean fadeOut;

		public void setFadeOut(boolean flag) {
			fadeOut = flag;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (fadeOut) {
				// in landscape mode, we only want to make levels invisible so they still take up space
				// in portrait, they get their own row only when visible, so make it completely gone
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					levelsGroup.setVisibility(View.INVISIBLE);
				else
					levelsGroup.setVisibility(View.GONE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			levelsGroup.setVisibility(View.VISIBLE);
		}
	}

	public class IconLongClickListener implements OnLongClickListener {
		@Override
		public boolean onLongClick(View view) {
			int position = (Integer) view.getTag();
			if (view.getId() == R.id.icon) {
				midiManager.selectRow(position);
			}
			return true;
		}
	}

	public class SampleRowAdapterAndOnClickListener extends
			ArrayAdapter<String> implements AdapterView.OnClickListener {
		int resourceId;
		ArrayList<ToggleButton> soloButtons = new ArrayList<ToggleButton>();

		public SampleRowAdapterAndOnClickListener(Context context,
				int resourceId, String[] sampleTypes) {
			super(context, resourceId, sampleTypes);
			this.resourceId = resourceId;
		}

		@Override
		public void onClick(View view) {
			int position = (Integer) view.getTag();
			if (view.getId() == R.id.icon) {
				// open new intent for sample edit view
				// and pass the number of the sample to the intent as extras
				Intent intent = new Intent();
				intent.setClass(this.getContext(), SampleEditActivity.class);
				intent.putExtra("trackNum", position);
				startActivity(intent);
			} else if (view.getId() == R.id.mute) {
				ToggleButton muteButton = (ToggleButton) view;
				if (muteButton.isChecked())
					playbackManager.muteTrack(position);
				else
					playbackManager.unmuteTrack(position);
			} else if (view.getId() == R.id.solo) {
				ToggleButton soloButton = (ToggleButton) view;
				if (soloButton.isChecked()) {
					playbackManager.soloTrack(position);
					for (ToggleButton otherSoloButton : soloButtons) {
						if (otherSoloButton.isChecked()
								&& !otherSoloButton.equals(soloButton)) {
							otherSoloButton.setChecked(false);
						}
					}
				} else
					playbackManager.unsoloTrack(position);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(resourceId, parent, false);
			ImageButton icon = (ImageButton) view.findViewById(R.id.icon);
			ToggleButton mute = (ToggleButton) view.findViewById(R.id.mute);
			ToggleButton solo = (ToggleButton) view.findViewById(R.id.solo);
			soloButtons.add(solo);
			icon.setTag(position);
			mute.setTag(position);
			solo.setTag(position);
			icon.setOnClickListener(this);
			mute.setOnClickListener(this);
			solo.setOnClickListener(this);
			icon.setOnLongClickListener(iconLongClickListener);
			icon.setBackgroundResource(drumIcons[position]);
			return view;
		}
	}

	private Animation fadeIn, fadeOut;
	// these are used as variables for convenience, since they are reference
	// frequently
	private ToggleButton volume, pan, pitch;
	private ViewGroup levelsGroup;
	private FadeListener fadeListener;
	private ListView sampleListView;
	private MidiManager midiManager;
	private PlaybackManager playbackManager;
	private RecordManager recordManager;
	private static AssetManager assetManager;

	private MidiView midiView;

	private IconLongClickListener iconLongClickListener = new IconLongClickListener();

	private final String[] sampleNames = { "kick_808.wav", "snare_808.wav",
			"hat_closed_808.wav", "hat_open_808.wav", "rimshot_808.wav",
			"tom_low_808.wav" };

	private final int[] drumIcons = { R.drawable.kick_icon_src,
			R.drawable.snare_icon_src, R.drawable.hh_closed_icon_src,
			R.drawable.hh_open_icon_src, R.drawable.rimshot_icon_src,
			R.drawable.bass_icon_src, };
	private long lastTapTime = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// assign hardware (ringer) volume +/- to media while this application
		// has focus
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.main);
		levelsGroup = (ViewGroup) findViewById(R.id.levelsLayout);
		volume = (ToggleButton) findViewById(R.id.volumeButton);
		pan = (ToggleButton) findViewById(R.id.panButton);
		pitch = (ToggleButton) findViewById(R.id.pitchButton);
		fadeListener = new FadeListener();
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		fadeIn.setAnimationListener(fadeListener);
		fadeOut.setAnimationListener(fadeListener);
		SampleRowAdapterAndOnClickListener adapter = new SampleRowAdapterAndOnClickListener(
				this, R.layout.sample_row, sampleNames);
		sampleListView = (ListView) findViewById(R.id.sampleListView);
		sampleListView.setAdapter(adapter);

		assetManager = getAssets();
		if (savedInstanceState == null) {
			createEngine(assetManager);
			createAllAssetAudioPlayers();
		}
		// get all Manager singletons
		playbackManager = PlaybackManager.getInstance(this, sampleNames);
		recordManager = RecordManager.getInstance();
		// if this context is being restored from a destroyed context,
		// recover the midiManager. otherwise, create a new one
		if (savedInstanceState == null)
			midiManager = MidiManager.getInstance(sampleNames.length);
		else
			midiManager = savedInstanceState.getParcelable("midiManager");

		midiManager.setPlaybackManager(playbackManager);
		midiManager.setActivity(this);
		recordManager.setMidiManager(midiManager);
		recordManager
				.setAudioClassificationManager(new AudioClassificationManager());
		recordManager
				.setThresholdBar((ThresholdBarView) findViewById(R.id.thresholdBar));
		midiManager.setRecordManager(recordManager);

		midiView = ((MidiView) findViewById(R.id.midiView));		
		midiView.setMidiManager(midiManager);
		midiView.setRecordManager(recordManager);
		midiView.setPlaybackManager(playbackManager);
		recordManager.setMidiView(midiView);
		if (savedInstanceState != null)
			midiView.readFromBundle(savedInstanceState);

		// set midiManager as a global variable, since it needs to be accessed
		// by separate MidiFileMenu activity
		// GlobalVars gv = (GlobalVars) getApplicationContext();
		// make midiManager a global var
		GlobalVars.setMidiManager(midiManager);
		GlobalVars.setPlaybackManager(playbackManager);

		((BpmView) findViewById(R.id.bpm)).setText(String
				.valueOf((int) midiManager.getBPM()));		
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
			recordManager.release();
			shutdown();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("midiManager", midiManager);
		midiView.writeToBundle(outState);
		outState.putBoolean("playing",
				playbackManager.getState() == PlaybackManager.State.PLAYING);
		outState.putBoolean("recording",
				recordManager.getState() != RecordManager.State.INITIALIZING);
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
			if (midiView.toggleSnapToGrid())
				item.setIcon(R.drawable.btn_check_buttonless_on);
			else
				item.setIcon(R.drawable.btn_check_buttonless_off);
			return true;
		case R.id.quantize_current:
			midiManager.quantize(GlobalVars.currBeatDivision);
			return true;
		case R.id.quantize_quarter:
			midiManager.quantize(1);
		case R.id.quantize_eighth:
			midiManager.quantize(2);
			return true;
		case R.id.quantize_sixteenth:
			midiManager.quantize(4);
			return true;
		case R.id.quantize_thirty_second:
			midiManager.quantize(8);
			return true;
		case R.id.save_wav:
			return true;
			// midi import/export menu item is handled as an intent -
			// MidiFileMenuActivity.class
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean createAllAssetAudioPlayers() {
		for (String sampleName : sampleNames)
			createAssetAudioPlayer(sampleName);
		return true;
	}

	public void setDeleteIconEnabled(boolean enabled) {
		((ImageButton) findViewById(R.id.delete)).setEnabled(enabled);
	}

	// DON'T USE YET! this needs to run on the UI thread somehow.
	public void activateIcon(int trackNum) {
		((ImageView) sampleListView.getChildAt(trackNum)).setImageState(
				new int[] { android.R.attr.state_checked }, true);
	}

	// DON'T USE YET! this needs to run on the UI thread somehow.
	public void deactivateIcon(int trackNum) {
		((ImageView) sampleListView.getChildAt(trackNum)).setImageState(
				new int[] { android.R.attr.state_empty }, true);
	}

	public void record(View view) {
		if (recordManager.getState() != RecordManager.State.INITIALIZING) {
			recordManager.stopListening();
			((ToggleButton) view).setChecked(false);
		} else {
			midiView.reset();
			// if we're already playing, midiManager is already ticking away.
			if (playbackManager.getState() != PlaybackManager.State.PLAYING)
				play(findViewById(R.id.playButton));
			recordManager.startListening();
		}
	}

	public void play(View view) {
		((ToggleButton) view).setChecked(true);
		if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
			midiManager.reset();
		} else if (playbackManager.getState() == PlaybackManager.State.STOPPED) {
			playbackManager.play();
			midiManager.start();
		}
	}

	public void stop(View view) {
		if (recordManager.getState() != RecordManager.State.INITIALIZING)
			record(findViewById(R.id.recordButton));
		if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
			playbackManager.stop();
			((ToggleButton) findViewById(R.id.playButton)).setChecked(false);
			spin(10); // wait for midi tick thread to notice that playback has
						// stopped
			midiManager.reset();
		}
	}

	public void undo(View view) {
		midiManager.undo();
		midiView.handleUndo();
	}

	public void levels(View view) {
		midiView.toggleLevelsView();
		if (midiView.getViewState() == MidiView.State.TO_LEVELS_VIEW) {
			fadeListener.setFadeOut(false);
			levelsGroup.startAnimation(fadeIn);
		} else {
			fadeListener.setFadeOut(true);
			levelsGroup.startAnimation(fadeOut);
		}
	}

	public void delete(View view) {
		midiManager.deleteSelectedNotes();
	}

	public void volume(View view) {
		volume.setChecked(true);
		pan.setChecked(false);
		pitch.setChecked(false);
		midiView.setLevelMode(LevelsViewHelper.LevelMode.VOLUME);
	}

	public void pan(View view) {
		volume.setChecked(false);
		pan.setChecked(true);
		pitch.setChecked(false);
		midiView.setLevelMode(LevelsViewHelper.LevelMode.PAN);
	}

	public void pitch(View view) {
		volume.setChecked(false);
		pan.setChecked(false);
		pitch.setChecked(true);
		midiView.setLevelMode(LevelsViewHelper.LevelMode.PITCH);
	}

	public void bpmTap(View view) {
		long tapTime = System.currentTimeMillis();
		float secondsElapsed = (tapTime - lastTapTime) / 1000f;
		lastTapTime = tapTime;
		float bpm = 60 / secondsElapsed;
		// bpm limits
		if (bpm < 30 || bpm > 500)
			return;
		((BpmView) findViewById(R.id.bpm)).setText(String.valueOf((int) bpm));
		midiManager.setBPM(bpm);
	}

	private void spin(long millis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millis)
			;
	}

	public static native void createEngine(AssetManager assetManager);

	public static native boolean createAssetAudioPlayer(String filename);

	public static native void shutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("nativeaudio");
	}
}