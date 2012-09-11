package com.kh.beatbot;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
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
				Managers.midiManager.selectRow(position);
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
					Managers.playbackManager.muteTrack(position);
				else
					Managers.playbackManager.unmuteTrack(position);
			} else if (view.getId() == R.id.solo) {
				ToggleButton soloButton = (ToggleButton) view;
				if (soloButton.isChecked()) {
					Managers.playbackManager.soloTrack(position);
					for (ToggleButton otherSoloButton : soloButtons) {
						if (otherSoloButton.isChecked()
								&& !otherSoloButton.equals(soloButton)) {
							otherSoloButton.setChecked(false);
						}
					}
				} else
					Managers.playbackManager.unsoloTrack(position);
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

	private void initAndroidSettings() {
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// assign hardware (ringer) volume +/- to media while this application
		// has focus
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	private void initLevelsIconGroup() {
		levelsGroup = (ViewGroup) findViewById(R.id.levelsLayout);
		volume = (ToggleButton) findViewById(R.id.volumeButton);
		pan = (ToggleButton) findViewById(R.id.panButton);
		pitch = (ToggleButton) findViewById(R.id.pitchButton);
		// fade animation for levels icons,
		// to match levels view is fading in/out in midiView
		fadeListener = new FadeListener();
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		fadeIn.setAnimationListener(fadeListener);
		fadeOut.setAnimationListener(fadeListener);
	}

	private void initSampleListView() {
		SampleRowAdapterAndOnClickListener adapter = new SampleRowAdapterAndOnClickListener(
				this, R.layout.sample_row, sampleNames);
		sampleListView = (ListView) findViewById(R.id.sampleListView);
		sampleListView.setAdapter(adapter);
	}
	
	private void initManagers(Bundle savedInstanceState) {
		Managers.init(savedInstanceState, sampleNames);
		Managers.midiManager.setActivity(this);
		setDeleteIconEnabled(false);
		((ThresholdBarView) findViewById(R.id.thresholdBar)).addLevelListener(Managers.recordManager);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initAndroidSettings();
		setContentView(R.layout.main);
		
		initLevelsIconGroup();
		initSampleListView();
		assetManager = getAssets();
		if (savedInstanceState == null) {
			createEngine(assetManager);
			createAllAssetAudioPlayers();
		}
		GlobalVars.init(sampleNames.length);
		GlobalVars.font = Typeface.createFromAsset(getAssets(), "REDRING-1969-v03.ttf");
		((TextView)findViewById(R.id.thresholdLabel)).setTypeface(GlobalVars.font);
		
		initManagers(savedInstanceState);
		
		midiView = ((MidiView) findViewById(R.id.midiView));
		midiView.initMeFirstTest();
		Managers.recordManager.setMidiView(midiView);
		
		if (savedInstanceState != null) {
			midiView.readFromBundle(savedInstanceState);
			// if going to levels view or in levels view, level icons should be visible
			int levelsVisibilityState = midiView.getViewState() == MidiView.State.TO_LEVELS_VIEW ||
					midiView.getViewState() == MidiView.State.LEVELS_VIEW ? View.VISIBLE : View.GONE;
			levelsGroup.setVisibility(levelsVisibilityState);

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
		midiView.writeToBundle(outState);
		outState.putBoolean("playing",
				Managers.playbackManager.getState() == PlaybackManager.State.PLAYING);
		outState.putBoolean("recording",
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
			if (midiView.toggleSnapToGrid())
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

	private boolean createAllAssetAudioPlayers() {
		createAssetAudioPlayer();
		for (String sampleName : sampleNames)
			initTrack(sampleName);
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
		if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING) {
			Managers.recordManager.stopListening();
			((ToggleButton) view).setChecked(false);
		} else {
			midiView.reset();
			// if we're already playing, Managers.midiManager is already ticking away.
			if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
				play(findViewById(R.id.playButton));
			Managers.recordManager.startListening();
		}
	}

	public void play(View view) {
		((ToggleButton) view).setChecked(true);
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			Managers.midiManager.reset();
		} else if (Managers.playbackManager.getState() == PlaybackManager.State.STOPPED) {
			Managers.playbackManager.play();
		}
	}

	public void stop(View view) {
		if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING)
			record(findViewById(R.id.recordButton));
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			Managers.playbackManager.stop();
			((ToggleButton) findViewById(R.id.playButton)).setChecked(false);
			spin(10); // wait for midi tick thread to notice that playback has
						// stopped
			Managers.midiManager.reset();
		}
	}

	public void undo(View view) {
		Managers.midiManager.undo();
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
		Managers.midiManager.deleteSelectedNotes();
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
		float millisElapsed = tapTime - lastTapTime;
		lastTapTime = tapTime;
		float bpm = 60000 / millisElapsed;
		// if bpm is far below MIN limit, consider this the first tap, otherwise,
		// if it's under but close, set to MIN_BPM
		if (bpm < MidiManager.MIN_BPM - 10)
			return;
		MidiManager.setBPM(bpm);
	}

	private void spin(long millis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millis)
			;
	}

	public static native void createEngine(AssetManager assetManager);

	public static native boolean createAssetAudioPlayer();
	public static native boolean initTrack(String filename);

	public static native void shutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("nativeaudio");
	}
}