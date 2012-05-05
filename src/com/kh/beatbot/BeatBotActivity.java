package com.kh.beatbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBox.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.view.BpmView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.ThresholdBarView;

public class BeatBotActivity extends Activity {
	public class SampleIconAdapter extends ArrayAdapter<String> {
		String[] sampleTypes;
		int resourceId;

		public SampleIconAdapter(Context context, int resourceId,
				String[] sampleTypes) {
			super(context, resourceId, sampleTypes);
			this.sampleTypes = sampleTypes;
			this.resourceId = resourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(resourceId, parent, false);
			ImageView icon = (ImageView) view.findViewById(R.id.iconView);

			switch (position) {
			case 0: // recorded/voice
				icon.setImageResource(R.drawable.microphone_icon_src);
				break;
			case 1: // kick
				icon.setImageResource(R.drawable.kick_icon_src);
				break;
			case 2: // snare
				icon.setImageResource(R.drawable.snare_icon_src);
				break;
			case 3: // hh closed
				icon.setImageResource(R.drawable.hh_closed_icon_src);
				break;
			case 4: // hh open
				icon.setImageResource(R.drawable.hh_open_icon_src);
				break;
			case 5: // rimshot
				icon.setImageResource(R.drawable.rimshot_icon_src);
				break;
			case 6: // bass
				icon.setImageResource(R.drawable.bass_icon_src);
				break;
			}
			return view;
		}
	}

	private ListView sampleListView;
	private MidiManager midiManager;
	private PlaybackManager playbackManager;
	private RecordManager recordManager;

	private MidiView midiView;

	private final int[] sampleResources = new int[] { R.raw.kick_808,
			R.raw.snare_808, R.raw.hat_closed_808, R.raw.hat_open_808,
			R.raw.rimshot_808, R.raw.tom_low_808 };
	
	private long lastTapTime = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// assign hardware (ringer) volume +/- to media while this application has focus 
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.main);
		String[] sampleTypes = getResources().getStringArray(
				R.array.sample_types);
		ArrayAdapter<String> adapter = new SampleIconAdapter(this,
				R.layout.sample_icon_view, sampleTypes);
		sampleListView = (ListView) findViewById(R.id.sampleListView);
		sampleListView.setAdapter(adapter);
		sampleListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView parentView,
							View childView, int position, long id) {
						// preview the sample with a default velocity of 80
						playbackManager.playSample(position - 1, 80);
					}
				});
		
		// get all Manager singletons
		playbackManager = PlaybackManager.getInstance(this, sampleResources);
		recordManager = RecordManager.getInstance();
		// if this context is being restored from a destroyed context,
		// recover the midiManager.  otherwise, create a new one
		if (savedInstanceState == null)
			midiManager = MidiManager.getInstance(sampleTypes.length - 1);
		else
			midiManager = savedInstanceState.getParcelable("midiManager");
		
		midiManager.setPlaybackManager(playbackManager);
		recordManager.setMidiManager(midiManager);
		// recordManager needs the threshold bar (with levels display) to send decibel levels
		recordManager
				.setThresholdBar((ThresholdBarView) findViewById(R.id.thresholdBar));
		midiManager.setRecordManager(recordManager);

		midiView = ((MidiView) findViewById(R.id.midiView));
		midiView.setMidiManager(midiManager);
		midiView.setRecordManager(recordManager);
		midiView.setPlaybackManager(playbackManager);
		if (savedInstanceState != null)
			midiView.readFromBundle(savedInstanceState);

		// set midiManager as a global variable, since it needs to be accessed by
		// separate MidiFileMenu activity
		GlobalVars gv = (GlobalVars)getApplicationContext();
		gv.setMidiManager(midiManager);
	
		((BpmView) findViewById(R.id.bpm)).setText(String.valueOf((int)midiManager
				.getBPM()));
		
		// recall from last instance state whether we were recording and/or playing
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
			playbackManager.release();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("midiManager", midiManager);
		midiView.writeToBundle(outState);
		outState.putBoolean("playing", playbackManager.getState() == PlaybackManager.State.PLAYING);
		outState.putBoolean("recording", recordManager.getState() != RecordManager.State.INITIALIZING);
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
			midiManager.quantize(midiView.getCurrentBeatDivision());
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
		// midi import/export menu item is handled as an intent - MidiFileMenu.class	
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// DON'T USE YET! this needs to run on the UI thread somehow.
	public void activateIcon(int sampleNum) {
		((ImageView) sampleListView.getChildAt(sampleNum)).setImageState(
				new int[] { android.R.attr.state_pressed }, true);
	}

	// DON'T USE YET! this needs to run on the UI thread somehow.
	public void deactivateIcon(int sampleNum) {
		((ImageView) sampleListView.getChildAt(sampleNum)).setImageState(
				new int[] { android.R.attr.state_empty }, true);
	}

	public void record(View view) {
		if (recordManager.getState() != RecordManager.State.INITIALIZING) {
			recordManager.stopListening();
			((ToggleButton)view).setChecked(false);
		} else {
			midiView.reset();
			// if we are already playing, the midiManager is already ticking away.
			if (playbackManager.getState() != PlaybackManager.State.PLAYING)
				play(findViewById(R.id.playButton));
			recordManager.startListening();
		}
	}
	
	public void play(View view) {
		((ToggleButton)view).setChecked(true);
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
			((ToggleButton)findViewById(R.id.playButton)).setChecked(false);
			spin(10); // wait for midi tick thread to notice that playback has stopped
			midiManager.reset();
		}
	}

	public void undo(View view) {
		midiManager.undo();
		midiView.updateSelectedLevelNotes();
	}
	
	public void levels(View view) {
		midiView.toggleLevelsView();
	}

	public void volume(View view) {
		((ToggleButton)findViewById(R.id.panButton)).setChecked(false);
		((ToggleButton)findViewById(R.id.pitchButton)).setChecked(false);
	}
	
	public void pan(View view) {
		((ToggleButton)findViewById(R.id.volumeButton)).setChecked(false);
		((ToggleButton)findViewById(R.id.pitchButton)).setChecked(false);
		
	}
	
	public void pitch(View view) {
		((ToggleButton)findViewById(R.id.volumeButton)).setChecked(false);
		((ToggleButton)findViewById(R.id.panButton)).setChecked(false);
		
	}
	
	public void bpmTap(View view) {
		long tapTime = System.currentTimeMillis();		
		float secondsElapsed = (tapTime - lastTapTime)/1000f;
		lastTapTime = tapTime;
		float bpm = 60/secondsElapsed;
		// bpm limits
		if (bpm < 30 || bpm > 500)
			return;		
		((BpmView)findViewById(R.id.bpm)).setText(String.valueOf((int)bpm));
		midiManager.setBPM(bpm);
	}
	
	private void spin(long millis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millis);
	}
}