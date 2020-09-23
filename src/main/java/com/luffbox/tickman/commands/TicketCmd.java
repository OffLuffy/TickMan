package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.cmd.*;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TicketCmd extends CmdHandler {
	public TicketCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"ticket", "t"},
				"Used in a ticket channel to modify or close the ticket", true, true, true,
				new CmdArg("action", CmdArgType.STRING, true)));
	}

	@Override
	public void onCommand(MessageReceivedEvent e, Config config, String[] args) {
		if (e.getChannelType() != ChannelType.TEXT) {
			QueueHelper.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " This command must be used in a ticket channel", QueueHelper.SHORT);
			if (e.getChannelType().isGuild()) { QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST); }
		}
		Ticket ticket = config.getTicketByChannel((TextChannel) e.getChannel());
		if (args.length == 0) {
			EmbedBuilder embed = config.newEmbed();
			embed.setTitle("Ticket Commands");
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "close", "Close the ticket"));
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "transfer *<dept>*", "Transfer the ticket to another department"));
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "invite *<member>*", "Invite an additional member to view this ticket"));
//			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "", ""));


			e.getChannel().sendMessage(embed.build()).queue(msg -> QueueHelper.queueLater(msg.delete(), QueueHelper.INST));
			return;
		}

		switch (args[0]) {
			case "close":
				break;
			case "transfer":
				break;
			case "invite":
				break;
			default:
				QueueHelper.tempSend(e.getChannel(), "Action not recognized", QueueHelper.SHORT);
		}
	}
}
