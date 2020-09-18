package com.luffbox.tickman.util.cmd;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.GuildOpts;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class CmdHandler extends ListenerAdapter {

	public final CmdOpts opts;
	protected final TickMan tickman;

	public CmdHandler(TickMan tickman, CmdOpts opts){ this.tickman = tickman; this.opts = opts; }

	public abstract void onCommand(MessageReceivedEvent event, GuildOpts guildData, String[] args);

	public void onPrivateMessage(MessageReceivedEvent event, String[] args) {
		if (opts.reqGuild()) {
			event.getChannel().sendMessage("This command must be used in a server!").queue();
		} else {
			onCommand(event, null, args);
		}
	}
}
