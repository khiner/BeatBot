package com.kh.beatbot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.Image;

public class Directory {
	protected String name = null, path = null,
			emptyMsg = "This directoy is empty.";
	protected IconResource iconResource = null;
	protected List<Directory> children = new ArrayList<Directory>();
	protected Directory parent = null;
	protected File file = null;

	public Directory(Directory parent, String name, IconResource iconResource) {
		this.parent = parent;
		this.name = name;
		this.iconResource = iconResource;
		if (parent == null) { // root directory
			path = DirectoryManager.appDirectoryPath + name + "/";
		} else {
			path = parent.path + name + "/";
			parent.addChild(this);
		}
		file = new File(path);
		file.mkdirs();
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public IconResource getIconResource() {
		return (iconResource == null && parent != null) ? parent.iconResource
				: iconResource;
	}

	public int getListTitleResource() {
		return iconResource != null ? ((Image) iconResource.listTitleDrawable).resourceId
				: -1;
	}

	public int getListViewResource() {
		return iconResource != null ? ((Image) iconResource.listViewDrawable).resourceId
				: -1;
	}

	public void setIconResource(IconResource iconResource) {
		this.iconResource = iconResource;
	}

	public Directory getParent() {
		return parent;
	}

	public void addChild(Directory child) {
		children.add(child);
	}

	public List<Directory> getChildren() {
		return children;
	}

	public String[] list() {
		return file.list();
	}

	public String[] getChildNames() {
		String[] names = new String[children.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = children.get(i).name;
		}
		return names;
	}

	public Directory getChild(int childNum) {
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

	public void clearTempFiles() {
		for (File sampleFile : file.listFiles()) {
			if (sampleFile.getAbsolutePath().contains(".raw")) {
				sampleFile.delete();
			}
		}
		for (Directory child : children) {
			child.clearTempFiles();
		}
	}
}
