package com.art.alligator.navigationfactories.registry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.art.alligator.Screen;
import com.art.alligator.functions.Function;

/**
 * Date: 25.03.2017
 * Time: 16:58
 *
 * @author Artur Artikov
 */

/**
 * Storage for dialog fragment screens
 */
public class DialogFragmentRegistry {
	private static final String KEY_SCREEN = "com.art.alligator.navigationfactories.registry.DialogFragmentRegistry.KEY_SCREEN";

	private Map<Class<? extends Screen>, Element> mElements = new HashMap<>();

	public <ScreenT extends Screen> void register(Class<ScreenT> screenClass, Function<ScreenT, DialogFragment> dialogFragmentCreationFunction, Function<DialogFragment, ScreenT> screenResolvingFunction) {
		checkThatNotRegistered(screenClass);
		mElements.put(screenClass, new Element(dialogFragmentCreationFunction, screenResolvingFunction));
	}

	public boolean isRegistered(Class<? extends Screen> screenClass) {
		return mElements.containsKey(screenClass);
	}

	@SuppressWarnings("unchecked")
	public DialogFragment createDialogFragment(Screen screen) {
		checkThatRegistered(screen.getClass());
		Element element = mElements.get(screen.getClass());
		return ((Function<Screen, DialogFragment>)element.getDialogFragmentCreationFunction()).call(screen);
	}

	public <ScreenT extends Screen> ScreenT getScreen(DialogFragment dialogFragment, Class<ScreenT> screenClass)  {
		checkThatRegistered(screenClass);
		Element element = mElements.get(screenClass);
		return (ScreenT) element.getScreenResolvingFunction().call(dialogFragment);
	}

	public static <ScreenT extends Screen> Function<ScreenT, DialogFragment> getDefaultDialogFragmentCreationFunction(final Class<ScreenT> screenClass, final Class<? extends DialogFragment> dialogFragmentClass) {
		return new Function<ScreenT, DialogFragment>() {
			@Override
			public DialogFragment call(ScreenT screen) {
				try {
					DialogFragment dialogFragment = dialogFragmentClass.newInstance();
					if (screen instanceof Serializable) {
						Bundle arguments = new Bundle();
						arguments.putSerializable(KEY_SCREEN, (Serializable) screen);
						dialogFragment.setArguments(arguments);
					}
					return dialogFragment;
				} catch (InstantiationException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	public static <ScreenT extends Screen> Function<DialogFragment, ScreenT> getDefaultScreenResolvingFunction(final Class<ScreenT> screenClass) {
		return new Function<DialogFragment, ScreenT>() {
			@Override
			@SuppressWarnings("unchecked")
			public ScreenT call(DialogFragment dialogFragment) {
				if (!Serializable.class.isAssignableFrom(screenClass)) {
					throw new IllegalArgumentException("Screen " + screenClass.getSimpleName() + " should be Serializable.");
				}
				if (dialogFragment.getArguments() == null) {
					return null;
				}
				return (ScreenT) dialogFragment.getArguments().getSerializable(KEY_SCREEN);
			}
		};
	}

	public static <ScreenT extends Screen> Function<DialogFragment, ScreenT> getNotImplementedScreenResolvingFunction(final Class<ScreenT> screenClass) {
		return new Function<DialogFragment, ScreenT>() {
			@Override
			public ScreenT call(DialogFragment dialogFragment) {
				throw new RuntimeException("Screen resolving function is not implemented for screen " + screenClass.getSimpleName());
			}
		};
	}

	private void checkThatNotRegistered(Class<? extends Screen> screenClass) {
		if(isRegistered(screenClass)) {
			throw new IllegalArgumentException("Screen " + screenClass.getSimpleName() + " is is already registered.");
		}
	}

	private void checkThatRegistered(Class<? extends Screen> screenClass) {
		if(!isRegistered(screenClass)) {
			throw new IllegalArgumentException("Screen " + screenClass.getSimpleName() + " is not registered as dialog fragment screen.");
		}
	}

	private static class Element {
		private Function<? extends Screen, DialogFragment> mDialogFragmentCreationFunction;
		private Function<DialogFragment, ? extends Screen> mScreenResolvingFunction;

		Element(Function<? extends Screen, DialogFragment> dialogFragmentCreationFunction, Function<DialogFragment, ? extends Screen> screenResolvingFunction) {
			mDialogFragmentCreationFunction = dialogFragmentCreationFunction;
			mScreenResolvingFunction = screenResolvingFunction;
		}

		Function<? extends Screen, DialogFragment> getDialogFragmentCreationFunction() {
			return mDialogFragmentCreationFunction;
		}

		Function<DialogFragment, ? extends Screen> getScreenResolvingFunction() {
			return mScreenResolvingFunction;
		}
	}
}