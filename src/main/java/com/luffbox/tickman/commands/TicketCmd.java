package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.cmd.*;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class TicketCmd extends CmdHandler {
	public TicketCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"ticket", "t"},
				"Used in a ticket channel to modify or close the ticket", true, true, true,
				new CmdArg("action", CmdArgType.STRING, true)));
	}

	@Override
	public void onCommand(MessageReceivedEvent e, Config config, String[] args) {
		if (e.getChannelType() != ChannelType.TEXT) {
			TickMan.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " This command must be used in a ticket channel", TickMan.Duration.SHORT);
			if (e.getChannelType().isGuild()) { TickMan.queueLater(e.getMessage().delete(), TickMan.Duration.INST); }
		}
		Ticket ticket = config.getTicketByChannel((TextChannel) e.getChannel());
		if (args.length == 0) {
			EmbedBuilder embed = config.newEmbed();
			embed.setTitle("Ticket Commands");
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "close", "Close the ticket"));
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "transfer *<dept>*", "Transfer the ticket to another department"));
//			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "", ""));


			e.getChannel().sendMessage(embed.build()).queue(msg -> TickMan.queueLater(msg.delete(), TickMan.Duration.INST));
			return;
		}

		switch (args[0]) {
			case "close":
				break;
			case "transfer":
				break;
			default:
				TickMan.tempSend(e.getChannel(), "Action not recognized", TickMan.Duration.SHORT);
		}
	}
}
