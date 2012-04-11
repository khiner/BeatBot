package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.KarlHiner.BeatBox.R;

public class BeatBotActivity extends Activity {
	private MidiManager midiManager;
	private PlaybackManager playbackManager;
	private RecordManager recordManager;
	
	private MidiSurfaceView midiSurfaceView;
	
    /** Called when the activity is first created. */	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        ListView sampleListView = (ListView)findViewById(R.id.sampleListView);
        String[] sampleTypes = getResources().getStringArray(R.array.sample_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sample_list_item, sampleTypes);
        sampleListView.setAdapter(adapter);      
        playbackManager = new PlaybackManager(this, loadRawResources());
        sampleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView parentView, View childView, int position ,long id) {
        		playbackManager.playSample(position);
        	}
		});        
        // numTracks = number of sample types + 1 for record track
        midiManager = new MidiManager(sampleTypes.length + 1);
        midiManager.setPlaybackManager(playbackManager);
        
        recordManager = RecordManager.getInstance();
        recordManager.setMidiManager(midiManager);
        recordManager.setThresholdBar((ThresholdBar)findViewById(R.id.thresholdBar));
        midiManager.setRecordManager(recordManager);
        
        midiSurfaceView = ((MidiSurfaceView)findViewById(R.id.midiSurfaceView)); 
        midiSurfaceView.setMidiManager(midiManager);
        midiSurfaceView.setRecorderService(recordManager);
        midiSurfaceView.setPlaybackManager(playbackManager);
    }
    
    public int[] loadRawResources() {
    	int[] samplePaths = new int[6];
    	samplePaths[0] = R.raw.kick_808;
    	samplePaths[1] = R.raw.snare_808;
    	samplePaths[2] = R.raw.hat_closed_808;
    	samplePaths[3] = R.raw.hat_open_808;
    	samplePaths[4] = R.raw.rimshot_808;
    	samplePaths[5] = R.raw.tom_low_808;
    	return samplePaths;
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
    	switch(item.getItemId()) {
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
    		return true;
    	case R.id.save_wav:
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    public void toggleListening(View view) {
    	ImageButton recButton = (ImageButton)view;
    	if (recordManager.getState() == RecordManager.State.LISTENING ||
    			recordManager.getState() == RecordManager.State.RECORDING) {
    		recordManager.stopListening();
    		recButton.setImageResource(R.drawable.rec_button_off_src);
    	} else {
    		midiSurfaceView.reset();    		
    		recordManager.startListening();
    		recButton.setImageResource(R.drawable.rec_button_on_src);
    	}
    }
    
    public void play(View view) {
    	Button playButton = (Button)view;
    	if (playbackManager.getState() == PlaybackManager.State.PLAYING) {
    		playbackManager.stop();
    		playButton.setText(R.string.play);
    	} else if (playbackManager.getState() == PlaybackManager.State.STOPPED) {
    		midiSurfaceView.reset();
    		playbackManager.play();
    		midiManager.start();
    		playButton.setText(R.string.stop);
    	}
    }
}