package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InviteCmd extends CmdHandler {
	public InviteCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"invite", "inv"}, "Invite " + tickman.getBotName() + " to your server", true, true, false, false));
	}

	@Override
	public void onCommand(MessageReceivedEvent event, Config config, String[] args) {
		if (!config.canInvite()) { return; }
		event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Open this link to invite me to your server!\n<" + tickman.getInviteUrl() + ">").queue());
	}
}
