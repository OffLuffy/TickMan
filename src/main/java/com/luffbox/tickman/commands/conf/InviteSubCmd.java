package com.luffbox.tickman.commands.conf;

import com.luffbox.tickman.util.cmd.ConfigSubCmd;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class InviteSubCmd extends ConfigSubCmd {
	public InviteSubCmd() {
		super(new String[] {"invite", "allowinvite"}, 1);
	}

	@Override
	public @NotNull String usage() {
		return null;
	}

	@Override
	public @NotNull String desc() {
		return null;
	}

	@Override
	public void execute(Config config, Message msg, String[] values) {
		// TODO: Implement
	}

	@Override
	public @NotNull String value(Config config) {
		return null;
	}
}
