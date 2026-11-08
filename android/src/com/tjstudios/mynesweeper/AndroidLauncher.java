package com.tjstudios.mynesweeper;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.tjstudios.mynesweeper.Mynesweeper;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new Mynesweeper(), config);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
			// Let Android choose the display rate, with a preference for responsive updates.
			graphics.getView().setRequestedFrameRate(View.REQUESTED_FRAME_RATE_CATEGORY_HIGH);
		}
	}
}
