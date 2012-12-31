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
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class DirectoryManager {

	public static String appDirectoryPath;

	public static final String[] drumNames = { "kick", "snare", "hh_closed",
			"hh_open", "rim" };

	public void initIcons() {
		getDrumInstrument(0).getBBIconSource().set(R.drawable.kick_icon_src,
				R.drawable.kick_icon, R.drawable.kick_icon_selected,
				R.drawable.kick_icon_listview, R.drawable.kick_icon_list_title);
		getDrumInstrument(1).getBBIconSource().set(R.drawable.snare_icon_src,
				R.drawable.snare_icon, R.drawable.snare_icon_selected,
				R.drawable.snare_icon_listview,
				R.drawable.snare_icon_list_title);
		getDrumInstrument(2).getBBIconSource().set(
				R.drawable.hh_closed_icon_src, R.drawable.hh_closed_icon,
				R.drawable.hh_closed_icon_selected,
				R.drawable.hh_closed_icon_listview,
				R.drawable.hh_closed_icon_list_title);
		getDrumInstrument(3).getBBIconSource().set(R.drawable.hh_open_icon_src,
				R.drawable.hh_open_icon, R.drawable.hh_open_icon_selected,
				R.drawable.hh_open_icon_listview,
				R.drawable.hh_open_icon_list_title);
		getDrumInstrument(4).getBBIconSource().set(R.drawable.rimshot_icon_src,
				R.drawable.rimshot_icon, R.drawable.rimshot_icon_selected2,
				R.drawable.rimshot_icon_listview,
				R.drawable.rimshot_icon_list_title);
		drumsDirectory.getBBIconSource().set(-1, -1, -1,
				R.drawable.drums_icon_listview,
				R.drawable.drums_icon_list_title);
		internalRecordDirectory.getBBIconSource().set(
				R.drawable.microphone_icon_src, R.drawable.microphone_icon,
				R.drawable.microphone_icon_selected,
				R.drawable.microphone_icon_listview,
				R.drawable.microphone_icon_list_title);
		internalBeatRecordDirectory.getBBIconSource().set(
				R.drawable.beat_icon_src, R.drawable.beat_icon,
				R.drawable.beat_icon_selected, R.drawable.beat_icon_listview,
				R.drawable.beat_icon_list_title);
		internalSampleRecordDirectory.getBBIconSource().set(
				R.drawable.sample_icon_src, R.drawable.sample_icon,
				R.drawable.sample_icon_selected,
				R.drawable.sample_icon_listview,
				R.drawable.sample_icon_list_title);
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
				"samples", new BBIconSource());
		internalBeatRecordDirectory = new Instrument(internalRecordDirectory,
				"beats", new BBIconSource());
		internalBeatRecordDirectory
				.setEmptyMsg("You haven't recorded any beats yet!  Use the record button at the top to record your beats.");
		for (String drumName : drumNames) {
			new Instrument(drumsDirectory, drumName, new BBIconSource());
		}
		instrumentSelectAlertBuilder = new AlertDialog.Builder(
				GlobalVars.mainActivity);
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
						BBDirectory parent = currDirectory;
						currDirectory = currDirectory.getChild(item);
						if (currDirectory == null) {
							// Instrument type
							if (addingTrack) {
								Managers.trackManager.addTrack(
										(Instrument) parent, item);
							} else {
								TrackPage.getTrack().setInstrument(
										(Instrument) parent, item);
							}
							MidiTrackControlHelper.updateInstrumentIcon(TrackPage.getTrack().getId());
							TrackPageFactory.updatePages();
							currDirectory = internalDirectory;
						}
						updateInstrumentSelectAlert(currDirectory);
						// if the current dir is the root directory, we've
						// already selected a sample
						// do not display any select alert list.
						if (currDirectory == internalDirectory) {
							return;
						}
						// if the directory is empty, display the directory's
						// empty message. otherwise, show the next child
						// directories in a select alert list
						if (currDirectory.getChildNames().length == 0) {
							Toast.makeText(GlobalVars.mainActivity,
									currDirectory.getEmptyMsg(),
									Toast.LENGTH_SHORT).show();
						} else {
							instrumentSelectAlert.show();
						}
					}

				});
		if (currDirectory.getBBIconSource() != null
				&& currDirectory.getBBIconSource().listTitleIconResource > 0) {
			instrumentSelectAlertBuilder.setIcon(
					currDirectory.getBBIconSource().listTitleIconResource)
					.setTitle(currDirectory.getName().toUpperCase());
		} else {
			instrumentSelectAlertBuilder.setIcon(0).setTitle(
					"Choose Instrument");
		}

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

	private String[] formatNames(String[] names) {
		String[] formattedNames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			formattedNames[i] = names[i].replace(".bb", "");
		}
		return formattedNames;
	}

	private void initInstrumentSelectAdapter(final Activity activity,
			final BBDirectory directory) {
		String[] list = formatNames(directory.getChildNames());
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
						iconSource.listViewIconResource, 0, 0, 0);
				// Add margin between image and text (support various screen
				// densities)
				int dpMargin = (int) (15 * activity.getResources()
						.getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dpMargin);

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
