package com.luffbox.tickman.commands.conf;

import com.luffbox.tickman.util.cmd.ConfigSubCmd;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;

public class EmbedColorSubCmd extends ConfigSubCmd {
	public EmbedColorSubCmd() {
		super(new String[] {"color", "embedcolor"}, 1);
	}

	@Override
	public String usage() {
		return null;
	}

	@Override
	public String desc() {
		return null;
	}

	@Override
	public void execute(Config config, Message msg, String[] values) {
		// TODO: Implement
	}

	@Override
	public String value(Config config) {
		return null;
	}
}
