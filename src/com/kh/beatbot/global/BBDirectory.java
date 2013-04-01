package com.kh.beatbot.global;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.DirectoryManager;

public class BBDirectory {
	protected String name = null;
	protected String path = null;
	protected IconSource bbIconSource = null;
	protected List<BBDirectory> children = new ArrayList<BBDirectory>();
	protected BBDirectory parent = null;
	protected String emptyMsg = "This directoy is empty.";

	public BBDirectory(BBDirectory parent, String name,
			IconSource bbIconSource) {
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

	public IconSource getIconSource() {
		if (bbIconSource == null && parent != null)
			return parent.bbIconSource;
		return bbIconSource;
	}

	public void setIconSource(IconSource iconSource) {
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
