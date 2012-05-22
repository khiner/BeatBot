package com.kh.beatbot.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class PlaybackManager {
	private class Sample {
		private int streamId;
		private boolean muted = false;
		private float leftVolume = 0;
		private float rightVolume = 0;
		
		public Sample(int streamId) {
			this.streamId = streamId;
		}
	}
	
	private static PlaybackManager singletonInstance = null;
	
	private State state;
	private SoundPool soundPool;
	
	private Sample[] samples;	

	private Sample soloSample = null;
	
	public enum State {
		PLAYING, STOPPED
	}
	
	public static PlaybackManager getInstance(Context context, int[] sampleRawResources) {
		if (singletonInstance == null) {
			singletonInstance = new PlaybackManager(context, sampleRawResources);
		}
		return singletonInstance;			
	}
	
	private PlaybackManager(Context context, int[] sampleRawResources) {
		state = State.STOPPED;		
		samples = new Sample[sampleRawResources.length];
		soundPool = new SoundPool(sampleRawResources.length, AudioManager.STREAM_MUSIC, 0);
		for (int i = 0; i < sampleRawResources.length; i++) {
			samples[i] = new Sample(soundPool.load(context, sampleRawResources[i], 1));
		}
	}
		
	public State getState() {
		return state;
	}
	
	public void play() {
		state = State.PLAYING;
	}

	public void stop() {
		state = State.STOPPED;
	}
	
	public void release() {
		state = State.STOPPED;
		soundPool.release();
	}
	
	public void setSolo(int sampleId) {
		if (sampleId >= 0) {
			soloSample = samples[sampleId];
			soundPool.setVolume(soloSample.streamId, soloSample.leftVolume, soloSample.rightVolume);
			for (Sample sample : samples) {
				if (!sample.equals(soloSample)) {
					soundPool.setVolume(sample.streamId, 0, 0);					
				}
			}					
		} else {			
			soloSample = null;
			for (Sample sample : samples) {
				if (!sample.muted)
					soundPool.setVolume(sample.streamId, sample.leftVolume, sample.rightVolume);					
			}			
		}		
	}
		
	public void setMute(int sampleId, boolean muted) {
		samples[sampleId].muted = muted;
		if (muted)
			soundPool.setVolume(samples[sampleId].streamId, 0, 0);
		else
			soundPool.setVolume(samples[sampleId].streamId, samples[sampleId].leftVolume, samples[sampleId].rightVolume);
	}
	
	public void playSample(int sampleNum, int velocity, int pan, int pitch) {
		if (sampleNum >= 0 && sampleNum < samples.length) {
			float normVelocity = velocity/127f;
			float normPan = pan/127f;
			float normPitch = 2*pitch/127f;
			// set the volume of the sample regardless of mute status, so it is remembered if unmuted mid-note
			samples[sampleNum].leftVolume = normVelocity*(1 - normPan);
			samples[sampleNum].rightVolume = normVelocity*normPan;
			float playVolumeLeft = samples[sampleNum].leftVolume;
			float playVolumeRight = samples[sampleNum].rightVolume;
			// if muted or another note is soloing, still play the sample, but just at 0 volume
			// so we can unmute the sample mid-note
			if (samples[sampleNum].muted || soloSample != null && !soloSample.equals(samples[sampleNum])) {
				playVolumeLeft = 0;
				playVolumeRight = 0;				
			}
			samples[sampleNum].streamId = soundPool.play(sampleNum, playVolumeLeft, playVolumeRight, 1, 0, 1);
			soundPool.setRate(samples[sampleNum].streamId, normPitch);
			// highlight the icon to indicate playing
			//context.activateIcon(sampleNum);
		}
	}
	
	public void stopSample(int sampleNum) {
		if (sampleNum >= 0 && sampleNum < samples.length) {
			soundPool.stop(samples[sampleNum].streamId);
			// set the icon back to normal after playing is done			
			//context.deactivateIcon(sampleNum);
		}
	}
	
	public void stopAllSamples() {
		for (int sampleID = 0; sampleID < samples.length; sampleID++) {
			soundPool.stop(samples[sampleID].streamId);
		}
	}
}
