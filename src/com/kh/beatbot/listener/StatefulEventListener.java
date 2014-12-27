package com.kh.beatbot.listener;

import com.kh.beatbot.event.Stateful;

public interface StatefulEventListener {
	public void onEventCompleted(Stateful event);
}
