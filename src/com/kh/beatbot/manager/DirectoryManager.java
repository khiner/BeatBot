package com.kh.beatbot.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.DataSetObserver;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBDirectory;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Instrument;
import com.kh.beatbot.layout.page.TrackPage;
import com.kh.beatbot.layout.page.TrackPageFactory;

public class DirectoryManager {

	public static String appDirectoryPath;

	public static final String[] drumNames = { "kick", "snare", "hh_closed",
			"hh_open", "rim" };

	public void initIcons() {
		getDrumInstrument(0).setIconResources(R.drawable.kick_icon_src,
				R.drawable.kick_icon, R.drawable.kick_icon_selected,
				R.drawable.kick_icon_listview);
		getDrumInstrument(1).setIconResources(R.drawable.snare_icon_src,
				R.drawable.snare_icon, R.drawable.snare_icon_selected,
				R.drawable.snare_icon_listview);
		getDrumInstrument(2).setIconResources(R.drawable.hh_closed_icon_src,
				R.drawable.hh_closed_icon, R.drawable.hh_closed_icon_selected,
				R.drawable.hh_closed_icon_listview);
		getDrumInstrument(3).setIconResources(R.drawable.hh_open_icon_src,
				R.drawable.hh_open_icon, R.drawable.hh_open_icon_selected,
				R.drawable.hh_open_icon_listview);
		getDrumInstrument(4).setIconResources(R.drawable.rimshot_icon_src,
				R.drawable.rimshot_icon, R.drawable.rimshot_icon_selected,
				R.drawable.rimshot_icon_listview);
		drumsDirectory.setIconResources(-1, -1, -1,
				R.drawable.drums_icon_listview);
		internalRecordDirectory.setIconResources(
				R.drawable.microphone_icon_src, R.drawable.microphone_icon,
				R.drawable.microphone_icon_selected,
				R.drawable.microphone_icon_listview);
	}

	private static DirectoryManager singletonInstance = null;

	private AlertDialog.Builder instrumentSelectAlertBuilder;
	private AlertDialog instrumentSelectAlert = null;
	private ListAdapter instrumentSelectAdapter = null;
	private OnShowListener instrumentSelectOnShowListener = null;

	private BBDirectory internalDirectory = null;
	private BBDirectory internalRecordDirectory = null;
	private BBDirectory internalBeatRecordDirectory = null;
	private BBDirectory internalSampleRecordDirectory = null;
	private BBDirectory userRecordDirectory = null;
	private BBDirectory userBeatRecordDirectory = null;
	private BBDirectory userSampleRecordDirectory = null;
	private BBDirectory drumsDirectory = null;

	private BBDirectory currDirectory = null;

	private boolean addingTrack = false;

