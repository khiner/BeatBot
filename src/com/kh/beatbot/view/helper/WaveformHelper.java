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
	public static final int DEFAULT_HEIGHT = 100;
	public static final int DEFAULT_SPP = 2; // default samples per pixel
	
	// byte buffers will be read from file in segments and
	// temporarily stored in this array until they are consolidated
	// into one large FloatBuffer and stored in completedWaveforms
	private ArrayList<FloatBuffer> waveformSegmentsVB = new ArrayList<FloatBuffer>();

	// holds full files of sound bytes after they have finished recording,
	// and before they are classified and displayed as MIDI notes
	private ArrayList<FloatBuffer> completedWaveforms = new ArrayList<FloatBuffer>();

	private Queue<byte[]> bytesQueue = new LinkedList<byte[]>();
		
	private boolean completed;
		
	public ArrayList<FloatBuffer> getCurrentWaveformVBs() {
		return waveformSegmentsVB;
	}

	public ArrayList<FloatBuffer> getCompletedWaveformVBs() {
		return completedWaveforms;
	}
	
	@Override
	public void run() {
		byte[] bytes;
		int xOffset = 0;
		
		while (true) {
			try {
				synchronized (bytesQueue) {
					while (bytesQueue.isEmpty()) {
						bytesQueue.wait();
					}
					bytes = bytesQueue.remove();
				}
				waveformSegmentsVB.add(bytesToFloatBuffer(bytes, DEFAULT_HEIGHT, xOffset));
				xOffset += bytes.length / (DEFAULT_SPP * 2);				
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
		float[] outputAry = new float[2 * size / DEFAULT_SPP];
		for (int x = 0; x < size / DEFAULT_SPP; x++) {
			// determine start and end points within WAV
			int start = x * DEFAULT_SPP;
			int end = (x + 1) * DEFAULT_SPP;
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for (int i = start; i < end; i++) {
				float val = data[i];
				min = val < min ? val : min;
				max = val > max ? val : max;
			}
			
			float y1 = height*(min + 1)/2;
			float y2 = height*(max + 1)/2;
			outputAry[x * 2] = x + xOffset;
			outputAry[x * 2 + 1] = (y1 + y2)/2;
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
