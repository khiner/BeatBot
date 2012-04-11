package com.kh.beatbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class RecordManager {
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private final int CHANNELS = 2;
	private final long BYTE_RATE = RECORDER_BPP * RECORDER_SAMPLERATE * CHANNELS / 8;
	private final long LONG_SAMPLE_RATE = RECORDER_SAMPLERATE;
	
	private static RecordManager singletonInstance = null;

	// Midi Manager to manage MIDI events
	private MidiManager midiManager;
	private ThresholdBar thresholdBar;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private State state;
	FileOutputStream os = null;

	// The amp at which recording starts/stops (like thermostat temp)
	private int onThreshold = 6000;
	private int offThreshold = 4000;

	private short currAmp = 0;

	public enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	public State getState() {
		return state;
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
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);
		state = State.INITIALIZING;
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getTempFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
	}

	public void setThresholdBar(ThresholdBar thresholdBar) {
		this.thresholdBar = thresholdBar;
	}

	private void writeAudioDataToFile() {
		byte buffer[] = new byte[bufferSize];
		recorder.read(buffer, 0, bufferSize);

		try {
			while (state == State.RECORDING || state == State.LISTENING) {
				recorder.read(buffer, 0, bufferSize);
				
				if (state == State.LISTENING && overThreshold(buffer)) {
					startRecording();
				}
				if (state == State.RECORDING) {
					os.write(buffer); // Write buffer to file
					if (underThreshold(buffer)) {
						stopRecording();
					}
				}
				// update status bar here. not working now.
				// thresholdBar.setProgress(currAmp/100);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startRecording() throws IOException {
		state = State.RECORDING;
		// MIDI-on event (velocity)
		midiManager.setRecordNoteOn(100);
		
		String filename = getTempFilename();
		os = new FileOutputStream(filename);
	}

	private void stopRecording() throws IOException {
		state = State.LISTENING;
		// MIDI-off event (velocity)
		midiManager.setRecordNoteOff(100);
		
		// close and write to WAV
		os.close();
		copyWaveFile(getTempFilename(), getFilename());
		deleteTempFile();
	}

	public void startListening() {
		recorder.startRecording();
		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");
		state = State.LISTENING;
		midiManager.start();
		recordingThread.start();
	}

	public void stopListening() {
		if (recorder != null) {
			recorder.stop();
			state = State.INITIALIZING;
		} else {
			Log.e(RecordManager.class.getName(), "stopListening in wrong state");
		}
	}

	public void release() {
		if (recorder != null)
			recorder.release();
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());

		file.delete();
	}

	private void copyWaveFile(String inFilename, String outFilename) {
		byte[] buffer = new byte[bufferSize];
		
		try {
			FileInputStream in = new FileInputStream(inFilename);
			FileOutputStream out = new FileOutputStream(outFilename);
			long totalAudioLen = in.getChannel().size();
			long totalDataLen = totalAudioLen + 36;

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen);

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
		for (int i = 0; i < bufferSize / 32; i++) {
			// 16bit sample size
			currAmp = getShort(buffer[i * 32], buffer[i * 32 + 1]);
			if (currAmp > offThreshold) { // Check amplitude
				return false;
			}
		}
		return true;
	}

	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen)
			throws IOException {

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
}