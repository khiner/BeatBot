package com.kh.beatbot.global;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.DirectoryManager;

public class BBDirectory {
	protected String name;
	protected String path;
	protected BeatBotIconSource bbIconSource;
	protected List<BBDirectory> children = new ArrayList<BBDirectory>();
	protected int iconSource;
	
	public BBDirectory(BBDirectory parent, String name, BeatBotIconSource bbIconSource) {
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
	
	public void setIconResources(int iconSource, int defaultIconResource,
			int selectedIconResource, int listViewIconResource) {
		this.iconSource = iconSource;
		bbIconSource.set(defaultIconResource, selectedIconResource,
				listViewIconResource);
	}
	
	public int getIconSource() {
		return iconSource;
	}
	
	public BeatBotIconSource getBBIconSource() {
		return bbIconSource;
	}
	
	public void addChild(BBDirectory child) {
		children.add(child);
	}
	
	public List<BBDirectory> getChildren() {
		return children;
	}
	
	public BBDirectory getChild(int childNum) {
		return children.get(childNum);
	}
}
