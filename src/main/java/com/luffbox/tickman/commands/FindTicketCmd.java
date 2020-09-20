package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.cmd.CmdArg;
import com.luffbox.tickman.util.cmd.CmdArgType;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FindTicketCmd extends CmdHandler {
	public FindTicketCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"find", "findticket", "search", "searchtickets"},
				"Find a ticket by ID or user", true, true, true,
				new CmdArg("criteria", CmdArgType.STRING, true)));
	}

	@Override
	public void onCommand(MessageReceivedEvent event, Config config, String[] args) {
		// TODO: Implement ticket searching
	}
}
