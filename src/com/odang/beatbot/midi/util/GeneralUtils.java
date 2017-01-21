package com.odang.beatbot.midi.util;

import java.util.List;

import android.app.Activity;
import android.media.AudioManager;
import android.view.Window;
import android.view.WindowManager;

public class GeneralUtils {
	public static final byte HALF_BYTE = 64;
	public static final float MIN_DB = -60;

	public static void initAndroidSettings(Activity activity) {
		// remove title bar
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// remove top toolbar (with battery icon, etc)
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// assign hardware (ringer) volume +/- to media while this application
		// has focus
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// prevent screen from turning off while this app is running
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public static float distanceFromPointSquared(float pointX, float pointY, float x, float y) {
		return (x - pointX) * (x - pointX) + (y - pointY) * (y - pointY);
	}

	public static float byteToLinear(byte value) {
		return (float) value / (float) Byte.MAX_VALUE;
	}

	/*
	 * Converts two bytes to a short, in LITTLE_ENDIAN format
	 */
	public static short bytesToShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}

	public static byte linearToByte(float linear) {
		return (byte) Math.ceil(clipToUnit(linear) * Byte.MAX_VALUE);
	}

	public static float dbToUnit(float db) {
		// db range = -60 - 0, need range 0-1 (not a conversion to linear scale! used for UI)
		return db <= MIN_DB ? 0 : db / MIN_DB + 1f;
	}

	public static short dbToShort(float db) {
		return db <= MIN_DB ? 0 : (short) ((double) Short.MAX_VALUE * Math.pow(10f, db / 20f));
	}

	public static float shortToDb(short amp) {
		return (float) (20 * Math.log10(Math.abs(amp) / (double) Short.MAX_VALUE));
	}
	
	public static boolean contains(int[] ary, int val) {
		for (int i = 0; i < ary.length; i++) {
			if (ary[i] == val) {
				return true;
			}
		}
		return false;
	}

	public static float[] floatListToArray(List<Float> list) {
		float[] array = new float[list.size()];
		for (int j = 0; j < list.size(); j++) {
			array[j] = list.get(j);
		}
		return array;
	}

	public static float clipToUnit(float value) {
		return clipTo(value, 0, 1);
	}

	public static float clipTo(float value, float min, float max) {
		if (min > max)
			return min; // sanity check, always favor min
		return value > min ? (value < max ? value : max) : min;
	}

	public static long clipTo(long value, long min, long max) {
		if (min > max)
			return min;
		return value > min ? (value < max ? value : max) : min;
	}
}
