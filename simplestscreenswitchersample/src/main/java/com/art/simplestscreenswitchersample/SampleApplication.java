package com.art.simplestscreenswitchersample;

import android.app.Application;

import com.art.alligator.NavigationContextBinder;
import com.art.alligator.NavigationFactory;
import com.art.alligator.Navigator;
import com.art.alligator.AndroidNavigator;
import com.art.alligator.ScreenResolver;

/**
 * Date: 21.01.2016
 * Time: 23:30
 *
 * @author Artur Artikov
 */
public class SampleApplication extends Application {
	private static AndroidNavigator sNavigator;

	@Override
	public void onCreate() {
		super.onCreate();
		sNavigator = new AndroidNavigator(new SampleNavigationFactory());
	}

	public static Navigator getNavigator() {
		return sNavigator;
	}

	public static NavigationContextBinder getNavigationContextBinder() {
		return sNavigator;
	}

	public static ScreenResolver getScreenResolver() {
		return sNavigator.getScreenResolver();
	}

	public static NavigationFactory getNavigationFactory() {
		return sNavigator.getNavigationFactory();
	}
}