	public static DirectoryManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DirectoryManager();
		}
		return singletonInstance;
	}

	private DirectoryManager() {
		initDataDir();
		internalDirectory = new BBDirectory(null, "internal", null);
		drumsDirectory = new BBDirectory(internalDirectory, "drums",
				new BBIconSource());
		userRecordDirectory = new BBDirectory(null, "recorded", null);
		userSampleRecordDirectory = new BBDirectory(userRecordDirectory,
				"samples", null);
		userBeatRecordDirectory = new BBDirectory(userRecordDirectory, "beats",
				null);
		internalRecordDirectory = new BBDirectory(internalDirectory,
				"recorded", new BBIconSource());
		internalSampleRecordDirectory = new Instrument(internalRecordDirectory,
				"samples", null);
		internalBeatRecordDirectory = new Instrument(internalRecordDirectory,
				"beats", null);
		internalBeatRecordDirectory
				.setEmptyMsg("You haven't recorded any beats yet!  Use the record button at the top to record your beats.");
		for (String drumName : drumNames) {
			new Instrument(drumsDirectory, drumName, new BBIconSource());
		}
		instrumentSelectAlertBuilder = new AlertDialog.Builder(
				GlobalVars.mainActivity);
		instrumentSelectAlertBuilder.setTitle("Choose Instrument");
		initInstrumentSelectOnShowListener();
	}

	public void updateDirectories() {
		for (BBDirectory dir : drumsDirectory.getChildren()) {
			((Instrument) dir).updateFiles();
		}
	}

	public void updateInstrumentSelectAlert(BBDirectory newDirectory) {
		initInstrumentSelectAdapter(GlobalVars.mainActivity, newDirectory);
		instrumentSelectAlertBuilder.setAdapter(instrumentSelectAdapter,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						currDirectory = currDirectory.getChild(item);
						if (currDirectory == null) {
							// Instrument type
							if (addingTrack) {
								Managers.trackManager.addTrack(
										(Instrument) currDirectory.getParent(),
										item);
							} else {
								TrackPage.getTrack().setInstrument(
										(Instrument) currDirectory.getParent(),
										item);
							}
							TrackPageFactory.updatePages();
							currDirectory = internalDirectory;
						}
						updateInstrumentSelectAlert(currDirectory);
						// if the current dir is the root directory, we've already selected a sample
						// do not display any select alert list.
						if (currDirectory == internalDirectory) {
							return;
						}
						// if the directory is empty, display the directory's empty message
						// otherwise, show the next child directories in a select alert list
						if (currDirectory.getChildren().isEmpty()) {
							Toast.makeText(GlobalVars.mainActivity,
									currDirectory.getEmptyMsg(),
									Toast.LENGTH_SHORT).show();
						} else {
							instrumentSelectAlert.show();
						}
					}

				});
		instrumentSelectAlert = instrumentSelectAlertBuilder.create();
		instrumentSelectAlert.setOnShowListener(instrumentSelectOnShowListener);
	}

	public void showAddTrackAlert() {
		addingTrack = true;
		currDirectory = internalDirectory;
		updateInstrumentSelectAlert(currDirectory);
		instrumentSelectAlert.show();
	}

	public void showInstrumentSelectAlert() {
		addingTrack = false;
		currDirectory = internalDirectory;
		updateInstrumentSelectAlert(currDirectory);
		instrumentSelectAlert.show();
	}

	public void showSampleSelectAlert() {
		addingTrack = false;
		currDirectory = TrackPage.getTrack().getInstrument();
		updateInstrumentSelectAlert(currDirectory);
		instrumentSelectAlert.show();
	}

	public void updateRecordDirectory() {
		((Instrument) internalBeatRecordDirectory).updateFiles();
	}

	public Instrument getDrumInstrument(int drumNum) {
		return (Instrument) drumsDirectory.getChild(drumNum);
	}

	public String getInternalDirectory() {
		return internalDirectory.getPath();
	}

	public String getUserRecordDirectory() {
		return userBeatRecordDirectory.getPath();
	}

	public String getInternalRecordDirectory() {
		return internalBeatRecordDirectory.getPath();
	}

	private void initDataDir() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// we can read and write to external storage
			String extStorageDir = Environment.getExternalStorageDirectory()
					.toString();
			appDirectoryPath = extStorageDir + "/BeatBot/";
		} else { // we need read AND write access for this app - default to
					// internal storage
			// appDirectoryPath = getFilesDir().toString() + "/";
			// TODO throw / catch exception - need External SD Card!
		}
	}

	private void initInstrumentSelectAdapter(final Activity activity,
			final BBDirectory directory) {
		String[] list = directory instanceof Instrument ? ((Instrument) directory)
				.getSampleNames() : directory.getChildNames();
		instrumentSelectAdapter = new ArrayAdapter<String>(activity,
				android.R.layout.select_dialog_item, android.R.id.text1, list) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);
				// Put the image on the TextView
				if (directory instanceof Instrument) {
					tv.setCompoundDrawables(null, null, null, null);
					return v;
				}
				BBIconSource iconSource = directory.getChild(position)
						.getBBIconSource();
				if (iconSource == null) {
					tv.setCompoundDrawables(null, null, null, null);
					return v;
				}
				tv.setCompoundDrawablesWithIntrinsicBounds(
						iconSource.listViewIcon.resourceId, 0, 0, 0);
				// Add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * activity.getResources()
						.getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);

				return v;
			}
		};
	}

	private void initInstrumentSelectOnShowListener() {
		instrumentSelectOnShowListener = new OnShowListener() {
			@Override
			public void onShow(DialogInterface alert) {
				ListView listView = ((AlertDialog) alert).getListView();
				final ListAdapter originalAdapter = listView.getAdapter();

				listView.setAdapter(new ListAdapter() {
					@Override
					public int getCount() {
						return originalAdapter.getCount();
					}

					@Override
					public Object getItem(int id) {
						return originalAdapter.getItem(id);
					}

					@Override
					public long getItemId(int id) {
						return originalAdapter.getItemId(id);
					}

					@Override
					public int getItemViewType(int id) {
						return originalAdapter.getItemViewType(id);
					}

					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						View view = originalAdapter.getView(position,
								convertView, parent);
						TextView textView = (TextView) view;
						textView.setTypeface(GlobalVars.font);
						textView.setText(textView.getText().toString()
								.toUpperCase());
						return view;
					}

					@Override
					public int getViewTypeCount() {
						return originalAdapter.getViewTypeCount();
					}

					@Override
					public boolean hasStableIds() {
						return originalAdapter.hasStableIds();
					}

					@Override
					public boolean isEmpty() {
						return originalAdapter.isEmpty();
					}

					@Override
					public void registerDataSetObserver(DataSetObserver observer) {
						originalAdapter.registerDataSetObserver(observer);

					}

					@Override
					public void unregisterDataSetObserver(
							DataSetObserver observer) {
						originalAdapter.unregisterDataSetObserver(observer);

					}

					@Override
					public boolean areAllItemsEnabled() {
						return originalAdapter.areAllItemsEnabled();
					}

					@Override
					public boolean isEnabled(int position) {
						return originalAdapter.isEnabled(position);
					}
				});
			}
		};
	}
}
