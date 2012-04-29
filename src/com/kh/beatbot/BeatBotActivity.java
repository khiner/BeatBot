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

import com.KarlHiner.BeatBox.R;
import com.kh.beatbot.menu.MidiFileMenu;

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

	private MidiSurfaceView midiSurfaceView;

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
		playbackManager = new PlaybackManager(this, sampleResources);
		sampleListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView parentView,
							View childView, int position, long id) {
						playbackManager.playSample(position - 1);
					}
				});
		if (savedInstanceState == null)
			midiManager = new MidiManager(sampleTypes.length);
		else
			midiManager = savedInstanceState.getParcelable("midiManager");

		midiManager.setPlaybackManager(playbackManager);

		recordManager = RecordManager.getInstance();
		recordManager.setMidiManager(midiManager);
		recordManager
				.setThresholdBar((ThresholdBar) findViewById(R.id.thresholdBar));
		midiManager.setRecordManager(recordManager);

		midiSurfaceView = ((MidiSurfaceView) findViewById(R.id.midiSurfaceView));
		midiSurfaceView.setMidiManager(midiManager);
		midiSurfaceView.setRecorderService(recordManager);
		midiSurfaceView.setPlaybackManager(playbackManager);
		GlobalVars gv = (GlobalVars)getApplicationContext();
		gv.setMidiManager(midiManager);
	
		((BpmView) findViewById(R.id.bpm)).setText(String.valueOf((int)midiManager
				.getBPM()));
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
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("midiManager", midiManager);
		outState.putBoolean("playing", playbackManager.getState() == PlaybackManager.State.PLAYING);
		outState.putBoolean("recording", recordManager.getState() != RecordManager.State.INITIALIZING);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		Intent midiFileMenuIntent = new Intent(this, MidiFileMenu.class);
		menu.findItem(R.id.midi_menu_item).setIntent(midiFileMenuIntent);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.snap:
			if (midiSurfaceView.toggleSnapToGrid())
				item.setIcon(R.drawable.btn_check_buttonless_on);
			else
				item.setIcon(R.drawable.btn_check_buttonless_off);
			return true;
		case R.id.quantize_current:
			midiManager.quantize(midiSurfaceView.currentBeatDivision());
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
		ImageButton recButton = (ImageButton) view;
		if (recordManager.getState() != RecordManager.State.INITIALIZING) {
			recordManager.stopListening();
			recButton.setImageResource(R.drawable.rec_btn_off_src);
		} else {
			midiSurfaceView.reset();
			// if we are already playing, the midiManager is already ticking away.
			if (playbackManager.getState() != PlaybackManager.State.PLAYING)
				play((ImageButton)findViewById(R.id.playButton));
			recordManager.startListening();
			recButton.setImageResource(R.drawable.rec_btn_on_src);
		}
	}
	
	public void play(View view) {
		if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
			midiManager.reset();
		} else if (playbackManager.getState() == PlaybackManager.State.STOPPED) {
			((ImageButton)findViewById(R.id.playButton)).setImageResource(R.drawable.play_btn_on_src);
			playbackManager.play();
			midiManager.start();			
		}
	}

	public void stop(View view) {
		if (recordManager.getState() != RecordManager.State.INITIALIZING)
			record((ImageButton)findViewById(R.id.recordButton));
		if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
			playbackManager.stop();
			spin(10); // wait for midi tick thread to notice that playback has stopped
			midiManager.reset();
			((ImageButton)findViewById(R.id.playButton)).setImageResource(R.drawable.play_btn_off_src);
		}
	}

	public void undo(View view) {
		midiManager.undo();
	}

	public void bpmTap(View view) {
		long tapTime = System.currentTimeMillis();		
		float secondsElapsed = (tapTime - lastTapTime)/1000f;
		lastTapTime = tapTime;
		// if too much time has passed, assume this is inteded as the first tap
		if (secondsElapsed > 1.8)
			return;
		
		float bpm = (1/secondsElapsed)*60;
		((BpmView)findViewById(R.id.bpm)).setText(String.valueOf((int)bpm));
		midiManager.setBPM(bpm);
	}
	
	private void spin(long millis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millis);
	}
}