package com.kh.beatbot.view.helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

import com.kh.beatbot.view.MidiView;

public class WaveformHelper extends Thread {
	// byte buffers will be read from file in segments and
	// temporarily stored in this array until they are consolidated
	// into one large FloatBuffer and stored in completedWaveforms
	private ArrayList<FloatBuffer> waveformSegmentsVB = new ArrayList<FloatBuffer>();

	// holds full files of sound bytes after they have finished recording,
	// and before they are classified and displayed as MIDI notes
	private ArrayList<FloatBuffer> completedWaveforms = new ArrayList<FloatBuffer>();

	Queue<byte[]> bytesQueue = new LinkedList<byte[]>();

	private float width, height;

	private int xOffset = 0;
	
	private boolean completed;

	public WaveformHelper(float width, float height) {
		this.width = width;
		this.height = height;
		start();
	}

	public ArrayList<FloatBuffer> getCurrentWaveformVBs() {
		return waveformSegmentsVB;
	}

	public ArrayList<FloatBuffer> getCompletedWaveformVBs() {
		return completedWaveforms;
	}
	
	@Override
	public void run() {
		byte[] bytes;
		while (true) {
			try {
				synchronized (bytesQueue) {
					while (bytesQueue.isEmpty()) {
						bytesQueue.wait();
					}
					bytes = bytesQueue.remove();
				}
				waveformSegmentsVB.add(bytesToFloatBuffer(bytes));
				if (completed) {				
					xOffset = 0;					
					waveformSegmentsVB.clear();
					completed = false;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addBytesToQueue(byte[] bytes) {
		synchronized (bytesQueue) {
			bytesQueue.add(bytes);
			bytesQueue.notify();
		}
	}

	public void endWaveform() {
		completed = true;
	}
	
	public FloatBuffer bytesToFloatBuffer(byte[] bytes) {
		float[] data = floatsFromBytes(bytes);
		int size = data.length;
		int samplesPerPixel = width <= 0 ? 200 : size/(int)width; // default to 200 if no width specified
		float[] outputAry = new float[8 * size / samplesPerPixel];
		for (int x = 0; x < size / samplesPerPixel; x++) {
			// determine start and end points within WAV
			int start = x * samplesPerPixel;
			int end = (x + 1) * samplesPerPixel;
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for (int i = start; i < end; i++) {
				float val = data[i];
				min = val < min ? val : min;
				max = val > max ? val : max;
			}
			float yMin = (height - ((min + 1) * .5f * height));
			float yMax = (height - ((max + 1) * .5f * height));
			outputAry[x * 4] = x + xOffset;
			outputAry[x * 4 + 1] = yMin;
			outputAry[x * 4 + 2] = x + xOffset;
			outputAry[x * 4 + 3] = yMax;
		}
		xOffset += size / samplesPerPixel;

		return MidiView.makeFloatBuffer(outputAry);
	}

	private float[] floatsFromBytes(byte[] input) {
		ShortBuffer sbuf = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] audioShorts = new short[sbuf.capacity()];
		sbuf.get(audioShorts);
		float[] audioFloats = new float[audioShorts.length];
		for (int i = 0; i < audioShorts.length; i++) {
			audioFloats[i] = ((float)audioShorts[i])/0x8000;
		}		
		return audioFloats;
	}
}
