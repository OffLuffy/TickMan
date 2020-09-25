package com.luffbox.tickman.commands.conf;

import com.luffbox.tickman.util.cmd.ConfigSubCmd;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class CmdPrefixSubCmd extends ConfigSubCmd {
	public CmdPrefixSubCmd() {
		super(new String[] {"prefix", "cmdprefix"}, 1);
	}

	@Override
	public @NotNull String usage() { return "prefix <prefix>"; }

	@Override
	public @NotNull String desc() { return "Sets the prefix required to run commands"; }

	@Override
	public void execute(Config config, Message msg, String[] values) { config.setCmdPrefix(values[0]); }

	@Override
	public @NotNull String value(Config config) { return config.getCmdPrefix(); }
}
