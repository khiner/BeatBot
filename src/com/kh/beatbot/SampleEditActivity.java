package com.kh.beatbot;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.SampleWaveformView;


public class SampleEditActivity extends Activity implements OnTouchListener {
	private PlaybackManager playbackManager = null;
	private SampleWaveformView sampleWaveformView = null;	
	private int sampleNum;
	private Button previewButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_edit);
		GlobalVars gv = (GlobalVars)getApplicationContext();
		playbackManager = gv.getPlaybackManager();
		previewButton = (Button)findViewById(R.id.preview_sample);
		previewButton.setOnTouchListener(this);
		sampleNum = getIntent().getExtras().getInt("sampleNum");
		sampleWaveformView = ((SampleWaveformView)findViewById(R.id.sample_waveform_view));
		sampleWaveformView.setPlaybackManager(playbackManager);
		sampleWaveformView.setSampleNum(sampleNum);		
		sampleWaveformView.setOnTouchListener(this);
		String sampleName = getIntent().getExtras().getString("sampleName");
		loadSampleBytes(sampleName);
		playbackManager.armTrack(sampleNum);
		((ToggleButton)findViewById(R.id.loop_toggle)).setChecked(playbackManager.isLooping(sampleNum));
	}

	private void loadSampleBytes(String sampleName) {
		try {
			AssetFileDescriptor fd = getAssets().openFd(sampleName);
			byte[] sampleBytes = new byte[(int) fd.getLength()];
			// read in the sample bytes
			fd.createInputStream().read(sampleBytes);
			// set the view sample bytes
			((SampleWaveformView) findViewById(R.id.sample_waveform_view)).setSampleBytes(sampleBytes);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void toggleLoop(View view) {
		playbackManager.toggleLooping(sampleNum);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (playbackManager.getState() != PlaybackManager.State.PLAYING)
			// if not currently playing, disarm the track
			playbackManager.disarmTrack(sampleNum);
	}
		
	private boolean isInsideView(float x, float y, View view) {
		int[] viewLoc = new int[2];
		view.getLocationOnScreen(viewLoc);
		Log.d("touch x, y", String.valueOf(x) + ", " + String.valueOf(y));
		Log.d("view x, y", String.valueOf(viewLoc[0]) + ", " + String.valueOf(viewLoc[1]));
		if (x >= viewLoc[0] && x <= viewLoc[0] + view.getWidth() &&
            y >= viewLoc[1] && y <= viewLoc[1] + view.getHeight())
			return true;
		else
			return false;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent e) {		
		// delegating the wave edit touch events through the parent activity to allow
		// touching of multiple views at the same time
		// TODO: still doesn't work!
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_DOWN:
			if (isInsideView(e.getRawX(), e.getRawY(), sampleWaveformView))
				sampleWaveformView.handleActionDown(e.getPointerId(0), e.getX(0));
			else if (isInsideView(e.getRawX(), e.getRawY(), previewButton))
				playbackManager.playTrack(sampleNum, .8f, .5f, .5f);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (isInsideView(e.getRawX(), e.getRawY(), sampleWaveformView))
				sampleWaveformView.handleActionPointerDown(e.getPointerId(index), e.getX(index));
			else if (isInsideView(e.getRawX(), e.getRawY(), previewButton))
				playbackManager.playTrack(sampleNum, .8f, .5f, .5f);
			break;
		case MotionEvent.ACTION_MOVE:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (isInsideView(e.getRawX(), e.getRawY(), sampleWaveformView))
				sampleWaveformView.handleActionMove(e);			
			break;
		case MotionEvent.ACTION_POINTER_UP:			
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (isInsideView(e.getRawX(), e.getRawY(), sampleWaveformView))
				sampleWaveformView.handleActionPointerUp(e.getPointerId(index), e.getX(index));
			else if (isInsideView(e.getRawX(), e.getRawY(), previewButton))
				playbackManager.stopTrack(sampleNum);				
			break;
		case MotionEvent.ACTION_UP:
			if (isInsideView(e.getRawX(), e.getRawY(), sampleWaveformView))
				sampleWaveformView.handleActionUp(e.getPointerId(0), e.getX(0));
			else if (isInsideView(e.getRawX(), e.getRawY(), previewButton))
				playbackManager.stopTrack(sampleNum);
			break;
		}
		return true;
	}
}
