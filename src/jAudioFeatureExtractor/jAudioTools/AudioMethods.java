/*
 * @(#)AudioMethods.java	1.03	May 29, 2005.
 *
 * Cory McKay
 * McGill Univarsity
 */

package jAudioFeatureExtractor.jAudioTools;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;


/**
 * A holder class for general static methods relating to sampled audio involving
 * classes used in the javax.sound.sampled package. Also includes methods for use
 * in converting back an forth between audio stored using this package and audio
 * stored as samples in arrays of doubles.
 *
 * @author	Cory McKay
 */
public class AudioMethods
{

	/**
	 * Returns an array of doubles representing the samples for each channel
	 * in the given AudioInputStream.
	 *
	 * <p>This method is only compatible with audio with bit depths of 8 or 16 bits
	 * that is encoded using signed PCM with big endian byte order.
	 *
	 * @param	audio_input_stream	The AudioInputStream to convert to sample values.
	 * @return						A 2-D array of sample values whose first indice indicates
	 *								channel and whose second indice indicates sample number.
	 *								In stereo, indice 0 corresponds to left and 1 to right.
	 *								All samples should fall between -1 and +1.
	 * @throws	Exception			Throws an informative exception if an invalid paramter
	 *								is provided.
	 */
	public static double[][] extractSampleValues(File file)
		throws Exception
	{
		byte[] audio_bytes = getBytesFromFile(file);
		int number_bytes = audio_bytes.length;

		// Extract information from this_audio_format
		int number_of_channels = 2;
		int bit_depth = 16;

		// Find the number of samples in the audio_bytes
		int number_of_bytes = audio_bytes.length;
		int bytes_per_sample = bit_depth / 8;
		int number_samples = number_of_bytes / bytes_per_sample / number_of_channels;

		// Throw exception if incorrect number of bytes given
		if ( ((number_samples == 2 || bytes_per_sample == 2) && (number_of_bytes % 2 != 0)) ||
		     ((number_samples == 2 && bytes_per_sample == 2) && (number_of_bytes % 4 != 0)) )
			throw new Exception("Uneven number of bytes for given bit depth and number of channels.");

		// Find the maximum possible value that a sample may have with the given
		// bit depth
		double max_sample_value = AudioMethods.findMaximumSampleValue(bit_depth) + 2.0;

		// Instantiate the sample value holder
		double[][] sample_values = new double[number_of_channels][number_samples];

		// Convert the bytes to double samples
		ByteBuffer byte_buffer = ByteBuffer.wrap(audio_bytes);
		ShortBuffer short_buffer = byte_buffer.asShortBuffer();
		for (int samp = 0; samp < number_samples; samp++)
			for (int chan = 0; chan < number_of_channels; chan++)
				sample_values[chan][samp] = (double) short_buffer.get() / max_sample_value;

		// Return the samples
		return sample_values;
	}

	/**
	 * Writes the samples in the <i>sample_values</i> parameter to the <i>buffer</i>
	 * parameter. It is implicit that the caller knows what the sampling rate is
	 * and will be able to use it to correctly interperet the samples stored in the
	 * buffer after writing. Encoding is done using big endian signed PCM samples.
	 * Sample vaules greater than 1 or less than -1 are automatically clipped.
	 *
	 * @param	sample_values		A 2-D array of doubles whose first indice
	 *								indicates channel and whose second indice
	 *								indicates sample value. In stereo, indice
	 *								0 corresponds to left and 1 to right. All
	 *								samples should fall between -1 and +1.
	 * @param	bit_depth			The bit depth to use for encoding the doubles
	 *								stored in <i>samples_to_modify</i>. Only bit
	 *								depths of 8 or 16 bits are accepted.
	 * @param	buffer				The buffer of bytes to write synthesized samples to.
	 */
	public static void writeSamplesToBuffer( double[][] sample_values,
	                                         int bit_depth,
	                                         byte[] buffer )
		throws Exception
	{
		// Throw exceptions for invalid parameters
		if (sample_values == null)
			throw new Exception( "Empty set of samples to write provided." );
		if (bit_depth != 8 && bit_depth != 16)
			throw new Exception( "Bit depth of " + bit_depth + " specified." +
			                     "Only bit depths of 8 or 16 currently accepted." );
		if (buffer == null)
			throw new Exception("Null buffer for storing samples provided.");

		// Clip values above +1 or below -1
		sample_values = clipSamples(sample_values);

		// Find the maximum value a sample may have under the current bit depth
		// (assuming signed samples)
		double max_sample_value = AudioMethods.findMaximumSampleValue(bit_depth);

		// Prepare buffer of audio samples to be written to by wrapping it in
		// a ByteBuffer so that bytes may easily be written to it
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);

		// Write samples to buffer (by way of byte_buffer)
		// Only works for bit depths of 8 or 16 bits and big endian signed samples
		if (bit_depth == 8)
		{
			for (int samp = 0; samp < sample_values[0].length; samp++)
				for (int chan = 0; chan < sample_values.length; chan++)
				{
					double sample_value = sample_values[chan][samp] * max_sample_value;
					byte_buffer.put( (byte) sample_value );
				}
		}
		else if (bit_depth == 16)
		{
			ShortBuffer short_buffer = byte_buffer.asShortBuffer();
			for (int samp = 0; samp < sample_values[0].length; samp++)
				for (int chan = 0; chan < sample_values.length; chan++)
				{
					double sample_value = sample_values[chan][samp] * max_sample_value;
					short_buffer.put( (short) sample_value );
				}
		}
	}


	/**
	 * Clips the given samples so that all values below -1 are set to -1
	 * and all values above 1 are set to 1. The returned array is a copy
	 * so the original array is not altered.
	 *
	 * @param	original_samples	A 2-D array of doubles whose first indice
	 *								indicates channel and whose second indice
	 *								indicates sample value. In stereo, indice
	 *								0 corresponds to left and 1 to right.
	 * @return						A clipped copy of the original_samples
	 *								parameter.
	 * @throws	Exception			If a null parameter is passed.
	 */
	public static double[][] clipSamples(double[][] original_samples)
		throws Exception
	{
		// Throw exceptions for invalid parameters
		if (original_samples == null)
			throw new Exception( "Empty set of samples to provided." );

		// Perform clipping
		double[][] clipped_samples = new double[original_samples.length][];
		for (int chan = 0; chan < clipped_samples.length; chan++)
		{
			clipped_samples[chan] = new double[original_samples[chan].length];
			for (int samp = 0; samp < clipped_samples[chan].length; samp++)
			{
				if (original_samples[chan][samp] < -1.0)
					 clipped_samples[chan][samp] = -1.0;
				else if (original_samples[chan][samp] > 1.0)
					 clipped_samples[chan][samp] = 1.0;
				else
					 clipped_samples[chan][samp] = original_samples[chan][samp];
			}
		}
		return clipped_samples;
	}


	/**
	 * Returns the maximum possible value that a signed sample can have under
	 * the given bit depth. May be 1 or 2 values smaller than actual max,
	 * depending on specifics of encoding used.
	 *
	 * @param	bit_depth	The bit depth to examine.
	 * @return				The maximum possible positive sample value as a double.
	 */
	public static double findMaximumSampleValue(int bit_depth)
	{
		int max_sample_value_int = 1;
		for (int i = 0; i < (bit_depth - 1); i++)
			max_sample_value_int *= 2;
		max_sample_value_int--;
		double max_sample_value = ((double) max_sample_value_int) - 1.0;
		return max_sample_value;
	}
	
	public static byte[] getBytesFromFile(File infile) {
		return new byte[10];
	}
}