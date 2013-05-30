package com.kh.beatbot.global;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.kh.beatbot.view.BBView;

public class SampleFile {

	private File file;
	private RandomAccessFile sampleFile = null;
	// holds a single float in the form of 4 bytes, used to input floats from a
	// byte file
	private ByteBuffer floatBuffer = ByteBuffer.allocate(4);
	private byte[] inBytes = new byte[2];
	private int bytesPerSample = 0;

	private float loopBeginSample, loopEndSample;

	public SampleFile(File file) {
		this.file = file;
		try {
			sampleFile = new RandomAccessFile(file, "r");
			floatBuffer.order(ByteOrder.LITTLE_ENDIAN);
			sampleFile.seek(22);
			int channels = sampleFile.readUnsignedByte();
			bytesPerSample = channels * 2;
			loopBeginSample = 0;
			loopEndSample = getNumSamples();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float getLoopBeginSample() {
		return loopBeginSample;
	}
	
	public float getLoopEndSample() {
		return loopEndSample;
	}

	public void setLoopBeginSample(float loopBeginSample) {
		this.loopBeginSample = loopBeginSample;
	}
	
	public void setLoopEndSample(float loopEndSample) {
		this.loopEndSample = loopEndSample;
	}
	
	public void renameTo(String name) {
		file.renameTo(new File(name));
	}

	public long getNumSamples() {
		long numSamples = 0;
		try {
			numSamples = (sampleFile.length() - 44) / bytesPerSample;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return numSamples;
	}

	public FloatBuffer floatFileToBuffer(BBView view, long offset,
			long numFloats, int xOffset) throws IOException {
		float spp = Math.min(2, numFloats / view.width);
		float[] outputAry = new float[2 * (int) (view.width * spp)];

		for (int x = 0; x < outputAry.length; x += 2) {
			float percent = (float) x / outputAry.length;
			int dataIndex = (int) (offset + percent * numFloats);
			sampleFile.seek(dataIndex * bytesPerSample + 44); // +44 for wav
																// header
			sampleFile.read(inBytes); // read in the float at the
										// current position
			short s = (short) (((inBytes[1] & 0xff) << 8) | (inBytes[0] & 0xff));
			floatBuffer.putFloat(0, s / 32768.0f);
			float y = view.height * (floatBuffer.getFloat(0) + 1) / 2;
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
	
	public String getName() {
		return file.getName();
	}
	
	public String getFullPath() {
		return file.getAbsolutePath();
	}
}
