package com.kh.beatbot.global;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.DirectoryManager;

public class BBDirectory {
	protected String name = null;
	protected String path = null;
	protected ImageIconSource bbIconSource = null;
	protected List<BBDirectory> children = new ArrayList<BBDirectory>();
	protected BBDirectory parent = null;
	protected String emptyMsg = "This directoy is empty.";

	public BBDirectory(BBDirectory parent, String name,
			ImageIconSource bbIconSource) {
		this.parent = parent;
		this.name = name;
		this.bbIconSource = bbIconSource;
		if (parent == null) { // root directory
			path = DirectoryManager.appDirectoryPath + name + "/";
		} else {
			path = parent.path + name + "/";
			parent.addChild(this);
		}
		new File(path).mkdirs();
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public ImageIconSource getIconSource() {
		return (bbIconSource == null && parent != null) ? parent.bbIconSource : bbIconSource;
	}

	public int getListTitleResource() {
		ImageIconSource iconSource = getIconSource();
		return iconSource != null ? iconSource.listTitleIconResource : -1;
	}

	public int getListViewResource() {
		ImageIconSource iconSource = getIconSource();
		return iconSource != null ? iconSource.listViewIconResource : -1;
	}
	
	public void setIconSource(ImageIconSource iconSource) {
		this.bbIconSource = iconSource;
	}

	public BBDirectory getParent() {
		return parent;
	}

	public void addChild(BBDirectory child) {
		children.add(child);
	}

	public List<BBDirectory> getChildren() {
		return children;
	}

	public String[] getChildNames() {
		String[] names = new String[children.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = children.get(i).name;
		}
		return names;
	}

	public BBDirectory getChild(int childNum) {
		if (childNum >= children.size()) {
			return null;
		}
		return children.get(childNum);
	}

	public String getEmptyMsg() {
		return emptyMsg;
	}

	public void setEmptyMsg(String emptyMsg) {
		this.emptyMsg = emptyMsg;
	}
}
