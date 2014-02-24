package com.kh.beatbot.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class IconResourceSet {
	public enum State {
		DEFAULT, PRESSED, SELECTED, DISABLED
	}

	private Map<State, IconResource> resources = new HashMap<State, IconResource>();

	public IconResourceSet(IconResourceSet resourceSet) {
		for (State state : State.values()) {
			IconResource otherResource = resourceSet.getResource(state);
			resources.put(state, otherResource == null ? null : otherResource.copy());
		}
	}

	public IconResourceSet(IconResource defaultResource) {
		this(defaultResource, null);
	}

	public IconResourceSet(IconResource defaultResource,
			IconResource pressedResource) {
		this(defaultResource, pressedResource, null);
	}

	public IconResourceSet(IconResource defaultResource,
			IconResource pressedResource, IconResource selectedResource) {
		this(defaultResource, pressedResource, selectedResource, null);
	}

	public IconResourceSet(IconResource defaultResource,
			IconResource pressedResource, IconResource selectedResource,
			IconResource disabledResource) {
		resources.put(State.DEFAULT, defaultResource);
		resources.put(State.PRESSED, pressedResource);
		resources.put(State.SELECTED, selectedResource);
		resources.put(State.DISABLED, disabledResource);
	}

	public IconResource getResource(State state) {
		return resources.get(state);
	}

	public synchronized void setFillColors(IconResourceSet resourceSet) {
		for (Entry<State, IconResource> resourcePair : resources.entrySet()) {
			IconResource otherResource = resourceSet.getResource(resourcePair
					.getKey());
			IconResource resource = resourcePair.getValue();
			if (null != resource && null != otherResource) {
				resource.fillColor = otherResource.fillColor;
			}
		}
	}

	public synchronized void setStrokeColors(IconResourceSet resourceSet) {
		for (Entry<State, IconResource> resourcePair : resources.entrySet()) {
			IconResource otherResource = resourceSet.getResource(resourcePair
					.getKey());
			IconResource resource = resourcePair.getValue();
			if (null != resource && null != otherResource) {
				resource.strokeColor = otherResource.strokeColor;
			}
		}
	}

	public void setResourceId(IconResourceSet resourceSet) {
		for (Entry<State, IconResource> resourcePair : resources.entrySet()) {
			IconResource otherResource = resourceSet.getResource(resourcePair
					.getKey());
			IconResource resource = resourcePair.getValue();
			if (null != resource && null != otherResource) {
				resource.resourceId = otherResource.resourceId;
			}
		}
	}
}
