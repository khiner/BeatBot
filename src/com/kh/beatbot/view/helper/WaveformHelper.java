package com.kh.beatbot.view.helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.kh.beatbot.view.MidiView;

public class WaveformHelper extends Thread {
	private boolean recording = false;
	
	public static final int DEFAULT_HEIGHT = 100;
	
	// byte buffers will be read from file in segments and
	// temporarily stored in this array until they are consolidated
	// into one large FloatBuffer and stored in completedWaveforms
	private ArrayList<FloatBuffer> waveformSegmentsVB = new ArrayList<FloatBuffer>();

	// holds full files of sound bytes after they have finished recording,
	// and before they are classified and displayed as MIDI notes
	private ArrayList<FloatBuffer> completedWaveforms = new ArrayList<FloatBuffer>();

	private Queue<byte[]> bytesQueue = new LinkedList<byte[]>();
		
	private boolean completed;
		
	public ArrayList<FloatBuffer> getCurrentWaveformVbs() {
		return waveformSegmentsVB;
	}

	public ArrayList<FloatBuffer> getCompletedWaveformVBs() {
		return completedWaveforms;
	}
	
	@Override
	public void run() {
		byte[] bytes = null;
		int xOffset = 0;
		recording = true;
		while (recording) {
			try {
				synchronized (bytesQueue) {
					while (bytesQueue.isEmpty()) {
						if (!recording)
							break;
						bytesQueue.wait();
					}
					if (!recording)
						break;
					bytes = bytesQueue.remove();
				}
				waveformSegmentsVB.add(bytesToFloatBuffer(bytes, DEFAULT_HEIGHT, xOffset));
				xOffset += bytes.length / 2;				
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

	public void endRecording() {
		recording = false;
		synchronized (bytesQueue) {
			bytesQueue.notify();
		}
	}
	
	public void endWaveform() {
		completed = true;
	}
	
	// default to 0 offset, used when only a single FloatBuffer is needed
	// (for a static sample)
	public static FloatBuffer bytesToFloatBuffer(byte[] bytes, float height) {
		return bytesToFloatBuffer(bytes, height, 0);
	}
	
	public static FloatBuffer bytesToFloatBuffer(byte[] bytes, float height, int xOffset) {
		float[] floats = bytesToFloats(bytes);
		return floatsToFloatBuffer(floats, height, xOffset);
	}
	
	public static FloatBuffer floatsToFloatBuffer(float[] data, float height, int xOffset) {
		int size = data.length;
		// int samplesPerPixel = width <= 0 || (int)(size/width) > 4 ? 4 : (int)(size/width);
		float[] outputAry = new float[2 * size];
		for (int x = 0; x < size; x++) {
			float y = height*(data[x] + 1)/2;
			outputAry[x * 2] = x + xOffset;
			outputAry[x * 2 + 1] = y;
		}

		return MidiView.makeFloatBuffer(outputAry);
	}

	public static float[] bytesToFloats(byte[] input) {
		ShortBuffer shortBuffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] shortData = new short[shortBuffer.capacity()];
		shortBuffer.get(shortData);
		float[] floatData = new float[shortData.length];
		for (int i = 0; i < shortData.length; i++) {
			floatData[i] = ((float)shortData[i])/0x8000;
		}		
		return floatData;
	}
}
