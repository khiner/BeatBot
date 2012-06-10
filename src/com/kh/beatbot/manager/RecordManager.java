package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.ThresholdBarView;

public class RecordManager {
	private static final int RECORDER_BPP = 16;
	private static final String SAVE_FOLDER = "BeatBot/Recorded_Audio";
	private static final String TEMP_FILE = "record_temp.raw";
	private String baseFilePath = null;
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final int CHANNELS = 2;
	private final long BYTE_RATE = RECORDER_BPP * RECORDER_SAMPLERATE
			* CHANNELS / 8;
	private final long LONG_SAMPLE_RATE = RECORDER_SAMPLERATE;

	private Random random = new Random();
	private String currFilename = null;

	private static RecordManager singletonInstance = null;

	// Midi Manager to manage MIDI events
	private MidiManager midiManager;
	private MidiView midiView;
	private AudioClassificationManager audioClassificationManager;

	private ThresholdBarView thresholdBar;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	// queue of paths to raw audio data to process
	private Queue<String> processingQueue = new LinkedList<String>();
	private Queue<Long> recordTickQueue = new LinkedList<Long>();
	private Thread recordingThread = null;
	private Thread smrtThread = null;
	private State state;
	private FileOutputStream os = null;

	private short currAmp = 0;
	private long recordStartTick = 0;

	public enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	public State getState() {
		return state;
	}

	public boolean isRecording() {
		return state == State.RECORDING;
	}
	
	public long getRecordStartTick() {
		return recordStartTick;
	}
	
