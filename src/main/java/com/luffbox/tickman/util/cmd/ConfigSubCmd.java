package com.luffbox.tickman.util.cmd;

import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;

/**
 * Represents a nested handler to dynamically handle Config value modification
 */
public abstract class ConfigSubCmd {

	public final String[] aliases;
	public final int reqArgs;

	public ConfigSubCmd(String[] aliases, int reqArgs) {
		this.aliases = aliases;
		this.reqArgs = reqArgs;
	}

	/**
	 * The message to be shown if the required number of arguments is not provided
	 * @return A String with the usage message <strong>after</strong> "[prefix]conf "
	 */
	public abstract String usage();

	/**
	 * The message to be shown to explain this sub command
	 * @return A String with the sub command's description
	 */
	public abstract String desc();

	/**
	 * Executes the sub command
	 * @param config The Guild's Config object
	 * @param msg The original sent Message object
	 * @param values The arguments provided after this sub command
	 */
	public abstract void execute(Config config, Message msg, String[] values);

	/**
	 * Gets the current value this sub command is responsible for modifying in String format
	 * @param config The Guild's Config object
	 * @return A String showing the current value this sub command is responsible for modifying
	 */
	public abstract String value(Config config);
}
