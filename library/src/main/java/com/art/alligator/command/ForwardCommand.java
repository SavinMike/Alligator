package com.art.alligator.command;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.art.alligator.Command;
import com.art.alligator.AnimationData;
import com.art.alligator.exception.CommandExecutionException;
import com.art.alligator.internal.DialogFragmentHelper;
import com.art.alligator.NavigationContext;
import com.art.alligator.NavigationFactory;
import com.art.alligator.Screen;
import com.art.alligator.animation.TransitionAnimation;
import com.art.alligator.TransitionType;
import com.art.alligator.exception.FailedResolveActivityException;
import com.art.alligator.internal.FragmentStack;
import com.art.alligator.internal.ScreenClassUtils;

/**
 * Date: 29.12.2016
 * Time: 10:22
 *
 * @author Artur Artikov
 */
public class ForwardCommand implements Command {
	private Screen mScreen;
	private boolean mForResult;
	private AnimationData mAnimationData;

	public ForwardCommand(Screen screen, boolean forResult, AnimationData animationData) {
		mScreen = screen;
		mForResult = forResult;
		mAnimationData = animationData;
	}

	@Override
	public boolean execute(NavigationContext navigationContext, NavigationFactory navigationFactory) throws CommandExecutionException {
		switch (navigationFactory.getViewType(mScreen.getClass())) {
			case ACTIVITY: {
				Activity activity = navigationContext.getActivity();
				Intent intent = navigationFactory.createIntent(activity, mScreen);
				ScreenClassUtils.putScreenClass(intent, mScreen.getClass());
				ScreenClassUtils.putPreviousScreenClass(intent, ScreenClassUtils.getScreenClass(activity, navigationFactory));

				if (intent.resolveActivity(activity.getPackageManager()) == null) {
					throw new FailedResolveActivityException(this, mScreen);
				}

				if (mForResult) {
					if (!navigationFactory.isScreenForResult(mScreen.getClass())) {
						throw new CommandExecutionException(this, "Screen " + mScreen.getClass().getSimpleName() + " is not registered as screen for result.");
					}
					int requestCode = navigationFactory.getRequestCode(mScreen.getClass());
					activity.startActivityForResult(intent, requestCode);
				} else {
					activity.startActivity(intent);
				}
				TransitionAnimation animation = getActivityAnimation(navigationContext, navigationFactory);
				CommandUtils.applyActivityAnimation(activity, animation);
				return false;
			}

			case FRAGMENT: {
				if (!navigationContext.hasContainerId()) {
					throw new CommandExecutionException(this, "ContainerId is not set.");
				}
				if (mForResult) {
					throw new CommandExecutionException(this, "goForwardForResult is not supported for fragment screens.");
				}

				Fragment fragment = navigationFactory.createFragment(mScreen);
				ScreenClassUtils.putScreenClass(fragment, mScreen.getClass());
				FragmentStack fragmentStack = FragmentStack.from(navigationContext);
				TransitionAnimation animation = getFragmentAnimation(navigationContext, fragmentStack.getCurrentFragment());
				fragmentStack.push(fragment, animation);
				return true;
			}

			case DIALOG_FRAGMENT: {
				DialogFragment dialogFragment = navigationFactory.createDialogFragment(mScreen);
				DialogFragmentHelper.from(navigationContext).showDialog(dialogFragment);
				return true;
			}

			default:
				throw new CommandExecutionException(this, "Screen " + mScreen.getClass().getSimpleName() + " is not registered.");
		}
	}

	private TransitionAnimation getActivityAnimation(NavigationContext navigationContext, NavigationFactory navigationFactory) {
		Class<? extends Screen> screenClassFrom = ScreenClassUtils.getScreenClass(navigationContext.getActivity(), navigationFactory);
		Class<? extends Screen> screenClassTo = mScreen.getClass();
		return navigationContext.getTransitionAnimationProvider().getAnimation(TransitionType.FORWARD, screenClassFrom, screenClassTo, true, mAnimationData);
	}

	private TransitionAnimation getFragmentAnimation(NavigationContext navigationContext, Fragment currentFragment) {
		if (currentFragment == null) {
			return TransitionAnimation.NONE;
		}

		Class<? extends Screen> screenClassFrom = ScreenClassUtils.getScreenClass(currentFragment);
		Class<? extends Screen> screenClassTo = mScreen.getClass();
		return navigationContext.getTransitionAnimationProvider().getAnimation(TransitionType.FORWARD, screenClassFrom, screenClassTo, false, mAnimationData);
	}
}
