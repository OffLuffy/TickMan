package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.cmd.*;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TicketCmd extends CmdHandler {
	public TicketCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"ticket", "t"},
				"Used in a ticket channel to modify or close the ticket", true, true, true,
				new CmdArg("action", CmdArgType.STRING, true)));
	}

	@Override
	public void onCommand(MessageReceivedEvent event, Config config, String[] args) {

	}
}
