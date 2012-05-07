package com.kh.beatbot.view.helper;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

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

	private float height;
	
	public WaveformHelper(float height) {
		this.height = height;
		start();
	}

	public ArrayList<FloatBuffer> getWaveformFloatBuffers() {
		return waveformSegmentsVB;
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
	
	private	FloatBuffer bytesToFloatBuffer(byte[] bytes) {
		float[] data = floatsFromBytes(bytes);
		int size = data.length;
		int samplesPerPixel = 10;
		float[] outputAry = new float[8*size/samplesPerPixel]; 
		for (int x = 0; x < size/samplesPerPixel; x++) {
            // determine start and end points within WAV
            int start = (int)(x * (float)samplesPerPixel);
            int end = (int)((x + 1) * (float)samplesPerPixel);
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            for (int i = start; i < end; i++) {
                float val = data[i];
                min = val < min ? val : min;
                max = val > max ? val : max;
            }
            float yMin = height - ((min + 1) * .5f * height);            
            float yMax = height - ((max + 1) * .5f * height);
            
            outputAry[x*4] = x * 2;
            outputAry[x*4 + 1] = yMin;
            outputAry[x*4 + 2] = x * 2 + 1;
            outputAry[x*4 + 3] = yMax;            
        }
		
		return MidiView.makeFloatBuffer(outputAry);
	}
	
	private float[] floatsFromBytes(byte[] input) {
		float[] output = new float[input.length / 4];
		ByteBuffer bb = ByteBuffer.wrap(input);
		bb.rewind();
		bb.asFloatBuffer().get(output);
		return output;		
	}
}
