package com.kh.beatbot.view.group;

import android.opengl.GLSurfaceView;

import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.MidiTrackView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class MidiGroup extends TouchableBBView {

	public MidiView midiView;
	public MidiTrackView midiTrackControl;
	
	public MidiGroup(TouchableSurfaceView parent) {
		super(parent);
	}

	public int getRenderMode() {
		// needs to continuously render
		return GLSurfaceView.RENDERMODE_CONTINUOUSLY;
	}
	
	public void trackAdded(int newTrackNum) {
		midiTrackControl.trackAdded(newTrackNum);
		midiView.trackAdded(newTrackNum);
	}
	
	@Override
	protected void loadIcons() {
		// parent view - no icons to load
	}

	@Override
	public void init() {
		
	}

	public void draw() {
		
	}

	@Override
	protected void createChildren() {
		midiView = new MidiView((TouchableSurfaceView)root);
		midiTrackControl = new MidiTrackView((TouchableSurfaceView)root);
		addChild(midiView);
		addChild(midiTrackControl);
	}

	@Override
	public void layoutChildren() {
		int numTracks = Managers.trackManager.getNumTracks();
		MidiView.allTracksHeight = height - MidiView.Y_OFFSET;
		MidiView.trackHeight = MidiView.allTracksHeight / numTracks;
		float w = MidiView.trackHeight * 2.5f;
		midiTrackControl.layout(this, 0, 0, w, height);
		midiView.layout(this, w, 0, width - w, height);
	}
}
