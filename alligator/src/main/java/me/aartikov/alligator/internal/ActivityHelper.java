package me.aartikov.alligator.internal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import me.aartikov.alligator.NavigationContext;
import me.aartikov.alligator.TransitionAnimation;

/**
 * Date: 26.03.2017
 * Time: 0:17
 *
 * @author Artur Artikov
 */

/**
 * Helper class for starting and finishing an activity with animation.
 */
public class ActivityHelper {
	private AppCompatActivity mActivity;

	public static ActivityHelper from(NavigationContext navigationContext) {
		return new ActivityHelper(navigationContext.getActivity());
	}

	public ActivityHelper(AppCompatActivity activity) {
		if (activity == null) {
			throw new IllegalArgumentException("Activity can't be null.");
		}
		mActivity = activity;
	}

	public boolean resolve(Intent intent) {
		return intent.resolveActivity(mActivity.getPackageManager()) != null;
	}

	public void start(Intent intent, TransitionAnimation animation) {
		Bundle optionsBundle = animation.getActivityOptionsBundle(mActivity);
		animation.applyBeforeActivityStarted(mActivity, intent);
		ActivityCompat.startActivity(mActivity, intent, optionsBundle);
		animation.applyAfterActivityStarted(mActivity);
	}

	public void startForResult(Intent intent, int requestCode, TransitionAnimation animation) {
		Bundle optionsBundle = animation.getActivityOptionsBundle(mActivity);
		animation.applyBeforeActivityStarted(mActivity, intent);
		ActivityCompat.startActivityForResult(mActivity, intent, requestCode, optionsBundle);
		animation.applyAfterActivityStarted(mActivity);
	}

	public void finish(TransitionAnimation animation) {
		animation.applyBeforeActivityFinished(mActivity);
		if (animation.needDelayActivityFinish()) {
			mActivity.supportFinishAfterTransition();
		} else {
			mActivity.finish();
		}
		animation.applyAfterActivityFinished(mActivity);
	}
}
