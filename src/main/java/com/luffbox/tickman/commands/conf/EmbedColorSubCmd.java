package com.luffbox.tickman.commands.conf;

import com.luffbox.tickman.util.cmd.ConfigSubCmd;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class EmbedColorSubCmd extends ConfigSubCmd {
	public EmbedColorSubCmd() {
		super(new String[] {"color", "embedcolor"}, 1);
	}

	@Override
	public @NotNull String usage() { return "color <color>"; }

	@Override
	public @NotNull String desc() { return "Sets the embed color the bot uses"; }

	@Override
	public void execute(Config config, Message msg, String[] values) { config.setCmdPrefix(values[0]); }

	@Override
	public @NotNull String value(Config config) { return String.format("%06x", config.getEmbedColor().longValue()); }
}
