package com.art.alligator;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Looper;

import com.art.alligator.commands.BackCommand;
import com.art.alligator.commands.BackToCommand;
import com.art.alligator.commands.FinishCommand;
import com.art.alligator.commands.ForwardCommand;
import com.art.alligator.commands.ReplaceCommand;
import com.art.alligator.commands.ResetCommand;
import com.art.alligator.commands.SwitchToCommand;
import com.art.alligator.exceptions.CommandExecutionException;

/**
 * Date: 29.12.2016
 * Time: 10:13
 *
 * @author Artur Artikov
 */
public class AndroidNavigator implements NavigationContextBinder, Navigator {
	private NavigationFactory mNavigationFactory;
	private NavigationContext mNavigationContext;
	private Queue<Command> mCommandQueue = new LinkedList<>();
	private boolean mIsExecutingCommands;
	private ScreenResolver mScreenResolver;
	private ScreenResultResolver mScreenResultResolver;

	public AndroidNavigator(NavigationFactory navigationFactory) {
		mNavigationFactory = navigationFactory;
		mScreenResolver = new ScreenResolver(navigationFactory);
		mScreenResultResolver = new ScreenResultResolver(navigationFactory);
	}

	public NavigationFactory getNavigationFactory() {
		return mNavigationFactory;
	}

	public ScreenResolver getScreenResolver() {
		return mScreenResolver;
	}

	public ScreenResultResolver getScreenResultResolver() {
		return mScreenResultResolver;
	}

	@Override
	public void bind(NavigationContext navigationContext) {
		checkThatMainThread();
		mNavigationContext = navigationContext;
		executeQueuedCommands();
	}

	@Override
	public void unbind() {
		checkThatMainThread();
		mNavigationContext = null;
	}

	@Override
	public void goForward(Screen screen) {
		goForward(screen, null);
	}

	@Override
	public void goForward(Screen screen, AnimationData animationData) {
		executeCommand(new ForwardCommand(screen, false, animationData));
	}

	@Override
	public void goForwardForResult(Screen screen) {
		goForwardForResult(screen, null);
	}

	@Override
	public void goForwardForResult(Screen screen, AnimationData animationData) {
		executeCommand(new ForwardCommand(screen, true, animationData));
	}

	@Override
	public void goBack() {
		goBack(null);
	}

	@Override
	public void goBack(AnimationData animationData) {
		executeCommand(new BackCommand(animationData));
	}

	@Override
	public void goBackTo(Class<? extends Screen> screenClass) {
		goBackTo(screenClass, null);
	}

	@Override
	public void goBackTo(Class<? extends Screen> screenClass, AnimationData animationData) {
		executeCommand(new BackToCommand(screenClass, animationData));
	}

	@Override
	public void replace(Screen screen) {
		replace(screen, null);
	}

	@Override
	public void replace(Screen screen, AnimationData animationData) {
		executeCommand(new ReplaceCommand(screen, animationData));
	}

	@Override
	public void reset(Screen screen) {
		reset(screen, null);
	}

	@Override
	public void reset(Screen screen, AnimationData animationData) {
		executeCommand(new ResetCommand(screen, animationData));
	}

	@Override
	public void finish() {
		finish(null);
	}

	@Override
	public void finish(AnimationData animationData) {
		executeCommand(new FinishCommand(null, animationData));
	}

	@Override
	public void finishWithResult(ScreenResult screenResult) {
		finishWithResult(screenResult, null);
	}

	@Override
	public void finishWithResult(ScreenResult screenResult, AnimationData animationData) {
		executeCommand(new FinishCommand(screenResult, animationData));
	}

	@Override
	public void switchTo(String screenName) {
		executeCommand(new SwitchToCommand(screenName));
	}

	protected void executeCommand(Command command) {
		checkThatMainThread();
		mCommandQueue.add(command);
		executeQueuedCommands();
	}

	private void executeQueuedCommands() {
		if(mIsExecutingCommands) {
			return;
		}

		mIsExecutingCommands = true;
		try {
			while (mNavigationContext != null && !mCommandQueue.isEmpty()) {
				Command command = mCommandQueue.remove();
				boolean canExecuteCommands = command.execute(mNavigationContext, mNavigationFactory);
				if (mNavigationContext.getNavigationCommandListener() != null) {
					mNavigationContext.getNavigationCommandListener().onNavigationCommandExecuted(command);
				}
				if(!canExecuteCommands) {
					mNavigationContext = null;
				}
			}
		} catch (CommandExecutionException e) {
			mCommandQueue.clear();
			mNavigationContext.getNavigationErrorListener().onNavigationError(e);
		} catch (Exception e){
			mCommandQueue.clear();
			throw e;
		} finally {
			mIsExecutingCommands = false;
		}
	}

	private void checkThatMainThread() {
		if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
			throw new RuntimeException("Can only be called from the main thread.");
		}
	}
}