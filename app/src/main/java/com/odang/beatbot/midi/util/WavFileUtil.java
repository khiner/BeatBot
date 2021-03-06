package com.odang.beatbot.midi.util;

import com.odang.beatbot.manager.PlaybackManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WavFileUtil {
    private static final int NUM_CHANNELS = 2;
    private static final int BITS_PER_SAMPLE = Short.SIZE;
    private static final long BYTE_RATE = BITS_PER_SAMPLE * PlaybackManager.SAMPLE_RATE
            * NUM_CHANNELS / Byte.SIZE;

    public static FileOutputStream writeWavFileHeader(String fileName, long totalAudioLen,
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
        header[16] = 2 * Byte.SIZE; // 2 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) NUM_CHANNELS;
        header[23] = 0;
        header[24] = (byte) (((long) PlaybackManager.SAMPLE_RATE) & 0xff);
        header[25] = (byte) ((((long) PlaybackManager.SAMPLE_RATE) >> 8) & 0xff);
        header[26] = (byte) ((((long) PlaybackManager.SAMPLE_RATE) >> 16) & 0xff);
        header[27] = (byte) ((((long) PlaybackManager.SAMPLE_RATE) >> 24) & 0xff);
        header[28] = (byte) (BYTE_RATE & 0xff);
        header[29] = (byte) ((BYTE_RATE >> 8) & 0xff);
        header[30] = (byte) ((BYTE_RATE >> 16) & 0xff);
        header[31] = (byte) ((BYTE_RATE >> 24) & 0xff);
        header[32] = (byte) 4; // block align
        header[33] = 0;
        header[34] = BITS_PER_SAMPLE; // bits per sample
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

    public static File insertLengthDataIntoWavFile(String fileName) {
        File file = null;
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

            file = new File(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
}
