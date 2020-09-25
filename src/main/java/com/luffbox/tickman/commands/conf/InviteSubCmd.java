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
	public @NotNull String usage() { return "invite <true|false>"; }

	@Override
	public @NotNull String desc() { return "Enable or disable the invite command"; }

	@Override
	public void execute(Config config, Message msg, String[] values) { config.setAllowInvite(Boolean.parseBoolean(values[0])); }

	@Override
	public @NotNull String value(Config config) { return config.canInvite() ? "Enabled" : "Disabled"; }
}
