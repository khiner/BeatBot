package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.ThresholdBarView;

public class RecordManager implements LevelListener {
	public static final long RECORD_LATENCY_TICKS = Managers.midiManager
			.millisToTick(250);
	private static final int RECORDER_BPP = 16;
	private static final String TEMP_FILE = "record_temp.bb";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int CHANNELS = 2;
	private static final long BYTE_RATE = RECORDER_BPP * RECORDER_SAMPLERATE
			* CHANNELS / 8;
	private static final long LONG_SAMPLE_RATE = RECORDER_SAMPLERATE;

	private String currWavFileName = null, currBBFileName = null;

	private String wavRecordDirectory = null, bbRecordDirectory = null;

	private static RecordManager singletonInstance = null;

	private ThresholdBarView thresholdBar;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	// queue of paths to raw audio data to process
	// private static Queue<Long> recordTickQueue = new LinkedList<Long>();
	private Thread recordingThread = null;
	private State state;
	private FileOutputStream os = null;

	private short currAmp = 0;
	private short currThreshold;
	private long recordStartTick = 0;
	private int currSampleNum = 0;
	static final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");

	public static enum State {
		LISTENING, RECORDING, INITIALIZING
	};

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
		wavRecordDirectory = Managers.directoryManager.getUserRecordDirectory();
		bbRecordDirectory = Managers.directoryManager
				.getInternalRecordDirectory();
		currSampleNum = findGreatestSampleNum(wavRecordDirectory) + 1;
		state = State.INITIALIZING;
	}

	public State getState() {
		return state;
	}

	public boolean isRecording() {
		return state == State.RECORDING;
	}

	public void startRecording() throws IOException {
		recordStartTick = getAdjustedRecordTick();
		updateFileNames();
		os = writeWaveFileHeader(currWavFileName, 0, 0);
		state = State.RECORDING;
	}

	public void stopRecording() throws IOException {
		int trackNum = 0;
		long recordEndTick = getAdjustedRecordTick();
		GlobalVars.midiView.addMidiNote(recordStartTick, recordEndTick,
				trackNum);
		state = State.LISTENING;
		// close output stream
		os.close();
		insertLengthDataIntoWavFile(currWavFileName);
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

	public long getRecordStartTick() {
		return recordStartTick;
	}

	public long getRecordCurrTick() {
		long recordCurrTick = Managers.midiManager.getCurrTick()
				- RECORD_LATENCY_TICKS;
		if (recordCurrTick < Managers.midiManager.getLoopBeginTick()
				|| state == State.RECORDING && recordCurrTick < recordStartTick) {
			// if recording past loop end, keep record note going as if there
			// was no loop
			recordCurrTick = Managers.midiManager.getLoopEndTick()
					- Managers.midiManager.getLoopBeginTick() + recordCurrTick;
		}
		return recordCurrTick;
	}

	public void startRecordingNative() {
		updateFileNames();
		try {
			FileOutputStream out = writeWaveFileHeader(currWavFileName, 0, 0);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startRecordingNative(currWavFileName, currBBFileName);
		state = State.RECORDING;
	}

	public String stopRecordingAndWriteWav() {
		stopRecordingNative();
		insertLengthDataIntoWavFile(currWavFileName);
		state = State.INITIALIZING;
		return currWavFileName;
	}

	/**
	 * Look through all the sample names in the given record directory and find
	 * the sample with the greatest number appended to the end (Recorded file
	 * naming convention is "R1, R2, R3, ...")
	 * 
	 * @param recordDirectoryName
	 * @return the greatest appended sample num in all the recorded sample file
	 *         names in the given dir
	 */
	private static int findGreatestSampleNum(String recordDirectoryName) {
		File recordDirectoryFile = new File(recordDirectoryName);
		recordDirectoryFile.mkdir(); // just in case the record dir does not
										// exist yet
		String[] allRecordedFileNames = recordDirectoryFile.list();
		int maxSampleNum = 0;
		for (String recordedFileName : allRecordedFileNames) {
			// strip ".wav" from end (and any other occurrences, which would be
			// weird)
			recordedFileName = recordedFileName.replace(".wav", "");
			Matcher matcher = lastIntPattern.matcher(recordedFileName);
			if (matcher.find()) {
				String numberString = matcher.group(1);
				// get ending integer
				int sampleNum = Integer.parseInt(numberString);
				maxSampleNum = Math.max(sampleNum, maxSampleNum);
			}
		}
		return maxSampleNum;
	}

	private void initRecorder() {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);
	}

	private void updateFileNames() {
		currWavFileName = wavRecordDirectory + "R" + (currSampleNum) + ".wav";
		currBBFileName = bbRecordDirectory + "R" + (currSampleNum++) + ".bb";
	}

	private void writeAudioDataToFile() {
		byte buffer[] = new byte[bufferSize];
		recorder.read(buffer, 0, bufferSize);
		recorder.read(buffer, 0, bufferSize);

		// let the midi view know recording has started
		// so it can listen for byte buffers to draw them
		GlobalVars.midiView.signalRecording();
		try {
			while (state != State.INITIALIZING) {
				recorder.read(buffer, 0, bufferSize);

				if (state == State.LISTENING && overThreshold(buffer)) {
					startRecording();
				}
				if (state == State.RECORDING) {
					os.write(buffer); // write buffer to file
					GlobalVars.midiView.drawWaveform(buffer); // draw waveform
					if (underThreshold(buffer)) {
						stopRecording();
						GlobalVars.midiView.endWaveform();
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
		GlobalVars.midiView.signalEndRecording();
	}

	private boolean overThreshold(byte[] buffer) {
		for (int i = 0; i < bufferSize / 32; i++) {
			// 16bit sample size
			currAmp = getShort(buffer[i * 32], buffer[i * 32 + 1]);
			if (currAmp > currThreshold) { // Check amplitude
				return true;
			}
		}
		return false;
	}

	private boolean underThreshold(byte[] buffer) {
		short offThreshold = (short) (currThreshold * .8f);
		for (int i = 0; i < bufferSize / 32; i++) {
			// 16bit sample size
			currAmp = getShort(buffer[i * 32], buffer[i * 32 + 1]);
			if (currAmp > offThreshold) { // Check amplitude
				return false;
			}
		}
		return true;
	}

	private static FileOutputStream writeWaveFileHeader(String fileName,
			long totalAudioLen, long totalDataLen) throws IOException {
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

		FileOutputStream out = new FileOutputStream(fileName);
		out.write(header);
		return out;
	}

	private static void insertLengthDataIntoWavFile(String fileName) {
		try {
			FileInputStream in = new FileInputStream(fileName);
			long totalAudioLen = in.getChannel().size() - 36;
			long totalDataLen = totalAudioLen + 36;
			in.close();

			byte[] headerLengthData = new byte[4];
			RandomAccessFile wavFile = new RandomAccessFile(fileName, "rw");

			wavFile.seek(4);
			headerLengthData[0] = (byte) (totalDataLen & 0xff);
			headerLengthData[1] = (byte) ((totalDataLen >> 8) & 0xff);
			headerLengthData[2] = (byte) ((totalDataLen >> 16) & 0xff);
			headerLengthData[3] = (byte) ((totalDataLen >> 24) & 0xff);
			wavFile.write(headerLengthData);

			wavFile.seek(40);
			headerLengthData[0] = (byte) (totalAudioLen & 0xff);
			headerLengthData[1] = (byte) ((totalAudioLen >> 8) & 0xff);
			headerLengthData[2] = (byte) ((totalAudioLen >> 16) & 0xff);
			headerLengthData[3] = (byte) ((totalAudioLen >> 24) & 0xff);
			wavFile.write(headerLengthData);
			wavFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Converts a byte[2] to a short, in LITTLE_ENDIAN format
	 */
	private static short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}

	public static float shortToDb(short amp) {
		return 20 * (float) Math.log10(Math.abs(amp) / 32768f);
	}

	public static short dbToShort(float db) {
		return (short) (32768 * Math.pow(10, db / 20));
	}

	private long getAdjustedRecordTick() {
		long adjustedTick = getRecordCurrTick();
		if (state != State.RECORDING) {
			// 16th note quantization for note beginning (but not end)
			adjustedTick = Managers.midiManager.getNearestMajorTick(
					adjustedTick, 4);
			if (adjustedTick >= Managers.midiManager.getLoopEndTick()) {
				adjustedTick %= Managers.midiManager.getLoopEndTick();
			}
		}
		return adjustedTick;
	}

	@Override
	public void notifyInit(LevelListenable levelListenable) {
		thresholdBar = (ThresholdBarView) levelListenable;
	}

	@Override
	public void notifyPressed(LevelListenable levelListenable, boolean pressed) {
		// TODO make threshold label light up
	}

	@Override
	public void notifyClicked(LevelListenable levelListenable) {
		// do nothing for clicks
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		currThreshold = dbToShort((level - 1.001f) * 60);
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// nothing - for level 2d
	}

	public native void startRecordingNative(String wavRecordFileName,
			String bbRecordFileName);

	public native void stopRecordingNative();
}