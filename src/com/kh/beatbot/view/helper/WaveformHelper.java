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

import com.kh.beatbot.view.BBView;

public class WaveformHelper extends Thread {
	// holds a single float in the form of 4 bytes, used to input floats from a
	// byte file
	private static ByteBuffer floatBuffer = ByteBuffer.allocate(4);
	static {
		floatBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	private static RandomAccessFile sampleFile = null;


	public static final int DEFAULT_HEIGHT = 100;

	// byte buffers will be read from file in segments and
	// temporarily stored in this array until they are consolidated
	// into one large FloatBuffer and stored in completedWaveforms
	private ArrayList<FloatBuffer> waveformSegmentsVB = new ArrayList<FloatBuffer>();

	public ArrayList<FloatBuffer> getCurrentWaveformVbs() {
		return waveformSegmentsVB;
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
	public static FloatBuffer bytesToFloatBuffer(BBView view, byte[] bytes) {
		return bytesToFloatBuffer(view, bytes, 0);
	}

	public static FloatBuffer bytesToFloatBuffer(BBView view, byte[] bytes, int xOffset) {
		float[] floats = bytesToFloats(bytes, 0);
		return floatsToFloatBuffer(view, floats, 0, (int) view.width, xOffset);
	}

	public static FloatBuffer floatFileToBuffer(BBView view,
			long offset, long numFloats, int xOffset) throws IOException {
		float spp = Math.min(2, numFloats / view.width);
		float[] outputAry = new float[2 * (int) (view.width * spp)];
		for (int x = 0; x < outputAry.length; x += 2) {
			float percent = (float) x / outputAry.length;
			int dataIndex = (int) (offset + percent * numFloats);
			sampleFile.seek(dataIndex * 8); // 4 bytes per float * 2 floats per
											// sample = 8 bytes
			sampleFile.read(floatBuffer.array()); // read in the float at the
													// current position
			float y = view.height * (floatBuffer.getFloat(0) + 1) / 2;
			outputAry[x] = percent * view.width + xOffset;
			outputAry[x + 1] = y;
		}
		return BBView.makeFloatBuffer(outputAry);
	}

	public static FloatBuffer floatsToFloatBuffer(BBView view, float[] data, long offset, long numFloats, int xOffset) {
		float spp = Math.min(2, numFloats / view.width);
		float[] outputAry = new float[2 * (int) (view.width * spp)];
		for (int x = 0; x < outputAry.length; x += 2) {
			float percent = (float) x / outputAry.length;
			int dataIndex = (int) (offset + percent * numFloats) * 2;
			// sanity check - default to y = 0 if index out of bounds
			float y = dataIndex < data.length && dataIndex >= 0 ? view.height
					* (data[(int) dataIndex] + 1) / 2 : 0;
			outputAry[x] = percent * view.width + xOffset;
			outputAry[x + 1] = y;
		}
		return BBView.makeFloatBuffer(outputAry);
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
