package com.kh.beatbot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.KarlHiner.BeatBox.R;

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
				icon.setImageResource(R.drawable.microphone_icon_small);
				break;
			case 1: // kick
				icon.setImageResource(R.drawable.kick_icon_small);
				break;
			case 2: // snare
				icon.setImageResource(R.drawable.snare_icon_small);
				break;
			case 3: // hh closed
				icon.setImageResource(R.drawable.hh_closed_icon_small);
				break;
			case 4: // hh open
				icon.setImageResource(R.drawable.hh_open_icon_small);
				break;
			case 5: // rimshot
				icon.setImageResource(R.drawable.rimshot_icon_small);
				break;
			case 6: // bass
				icon.setImageResource(R.drawable.bass_icon_small);
				break;
			}
			return view;
		}
	}

	private MidiManager midiManager;
	private PlaybackManager playbackManager;
	private RecordManager recordManager;

	private MidiSurfaceView midiSurfaceView;

	private final int[] sampleResources = new int[] { R.raw.kick_808,
			R.raw.snare_808, R.raw.hat_closed_808, R.raw.hat_open_808,
			R.raw.rimshot_808, R.raw.tom_low_808 };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		String[] sampleTypes = getResources().getStringArray(
				R.array.sample_types);
		ArrayAdapter<String> adapter = new SampleIconAdapter(this,
				R.layout.instrument_icon_view, sampleTypes);
		ListView sampleListView = (ListView) findViewById(R.id.sampleListView);
		sampleListView.setAdapter(adapter);
		playbackManager = new PlaybackManager(this, sampleResources);
		sampleListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView parentView,
							View childView, int position, long id) {
						playbackManager.playSample(position);
					}
				});
		midiManager = new MidiManager(sampleTypes.length);
		midiManager.setPlaybackManager(playbackManager);

		recordManager = RecordManager.getInstance();
		recordManager.setMidiManager(midiManager);
		recordManager.setThresholdBar((ThresholdBar) findViewById(R.id.thresholdBar));
		midiManager.setRecordManager(recordManager);

		midiSurfaceView = ((MidiSurfaceView) findViewById(R.id.midiSurfaceView));
		midiSurfaceView.setMidiManager(midiManager);
		midiSurfaceView.setRecorderService(recordManager);
		midiSurfaceView.setPlaybackManager(playbackManager);
	}

	@Override
	public void onDestroy() {
		if (isFinishing())
			recordManager.release();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
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
		case R.id.save_midi:
			midiManager.writeToFile();
			return true;
		case R.id.save_wav:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void toggleListening(View view) {
		ImageButton recButton = (ImageButton) view;
		if (recordManager.getState() == RecordManager.State.LISTENING
				|| recordManager.getState() == RecordManager.State.RECORDING) {
			recordManager.stopListening();
			recButton.setImageResource(R.drawable.rec_btn_off_src);
		} else {
			midiSurfaceView.reset();
			recordManager.startListening();
			recButton.setImageResource(R.drawable.rec_btn_on_src);
		}
	}

	public void play(View view) {
		ImageButton playButton = (ImageButton) view;
		if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
			playbackManager.stop();
			playButton.setImageResource(R.drawable.play_btn_src);
		} else if (playbackManager.getState() == PlaybackManager.State.STOPPED) {
			midiSurfaceView.reset();
			playbackManager.play();
			midiManager.start();
			playButton.setImageResource(R.drawable.pause_btn_src);
		}
	}

	public void stop(View view) {
		ImageButton stopButton = (ImageButton)view;
		if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
			playbackManager.stop();
			stopButton.setImageResource(R.drawable.stop_btn_src);
		}		
	}

	public void undo(View view) {

	}
}