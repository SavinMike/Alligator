package com.art.alligator.exceptions;

import com.art.alligator.Command;

/**
 * Date: 26.02.2017
 * Time: 18:56
 *
 * @author Artur Artikov
 */

/**
 * Exception thrown when a navigation error has occurred.
 */
public class CommandExecutionException extends Exception {
	private Command mCommand;
	private String mReason;

	public CommandExecutionException(Command command, String reason) {
		super("Failed to execute navigation command " + command.getClass().getSimpleName() + ". " + reason);
		mCommand = command;
		mReason = reason;
	}

	/**
	 * Returns a {@link Command} that was executed when an error has occurred.
	 *
	 * @return command that was executed when an error has occurred.
	 */
	public Command getCommand() {
		return mCommand;
	}

	/**
	 * Returns a text description of an error.
	 *
	 * @return text description of an error.
	 */
	public String getReason() {
		return mReason;
	}
}
