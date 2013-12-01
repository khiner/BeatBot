package com.kh.beatbot.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.ui.view.control.ThresholdBarView;

public class RecordManager implements ParamListener {

	public static enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	public static final long RECORD_LATENCY_TICKS = MidiManager.millisToTick(250);
	private static final int RECORDER_BPP = 16;
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int CHANNELS = 2;
	private static final long BYTE_RATE = RECORDER_BPP * RECORDER_SAMPLERATE
			* CHANNELS / 8;
	private static final long LONG_SAMPLE_RATE = RECORDER_SAMPLERATE;

	private static String currRecordFileName = null;

	private static ThresholdBarView thresholdBar;

	private static int bufferSize = AudioRecord.getMinBufferSize(
			RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

	private static AudioRecord recorder = new AudioRecord(
			MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
			RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

	// queue of paths to raw audio data to process
	// private static Queue<Long> recordTickQueue = new LinkedList<Long>();
	private static Thread recordingThread = null;
	private static State state = State.INITIALIZING;
	private static FileOutputStream os = null;

	private static short currAmp = 0;
	private static short currThreshold;
	private static int currSampleNum = 0;

	private static final Pattern lastIntPattern = Pattern
			.compile("[^0-9]+([0-9]+)$");

	public static boolean isRecording() {
		return state == State.RECORDING;
	}

	public static void startRecording() {
		updateFileNames();
		try {
			FileOutputStream out = writeWaveFileHeader(currRecordFileName, 0, 0);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startRecordingNative(currRecordFileName);
		state = State.RECORDING;
	}

	public static String stopRecording() {
		stopRecordingNative();
		insertLengthDataIntoWavFile(currRecordFileName);
		state = State.INITIALIZING;
		return currRecordFileName;
	}

	private static void updateFileNames() {
		currRecordFileName = FileManager.beatRecordDirectory.getPath() + "/R" + (currSampleNum++)
				+ ".wav";
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

	@Override
	public void onParamChanged(Param param) {
		currThreshold = dbToShort((param.viewLevel - 1.001f) * 60);
	}

	public static native void startRecordingNative(String recordFileName);

	public static native void stopRecordingNative();
}