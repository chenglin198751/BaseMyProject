package base;

import android.app.Application;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import utils.Constants;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		getDisplayMetrics();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	private void getDisplayMetrics() {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Constants.screenWidth = display.getWidth();
		Constants.screenHeight = display.getHeight();
	}

}