	public static RecordManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new RecordManager();
		}
		return singletonInstance;
	}

	private RecordManager() {
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
		initRecorder();
		baseFilePath = createBaseRecordPath();
		state = State.INITIALIZING;
	}

	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
	}

	public void setThresholdBar(ThresholdBarView thresholdBar) {
		this.thresholdBar = thresholdBar;
	}
	
	public void setMidiView(MidiView midiView) {
		this.midiView = midiView;
	}
	
	public void setAudioClassificationManager(AudioClassificationManager audioClassificationManager) {
		this.audioClassificationManager = audioClassificationManager;
	}
	
	private void initRecorder() {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, SAVE_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav");
	}

	private String createBaseRecordPath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, SAVE_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}

	private String getTempFilename() {
		File tempFile = new File(baseFilePath, TEMP_FILE);

		int i = 0;
		while (tempFile.exists()) {
			i++;
			tempFile = new File(baseFilePath, TEMP_FILE + i);
		}

		return tempFile.getAbsolutePath();
	}

	private void writeAudioDataToFile() {
		byte buffer[] = new byte[bufferSize];
		recorder.read(buffer, 0, bufferSize);
		recorder.read(buffer, 0, bufferSize);

		// let the midi view know recording has started
		// so it can listen for byte buffers to draw them
		midiView.signalRecording();
		
		try {
			while (state != State.INITIALIZING) {
				recorder.read(buffer, 0, bufferSize);

				if (state == State.LISTENING && overThreshold(buffer)) {
					startRecording();
				}
				if (state == State.RECORDING) {
					os.write(buffer); // write buffer to file
					midiView.drawWaveform(buffer); // draw waveform
					if (underThreshold(buffer)) {
						stopRecording();
						midiView.endWaveform();
					}
				}
				// update threshold bar
				float Db = shortToDb(currAmp);
				thresholdBar.setChannelLevel(Db);
			}
		} catch (IOException e) {
			try {
				stopRecording();
			} catch (IOException e2) {
				e.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	public void notifyLoop() {
		if (state == State.RECORDING) {
			recordTickQueue.add(midiManager.getLoopTick());
			addRecordNote(random.nextInt(midiManager.getNumSamples()));
			recordTickQueue.add((long)0);
			recordStartTick = 0;
		}
	}
	
	private void addRecordNote(int note) {
		long recordNoteOnTick = recordTickQueue.remove();
		long recordNoteOffTick = recordTickQueue.remove();
		midiView.addMidiNote(recordNoteOnTick, recordNoteOffTick, note);
		midiView.handleMidiCollisions();		
	}
	
	private void processByteBuffersOnQueue() {
		String filePath = null;
		while ((state == State.RECORDING || state == State.LISTENING) || !processingQueue.isEmpty()) {
			try {
				synchronized (processingQueue) {
					while (processingQueue.isEmpty())
						processingQueue.wait();
					filePath = processingQueue.remove();
				}
				try {
					// copy file to byte array
					// byte[] bytes = getBytesFromFile(new
					// File(filePath));
					// copy to wave
					copyWaveFile(filePath, getFilename());
					// close file
					//audioClassificationManager.extractFeatures(new File(filePath));
					new File(filePath).delete();
					// cl = classify(xml);
					int cl = random.nextInt(midiManager.getNumSamples());
					addRecordNote(cl);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void startRecording() throws IOException {
		recordStartTick = midiManager.getCurrTick();
		recordTickQueue.add(recordStartTick);
		currFilename = getTempFilename();
		os = new FileOutputStream(currFilename);
		state = State.RECORDING;
	}

	public void stopRecording() throws IOException {
		state = State.LISTENING;
		recordTickQueue.add(midiManager.getCurrTick());		
		// close and add to processing queue
		os.close();
		synchronized (processingQueue) {
			processingQueue.add(currFilename);
			processingQueue.notify();
		}
	}

	public void startListening() {
		if (recorder.getState() != AudioRecord.STATE_INITIALIZED)
			initRecorder();
		recorder.startRecording();
		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");
		state = State.LISTENING;
		recordingThread.start();
		smrtThread = new Thread(new Runnable() {
			@Override
			public void run() {
				processByteBuffersOnQueue();
			}
		}, "Smart Thread");
		smrtThread.start();
	}

	public void stopListening() {
		boolean recording = (state == State.RECORDING);
		state = State.INITIALIZING;
		if (recorder != null) {
			recorder.stop();
			try {
				if (recording) {
					stopRecording();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void release() {
		if (recorder != null) {
			if (state == State.LISTENING || state == State.RECORDING)
				stopListening();
			recorder.release();
		}
	}

	private void copyWaveFile(String inFilename, String outFilename) {
		byte[] buffer = new byte[bufferSize];

		try {
			FileInputStream in = new FileInputStream(inFilename);
			FileOutputStream out = new FileOutputStream(outFilename);
			long totalAudioLen = in.getChannel().size();
			long totalDataLen = totalAudioLen + 36;

			writeWaveFileHeader(out, totalAudioLen, totalDataLen);

			while (in.read(buffer) != -1) {
				out.write(buffer);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean overThreshold(byte[] buffer) {
		short onThreshold = thresholdBar.getShortThreshold();
		for (int i = 0; i < bufferSize / 32; i++) {
			// 16bit sample size
			currAmp = getShort(buffer[i * 32], buffer[i * 32 + 1]);
			if (currAmp > onThreshold) { // Check amplitude
				return true;
			}
		}
		return false;
	}

	private boolean underThreshold(byte[] buffer) {
		short offThreshold = (short) (thresholdBar.getShortThreshold() * .8);
		for (int i = 0; i < bufferSize / 32; i++) {
			// 16bit sample size
			currAmp = getShort(buffer[i * 32], buffer[i * 32 + 1]);
			if (currAmp > offThreshold) { // Check amplitude
				return false;
			}
		}
		return true;
	}

	// Returns the contents of the file in a byte array.
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) CHANNELS;
		header[23] = 0;
		header[24] = (byte) (LONG_SAMPLE_RATE & 0xff);
		header[25] = (byte) ((LONG_SAMPLE_RATE >> 8) & 0xff);
		header[26] = (byte) ((LONG_SAMPLE_RATE >> 16) & 0xff);
		header[27] = (byte) ((LONG_SAMPLE_RATE >> 24) & 0xff);
		header[28] = (byte) (BYTE_RATE & 0xff);
		header[29] = (byte) ((BYTE_RATE >> 8) & 0xff);
		header[30] = (byte) ((BYTE_RATE >> 16) & 0xff);
		header[31] = (byte) ((BYTE_RATE >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	/*
	 * Converts a byte[2] to a short, in LITTLE_ENDIAN format
	 */
	private short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}

	private float shortToDb(short amp) {
		return 20 * (float) Math.log10(Math.abs(amp) / 32768f);
	}
}