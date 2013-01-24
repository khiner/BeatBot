package com.kh.beatbot.view.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.kh.beatbot.view.GLSurfaceViewBase;

public class WaveformHelper extends Thread {
	// holds a single float in the form of 4 bytes, used to input floats from a
	// byte file
	private static ByteBuffer floatBuffer = ByteBuffer.allocate(4);
	static {
		floatBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	private static RandomAccessFile sampleFile = null;

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
				waveformSegmentsVB.add(bytesToFloatBuffer(bytes,
						DEFAULT_HEIGHT, xOffset));
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

	public static void setSampleFile(File file) {
		try {
			sampleFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static RandomAccessFile getSampleFile() {
		return sampleFile;
	}

	// default to 0 offset, used when only a single FloatBuffer is needed
	// (for a static sample)
	public static FloatBuffer bytesToFloatBuffer(byte[] bytes, float width,
			float height) {
		return bytesToFloatBuffer(bytes, width, height, 0);
	}

	public static FloatBuffer bytesToFloatBuffer(byte[] bytes, float width,
			float height, int xOffset) {
		float[] floats = bytesToFloats(bytes, 0);
		return floatsToFloatBuffer(floats, width, height, 0, (int) width,
				xOffset);
	}

	public static FloatBuffer floatFileToBuffer(float width, float height,
			long offset, long numFloats, int xOffset) throws IOException {
		float spp = Math.min(2, numFloats / width);
		float[] outputAry = new float[2 * (int) (width * spp)];
		for (int x = 0; x < outputAry.length; x += 2) {
			float percent = (float) x / outputAry.length;
			int dataIndex = (int) (offset + percent * numFloats);
			sampleFile.seek(dataIndex * 8); // 4 bytes per float * 2 floats per
											// sample = 8 bytes
			sampleFile.read(floatBuffer.array()); // read in the float at the
													// current position
			float y = height * (floatBuffer.getFloat(0) + 1) / 2;
			outputAry[x] = percent * width + xOffset;
			outputAry[x + 1] = y;
		}
		return GLSurfaceViewBase.makeFloatBuffer(outputAry);
	}

	public static FloatBuffer floatsToFloatBuffer(float[] data, float width,
			float height, long offset, long numFloats, int xOffset) {
		float spp = Math.min(2, numFloats / width);
		float[] outputAry = new float[2 * (int) (width * spp)];
		for (int x = 0; x < outputAry.length; x += 2) {
			float percent = (float) x / outputAry.length;
			int dataIndex = (int) (offset + percent * numFloats) * 2;
			// sanity check - default to y = 0 if index out of bounds
			float y = dataIndex < data.length && dataIndex >= 0 ? height
					* (data[(int) dataIndex] + 1) / 2 : 0;
			outputAry[x] = percent * width + xOffset;
			outputAry[x + 1] = y;
		}
		return GLSurfaceViewBase.makeFloatBuffer(outputAry);
	}

	public static float[] bytesToFloats(byte[] input, int skip) {
		ShortBuffer shortBuffer = ByteBuffer.wrap(input)
				.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] shortData = new short[shortBuffer.capacity()];
		shortBuffer.get(shortData);
		float[] floatData = new float[shortData.length - skip];
		for (int i = 0; i < shortData.length - skip; i++) {
			floatData[i] = ((float) shortData[i + skip]) / 0x8000;
		}
		return floatData;
	}
}
