package com.kh.beatbot.view.helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.kh.beatbot.view.SurfaceViewBase;

public class WaveformHelper extends Thread {
	private boolean recording = false;
	
	public static final int DEFAULT_HEIGHT = 100;
	
	// byte buffers will be read from file in segments and
	// temporarily stored in this array until they are consolidated
	// into one large FloatBuffer and stored in completedWaveforms
	private ArrayList<FloatBuffer> waveformSegmentsVB = new ArrayList<FloatBuffer>();

	private Queue<byte[]> bytesQueue = new LinkedList<byte[]>();
		
	private boolean completed;
		
	public ArrayList<FloatBuffer> getCurrentWaveformVbs() {
		return waveformSegmentsVB;
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
	public static FloatBuffer bytesToFloatBuffer(byte[] bytes, float width, float height) {
		return bytesToFloatBuffer(bytes, width, height, 0);
	}
	
	public static FloatBuffer bytesToFloatBuffer(byte[] bytes, float width, float height, int xOffset) {
		float[] floats = bytesToFloats(bytes, 0);
		return floatsToFloatBuffer(floats, width, height, 0, (int)width, xOffset);
	}
	
	public static FloatBuffer floatsToFloatBuffer(float[] data, float width, float height, int offset, int numFloats, int xOffset) {
		float spp = Math.min(2, numFloats / width);
		float[] outputAry = new float[2 * (int)(width * spp)];
		for (int x = 0; x < outputAry.length / 2; x++) {
			float percent = (float)(x * 2) / outputAry.length;
			float y = height*(data[offset + (int)(percent * numFloats)] + 1)/2;
			outputAry[x * 2] = percent * width + xOffset;
			outputAry[x * 2 + 1] = y;
		}
		return SurfaceViewBase.makeFloatBuffer(outputAry);
	}

	public static float[] bytesToFloats(byte[] input, int skip) {
		ShortBuffer shortBuffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] shortData = new short[shortBuffer.capacity()];
		shortBuffer.get(shortData);
		float[] floatData = new float[shortData.length - skip];
		for (int i = 0; i < shortData.length - skip; i++) {
			floatData[i] = ((float)shortData[i + skip])/0x8000;
		}		
		return floatData;
	}
}
