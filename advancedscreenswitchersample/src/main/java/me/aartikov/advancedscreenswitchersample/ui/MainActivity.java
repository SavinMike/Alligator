package me.aartikov.advancedscreenswitchersample.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.aartikov.advancedscreenswitchersample.R;
import me.aartikov.advancedscreenswitchersample.SampleApplication;
import me.aartikov.advancedscreenswitchersample.SampleTransitionAnimationProvider;
import me.aartikov.advancedscreenswitchersample.screens.TabScreen;
import me.aartikov.alligator.AnimationData;
import me.aartikov.alligator.NavigationContext;
import me.aartikov.alligator.NavigationContextBinder;
import me.aartikov.alligator.Navigator;
import me.aartikov.alligator.Screen;
import me.aartikov.alligator.ScreenSwitchingListener;
import me.aartikov.alligator.TransitionAnimation;
import me.aartikov.alligator.animations.transition.SimpleTransitionAnimation;
import me.aartikov.alligator.screenswitchers.FactoryFragmentScreenSwitcher;
import me.aartikov.alligator.screenswitchers.FragmentScreenSwitcher;

/**
 * Date: 21.01.2016
 * Time: 23:30
 *
 * @author Artur Artikov
 */
public class MainActivity extends AppCompatActivity implements OnTabSelectListener, ScreenSwitchingListener {
	private static final String ANDROID_SCREEN_NAME = "ANDROID";
	private static final String BUG_SCREEN_NAME = "BUG";
	private static final String DOG_SCREEN_NAME = "DOG";

	private Navigator mNavigator;
	private NavigationContextBinder mNavigationContextBinder;
	private TabsInfo mTabsInfo;
	private FragmentScreenSwitcher mScreenSwitcher;

	@BindView(R.id.bottom_bar)
	BottomBar mBottomBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNavigator = SampleApplication.getNavigator();
		mNavigationContextBinder = SampleApplication.getNavigationContextBinder();
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		mBottomBar.setOnTabSelectListener(this, false);
		initTabsInfo();
		initScreenSwitcher();

		if (savedInstanceState == null) {
			mNavigator.switchTo(ANDROID_SCREEN_NAME);
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		bindNavigationContext();
	}

	@Override
	protected void onPause() {
		mNavigationContextBinder.unbind();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		mNavigator.goBack();
	}

	@Override
	public void onTabSelected(@IdRes int tabId) {
		mNavigator.switchTo(mTabsInfo.getScreenName(tabId));
	}

	@Override
	public void onScreenSwitched(@Nullable String screenNameFrom, String screenNameTo) {
		bindNavigationContext();
		selectTab(mTabsInfo.getTabId(screenNameTo));
	}

	private void initTabsInfo() {
		mTabsInfo = new TabsInfo();
		mTabsInfo.add(ANDROID_SCREEN_NAME, R.id.tab_android, new TabScreen(getString(R.string.tab_android)));
		mTabsInfo.add(BUG_SCREEN_NAME, R.id.tab_bug, new TabScreen(getString(R.string.tab_bug)));
		mTabsInfo.add(DOG_SCREEN_NAME, R.id.tab_dog, new TabScreen(getString(R.string.tab_dog)));
	}

	private void initScreenSwitcher() {
		mScreenSwitcher = new FactoryFragmentScreenSwitcher(getSupportFragmentManager(), R.id.main_container, SampleApplication.getNavigationFactory()) {
			@Override
			protected Screen getScreen(String screenName) {
				return mTabsInfo.getScreen(screenName);
			}

			@Override
			protected TransitionAnimation getAnimation(String screenNameFrom, String screenNameTo, @Nullable AnimationData animationData) {
				int indexFrom = mTabsInfo.getTabIndex(screenNameFrom);
				int indexTo = mTabsInfo.getTabIndex(screenNameTo);
				if (indexTo > indexFrom) {
					return new SimpleTransitionAnimation(R.anim.slide_in_right, R.anim.slide_out_left);
				} else {
					return new SimpleTransitionAnimation(R.anim.slide_in_left, R.anim.slide_out_right);
				}
			}
		};
	}

	private void bindNavigationContext() {
		Fragment fragment = mScreenSwitcher.getCurrentFragment();
		NavigationContext.Builder builder = new NavigationContext.Builder(this)
				.screenSwitcher(mScreenSwitcher)
				.screenSwitcherListener(this)
				.transitionAnimationProvider(new SampleTransitionAnimationProvider());

		if (fragment != null && fragment instanceof ContainerIdProvider) {
			builder.containerId(((ContainerIdProvider) fragment).getContainerId())
					.fragmentManager(fragment.getChildFragmentManager());
		}

		mNavigationContextBinder.bind(builder.build());
	}

	private void selectTab(@IdRes int tabId) {
		mBottomBar.setOnTabSelectListener(null);    // workaround to don't call listener
		mBottomBar.selectTabWithId(tabId);
		mBottomBar.setOnTabSelectListener(this, false);
	}
}
