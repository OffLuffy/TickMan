package com.luffbox.tickman.util.cmd;

import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;

public abstract class ConfigSubCmd {

	public final String[] aliases;
	public final int reqArgs;

	public ConfigSubCmd(String[] aliases, int reqArgs) {
		this.aliases = aliases;
		this.reqArgs = reqArgs;
	}

	public abstract String usage();
	public abstract String desc();
	public abstract void modify(Config config, Message msg, String[] values);
}
