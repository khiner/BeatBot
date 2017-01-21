package com.odang.beatbot.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.odang.beatbot.listener.FileListener;

public class FileManager implements FileListener {

	public static final String[] SUPPORTED_EXTENSIONS = { ".wav", ".flac", ".ogg", ".oga", ".aif",
			".aiff", ".aifc", ".au", ".snd", ".raw", ".paf", ".iff", ".svx", ".sf", ".voc", ".w64",
			".mat4", ".mat5", ".pvf", ".xi", ".htk", ".caf", ".sd2" };

	public static final String[] SYSTEM_DIRECTORY_NAMES = {
		"acct", "proc", "app-cache", "dev", "etc", "mnt", "sys", "system"
	};

	public static final String[] ASSET_TYPES = { "drums" };

	private File filesDirectory, rootDirectory, audioDirectory, projectDirectory, midiDirectory,
			recordDirectory, drumsDirectory, beatRecordDirectory, sampleRecordDirectory;

	private AssetManager assetManager;
	private byte[] copyBuffer = new byte[1024];
	private String appDirectoryPath;

	// order matters here - track should always be updated first
	private List<FileListener> listeners = new ArrayList<FileListener>();

	public FileManager(final Context context, final AssetManager assetManager) {
		this.filesDirectory = context.getFilesDir();
		this.assetManager = assetManager;

		initDataDir();
		rootDirectory = new File("/");
		audioDirectory = new File(appDirectoryPath + "/audio");
		projectDirectory = new File(appDirectoryPath + "/projects");
		midiDirectory = new File(appDirectoryPath + "/midi");
		drumsDirectory = new File(audioDirectory.getPath() + "/drums");
		recordDirectory = new File(audioDirectory.getPath() + "/recorded");
		beatRecordDirectory = new File(recordDirectory.getPath() + "/beats");
		sampleRecordDirectory = new File(recordDirectory.getPath() + "/samples");

		drumsDirectory.mkdirs();
		projectDirectory.mkdirs();
		midiDirectory.mkdirs();
		beatRecordDirectory.mkdirs();
		sampleRecordDirectory.mkdirs();

		try {
			copyAllSamplesToStorage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	public File getDrumsDirectory() {
		return drumsDirectory;
	}

	public File getRecordDirectory() {
		return recordDirectory;
	}

	public File getMidiDirectory() {
		return midiDirectory;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public void addListener(FileListener listener) {
		listeners.add(listener);
	}

	public String recordPathForSource(int recordSourceId) {
		final File recordDirectory = recordSourceId == RecordManager.MICROPHONE_RECORD_SOURCE_ID ? sampleRecordDirectory
				: beatRecordDirectory;

		return recordDirectory.getPath();
	}

	public static String formatSampleName(String sampleName) {
		for (String extension : SUPPORTED_EXTENSIONS) {
			if (sampleName.endsWith(extension)) {
				return sampleName.replace(extension, "");
			}
		}
		return sampleName;
	}

	private void initDataDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// we can read and write to external storage
			String extStorageDir = Environment.getExternalStorageDirectory().toString();
			appDirectoryPath = extStorageDir + "/BeatBot/";
		} else { // we need read AND write access for this app - default to
					// internal storage
			appDirectoryPath = filesDirectory.toString() + "/";
			// TODO throw / catch exception - need External SD Card!
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		int read;
		while ((read = in.read(copyBuffer)) != -1) {
			out.write(copyBuffer, 0, read);
		}
		in.close();
		in = null;
		out.flush();
		out.close();
		out = null;
	}

	private void copyFromAssetsToExternal(String assetPath) throws IOException {
		File destDir = new File(audioDirectory.getPath() + "/" + assetPath + "/");

		// create the dir
		destDir.mkdirs();
		File[] files = destDir.listFiles();
		if (null == files || files.length > 0) {
			// Only copy files into this dir if it is empty.
			// Files can be renamed, so we can't make assumptions
			// about whether an individual file already exists
			return;
		}

		for (String fileName : assetManager.list(assetPath)) {
			// copy audio file exactly from assets to sdcard
			InputStream in = assetManager.open(assetPath + "/" + fileName);
			String outPath = destDir.getPath() + "/" + fileName;
			FileOutputStream rawOut = new FileOutputStream(outPath);
			copyFile(in, rawOut);
		}
	}

	private void copyAllSamplesToStorage() throws IOException {
		for (String assetType : ASSET_TYPES) {
			for (String fileName : assetManager.list(assetType)) {
				// the sample folder for this sample type does not yet exist.
				// create it and write all assets of this type to the folder
				String assetPath = assetType + "/" + fileName;
				copyFromAssetsToExternal(assetPath);
			}
		}
	}

	@Override
	public void onNameChange(File file, File newFile) {
		for (FileListener listener : listeners) {
			listener.onNameChange(file, newFile);
		}
	}
}
