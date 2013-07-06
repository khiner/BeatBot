package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public abstract class Page extends TouchableView {
	public static MainPage mainPage;
	public static EffectPage effectPage;
	
	public abstract void update();
}
