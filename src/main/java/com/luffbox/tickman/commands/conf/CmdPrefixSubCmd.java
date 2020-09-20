package com.luffbox.tickman.commands.conf;

import com.luffbox.tickman.util.cmd.ConfigSubCmd;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;

public class CmdPrefixSubCmd extends ConfigSubCmd {
	public CmdPrefixSubCmd() {
		super(new String[] {"prefix", "cmdprefix"}, 1);
	}

	@Override
	public String usage() { return "prefix <prefix>"; }

	@Override
	public String desc() { return "Sets the prefix required to run commands"; }

	@Override
	public void modify(Config config, Message msg, String[] values) {

	}
}
