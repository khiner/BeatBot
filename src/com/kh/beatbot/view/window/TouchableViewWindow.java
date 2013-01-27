package com.kh.beatbot.view.window;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.view.TouchableSurfaceView;

public abstract class TouchableViewWindow extends ViewWindow {

	// map of pointer ID #'s that this window is responsible for to their current
	// position relative to this window
	protected Map<Integer, Position> pointerIdToPos = new HashMap<Integer, Position>();
	
	public TouchableViewWindow(TouchableSurfaceView parent) {
		super(parent);
	}

	public final boolean ownsPointer(int id) {
		return pointerIdToPos.containsKey(id);
	}
	
	public final int pointerCount() {
		return pointerIdToPos.size();
	}
	
	public void notifyActionDown(int id, float x, float y) {
		Position pos = new Position(x, y);
		pointerIdToPos.put(id, pos);
		handleActionDown(id, x, y);
	}

	public void notifyActionUp(int id, float x, float y) {
		handleActionUp(id, x, y);
		pointerIdToPos.clear();
	}

	public void notifyActionPointerDown(int id, float x, float y) {
		Position pos = new Position(x, y);
		pointerIdToPos.put(id, pos);
		handleActionPointerDown(id, x, y);
	}

	public void notifyActionPointerUp(int id, float x, float y) {
		handleActionPointerUp(id, x, y);
		pointerIdToPos.remove(id);
	}

	public void notifyActionMove(int id, float x, float y) {
		if (!ownsPointer(id))
			return;
		pointerIdToPos.get(id).set(x, y);
		handleActionMove(id, x, y);
	}
	
	protected abstract void handleActionDown(int id, float x, float y);

	protected abstract void handleActionUp(int id, float x, float y);

	protected abstract void handleActionPointerDown(int id, float x, float y);

	protected abstract void handleActionPointerUp(int id, float x, float y);

	protected abstract void handleActionMove(int id, float x, float y);
}
